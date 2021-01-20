apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
    name: {{DESTINATION_RULE_NAME}}
spec:
    host: {{HOST_NAME}}
    trafficPolicy:
      loadBalancer:
        consistentHash:
                  httpHeaderName: x-user