![HyScale](https://www.hyscale.io/wp-content/uploads/2019/01/hyscale-logo.png)

[![Actions Status](https://github.com/hyscale/hyscale/workflows/Build/badge.svg)](https://github.com/hyscale/hyscale/actions?query=workflow%3ABuild)

HyScale is an application deployment tool that helps you effortlessly deploy to Kubernetes. It offers a high-level abstraction on top of Kubernetes so that teams can deploy software to K8s while focusing more on code rather than on low-level details.

**You can use HyScale to:**

+ Generate Docker files & images from packaged code like binaries, jar, war, scripts etc 
+ Generate Kubernetes manifests from an app-centric declarative description of services/applications & configurations
+ Deploy manifests into directed clusters and get a live service/app URL on Kubernetes
+ Easily troubleshoot any deployment issues by getting actionable easily-understood app-centric error messages than the cryptic low-level messages that K8s returns. 

## Our Belief

At HyScale, we believe all complexities around deployment can be abstracted and eventually be eliminated. Our goal here is to create a community of users building and deploying apps to Kubernetes in the most efficient way possible.  Our roadmap and priorities will be shared soon along with tasks and features that can help the community,

## Contribute

Do you have any inputs that can make HyScale better? Say, a bug or a feature request? Please open a new issue [here](https://github.com/hyscale/hyscale/issues). See our architecture & contributor documentation [here](https://github.com/hyscale/hyscale/blob/master/docs/contributor-guide.md).

## Community
Let us know your experiences with HyScale! Questions, Issues, Suggestions - we look forward to them all!
* Follow [@hyscaleio](https://twitter.com/hyscaleio) updates on Twitter 
* Read [@teamhyscale](https://medium.com/@teamhyscale)  updates and musings on Medium
* Write to us at connect@hyscale.io  and we will respond as quickly as we can.

## Capabilities

**1. Automatic containerization & auto-generation of K8s yamls**

HyScale offers a declarative spec for K8s abstraction using which K8s manifests & docker files are automatically generated, docker images are built & pushed to the target docker registry, and the manifests are deployed to the K8s cluster resulting in a URL.

**2. App-centric abstraction**

HyScale's app centric abstraction helps you achieve the folowing with just a few lines of declaration:

+ Setting up resource-limits and enabling auto-scaling.

+ Enabling health-checks on a http path or tcp port.

+ Declaring volume paths for storing service data.

+ Providing configuration properties that are automatically made available as a file within the pod and as env props in the service container.

+ Declaring the keys for secrets such as passwords & tokens that are automatically made available from the K8s secrets store.

+ Attaching log, monitoring and tracing agents to the service.

+ Override or add different configurations for different environments using profiles.


**3. App-centric troubleshooting**

Deployment failures at Kubernetes are cryptic and not intuitive for debugging. Users have to refer many things to identify the root cause of the failure like pod status, describe pod , statuses of other kinds etc. When issues occur abstraction is needed to simplify troubleshooting. So instead of presenting users with an error like "CrashLoopBackOff", HyScale executes a troubleshooting flowchart that will basically try to figure out the possible causes and inform the user in plain terms. 
Hyscale abstracts Kubernetes errors to an app-centric model eg.: a "Pending" state may mean one of many things such as "New services cannot be accommodated as cluster capacity is full" or "Specified volume cannot be attached to the service"


## Getting started

Here is what you need to do:

<img src="docs/images/user-workflow.png" height="125" />

Here is a glimpse of what HyScale does when you invoke it

<img src="docs/images/inside-hyscale.png" height="400" />

To get started, install hyscale as per the below [instructions](https://github.com/hyscale/hyscale#prerequisites) & follow the [tutorial](https://www.hyscale.io/tutorial/get-started/) to deploy your first app.
For detailed information, refer [hspec](https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md).

## Prerequisites
In order to deploy your service to K8s, you must have the following configurations and installations in place on your machine from which you wish to deploy your application.
1. Docker 18.09.x or above. Your Linux user should be part of the docker group and `docker.sock` should be present at /var/run/docker.sock (Default location) 
2. Kubernetes authentication credentials kubeconfig file having the cluster token placed at $HOME/.kube/config
3. Image registry credentials at $HOME/.docker/config.json . Make sure `config.json` has the latest auth creds by logging into the image registry using `docker login` prior to deployment.

If you do not have access to a kubernetes cluster and wish to deploy your application to a local cluster on your machine, you could try setting up [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) or [kind](https://github.com/kubernetes-sigs/kind).

## Installation

#### Linux

Open your terminal and enter the following:

```sh
curl -sSL https://get.hyscale.io | bash
```

#### Mac & Windows
Usage Pre-Requisites:

* JDK version 11 and above
* Download the [hyscale jar](https://github.com/hyscale/hyscale/releases/latest/download/hyscale.jar) to your local machine

Usage:

`java -Djdk.tls.client.protocols=TLSv1.2 -jar </path/to/hyscale.jar> <commands>` 

Note:
jdk.tls.client.protocols property is set to overcome a known issue in open jdk.


For commands refer [here](https://github.com/hyscale/hyscale/blob/master/docs/hyscale-commands-reference.md) by replacing `hyscale` with `java command`.  

```
Example :  java -Djdk.tls.client.protocols=TLSv1.2 -jar </path/to/hyscale.jar> deploy service -f myservice.hspec -n my-namespace -a my-app
```

Verified on CentOS, Ubuntu and Debian Linux, Mac, Windows.

## Deploying to K8s

### Preparing your first service spec (hspec)

Here is a basic service spec for deploying tomcat (without any application). To get started with more options see the [tutorial](https://www.hyscale.io/tutorial/get-started/).

##### myservice.hspec

```yaml
name: myservice
image:
    registry: registry.hub.docker.com
    name: library/tomcat
    tag: 8.5.0-jre8
 
volumes:
    - name: tomcat-logs-dir
      path: /usr/local/tomcat/logs
      size: 1Gi
      storageClass: standard

replicas:
    min: 1
    max: 3
    cpuThreshold: 40%
 
external: true
ports:
  - port: 8080/tcp
    healthCheck:
       httpPath: /docs/images/tomcat.gif

```
Managing configuration differences across environments is necessary, so a hspec alone may not be sufficient across all environments. Environment specific configurations can be achieved through [profiles](https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md#profile-files) as shown in the example below.

####  Stage profile for myservice can be like

##### stage-myservice.hprof

```yaml
environment: stage
overrides: myservice
 
volumes:
    - name: tomcat-logs-dir
      size: 2Gi

replicas:
    min: 1
    max: 4
    cpuThreshold: 30%
 

```

### Deploy the service

**To deploy, invoke the hyscale deploy command:**
    
```sh
hyscale deploy service -f `<myservice.hspec>` -n `<my-namespace>` -a `<my-app-name>`
```

**To deploy with profiles, invoke the hyscale deploy command:**
    
```sh
hyscale deploy service -f `<myservice.hspec>` -n `<my-namespace>` -a `<my-app-name>` -p `<stage-myservice.hprof>`
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

### HyScale version compatibility
####  HyScale vs [hspec version](https://github.com/hyscale/hspec) 
<table>
<tr>
    <th class="tg-0lax">hspec-version ➝ </th>
    <th class="tg-cly1"><a href="https://github.com/hyscale/hyscale/blob/v0.9/docs/hyscale-spec-reference.md">0.5</a></th>
    <th class="tg-cly1"><a href="https://github.com/hyscale/hspec/blob/v0.6/docs/hyscale-spec-reference.md">0.6</a></th>
    <th class="tg-0lax"><a href="https://github.com/hyscale/hspec/blob/v0.6.1/docs/hyscale-spec-reference.md">0.6.1</a></th>
    <th class="tg-0lax"><a href="https://github.com/hyscale/hspec/blob/v0.6.1.1/docs/hyscale-spec-reference.md">0.6.1.1</a></th>
  </tr>
  <tr>
    <td class="tg-0lax">0.9</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-cly1">-️</td>
    <td class="tg-0lax">-️</td>
    <td class="tg-0lax">-️</td>
  </tr>
  <tr>
    <td class="tg-0lax">0.9.1</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">-️</td>
    <td class="tg-0lax">-️</td>
  </tr>
  <tr>
    <td class="tg-0lax">0.9.2</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">-️</td>
  </tr>
  <tr>
    <td class="tg-0lax">0.9.3</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
  </tr>
  <tr>
    <td class="tg-0lax">0.9.4</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
  </tr>
  </table>
  
#### HyScale vs Kubernetes cluster 

<table class="tg">
  <tr>
    <th class="tg-cly1">cluster-version ➝ </th>
    <th class="tg-cly1">1.12</th>
    <th class="tg-0lax">1.13</th>
    <th class="tg-0lax">1.14</th>
    <th class="tg-0lax">1.15</th>
    <th class="tg-0lax">1.16</th>
  </tr>
  <tr>
    <td class="tg-cly1">0.9</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
  </tr>
  <tr>
    <td class="tg-cly1">0.9.1</td>
    <td class="tg-cly1">✔️</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
  </tr>
  <tr>
    <td class="tg-cly1">0.9.2</td>
    <td class="tg-cly1">+</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
  </tr>
  <tr>
    <td class="tg-cly1">0.9.3</td>
    <td class="tg-cly1">+</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">-</td>
    <td class="tg-0lax">-</td>
  </tr>
  <tr>
    <td class="tg-cly1">0.9.4</td>
    <td class="tg-cly1">+</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
    <td class="tg-0lax">✔️</td>
  </tr>
</table>

Key: 

* `✔` Supported version 
* `-` Unsupported version
* `+` Backward compatible


