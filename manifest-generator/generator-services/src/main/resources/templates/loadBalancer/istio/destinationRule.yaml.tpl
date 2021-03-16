host: {{HOST_NAME}}
trafficPolicy:
  loadBalancer:
    consistentHash:
      httpCookie:
         name: {{DEFAULT_COOKIE_NAME}}
         ttl: {{DEFAULT_COOKIE_TLS}}s