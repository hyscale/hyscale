apiVersion: v1
kind: Secret
metadata:
  name: {{ SECRET_NAME }}
  labels:
     hyscale.io/app-name: {{APP_NAME}}
     hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
     hyscale.io/service-name: {{SERVICE_NAME}}
data:
    {{#configSecrets.isNotEmpty}}
    {{#configSecrets}}
    {{key}}: "{{{value}}}"
    {{/configSecrets}}
    {{/configSecrets.isNotEmpty}}
    {{#configSecrets.isNotEmpty}}
    {{SECRET_FILE_NAME}}: "{{{SECRET_FILE_DATA}}}"
    {{/configSecrets.isNotEmpty}}
