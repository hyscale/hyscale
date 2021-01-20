apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: {{VIRTUAL_SERVICE_NAME}}
spec:
  hosts:
  {{#hosts}}
  - {{ . }}
  {{/hosts}}
  gateways:
  {{#gateways}}
  - {{ . }}
  {{/gateways}}
  http:
  - match:
    {{#matchRequests}}
    - uri:
       prefix: {{uri.matchType.prefix}}
    {{/matchRequests}}
    headers:
      request:
        set:
         {{#headers}}
         {{key}}: {{value}}
         {{/headers}}
    route:
    {{#routes}}
    - destination:
        host: {{destination.host}}
        port:
          number: {{destination.port.number}}
    {{/routes}}