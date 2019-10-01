apiVersion: v1
kind: Secret
metadata:
  name: {{ SECRET_NAME }}
  labels:
     hyscale.io/app-name: {{APP_NAME}}
     hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
data:
    tls.crt: {{{ TLS_CERTIFICATE }}}
    tls.key: {{{ TLS_KEY }}}
