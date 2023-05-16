# Overview

### NINO Insights

This API enables your application to get an opinion of the riskiness of a national insurance number (NINO).

Given a NINO, the response will provide:

* Risk score from 0 (no risk) to 100 (high risk)
* Reason providing an indication of why the risk score has been allocated
* Correlation Id - so you can reference the transaction in any feedback

### What and how?
This endpoint checks NINOs against a watch list of accounts known or suspected to have been involved in fraudulent activity. If the details are present on the list a score of 100 is returned. Otherwise, we return 0.

This logic will change and become more refined as more checks are added.

### How to use the results
The intended consumers of this API are services preparing to make payments to an individual.

You may choose to reject any payments to individuals linked to a NINO with a score above a certain threshold, or log this result in your own systems pending investigation.

### Request\response details

All requests must include a uniquely identifiable `user-agent` header. Please contact us for assistance when first connecting.  

* POST /check/insights
    * [Request](request-sample.json) ([schema](request.json))
    * [Response](response-sample.json) ([schema](response.json))

