hosts:
{{#hosts}}
- {{ . }}
{{/hosts}}
gateways:
{{#gateways}}
- {{ . }}
{{/gateways}}
http:
{{#loadBalancer.mapping}}
- match:
{{#contextPaths}}
  - uri:
      {{MATCH_TYPE}}: {{.}}
 {{/contextPaths}}
  headers:
    request:
      set:
        {{#headers}}
        {{key}}: {{value}}
        {{/headers}}
  route:
  - destination:
      host: {{serviceName}}
      port:
        number: {{portNumber}}
{{/loadBalancer.mapping}}