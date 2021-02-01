selector:
{{#labels}}
  {{key}}: {{value}}
{{/labels}}
servers:
{{#loadBalancer.mapping}}
- hosts:
  {{#hosts}}
    - {{ . }}
  {{/hosts}}
  port:
    number: {{PORT_NUMBER}}
    name: {{PROTOCOL}}
    protocol: {{PROTOCOL}}
  {{#loadBalancer.tlsSecret}}
  tls:
    mode: {{TLS_MODE}}
    credentialName: {{loadBalancer.tlsSecret}}
  {{/loadBalancer.tlsSecret}}
{{/loadBalancer.mapping}}