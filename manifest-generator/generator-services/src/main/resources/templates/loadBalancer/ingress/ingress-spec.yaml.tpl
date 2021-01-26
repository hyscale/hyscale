tls:
  - hosts:
     - {{ host }}
    secretName: {{ loadBalancer.tlsSecret }}
rules:
  - http:
      paths:
      {{#loadBalancer.mapping}}
      {{#contextPaths}}
        - backend:
           serviceName: {{ serviceName }}
           servicePort: {{ port }}
          path : {{ . }}
      {{/contextPaths}}
      {{/loadBalancer.mapping}}
    host: {{ host }}