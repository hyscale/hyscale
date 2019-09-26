apiVersion: v1
kind: Secret
metadata:
  name: {{name}}
  labels:
     hyscale.io/app-name: {{APP_NAME}}
     hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson : {{{configJson}}}