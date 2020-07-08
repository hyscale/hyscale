# Troubleshooting 

Deployment failures at Kubernetes are cryptic and not intuitive for debugging. Users need to identify the errors from pod logs, pod events(describe pod), pod status, etc. However, this does not help to troubleshoot the error. When issues occur, abstraction is necessary for users to simplify troubleshooting.

The following flow chart explains the troubleshooting flow for any hyscale deployment.

* This guide explains how to troubleshoot deployments through hyscale and it abstracts the kubernetes errors to app-centric models.
* Some of the errors which occur in a plain kubernetes deployments can be prevented through hyscale because it automates the process of manifest generation. These kinds of prevention errors are greyed out in the flowchart

<img src="docs/images/troubleshooting.jpg" height="125" />

Base Image Credit: [learnk8s.io](https://learnk8s.io/troubleshooting-deployments)

Troubleshooting is enabled in 2 ways in hyscale

* **Deployment:**  This helps an end-user to troubleshoot the entire hyscale deployment. This happens along with the deployment workflow incase of deployment failure
* **Service status:** The end-user will be able to make out what went wrong with his service incase of failure deployments. 

These troubleshooting mechanisms abstract out the k8s terminologies & complexities making it more app-centric. 

### Scope:

* **Hyscale scoped :** This scope helps the end-user not to struggle with hyscale logs in case of failure. Troubleshooting helps to understand where and what went wrong in the deployment before applying the manifests to the cluster.
* **Kubernetes scoped  :** Helps end-user not to struggle with k8s complexities , allows him to understand what went wrong with his deployment in an app-centric way.
    * Pod scope : Abstracts out errors only at pod level. 
    * Pod Dependent  scope: Abstracts out errors by crawling to the pod’s parent level and also to its dependent / referring resources
    * Post pod readiness : Abstracts out errors post pod readiness till the service is responsive to the end-user. Ex: If an Ingress controller failed to start up due to lack of resources or an ingress controller configuration that went wrong during deployment etc.

### Implementation

Once the application is deployed, we create an abstraction for troubleshooting. Whenever  Kubernetes  sends an error message, we run through a flow chart inside the tool automatically and check various things and inform where the problem may be like at start command or health check and try to translate and abstract the complex or cryptic message into a developer-friendly language. 

### Troubleshooting comparison 

Below table shows how hyscale decodes the complex error message by kubernetes,
 in a simple and understandable way.
 
<table class="tg">
<thead>
  <tr>
    <th class="tg-0pky">K8S Error message</th>
    <th class="tg-0pky">HyScale Error message</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0pky">CrashLoopBackOff</td>
    <td class="tg-0pky">Service observed to be crashing. Please verify the startCommands in hspec or CMD in Dockerfile</td>
  </tr>
  <tr>
    <td class="tg-0pky">CrashLoopBackOff</td>
    <td class="tg-0pky">Service container exited abruptly<br>Possible errors in ENTRYPOINT/ CMD in Dockerfile or missing ENTRYPOINT.</td>
  </tr>
  <tr>
    <td class="tg-0pky">CrashLoopBackOff ⇄ Running</td>
    <td class="tg-0pky">Health checks specified for service failed 3 times in succession.</td>
  </tr>
  <tr>
    <td class="tg-0pky">ImagePullBackOff</td>
    <td class="tg-0pky">Incorrect registry credentials</td>
  </tr>
  <tr>
    <td class="tg-0pky">ImagePullBackOff/ErrImagePull</td>
    <td class="tg-0pky">Invalid Image name or tag provided. Recheck the image name or tag in &lt;myimage&gt; service spec.</td>
  </tr>
  <tr>
    <td class="tg-0pky">ImagePullBackOff/ErrImagePull</td>
    <td class="tg-0pky">Missing target registry credentials for &lt;myregistry&gt;.</td>
  </tr>
  <tr>
    <td class="tg-0pky">Pending</td>
    <td class="tg-0pky">Cannot accommodate new services as the cluster is full. Please contact your cluster administrator to add cluster capacity or deploy to a different cluster.</td>
  </tr>
  <tr>
    <td class="tg-0pky">Pending</td>
    <td class="tg-0pky">Cannot provision new volumes, no storage class configured in your cluster. Please contact your cluster administrator.</td>
  </tr>
  <tr>
    <td class="tg-0pky">Pending</td>
    <td class="tg-0pky">Deployment is still in progress, service is not yet ready. Try querying after sometime.</td>
  </tr>
  <tr>
    <td class="tg-0pky">OOMKilled</td>
    <td class="tg-0pky">Out of memory errors. Not enough memory to run &lt;myservice&gt;. Increase the memory limits in service spec and try redeploying.</td>
  </tr>
  <tr>
    <td class="tg-0pky">Error</td>
    <td class="tg-0pky">Service startup commands failed with exitcode 1. Possible errors in startCommands in service spec or ENTRYPOINT/CMD in Dockerfile</td>
  </tr>
</tbody>
</table>
 


