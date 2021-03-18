host: {{HOST_NAME}}
trafficPolicy:
  loadBalancer:
    consistentHash:
      httpCookie:
         name: {{COOKIE_NAME}}
         ttl: {{COOKIE_TTL}}m