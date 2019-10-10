apiVersion: v1
kind: Service
metadata:
  name: {{ K8S_SERVICE_NAME }}
  {{#annotations.isNotEmpty}}
  annotations:
  {{/annotations.isNotEmpty}}
  {{#annotations}}
    {{key}}: "{{{value}}}"
  {{/annotations}}
  labels:
    hyscale.io/app-name: {{APP_NAME}}
    hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
    hyscale.io/service-name: {{SERVICE_NAME}}
spec:
  type: {{ SERVICE_TYPE }}
  ports:
  {{#serviceports}}
  -  name: {{ name }}
     port: {{ port }}
     protocol: {{ protocol }}
     targetPort: {{ targetPort }}
  {{/serviceports}}
  sessionAffinity: {{ SESSION_AFFINITY }}
  selector:
    hyscale.io/app-name: {{APP_NAME}}
    hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
    hyscale.io/service-name: {{SERVICE_NAME}}
