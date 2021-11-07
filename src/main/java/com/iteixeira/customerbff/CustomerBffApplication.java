package com.iteixeira.customerbff;

import java.io.Serializable;
import java.text.MessageFormat;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
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

	@Autowired
	private QueueMessagingTemplate queueMessagingTemplate;
	@Value("${cloud.aws.sqs.customer-register}")
	private String endpoint;

	@GetMapping("")
	public void callSQS() {
		final Customer c = new Customer(MessageFormat.format("Customer-{0}", System.currentTimeMillis()), "32165498778");
		queueMessagingTemplate.convertAndSend(endpoint, c);
		logger.info("Cliente enviado SQS {}", c);
	}

}

// @Component
// class ConsumerConsumer {
// 	private Logger logger = LoggerFactory.getLogger(CustomerResource.class);
// 	@SqsListener(value = "customer-register", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
// 	public void processMessage(Customer customer) {
// 		logger.info("Cliente recebido: {}", customer);
// 	}
// }

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