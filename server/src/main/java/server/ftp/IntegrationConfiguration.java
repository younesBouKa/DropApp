package server.ftp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.ftp.server.ApacheMinaFtpEvent;
import org.springframework.integration.ftp.server.ApacheMinaFtplet;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageChannel;

import java.util.Arrays;
import java.util.logging.Logger;

@Configuration
class IntegrationConfiguration {

	private final Logger logger = Logger.getLogger(IntegrationConfiguration.class.getName());

	@Bean
	ApacheMinaFtplet apacheMinaFtplet() {
		return new ApacheMinaFtplet();
	}

	@Bean
	MessageChannel eventsChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	IntegrationFlow integrationFlow() {
		return IntegrationFlows.from(this.eventsChannel())
			.handle((GenericHandler<ApacheMinaFtpEvent>) (apacheMinaFtpEvent, messageHeaders) -> {
				logger.info("new event: " + apacheMinaFtpEvent.getClass().getName() + ':' + apacheMinaFtpEvent.getSession());
				logger.info("new event [messageHeaders]: " + Arrays.toString(messageHeaders.entrySet().toArray()));
				return apacheMinaFtpEvent;
			})
			.get();
	}

	@Bean
	ApplicationEventListeningMessageProducer applicationEventListeningMessageProducer() {
		ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
		producer.setEventTypes(ApacheMinaFtpEvent.class);
		producer.setOutputChannel(eventsChannel());
		return producer;
	}
}
