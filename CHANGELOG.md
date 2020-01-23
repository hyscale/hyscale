# v0.9.2 Release

This is a minor release to support profiles , 1.14 cluster support

### Features:

* Support for profiles with hprof file
* Support for kubernetes 1.14 cluster.

### Fixes

* Fix for apiversion of deployment, statefulset manifest generation with apps/v1
* Fix for secretsVolumePath not being mounted as secret.props
* Fix for manifest cleanup with installater
* Fix for image build with dockerfile


# v0.9.1 Release

This is a minor release to support mac , sidecar and autoscaling support.

### Features:

* Support for Mac with binary.
* Support for agents which can be attached as a sidecar to primary service
* Support to configure service Horizontal Auto Scaling 
* Sample templates for Apache Load Balancer, Go-lang, MongoDB, Mysql, Nginx, NodeJS, Python, RabbitMQ, Redis to quick start deployment.


### Fixes:

* Fix to merge array elements during plugins generation
* Fix to support a file as a service property. 
* Fix to support configmap file type
* Fix for docker registries with service account authentication.
* Fix for run commands as Docker Entrypoint
* Fix for image repo digest
* Fix for merge array elements for plugin snippets merge. 
* Fix for Application Name, Namespace, ServiceName validation



# v0.9 Release

This is the first open source release of HyScale.

### Features:

* Generates Dockerfile with artifacts, commands
* Builds docker image &  pushes the image to target registry
* Generates Kubernetes manifests
* Applies the manifests to configured Kubernetes cluster
* Enables troubleshooting of deployments with service status & logs
* Provides undeployment of service / app from the cluster
