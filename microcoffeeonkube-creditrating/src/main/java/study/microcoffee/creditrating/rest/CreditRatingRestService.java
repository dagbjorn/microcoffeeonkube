package study.microcoffee.creditrating.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import study.microcoffee.creditrating.behavior.ServiceBehavior;
import study.microcoffee.creditrating.domain.CreditRating;

/**
 * Class implementing the Credit Rating REST service for checking if customers are creditworthy.
 * <p>
 * <b>TODO:</b> Currently a hardcoded credit rating of 70 is always returned. Needs to make it dependent on the actual customer.
 */
@RestController
@RequestMapping(path = "/coffeeshop", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
public class CreditRatingRestService {

    private Logger logger = LoggerFactory.getLogger(CreditRatingRestService.class);

    public ServiceBehavior serviceBehavior;

    public CreditRatingRestService(ServiceBehavior serviceBehavior) {
        this.serviceBehavior = serviceBehavior;
    }

    @GetMapping(path = "/creditrating/{customerId}")
    public CreditRating getCreditRating(@PathVariable("customerId") String customerId) {
        logger.debug("GET /creditrating/{}", customerId);

        serviceBehavior.execute();

        // TODO Create some kind of database table where customers credit rating is found.
        CreditRating creditRating = new CreditRating(70);

        return creditRating;
    }
}
