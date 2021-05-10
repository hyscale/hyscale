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
  {{#FRONTEND_ENTRY_POINTS}}
  traefik.ingress.kubernetes.io/frontend-entry-points: {{FRONTEND_ENTRY_POINTS}}
  {{/FRONTEND_ENTRY_POINTS}}
  {{#REDIRECT_ENTRYPOINTS}}
  traefik.ingress.kubernetes.io/redirect-entry-point: {{ REDIRECT_ENTRY_POINTS }}
  {{/REDIRECT_ENTRYPOINTS}}
  {{#HEADERS_EXPRESSION }}
  ingress.kubernetes.io/custom-request-headers: {{ HEADERS_EXPRESSION }}
  {{/HEADERS_EXPRESSION }}
