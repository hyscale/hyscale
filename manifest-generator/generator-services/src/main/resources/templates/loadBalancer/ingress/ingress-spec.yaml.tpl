tls:
{{#tls}}
  - hosts:
    {{#hosts}}
     - {{ . }}
    {{/hosts}}
    secretName: {{ secretName }}
{{/tls}}
rules:
{{#rules}}
  - http:
      paths:
      {{#http.paths}}
        - backend:
           serviceName: {{ backend.serviceName }}
           servicePort: {{ backend.servicePort }}
          path : {{ path }}
      {{/http.paths}}
    host: {{ host }}
{{/rules}}
