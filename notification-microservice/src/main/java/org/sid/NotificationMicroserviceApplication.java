package org.sid;

import org.sid.event.OrderNotificationEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class NotificationMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationMicroserviceApplication.class, args);
	}

	@KafkaListener(topics="NotificationTopic")
	public void handleNotification(OrderNotificationEvent orderNotificationEvent)
	{
		//Send an email
		log.info("Received order notification for - {}", orderNotificationEvent.getOrderNumber());
	}
}
