apiVersion: extensions/v1beta1
kind: Ingress
metadata:
   name: {{ INGRESS_NAME }}
   labels:
      hyscale.io/app-name: {{APP_NAME}}
      hyscale.io/service-name: hyscale-ingress-controller
      hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
      hyscale.io/component-group: {{ INGRESS_GROUP }}
      hyscale.io/platform-domain: {{ PLATFORM_DOMAIN }}
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
