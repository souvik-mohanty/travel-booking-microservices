package com.tourflow.engagement.client;

import com.tourflow.engagement.exception.ExternalServiceException;
import com.tourflow.engagement.exception.ReviewValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

// HTTP client used by Review Service to communicate with Business Service.
@Component
public class BusinessClient {

    private final RestClient restClient;
    private final String businessServiceUrl;

    public BusinessClient(
            RestClient restClient,
            @Value("${services.business.url}") String businessServiceUrl
    ) {
        this.restClient = restClient;
        this.businessServiceUrl = businessServiceUrl;
    }

    public void verifyBusiness(UUID businessId) {

        try {

            JsonNode business = restClient.get()
                    .uri(businessServiceUrl + "/api/businesses/" + businessId)
                    .retrieve()
                    .body(JsonNode.class);

            if (business == null || business.isNull()) {
                throw new ReviewValidationException(
                        "Business not found: " + businessId
                );
            }

        } catch (ReviewValidationException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ExternalServiceException(
                    "Unable to communicate with Business Service",
                    ex
            );
        }
    }

    public void verifyActivityBelongsToBusiness(
            UUID businessId,
            UUID activityId
    ) {

        try {

            JsonNode activities = restClient.get()
                    .uri(businessServiceUrl + "/api/businesses/" + businessId + "/activities")
                    .retrieve()
                    .body(JsonNode.class);

            if (activities == null || !activities.isArray()) {
                throw new ReviewValidationException(
                        "Unable to verify activity"
                );
            }

            boolean found = false;

            for (JsonNode activity : activities) {

                if (activity.has("id")
                        && activityId.toString().equals(activity.get("id").asText())) {

                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new ReviewValidationException(
                        "Activity does not belong to business"
                );
            }

        } catch (ReviewValidationException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ExternalServiceException(
                    "Unable to communicate with Business Service",
                    ex
            );
        }
    }
}
