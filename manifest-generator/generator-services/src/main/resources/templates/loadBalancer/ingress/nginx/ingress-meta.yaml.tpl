name: {{ INGRESS_NAME }}
labels:
  hyscale.io/app-name: {{APP_NAME}}
  hyscale.io/service-name: {{SERVICE_NAME}}
  hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
  hyscale.io/component-group: {{ INGRESS_GROUP }}
  hyscale.io/ingress-provider: {{ INGRESS_PROVIDER }}
annotations:
  kubernetes.io/ingress.class: "{{INGRESS_CLASS}}"
  kubernetes.io/ingress.allow-http: {{ ALLOW_HTTP }}
  nginx.ingress.kubernetes.io/ssl-redirect: {{ SSL_REDIRECT }}
  nginx.ingress.kubernetes.io/use-regex: "true"
  nginx.ingress.kubernetes.io/affinity: {{ STICKY }}
  {{#CONFIGURATION_SNIPPET}}
  nginx.ingress.kubernetes.io/configuration-snippet: |
    {{{ CONFIGURATION_SNIPPET }}}
  {{/CONFIGURATION_SNIPPET}}
