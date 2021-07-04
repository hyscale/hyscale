name: {{ INGRESS_NAME }}
labels:
  hyscale.io/app-name: {{APP_NAME}}
  hyscale.io/service-name: {{SERVICE_NAME}}
  {{#ENVIRONMENT_NAME}}
  hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
  {{/ENVIRONMENT_NAME}}
annotations:
  {{#INGRESS_CLASS}}
  kubernetes.io/ingress.class: {{INGRESS_CLASS}}
  {{/INGRESS_CLASS}}
  {{^ALLOW_HTTP }}
  kubernetes.io/ingress.allow-http: "true"
  {{/ALLOW_HTTP }}
  {{#ALLOW_HTTP }}
  kubernetes.io/ingress.allow-http: "false"
  {{/ALLOW_HTTP }}
  {{^SSL_REDIRECT }}
  nginx.ingress.kubernetes.io/ssl-redirect: "false"
  {{/SSL_REDIRECT }}
  {{#SSL_REDIRECT }}
  nginx.ingress.kubernetes.io/ssl-redirect: "true"
  {{/SSL_REDIRECT }}
  nginx.ingress.kubernetes.io/use-regex: "true"
  {{#STICKY }}
  nginx.ingress.kubernetes.io/affinity: {{ STICKY }}
  {{/STICKY }}
  {{#CONFIGURATION_SNIPPET}}
  nginx.ingress.kubernetes.io/configuration-snippet: |
    {{{ CONFIGURATION_SNIPPET }}}
  {{/CONFIGURATION_SNIPPET}}
