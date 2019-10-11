
# Hyscale Spec File Reference

> Version 1.0B <br /> Last Updated 13th September 2019


Table of contents
=================

<!--ts-->
   * [Overview](#overview)
   * [Reference Spec File](#Reference-Spec-File-with-all-options)
   * [Example Spec](#Example)  
   * [Field Reference](#Field-Reference)
   * [Spec Template File](#Spec-Template-File)
   * [Profile Files](#Profile-Files)
<!--te-->


## Overview

*   The Hyscale Service Spec file is meant for user to declaratively describe the service/application and its related configuration.
*   Hyscale Spec File is a valid YAML file.
*   Abstracts Kubernetes concepts/knowledge with a simplified DSL.
*   Developer friendly and can be added to version control.
*   Spec file should end with “.hspec.yaml” so that service specs can be easily identified.
*   Filename  should be named exactly as per service name `<service-name>.hspec.yaml`
*   Multiple spec files can be present in a directory.
*   Support environment profiles (eg: dev, stage and prod etc.) Env props and customizations are in profile file. `Future item` 
*   Profile files may mirror some of the fields given in the service spec. Only few fields allowed as indicated in this reference. Any fields specified in profile may get merged or override the ones given in spec, this behaviour is specified below for each field. `Future item`
*   Supports camel case for keys. Along with camel case, Uppercase for keys can be accepted in the future. In case of Uppercase, multi-word keys are separated by underscore `(_)` delimiter.

*   Works for any service be it bespoke, off-the-shelf, stateless, stateful, etc.
*   Following Infra targets are assumed :
      *  kubernetes cluster from ~/.kube/config
      * registry authentication credentials from ~/.docker/config.json


## Reference Spec File with all options

```yaml

name: <service-name>                # should be same as in filename
image:
    name:  <image-name>
    registry: <target-registry-url>
    tag: <tag>
    dockerfile:
        path: <build-context-path>               # default is ./
        dockerfilePath: <dockerfile-path>        # default is <path>/Dockerfile
        args:
            <key1>:<value1> 
    buildSpec:
       stackImage: [<pull-registry-url>/]<stack-image-name>[:<stack-image-tag>]
       artifacts:
           - name: <artifact-name>
             source: <relative source path>      # eg: /login/target/{{ buildNumber }}/login.war
             destination: <destination-in-container>

       configCommandsScript: <config-commands-script>    # output of this script is baked inside image
       configCommands: |-
         <config-command1>
         [<config-command2>]
         .
         .
         [<config-commandN>]

       runCommandsScript: <run-commands-script>          # runs on container start
                                          
                                                 # CMD in dockerfile
       runCommands: |-
         # Executes while container start
        [<run-command1>]
        [<run-command2>]
         .
         .
        [<run-commandN>]

startCommand: <start-command with args>          # command+args in k8s yaml, overrides ENTRYPOINT+CMD
replicas: <replica-count>                        # default is 1
memory: [<min-memory>-]<max-memory>              # Supported units are
	 					 # 1. Ki|Mi|Gi|Ti|Pi|Ei as power of two equivalents
                                                 # 2. n|u|m|k|M|G|T|P|E as plain integers
cpu: [<min-cpu>-]<max-cpu>			 # Supported units are `m` or `none`

external: <true/false>				 # default is false
ports:
    - port: <port-number1>[/<port-type>]         # default type is tcp
      healthCheck:
          httpPath: <http-path>                  # default is false

   [- port: <port-number2>/<port-type>]

volumes:                        
    - name: <volume-name1>
      path: <volume-mount-path>
     [size: <size>]             		 # default size is 1Gi 
     						 # Supported units are
	 					 # 1. Ki|Mi|Gi|Ti|Pi|Ei as power of two equivalents
                                                 # 2. n|u|m|k|M|G|T|P|E as plain integers
     storageClass: <storageClass>

propsVolumePath: <volume-path-of-configmap> 
props: 
    <key1>: [<file/endpoint/string>(]<value1>[)]     # default type is string
   [<key2>: <value2>]
   .
   [<keyN>: <valueN>]

secrets:		 	# Secrets accept both list of keys & key value pairs 
    - <secret1>			# Incase of list, secret should be created prior as <appname>-<servicename>
    - <secret2>			# - <key1> : <value1>
    .   			# - <key2> : <value2>
   - <secretN>			# - <keyN> : <valueN>

secretsVolumePath: <volume-path-of-secrets>

```

Here is the [Service Spec Schema](../json-schemas/README.md)

## Example


```yaml

name: hrms-frontend
image:
   name: gcr.io/hyscale-hrms/hrms-frontend:1.0
   dockerfile: {} # all default values
replicas: 1
volumes:
  - name: logs
    path: /usr/local/tomcat/logs
    size: 1Gi
  - name: data
    path: /data
    size: 2Gi
 
secrets:
  - keystore_password
 
props:
    max_conn: file(./config/config.xml)
    country: india
    region: hyderabad
    mysql_ost: endpoint(mysqldb)
    max_threads: 15
    server_xml: file(./config/tomcat/server.xml)
external: true
ports:
  - port: 8080/tcp
    healthCheck:
  	httpPath: /hrms
    
profiles: # we can also write conditions to automatically activate one of the profile
# dev profile
- name: dev
  replicas: 2
  props:
 	country: us
 	region: california
 	max_threads: 20
 	server_xml: file(./config/tomcat/server.xml)

  volumes:
    - name: logs
      size: 2Gi

# stage profile
- name: stage
  replicas: 3
  props:
      country: india
      region: hyderabad
      max_threads: 10
      server_xml: file(./config/tomcat/server.xml)

  volumes:
    - name: logs
      size: 2Gi

```


## Field Reference


<table>
  <tr>
   <td><strong>Field</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td><a href="#image">image</a>
   </td>
   <td>struct
   </td>
   <td>
   </td>
   <td><em>Can’t be overridden</em>
<p>
Describes an image to be deployed.
<p>
image section contains following:
<ol>

<li>name and tag

<li>Registry url where the image resides

<li>dockerfile or buildSpec fields
</li>
</ol>
   </td>
  </tr>
  <tr>
   <td>replicas
   </td>
   <td>int
   </td>
   <td>1
   </td>
   <td>Optional
<p>
<em>Can be overridden</em>
<p>
Number of instances of the service
   </td>
  </tr>
  <tr>
   <td>memory
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Optional
<p>
<em>Can be overridden</em>
<p>
Specify the range 
<p>
[&lt;minMemory&gt;-]&lt;maxMemory&gt;
<p>
Eg: 512m-1024m or 512m
   </td>
  </tr>
  <tr>
   <td>cpu
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Optional
<p>
<em>Can be overridden</em>
<p>
Specify the cpu range 
<p>
[&lt;minCpu&gt;-]&lt;maxCpu&gt;
<p>
Eg: 60-80 or 50
   </td>
  </tr>
  <tr>
   <td>startCommand
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Optional
<p>
<em>Can't be overridden</em>
<ul>

<li>startCommand is a command which gets executed at the time of container start

<li>it includes both ENTRYPOINT and CMD separated by space delimiter

<li>User may refer to the runCommandsScript above in CMD
</li>
</ul>
   </td>
  </tr>
<tr>
   <td>external
   </td>
   <td>string
   </td>
   <td>false
   </td>
   <td>Optional
<p>
<true/false>
<ul>

<li>Exposes the service externally.

<li>External IP would be assigned for the service.
</li>
</ul>
   </td>
  </tr>
  <tr>
   <td><a href="#ports">ports</a>
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Can be overridden</em>
<p>
List of ports to be declared along healthcheck and ingressrules if any.
   </td>
  </tr>
  <tr>
   <td>props
   </td>
   <td>list 
   </td>
   <td>
   </td>
   <td><em>Optional</em>
<p>
<em>Can be overridden</em>
<pre>
<code>
&lt;keyName&gt;:[&lt;file/endpoint/string&gt;(]&lt;value&gt;[)]
</code>
</pre>
<ul>

<li>List of key value pairs

<li>Value is typed and can be of type: string, file, endpoint. 
<ul>
<li> if the value is file typed. utf-8 content of the file will be sent.

<li> if the value is endpoint typed. service discovery name of the given service is passed as the value.

</li>
</ul>


<li>DEFAULT type is string
</li>
</ul>
<p>
<strong>Eg:</strong>
<pre> <code>props:
   MAX_NO_OF_CONNECTIONS: STRING(10)
   MYSQL_HOST: ENDPOINT(mysql)
   KEY_FILE: FILE(/tmp/file.txt)</code></pre>
   </td>
  </tr>
  <tr>
   <td>secrets
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em>   <em>Can be overridden</em>
<p>
<secretKeyName>
<p>
List of secret key Names
<p>
<strong>Eg:</strong>
<pre><code>
secrets:
- "MYSQL_ROOT_PASSWORD"
</code>
</pre>
   </td>
  </tr>
  <tr>
   <td><a href="#volumes">volumes</a>
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em> <em>Can be overridden</em>
<p>
List of volumes to be specified in a pod.
   </td>
  </tr>
</table>


### image

Following are the fields of image section.


<table>
  <tr>
   <td><strong>Field</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Name of the image to be built 
<p>
Note: retagging should be done for the already provided image
   </td>
  </tr>
  <tr>
   <td>tag
   </td>
   <td>string
   </td>
   <td>latest
   </td>
   <td>Tag would be overridden if tagpolicy is mentioned 
   </td>
  </tr>
  <tr>
   <td>registry
   </td>
   <td>string
   </td>
   <td>docker.io/library
   </td>
   <td>registry url along with the namespace
   </td>
  </tr>
  <tr>
   <td><a href="#buildspec">buildSpec</a>
   </td>
   <td>struct
   </td>
   <td>
   </td>
   <td><em>Optional</em> generate Docker and build with local docker daemon
   </td>
  </tr>
  <tr>
   <td><a href="#dockerfile">dockerfile</a>
   </td>
   <td>struct
   </td>
   <td>
   </td>
   <td>Optional build with Dockerfile using local docker daemon 
   </td>
  </tr>
  <tr>
   <td>tagPolicy
<p>
<em>(future, currently unspecified)</em>
   </td>
   <td>struct
   </td>
   <td>sha256
   </td>
   <td><em>Optional</em>
<p>
Supports following tag policies
<ul>

<li>Gitcommit 

<li>sha256

<li>dateTime
</li>
</ul>
   </td>
  </tr>
</table>


#### Builders:

Hyscale supports following to build your image:

*   hyscaleBuildSpec locally with Docker
*   Dockerfile with local docker

> Note: 
Following additional building mechanisms can be supported in future: <br />
Dockerfile with kaniko in-cluster <br />
jib maven and gradle projects locally <br />
bazel locally <br />
Cloud-native build packs <br />

## buildSpec

HyscaleBuildSpec locally with Docker

<table>
  <tr>
   <td><strong>Option</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td><a href="#stackImage">stackImage</a>
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Stack indicates the base image on top of which artifacts and configuration is layered.
   </td>
  </tr>
  <tr>
   <td><a href="#artifacts">artifacts</a>
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em> Represents the list of artifacts to be present inside the container at defined destinations.
   </td>
  </tr>
  <tr>
   <td><a href="#configCommandsScript">configCommandsScript</a>
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Optional path-to-script relative or absolute path to a config-Commands script
   </td>
  </tr>
  <tr>
   <td>configCommands
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em> Array of commands which is invoked during image build. The commands are also part of the image.
   </td>
  </tr>
  <tr>
   <td><a href="#runCommandsScript">runCommandsScript</a>
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Optional path-to-script relative or absolute path to a run-Commands script
   </td>
  </tr>
  <tr>
   <td>runCommands
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em> runCommand is a command which gets executed at the time of container start and is baked into the image
   </td>
  </tr>
</table>


### stackImage

`
	stack: [<registryUrl>/]<image>[:<tag>]
`

*   Stack indicates the base image on top of which artifacts and configuration is layered.
*   registryUrl is `optional`.
*   tag or digest is `optional`. latest is assumed.

   

   Eg: 

```yaml
   stackImage: tomcat:8.5
```

### artifacts

> can be overridden

```yaml
    artifacts:
      - name: <artifactName1>
      	destination: <destination1InContainer>
      	provider: [ssh/http/local]                  # default local (ssh, http will be implemented in future versions)
        source: <url>
```
                      

#### Fields:


<table>
  <tr>
   <td><strong>Option</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Name of the artifact
   </td>
  </tr>
  <tr>
   <td>destination
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Destination path to copy the artifact inside the container
   </td>
  </tr>
  <tr>
   <td>provider
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td><em>Optional</em> type of provider. Could be one of:
<ul>

<li>local  
<ul>
 
<li>Artifact available locally
 
<li>Relative path to the artifact is expected in the source field
</li> 
</ul>

<li>ssh 
<ul>
 
<li>Artifact available remotely 
 
<li>Accessible through ssh
 
<li>Auth credentials can be mentioned in infrastructure spec
 
<li>source is ssh url
</li> 
</ul>

<li>http 
<ul>
 
<li>Artifact available remotely
 
<li>Accessible through http
 
<li>Auth credentials can be mentioned in infrastructure spec
 
<li>Source is http url

<p>
 
</li> 
</ul>
</li> 
</ul>
   </td>
  </tr>
  <tr>
   <td>source
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Url to the artifact
   </td>
  </tr>
</table>


*   List of artifacts to be copied inside the container image
*   Copies the artifact mentioned in sourcePath of local folder to destinationPath inside image
*   Artifact can be from one of the following sources:
*   local
*   Remote (like jfrog, jenkins etc..)

### configCommandsScript

> Optional  _string type_

*   Path to a Script containing config commands.
*   It is mutually exclusive with configCommands field. 
*   If configCommands is given and not empty configCommandsScript has no effect. 

### runCommandsScript

> Optional  _string type_



*   Path to a Script containing runConfig commands.
*   It is mutually exclusive with runCommands field. 
*   If runCommands is given and not empty runCommandsScript has no effect. 

### dockerfile

Use local docker to build docker image with the given Dockerfile

```yaml
   dockerfile:
  	target: <target>
        path: <buildContextPath>            # Optional buildcontext path
        dockerfilePath: <DockerfilePath>    # Optional path to Dockerfile
  	useBuildKit: <true/false>           # use buildKit for building (will be implemented in future versions)
  	buildArgs:
  	    - <buildarg1>
           [- <buildarg2>]
            .
            .
           [- <buildargN>]
```

<table>
  <tr>
   <td><strong>Option</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td>path
   </td>
   <td>string
   </td>
   <td>./
   </td>
   <td><em>Optional</em> buildContext for docker build
   </td>
  </tr>
  <tr>
   <td>dockerfilePath
   </td>
   <td>string
   </td>
   <td>./Dockerfile
   </td>
   <td>Optional dockerfile path with in the build context
   </td>
  </tr>
  <tr>
   <td>target
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td><em>Optional</em> target stage to be built
   </td>
  </tr>
  <tr>
   <td>useBuildKit
   </td>
   <td>boolean
   </td>
   <td>
   </td>
   <td>Optional Use buildkit <em>(will be implemented in future versions)</em>
   </td>
  </tr>
  <tr>
   <td>buildArgs
   </td>
   <td>list
   </td>
   <td>
   </td>
   <td><em>Optional</em> List of build arguments to be passed
   </td>
  </tr>
</table>


Eg:

```yaml
   dockerfile:
  	path: ./
        dockerfilePath: Dockerfile.build
  	target: finalstage
  	useBuildKit: false
  	buildArgs:
           - foo=value1
  	   - bar=value2
```

### ports

List of port objects.

```yaml
ports:
  - port: <portNumber1>[/<portType>]
    healthCheck:
       httpPath: <httpPath> # optional if not http type

  [- port: <portNumber2>/<portType>]
```

**Port Object contains:**

*   port to be declared in a pod
*   healthcheck if available for the port to be specified along with the port definition

> Note:
Currently Health Check would be present for only one port if any. If there are multiple healthChecks declared first healthCheck will be picked.      

Following are the **Fields** of Port object:


<table>
  <tr>
   <td><strong>Option</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td>port
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>&lt;portNumber&gt;[/&lt;portType&gt;]
<ul>
<li>portNumber Port Number to be  declared in pod 0-65535
<li>portType tcp/udp
</li>
</ul>
   </td>
  </tr>
  <tr>
   <td>healthCheck
   </td>
   <td>struct
   </td>
   <td><strong>http</strong> incase healthCheckPath is given, <strong>tcp</strong> if nothing given 
   </td>
   <td><em>Optional</em>
<pre>
<code>healthCheck:
&nbsp;&nbsp;&nbsp;&nbsp;type: &lt;tcp/http/udp&gt; # optional type of health check (will be implemented in future versions)
&nbsp;&nbsp;&nbsp;&nbsp;httpPath: &lt;httpPath&gt; # optional if not type: http </code>
</pre>
<p>
<strong>type</strong>
<p>
type string <em>Optional</em>
<p>
Type of HealthCheck for the specified port
<ul>

<li>tcp 

<li>http 

<p>
<strong>http</strong> incase httpPath is given, <strong>tcp</strong> if nothing given
<p>
<strong>Note:</strong> exec/command healthchecks should be mentioned in a separate section
</li>
</ul>
<p>
<strong>httpPath</strong>
<br />
type string Optional 
<br />
http Path for http health check
<br />

Eg:
<br />
<pre>
<code>healthcheck:
&nbsp;&nbsp;&nbsp;&nbsp;httpPath: /hmrs </code>
</pre>
   </td>
  </tr>
  </tr>
</table>


Eg:
```yaml
    ports:
      - port: 8080/TCP
  	healthCheck:
  	    httpPath: "/hrms" # optional

      - port: 8008/TCP
```


### volumes

List of volume Objects.

```yaml
  volumes:# optional can be inferred from docker inspection and default 2 GB + default sc
    - name: <volumeName1>
      path: <volumeMountPath>
     [size: <size>]
```

**volume Object contains:**

*   name - volume name
*   path - mount Path inside container
*   size - volume size to provision using environment defined storage class.

`
Note:
volumes referring other volumes is _future, currently unspecified_
`

Following are the **Fields** in dataPath object

<table>
  <tr>
   <td><strong>Option</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>default</strong>
   </td>
   <td><strong>Explanation</strong>
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Name of volume
<p>
should be up to maximum length of 253 characters and consist of lower case alphanumeric characters, -, and ., but certain resources have more specific restrictions.
<p>
Eg:
<p>
name: logsDirectory
   </td>
  </tr>
  <tr>
   <td>path
   </td>
   <td>string
   </td>
   <td>
   </td>
   <td>Mount Path of the volume inside container 
<p>
<strong>Eg:</strong> <code>path: /usr/local/tomcat/logs</code>
   </td>
  </tr>
  <tr>
   <td>size
   </td>
   <td>string
   </td>
   <td>1Gi
   </td>
   <td><em>Optional</em>
<p>
Size of volume to be provisioned
<p>
Makes use of environment tied kubernetes storage class to provision volume.
<p>
Eg: size: 1Gi
   </td>
  </tr>
</table>


Eg:


```yaml
volumes:
  - name: tomcat-logs
    path: /usr/local/content/tomcat/current/logs
    size: 1Gi

  - name: data
    path: /usr/local/content/tomcat/current/webapps
```

### 


## Spec Template File

> will be implemented in future versions

For Off-the-Shelf (OTS) services as well as for commonly used configurations of known services, spec template files could be made available as a starting point. Once a spec template is downloaded for use, it can be extended to create a service spec (hspec) in order to override commonly specified configurations. 

For example, a mysql spec template may include things like 3306 for ports, `/var/lib/mysql/` as a volume path, etc. Anyone extending this template to create their hspec may wish to override things like the password secret, etc.

The following rules apply to a spec template:

1. Spec templates are valid YAML files.
2. Spec template end with the extension `htpl.yaml`
3. A template file must include a `name` and `version` attribute. Name is same as in the filename of the template. Name & version would be used in the hspec which extends.
4. The filename of the spec template should be same as the service name + version. Eg. `<service-name>-<version>.htpl.yaml`
5. A template file may include any of the fields that a regular hspec can have, except as per the following. 
6. A template file cannot include:
    1. buildSpec & dockerSpec
    2. external

```
NOTE: Consideration for future versions of spec templates:
	a. Spec Templates as bundles (htpl.tar)
	b. This enables things like artifacts, dockerfiles, config files, etc. to be included in the htpl bundle.
	c. This will enable support for buildSpec and dockerSpec.
	d. The bundle could be name.htpl.tar which expands and should mandatorily include name.htpl.yaml
	e. Allow remote spec template repositories & corresponding spec
```


## Extending a Spec Template
> (htpl) into a Service Spec (hspec)

In a service spec, use the `extends` field as follows:

`
    extends: <service-template-name>:<template-version>
`
In the hspec, any **props, secrets, volumes, ports** specified are merged or overridden if having same keyname. 

`image` is overridden if specified in hspec, and the `image` given in the htpl is used as stack image within the buildSpec.


```
Future versions may support:
	Specifying a remote URL in the extends field
	`$remove: <key-name>` in the hspec in order to skip that key & corresponding sub-tree from the htpl
	Consider extending multiple htpl files in a single hspec file
```



## Profile Files 

> will be implemented in future versions.

In order to deploy a service into different environments (such as QA, Stage, UAT, etc.), it maybe necessary to override certain fields to customize as per that environment. This is supported by the use of profile files.

A profile file may override a spec file by specifying the following:

```yaml
environment: <profile-name>
overrides: <service-name>
```

Profile files should be `<service-name>.<profile-name>.yaml`

The following fields of service spec can be overridden or additionally specified (merged) in a profile file:

**props, secrets, replicas, resource-limits, size of volumes.

_(overrides if same keyname)_


```
Future versions may support:
	Disabling of healthChecks using an additional field in the relevant section such as:
$disable: true
```
