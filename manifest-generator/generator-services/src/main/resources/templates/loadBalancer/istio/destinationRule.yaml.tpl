host: {{HOST_NAME}}
trafficPolicy:
  loadBalancer:
    consistentHash:
      httpHeaderName: "x-user"