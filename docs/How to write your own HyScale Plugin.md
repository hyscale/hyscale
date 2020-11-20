HyScale implements an intent driven manifest generation. It translates given user intents to respective kubernetes manifest snippets. Hyscale is built over an extensible plugin based architecture where each plugin is responsible to translate a given user intent. 

HyScale does k8s manifest generation executing plugins in a specific order. HyScale comes with a set of built-in manifest generation plugins which covers the majority of the general use-cases.

If you have a custom and specific use-case and want to extend HyScale, you can achieve it by writing a new custom `Manifest Generation Plugin`.

And if you feel your newly developed custom plugin is helpful for others and serves a generic use case. You can feel free to [contribute](#Plugin-contribution)

For example:

   To implement Data backup using k8s snapshot resource you can create your own `SnapShotManifestGeneration` Plugin.

## Writing your own custom plugin

Before jumping into custom plugin creation, let's look at an overview of HyScale Architecture:

### Architecture Overview

![architecture](images/architecture.jpg)

Workflow controller reads the servicespec (hspec) and performs the entire development workflow in kubernetes.

Workflow controller works with the following components: `DockerfileGenerator`, `Build`, `Generate` and `Deploy`. Each component would process its task and respond back to the controller. 

This controller would give the necessary input to each component. It reads the kubernetes cluster details from `<user.home>/.kube/config` and the registry details from `<user.home>/.docker/config.json`

### Manifest Generator module

`Generate` (Manifest Generator) component is invoked by the workflow controller once `DockerfileGenerator` and `Build` components are executed.

Generate component takes the hspec as input and translates it to the respective kubernetes manifest yamls.

It is a combination of different manifest generation plugins where each one is responsible to translate a particular user’s intent and
these plugins will get executed in the same order as mentioned in `plugins.txt` available at `src/main/resources/`. 

Each MG plugin is responsible to read the fields of service spec known by it and generate respective snippets of kubernetes manifests.
Manifest Generator component does the heavy lifting of merging all the snippets together to generate the final set of Kubernetes manifest yamls.

Following are some examples of Manifest Generation Plugins:
* Ingress plugin
* Agent plugin
* Ports plugin, etc


### How to write a new Manifest Generation plugin

#### Prerequisites:

* Java 1.11
* Docker >= 18.03
* Docker registry and the credentials
* Kubernetes cluster & the kubeconfig


A manifest generation plugin generates kubernetes yaml snippets for a specific user’s intent defined in Service Spec.
Before writing a plugin, the author should define the new fields and their syntax in `hspec`.

For example, a `ports` plugin defines the same as shown here. 
The relevant schemas should also be updated with the new modifications so that the schema validations will happen as per the new changes

A sub module of manifest-generator `plugin-framework` provides an interface shown below as a contract to any Plugin that intends to abstract and generate kubernetes yaml snippets for a specific intent of the user. 

```java
public interface ManifestHandler {
  List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
}
```

A plugin should adhere to this interface and generate the required kubernetes yaml snippets making use of the following input arguments
`serviceSpec` - hspec yaml loaded into ServiceSpec object. This model exposes utility apis to parse and map the snippets into defined models. For example,
```java
String imageName = serviceSpec.get(“image.name”, String.class)
List<Port> ports = serviceSpec.get(“ports”, new TypeReference<List<Port>>() {});
```
`manifestContext` - A context object including common contextual attributes like the namespace, application name, service name, target image registry and labels (application context for grouping purpose) which we set in all the kubernetes manifest yamls, supplied input etc.

| Field | Type | Description |
| :--- | :--- | :--- |
| appName | `String` | application name |
| envName | `String` | Environment name |
| namespace | `String` | namespace |
| imageRegistry | ImageRegistry | target registry to which the images gets pushed |
| generationAttributes | `Map` | A plugin can publish any kind of attribute so that other plugins can make use of. Make sure that publisher precedes the consumer while defining the order of plugins |

Plugin should generate the required manifest snippets and should be returned as a list of ManifestSnippet objects. 

`ManifestSnippet` i.e kubernetes Manifest Snippet, it can be either `jsonPatch` or a `mergePatch` (full yaml also treated as `mergePatch`).

Each ManifestSnippet includes the Kind & Name of the kubernetes resource that the snippet refers to. It also includes the generated Snippet and the Path at which the provided snippet should get merged. Yaml snippets can be generated by different approaches involving Models or Templates. Choice of approach is up to the Plugin author. 

### Generating snippets the Mustache way
HyScale comes with a direct dependency of Mustache, a logicless template engine. Under this approach, a mustache template of the kubernetes YAML snippet with place holders should be available at `src/main/resources/templates/`. For example,
```yaml
scaleTargetRef:
    apiVersion: {{ TARGET_APIVERSION }}
    kind: {{ TARGET_KIND }}
    name: {{ TARGET_NAME }}
minReplicas: {{ MIN_REPLICAS }}
maxReplicas: {{ MAX_REPLICAS }}
targetCPUUtilizationPercentage: {{ AVERAGE_UTILIZATION }}
```

A `TemplateResolver` available in the commons module can be used to resolve the template with the specific values based on the incoming spec. Resolver takes a context as a map of place holders and their respective values. And the context can be populated as below
```java
context.put(MIN_REPLICAS, getMinReplicas(serviceSpec));
context.put(MAX_REPLICAS, getMaxReplicas(serviceSpec));
```

One can easily autowire the TemplateResolver and use the below snippet to generate the yaml. 
```java
String yamlString = templateResolver.resolveTemplate(template_path, context);
```

### Generating snippets the Model way

The manifest-generator module has a direct dependency on the official kubernetes java client.
A plugin can make use of kubernetes models defined in the client to generate the required YAML snippets.

A model should be populated with the required values and can be serialized using the below snippet that makes use of a `GsonSnippetConvertor` utility available in Plugin Framework
```java
V1HTTPGetAction v1HTTPGetAction = new V1HTTPGetAction();
v1HTTPGetAction.setPort(getPort(serviceSpec);
v1HTTPGetAction.setPath(getHealthCheckPath(serviceSpec));
V1Probe v1Probe = new V1Probe();
v1Probe.setHttpGet(v1HTTPGetAction);
String yamlString = GsonSnippetConvertor.serialize(v1Probe) 
```

Once the plugin development is done, it should be included into the execution by adding an entry in the `plugins.txt` file available at `src/main/resources/`. 
Make sure to add the plugin at the right position as the plugins get executed in the same order as defined here. 

### Plugin contribution:

* Open an issue for plugin contribution in `hyscale` and [hspec](https://github.com/hyscale/hspec) repo

* Update schema & field reference documentation in `hspec` repo and create a PR.

* Create a PR in hyscale repo

### Plugin management (version management)

todo...

### Installation (Test your Plugin)
* Clone the hyscale repo and make your plugin related changes 
* Compile the entire hyscale repo along with your plugin changes
* Create an artifact. You can chose either of the below:
   * Jar bundle
      * create a complete bundle hsycale.jar    
   * Docker image 
       * Build the docker image using the below command
          ```console
          docker build -t <your imagename>
          ```
* Make use of the artifact 
  * Jar 
    ```console
    java -jar hyscale.jar deploy -f <service.spec> -a <appName> -n <namespaceName>
    ```
  * Docker image 
     * Change the hsycale invoker script 
         * vim `/usr/local/bin/hyscale` and change the docker image and point to your newly created docker image
* Or just place plugin.jar in plugins folder and restart the server

todo...

### How to contribute your plugin to hyscale repo ?
If you think, your newly created custom Manifest plugin serves a Generic use case and is valid across majority Enterprise requirements you can contribute your plugin to HyScale Repo. 
Once your plugin is successfully tested.
* Raise an Issue & Create one PR in hspec repo with your hspec changes related to plugin

> Note: Make sure you have updated the spec reference document with your plugin specific fields usage.
* Raise an issue stating your plugin usage and the respective use case and then Create one PR in hyscale repo with your plugin code changes

> Note: Make sure you added an example for your plugin usecase.

For more details check this contribution section






