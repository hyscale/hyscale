metadata:
  name: {{ INGRESS_NAME }}
  labels:
    hyscale.io/platform-domain: {{ PLATFORM_DOMAIN }}
    hyscale.io/ingress-provider: {{ INGRESS_PROVIDER }}
  annotations:
    kubernetes.io/ingress.class: "{{INGRESS_CLASS}}"
    traefik.ingress.kubernetes.io/frontend-entry-points: {{ FRONTEND_ENTRYPOINTS }}
    {{#REDIRECT_ENTRYPOINTS}}
    traefik.ingress.kubernetes.io/redirect-entry-point: {{ REDIRECT_ENTRYPOINTS }}
    {{/REDIRECT_ENTRYPOINTS}}
    {{#HEADERS_EXPRESSION }}
    ingress.kubernetes.io/custom-request-headers: {{ HEADERS_EXPRESSION }}
    {{/HEADERS_EXPRESSION }}