NINO Insights
---------------------

`nino-insights` provides insights for the NINO provided.

Given a request of the following form

```json
{
  "nino": "AB123456A"
}
```

the API may provide a response of the following form

```json
{
    "ninoInsightsCorrelationId": "ab8514f3-0f3c-4823-aba6-58f2222c33f1",
    "riskScore": 100,
    "reason": "NINO_ON_WATCH_LIST"
}
```

* `ninoInsightsCorrelationId` - A unique `UUID` to allow the request/response to be tracked
* `riskScore`     - A score indicating the _riskiness_ of the NINO in question. `0` indicates low risk and `100` indicate high risk
* `reason`        - The reason for the score. `NINO_ON_WATCH_LIST` indicates the NINO in question was found on a watch list, `NINO_NOT_ON_WATCH_LIST` indicates it was not found on any watch list


### Response status codes
* **200** - The request was serviced
* **400** - The request payload was not valid
