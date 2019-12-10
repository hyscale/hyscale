#Snippet for HorizontalPodAutoScaler spec
scaleTargetRef:
    apiVersion: {{TARGET_APIVERSION}}
    kind: {{TARGET_KIND}}
    name: {{TARGET_NAME}}
minReplicas: {{MIN_REPLICAS}}
maxReplicas: {{MAX_REPLICAS}}
targetCPUUtilizationPercentage: {{AVERAGE_UTILIZATION}}