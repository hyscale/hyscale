# HyScale
### The k8s Deployment Tool 

HyScale is an app deployment tool for deploying apps to Kubernetes quickly without having to learn k8s concepts or write & maintain k8s manifests. It provides a convenient CLI for deploying, viewing status, logs and undeploying. It includes a declarative service spec parser for k8s abstraction and the automatic generation of k8s manifests & docker files.

See documentation [here](docs/developer-guide.md).

## Installation

Open your terminal and enter the following:

    $  curl -sSL http://get.hyscale.io | bash

## Prerequisites
In order to deploy your service to k8s, you must have the following configurations and installations in place on your machine from which you wish to deploy your application.
1. Docker 18.09.x or above
2. kube config file with the cluster token placed at $HOME/.kube/config
3. Image registry credentials at $HOME/.docker/config.json (make sure `config.json` has the latest auth creds by logging into the image registry using `docker login` prior to deployment)

## Deploying to k8s
### Preparing your first service spec

Here is a small service spec that works for a basic java app. For all possible options, see the [spec reference](docs/hyscale-spec-reference.md).

```yaml
name: myservice
image:
    registry: registry.hub.docker.com/library
    name: tomcat
    tag: 8.5.0-jre8
 
volumes:
    - name: tomcat-logs-dir
      path: /usr/local/tomcat/logs
      size: 1Gi
      storageClass: standard
 
external: true
ports:
  - port: 8080/tcp
    healthCheck:
       httpPath: /docs/images/tomcat.gif

```

### Deploy the service

To deploy, invoke the hyscale deploy command:
hyscale deploy service -f `<my-service.hspec.yaml>` -n `<my-namespace>` -a `<my-app-name>`

To view the status of your deployment:
hyscale get service status -s `<my-service>` -n `<my-namespace>` -a `<my-app-name>`

To view logs:
hyscale get service logs -s `<my-service>` -n `<my-namespace>` -a `<my-app-name>`

For all possible commands, see the [command reference](docs/hyscale-commands-reference.md).

These commands have been tested on CentOS, Ubuntu and Debian Linux
