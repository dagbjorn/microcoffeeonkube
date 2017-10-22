package study.microcoffee.order.consumer.creditrating;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import study.microcoffee.order.exception.ServiceCallFailedException;

/**
 * REST-based implementation of a CreditRatingConsumer.
 */
@Component
public class CreditRatingCustomerImpl implements CreditRatingConsumer {

    private static final String GET_CREATE_RATING_RESOURCE = "/coffeeshop/creditrating/{customerId}";

    private final Logger logger = LoggerFactory.getLogger(CreditRatingCustomerImpl.class);

    @Value("#{creditRatingRestTemplateFactory.createRestTemplate()}")
    private RestTemplate restTemplate;

    @Value("${creditrating.endpointurl}")
    private String createRatingEndpointUrl;

    @Override
    public int getCreateRating(String customerId) {
        String url = createRatingEndpointUrl + GET_CREATE_RATING_RESOURCE;

        logger.debug("GET request to {}, customerId={}", url, customerId);

        try {
            ResponseEntity<CreditRating> response = restTemplate.getForEntity(url, CreditRating.class, customerId);

            logger.debug("GET response from {}, response={}", url, response.getBody());

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                return response.getBody().getCreditRating();
            } else {
                throw new ServiceCallFailedException(response.getStatusCode() + " " + response.getStatusCode().getReasonPhrase());
            }
        } catch (RestClientException e) {
            throw new ServiceCallFailedException(e);
        }
    }
}
