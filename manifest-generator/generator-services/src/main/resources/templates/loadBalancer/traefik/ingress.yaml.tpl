apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: {{ INGRESS_NAME }}
  labels:
    hyscale.io/app-name: {{APP_NAME}}
    hyscale.io/service-name: hyscale-ingress-controller
    hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
    hyscale.io/service-name: {{SERVICE_NAME}}
    hyscale.io/component-group: {{ INGRESS_GROUP }}
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
spec:
   {{#tls.isNotEmpty}}
   tls:
   {{/tls.isNotEmpty}}
   {{#tls}}
   {{#hosts.isNotEmpty}}
   - hosts:
   {{/hosts.isNotEmpty}}
   {{#hosts}}
     - {{ . }}
   {{/hosts}}
     secretName: {{ secretName }}
   {{/tls}}
   {{#rules.isNotEmpty}}
   rules:
   {{/rules.isNotEmpty}}
   {{#rules}}
      - http:
          {{#http.paths.isNotEmpty}}
          paths:
          {{/http.paths.isNotEmpty}}
          {{#http.paths}}
            - backend:
               serviceName: {{ backend.serviceName }}
               servicePort: {{ backend.servicePort }}
              path : {{ path }}
          {{/http.paths}}
        host: {{ host }}
   {{/rules}}
