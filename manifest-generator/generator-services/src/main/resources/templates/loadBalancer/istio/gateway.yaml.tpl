apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: {{GATEWAY_NAME}}
spec:
  selector:
    {{#labels}}
    {{key}}: {{value}}
    {{/labels}}
  servers:
  {{#servers}}
    - hosts:
      {{#hosts}}
        - {{ . }}
      {{/hosts}}
      port:
        number: {{port.number}}
        name: {{port.name}}
        protocol: {{port.protocol}}
      tls:
        mode: {{tls.mode}}
        credentialName: {{tls.credentialName}}
  {{/servers}}