# Custom K8s Snippets in Hyscale
#### Description

Hyscale generates K8s manifest yamls with respect to Hspec that satisfies the majority of application use cases with exception to few kubernetes concepts.The thought behind Custom K8s Snippets is to eliminate the effect of current/temporary limitations of Hyscale over user’s K8s requirements and approach regarding application use cases.

Allowing the end user to attach their own k8s yaml snippets to the Hspec provides a choice to customize or override on top of generated manifests by Hyscale. Also ensuring that the usage of Hspec and deploy using Hyscale should not restrict an end user from utilizing any K8s Features which are yet to be abstracted by Hyscale.

### Introducing 'k8sSnippets'

We have introduced a new field "k8sSnippets" in Hspec where in the user will provide paths for List of k8s snippets that needs to be patched on the generated manifest files. 
A simple hspec with custom k8s snippets looks like :

```yaml
name: myservice
image:
    registry: registry.hub.docker.com
    name: library/tomcat
    tag: 8.5.0-jre8
    
replicas: 2
external: true
ports:
  - port: 8080/tcp
    healthCheck:
       httpPath: /docs/images/tomcat.gif
       
k8sSnippets:
  - ./snippets/init-container-snippet.yaml
  - ./snippets/security-context-snippet.yaml
```
A sample hprof looks like:

```yaml
environment: stage
overrides: myservice
replicas:
    min: 1
    max: 4
    cpuThreshold: 30%
k8sSnippets:
  - ./snippets/init-container-snippet.yaml
  - ./snippets/security-context-snippet.yaml
```
