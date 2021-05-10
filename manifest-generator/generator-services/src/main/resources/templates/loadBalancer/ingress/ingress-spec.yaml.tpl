{{#loadBalancer.tlsSecret}}
tls:
  - hosts:
     - {{ host }}
    secretName: {{ loadBalancer.tlsSecret }}
{{/loadBalancer.tlsSecret}}
rules:
  - http:
      paths:
      {{#loadBalancer.mapping}}
      {{#contextPaths}}
        - backend:
           serviceName: {{ serviceName }}
           servicePort: {{ portNumber }}
          path : {{ . }}
      {{/contextPaths}}
      {{/loadBalancer.mapping}}
    host: {{ host }}