# v0.1.0 Release - 26/9/2019

This is the first release of Hyscale.

Hyscale is under active development, some features may change at any time.

New Features:

* Added hyscale up command to build container image, generate manifests and applying onto kubernetes cluster.

* Added hyscale delete command to delete k8s resources if any.

* Added hyscale get command to get detailed hyscale resources.

* Added hyscale version command.

* Added hyscale up --generate option to generate manifests but not to apply.

* supported both dockerfile spec as well buildspec for container image creation.

* Added builders: Local Docker, kaniko in cluster

* Added deployers: kubernetes java client

* Added support for pushing container image to registry.

* Log Streaming of Deployed K8s resources.

* Status of deployed K8s resources.

* Added examples for quickstart.

* Added use of docker config json for docker registry authentication.

* Added use of service account present in kubeconfig for kubernetes cluster authentication.

Docs:

* Service spec Reference (link to the checked in docs)
* Tool command Reference (link to the checked in docs)
* etc


Bug Fixes:
