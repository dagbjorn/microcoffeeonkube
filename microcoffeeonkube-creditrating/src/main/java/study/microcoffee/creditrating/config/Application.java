package study.microcoffee.creditrating.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import study.microcoffee.creditrating.behavior.FailingServiceBehavior;
import study.microcoffee.creditrating.behavior.ServiceBehavior;
import study.microcoffee.creditrating.behavior.SlowServiceBehavior;
import study.microcoffee.creditrating.behavior.StableServiceBehavior;
import study.microcoffee.creditrating.behavior.UnstableServiceBehavior;

/**
 * The Spring Boot application of the microservice.
 */
@SpringBootApplication(scanBasePackages = { "study.microcoffee.creditrating" })
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);

        logger.debug("Running: {}", context.getDisplayName());
    }

    @Bean
    @Autowired
    public ServiceBehavior serviceBehavior(@Value("${creditrating.service.behavior}") int serviceBehavior,
        @Value("${creditrating.service.behavior.delay}") int delay) {

        logger.debug("CreditRating service behavior={}, delay={}", serviceBehavior, delay);

        switch (serviceBehavior) {
            case 0:
                return new StableServiceBehavior();

            case 1:
                return new FailingServiceBehavior();

            case 2:
                return new SlowServiceBehavior(delay);

            case 3:
                return new UnstableServiceBehavior(delay);

            default:
                return new StableServiceBehavior();
        }
    }
}
