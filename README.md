![HyScale](https://www.hyscale.io/wp-content/uploads/2019/01/hyscale-logo.png)

### The k8s App Deployment Tool 

Kubernetes (k8s) has emerged as the defacto container orchestration platform offering excellent abstraction over infrastructure. But service deployments and delivery mechanisms to k8s are still way too complex. Delivery tools should simplify things for a developer so that developers can focus on writing interesting stuff & building value. This is best achieved if all the complexity of deployment completely disappears!

HyScale is a starting point for how a simplified service spec can allow developers to easily deploy their apps to k8s without having to wade through k8s complexities and also without having to write or maintain hundreds of lines of yaml.

It offers a declarative service spec parser for k8s abstraction and the automatic generation of k8s manifests & docker files.

Here is what you need to do:

![User-Workflow](docs/images/user-workflow.png)

To get started, install hyscale as per the below instructions & follow the tutorial to deploy your first app.

See full documentation [here](docs/developer-guide.md).

## Prerequisites
In order to deploy your service to k8s, you must have the following configurations and installations in place on your machine from which you wish to deploy your application.
1. Docker 18.09.x or above
2. kube config file with the cluster token placed at $HOME/.kube/config
3. Image registry credentials at $HOME/.docker/config.json (make sure `config.json` has the latest auth creds by logging into the image registry using `docker login` prior to deployment)
4. Your Linux user should be part of the docker group and `docker.sock` to be present at /var/run/docker.sock (Default location) 

If you do not have access to a kubernetes cluster and wish to deploy to your local machine, you could try setting up [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) or [kind](https://github.com/kubernetes-sigs/kind).

## Installation

Open your terminal and enter the following:

```sh
curl -sSL http://get.hyscale.io | bash
```

## Deploying to k8s

### Preparing your first service spec

Here is a basic service spec for deploying tomcat (without any application). For all possible options, see the [spec reference](docs/hyscale-spec-reference.md).

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

**To deploy, invoke the hyscale deploy command:**
    
```sh
hyscale deploy service -f `<myservice.hspec.yaml>` -n `<my-namespace>` -a `<my-app-name>`
```

**To view the status of your deployment:**

```sh
hyscale get service status -s `<myservice>` -n `<my-namespace>` -a `<my-app-name>`
```

**To view logs:**
```sh
hyscale get service logs -s `<myservice>` -n `<my-namespace>` -a `<my-app-name>`
```

For all possible commands, see the [command reference](docs/hyscale-commands-reference.md).

These commands have been verified on CentOS, Ubuntu and Debian Linux
