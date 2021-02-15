#Snippet for NetworkPolicy spec
podSelector:
  matchLabels:
    hyscale.io/service-name: {{service_name}}
policyTypes:
  - Ingress

{{^enable}}
ingress:
  {{#rules}}
  - from: {{^from}}[]{{/from}}
    {{#from}}
    - podSelector:
       matchLabels:
        hyscale.io/service-name: {{.}}
    {{/from}}
    ports: {{^ports}}[]{{/ports}}
    {{#ports}}
    - port: {{.}}
    {{/ports}}
  {{/rules}}
{{/enable}}