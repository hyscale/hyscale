apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ CONFIGMAP_NAME }}
  labels:
    hyscale.io/app-name: {{APP_NAME}}
    hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
    hyscale.io/service-name: {{SERVICE_NAME}}
data:
   {{#configProps.isNotEmpty}}
   {{#configProps}}
   {{key}}: "{{{value}}}"
   {{/configProps}}
   {{/configProps.isNotEmpty}}
   {{#CONFIGPROPS_FILE}}
   {{CONFIGPROPS_FILE}}: "{{{CONFIGPROPS_FILEDATA}}}"
   {{/CONFIGPROPS_FILE}}
{{#fileProps.isNotEmpty}}
binaryData:
   {{#fileProps}}
   {{key}}: {{{value}}}
   {{/fileProps}}
{{/fileProps.isNotEmpty}}