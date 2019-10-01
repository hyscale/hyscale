apiVersion: apps/v1beta2
kind: StatefulSet
metadata:
  name: {{STATEFULSET_NAME}}
  labels:
   hyscale.io/app-name: {{APP_NAME}}
   hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
   hyscale.io/service-name: {{SERVICE_NAME}}
spec:
  updateStrategy:
    type: RollingUpdate
  replicas: {{REPLICAS_COUNT}}
  selector:
    matchLabels:
        hyscale.io/app-name: {{APP_NAME}}
        hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
        hyscale.io/service-name: {{SERVICE_NAME}}
  template:
    metadata:
      labels:
        hyscale.io/app-name: {{APP_NAME}}
        hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
        hyscale.io/service-name: {{SERVICE_NAME}}
        hyscale.io/release-version: {{RELEASE_VERSION}}
    spec:
      {{#volumes.isNotEmpty}}
      volumes:
      {{/volumes.isNotEmpty}}
      {{#volumes}}
       - name: {{ name }}
         {{#persistentVolumeClaim}}
         persistentVolumeClaim:
          claimName: {{ persistentVolumeClaim.claimName }}
         {{/persistentVolumeClaim}}
         {{#configMap}}
         configMap:
           name: {{ name }}
         {{/configMap}}
         {{#secret}}
         secret:
            secretName: {{secretName}}
         {{/secret}}
      {{/volumes}}
      imagePullSecrets:
       - name: {{IMAGE_PULL_SECRET_NAME}}
      containers:
      {{#containers}}
      - name: {{name}}
        image: {{image}}
        imagePullPolicy: {{imagePullPolicy}}
      {{#command.isNotEmpty}}
        command:
      {{/command.isNotEmpty}}
      {{#command}}
        - "{{{.}}}"
      {{/command}}
      {{#args.isNotEmpty}}
        args:
      {{/args.isNotEmpty}}
      {{#args}}
        - "{{{.}}}"
      {{/args}}
        {{#env.isNotEmpty}}
        env:
        {{/env.isNotEmpty}}
        {{#env}}
        - name: {{name}}
          {{#value}}
          value: {{value}}
          {{/value}}
          {{#valueFrom}}
          valueFrom:
             {{#configMapKeyRef}}
             configMapKeyRef:
                 name: {{name}}
                 key: {{key}}
             {{/configMapKeyRef}}
             {{#secretKeyRef}}
             secretKeyRef:
                 name: {{name}}
                 key: {{key}}
             {{/secretKeyRef}}
          {{/valueFrom}}
        {{/env}}
        {{#volumeMounts.isNotEmpty}}
        volumeMounts:
        {{/volumeMounts.isNotEmpty}}
        {{#volumeMounts}}
        - name: {{name}}
          mountPath: {{mountPath}}
          readOnly: {{readOnly}}
        {{/volumeMounts}}
        {{#ports.isNotEmpty}}
        ports:
        {{/ports.isNotEmpty}}
        {{#ports}}
        - name: {{name}}
          containerPort: {{containerPort}}
          protocol: {{protocol}}
        {{/ports}}
        {{#readinessProbe}}
        readinessProbe:
            failureThreshold: {{failureThreshold}}
            {{#exec}}
            exec:
              command: {{execCommand}}
            {{/exec}}
            {{#tcpSocket}}
            tcpSocket:
              port: {{tcpSocket.port}}
            {{/tcpSocket}}
            {{#httpGet}}
            httpGet:
              path: {{httpGet.path}}
              port: {{httpGet.port}}
              scheme: {{httpGet.scheme}}
            {{/httpGet}}
            periodSeconds: {{periodSeconds}}
            initialDelaySeconds: {{initialDelaySeconds}}
            successThreshold: {{successThreshold}}
            timeoutSeconds: {{timeoutSeconds}}
        {{/readinessProbe}}
        {{#livenessProbe}}
        livenessProbe:
            failureThreshold: {{failureThreshold}}
            {{#exec}}
            exec:
              command: {{execCommand}}
            {{/exec}}
            {{#tcpSocket}}
            tcpSocket:
              port: {{tcpSocket.port}}
            {{/tcpSocket}}
            {{#httpGet}}
            httpGet:
              path: {{httpGet.path}}
              port: {{httpGet.port}}
              scheme: {{httpGet.scheme}}
            {{/httpGet}}
            periodSeconds: {{periodSeconds}}
            initialDelaySeconds: {{initialDelaySeconds}}
            successThreshold: {{successThreshold}}
            timeoutSeconds: {{timeoutSeconds}}
        {{/livenessProbe}}
        {{#k8sResourceRequirements}}
        resources:
           {{#requests}}
           requests:
               {{#requests.cpu}}
               cpu: {{requests.cpu}}
               {{/requests.cpu}}
               {{#requests.memory}}
               memory: {{requests.memory}}
               {{/requests.memory}}
           {{/requests}}
           {{#limits}}
           limits:
               {{#limits.cpu}}
               cpu: {{limits.cpu}}
               {{/limits.cpu}}
               {{#limits.memory}}
               memory: {{limits.memory}}
               {{/limits.memory}}
           {{/limits}}
        {{/k8sResourceRequirements}}
        {{/containers}}
  {{#volumeClaimTemplates.isNotEmpty}}
  volumeClaimTemplates:
  {{/volumeClaimTemplates.isNotEmpty}}
  {{#volumeClaimTemplates}}
  - metadata:
      name: {{ name }}
      labels:
           hyscale.io/app-name: {{APP_NAME}}
           hyscale.io/environment-name: {{ENVIRONMENT_NAME}}
           hyscale.io/service-name: {{SERVICE_NAME}}
           hyscale.io/volume-name: {{ volumeName }}
      annotations:
          volume.beta.kubernetes.io/storage-class: {{ storageClassName }}
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: {{ size }}
  {{/volumeClaimTemplates}}
