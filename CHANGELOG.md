# v0.9.7 Release

### Features:

* [Installer support for Mac & Windows](https://github.com/hyscale/hyscale/issues/222)
* [1.17 cluster support](https://github.com/hyscale/hyscale/issues/204)
* [Structured output for hyscale deployments](https://github.com/hyscale/hyscale/issues/206)
* [Support for Credstore](https://github.com/hyscale/hyscale/issues/222)
### Fixes

* [Fix for retrieving service IP is based on labels rather than service name](https://github.com/hyscale/hyscale/issues/192)
* [Fix for change in props not reflected as env in hyscale deployments](https://github.com/hyscale/hyscale/issues/210)
* [Fix for fetching service logs of multiple replicas with retry option when provided an invalid replica name](https://github.com/hyscale/hyscale/issues/219)
* [Fix for service status command, when service is deployed with zero ports](https://github.com/hyscale/hyscale/issues/220)
* [Fix for troubleshooting message , when a service is deployed with invalid start commands](https://github.com/hyscale/hyscale/issues/221)
* [Fix for artifacts not found message](https://github.com/hyscale/hyscale/issues/231)
* [Renamed env IMAGE_CLEANUP_POLICY to HYS_IMAGE_CLEANUP_POLICY](https://github.com/hyscale/hyscale/issues/278)  

# v0.9.6 Release

### Features:

* Support for imperative scaling 
* Deployment wait time tuning proportional to replicas  

### Fixes

* Reduced API calls to improve performance for service and app status commands
* Fix for handling zero replicas for logs and status commands
* Performance improvements with lazy loading   

# v0.9.5 Release

 ### Features:

* Support for multi-stage dockerfiles in hspec   
* Support for profiles with ProfileName(-P) option
* Get Apps include profile column
* Get replica status of a particular service 

### Fixes

* Fix for fetching schema from remote repository
* Fix for validating for cluster,registry etc. entities upfront
* Fix for using TLS 1.2 as default protocol while communcicating with kubernetes cluster as temporary workaround for [OpenJDK bug](https://bugs.openjdk.java.net/browse/JDK-8236039) 

# v0.9.4.1 Release

This is a minor release to support listing applications in a cluster,https healthcheck for services,troubleshooting failures with pod exitcodes, 

### Features:

* Support for https healthcheck of services.  
* To list application that are deployed in a cluster.
* Improved troubleshooting error scope to include pod exit codes

### Fixes

* Fixed order of ImagePullSecret being created before pod
* Moved KubeConfig & DockerConfig file validations to runtime.

# v0.9.4 Release

This is a minor release to support windows,  1.16 cluster support , improved troubleshooting error scope 

### Features:

* Improved troubleshooting error scope to pod parent 
* Support for 1.16 Kubernetes clusters.  
* Support for windows as a binary.

### Fixes

* Image clean up with a policy after building images from hyscale.
* Added multiple default storage classes validation.

# v0.9.3 Release

This is a minor release to support schema validation, replica logs & troubleshooting.

### Features:

* Support for service & profile spec schema validators 
* Support for fetching & tailing specific replica logs of a service.
* Beta release to support troubleshooting k8s errors.

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
