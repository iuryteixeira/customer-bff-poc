package com.iteixeira.customerbff;

import java.io.Serializable;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSns;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CustomerBffApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerBffApplication.class, args);
	}

}

class Customer implements Serializable {
	String name, vat;

	public Customer() {
	}

	public Customer(String name, String vat) {
		this.name = name;
		this.vat = vat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVat() {
		return vat;
	}

	public void setVat(String vat) {
		this.vat = vat;
	}

	@Override
	public String toString() {
		return "Customer [name=" + name + ", vat=" + vat + "]";
	}

}

@RestController
@RequestMapping("/customers")
class CustomerResource {

	private Logger logger = LoggerFactory.getLogger(CustomerResource.class);

	// @Autowired
	// private QueueMessagingTemplate queueMessagingTemplate;
	// @Value("${cloud.aws.sqs.customer-register}")
	// private String endpoint;

	@Autowired
	private NotificationMessagingTemplate notificationMessagingTemplate;
	@Value("${cloud.aws.sns.customer-register-topic}")
	private String registerTopic;

	@GetMapping("/{name}")
	public void callSQS(@PathVariable("name") String name) {
		final Customer c = new Customer(name, "32165498778");
		// send queue direct
		// queueMessagingTemplate.convertAndSend(endpoint, c);
		notificationMessagingTemplate.convertAndSend(registerTopic, c);
		logger.info("Cliente enviado para SNS {}", c);
	}

	@GetMapping("/health")
	public void health() {
		logger.info("Health ok");
	}

	@PostMapping("/confirm")
	public String registered(@RequestBody Customer customer) {
		logger.info("Cliente confirmado na API {}", customer);
		return "Cliente recebido na confirmação do BFF: " + customer;
	}
}

// @Component
// class ConsumerConsumer {
// private Logger logger = LoggerFactory.getLogger(CustomerResource.class);

// @SqsListener(value = "customer-register", deletionPolicy =
// SqsMessageDeletionPolicy.ON_SUCCESS)
// public void processMessage(Customer customer) {
// logger.info("Cliente recebido: {}", customer);
// }
// }

@EnableSqs
@Configuration
class SQSConfig {
	@Value("${cloud.aws.region.static}")
	private String region;
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;
	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Bean
	public QueueMessagingTemplate queueMessagingTemplate() {
		return new QueueMessagingTemplate(amazonSQSAsync());
	}

	@Bean
	@Primary
	public AmazonSQSAsync amazonSQSAsync() {
		return AmazonSQSAsyncClientBuilder.standard().withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
}

@EnableSns
@Configuration
class SNSConfig {
	@Value("${cloud.aws.region.static}")
	private String region;
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;
	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Bean
	public NotificationMessagingTemplate snsMessagingTemplate() {
		return new NotificationMessagingTemplate(amazonSNSAsync());
	}

	@Bean
	@Primary
	public AmazonSNSAsync amazonSNSAsync() {
		return AmazonSNSAsyncClientBuilder.standard().withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
	}
}