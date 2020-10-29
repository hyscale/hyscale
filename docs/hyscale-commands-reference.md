# HyScale Tool Command Reference

---

## deploy

```markdown
Usage:  hyscale deploy service [OPTIONS] 

Deploy an application service

Options:
  -f, --file string          Service spec file `<serviceName>.hspec`. Can be repeated for multiple service deployment.
  -p, --profile string       `Optional` service profile file  `<profileName>-<serviceName>.hprof`. Can be repeated for different services.
  -n, --namespace string     name of namespace `<namespace>`
  -a, --application string   name of application `<applicationName>`
  -v  --verbose              `Optional` verbose mode
  -P  string                 `Optional` profile name 
  -o, --output json          `Optional` Show deployment status in the required format. Allowed values are json/JSON.
```

#### Description

To deploy an application service which is defined in the hspec file, use the "deploy" command. The command requires hspec file, Kubernetes namespace and application name as inputs, additionally profile can also be provided for a service.  Multiple hspec files (-f `<serviceName1>`.hspec -f `<serviceName2>`.hspec -f `<serviceNameN>`.hspec or comma separated -f `<serviceName1>`.hspec,`<serviceName2>`.hspec) can be provided to deploy multiple services. Similarly profiles files (-p `<profileName1>-<serviceName1>`.hprof -p `<profileName2>-<serviceName2>`.hprof -p `<profileNameN>-<serviceNameN>`.hprof or comma separated -p `<profileName1>-<serviceName1>`.hprof,`<profileName2>-<serviceName2>`.hprof) can also be provided for services. At max one profile is allowed per service.

-P profilename option lookups for the profile files in the format `<profilename>-<servicename>`.hprof in the directory of hspec ,say dir(hspec) or in the dir(hspec)/profiles. In this case profile is compulsory for all the service spec(s) provided.

Images that are built part of hyscale deploy are handled based on the HYS_IMAGE_CLEANUP_POLICY environment variable. Possible values of the policy are 
* PRESERVE_N_RECENTLY_USED (default) , preserves the last 3 recently used images
* DELETE_AFTER_BUILD, deletes the image immediately after the build.
* PRESERVE_ALL, preserves the images


## service status

```markdown
Usage:  hyscale get service status [OPTIONS]

Status of an Application Service.

Options:
  -s --service string         name of service `<serviceName>`. Can be repeated for multiple services.
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`   
```

#### Description

To get the status of a particular deployed service, use "get service status" command. The command requires service name, Kubernetes namespace and application name as inputs.  Multiple service names (-s `<serviceName1>` -s `<serviceName2>` -s `<serviceNameN>` or comma separated like -s `<serviceName1>`,`<serviceName2>`)  can be provided to get status of multiple services of an application.

## app status

```markdown
Usage: hyscale get app status [OPTIONS]

Status of Deployed Application.

Options:
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

To get the status of all the deployed services in an application, use "get app status" command. The command requires the Kubernetes namespace and application name as inputs.

## logs

```markdown
Usage: hyscale get service logs [OPTIONS]

Logs of an application Service

Options:
  -s --service string         name of service `<serviceName>`
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`
  -l --lines int              `Optional` output the last given lines
  -t --tail                   `Optional` follow the logs
```

#### Description

To get the stdout logs of the deployed service, use "get service logs" command. The command requires service name, Kubernetes namespace and application name as inputs. The command can tail the logs with the specified number of lines using -t and -l options to the command. In case of more than 1 replicas, user will be asked to select the replica for which he wants to see the logs.

## undeploy service

```markdown
Usage: hyscale undeploy service [OPTIONS]   

Undeploy an existing deployed application service.

Options: 
  -s --service string         name of service `<serviceName>`. Can be repeated for multiple services.
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

To undeploy a particular application service which was deployed by HyScale, use "undeploy service" command. The undeploy command requires the service name, Kubernetes namespace and the application name as inputs. Multiple service names (-s `<serviceName1>` -s `<serviceName2>` -s `<serviceNameN>` or comma separated -s `<serviceName1>`,`<serviceName2>`) can be provided to undeploy multiple services.

## undeploy app

```markdown
Usage: hyscale undeploy app [OPTIONS]

Undeploy an existing deployed application.

Options:
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

To undeploy all services  for an application which was deployed by HyScale, use "undeploy app" command. The command requires the Kubernetes namespace and the application name as inputs.

## generate manifests

```markdown
Usage: hyscale generate service manifests [OPTIONS]

Generate kubernetes manifests for the specified services.

Options:
  -f, --file string          service spec file `<serviceName>.hspec`. Can be repeated for multiple service specifications.
  -p, --profile string       `Optional` service profile file `<profileName>-<serviceName>.hprof`. Can be repeated for different services.
  -a --application string     name of application `<applicationName>`
  -P  string                 `Optional` profile name
```
#### Description

HyScale abstracts the generation of Kubernetes manifests for deployments, however a user can generate the Kubernetes manifests without deployment using the "generate service manifests" command. The command requires service name, the application name as inputs, additionally profile can also be provided for a service. Multiple service names (-f `<serviceName1>`.hspec -f `<serviceName2>`.hspec `<serviceNameN>`.hspec or comma separated -f `<serviceName1>`.hspec,`<serviceName2>`.hspec) can be provided to generate Kubernetes manifests for multiple services. Additionally profiles files (-p `<profileName1>-<serviceName1>`.hprof -p `<profileName2>-<serviceName2>`.hprof -p `<profileNameN>-<serviceNameN>`.hprof or comma separated -p `<profileName1>-<serviceName1>`.hprof,`<profileName2>-<serviceName2>`.hprof) can also be provided for services. At max one profile is allowed per service.

-P profilename option lookups for the profile files in the format `<profilename>-<servicename>`.hprof in the directory of hspec, say dir(hspec) or in the dir(hspec)/profiles. In this case profile is compulsory for all the service spec(s) provided.

## get apps

```markdown
Usage: hyscale get apps

Display application along with the namespace they are deployed in.

```
#### Description

To get all the deployed applications, use "get apps" command. The command will display all the applications along with namespace deployed through hyscale.

## get replica status

```markdown
Usage:  hyscale get replica status [OPTIONS]

Replica Status of an Application Service.

Options:
  -s --service string         name of service `<serviceName>`.
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`   
```

#### Description:

To get the replica status of a particular deployed service, use "get replica status" command. The command requires service name, Kubernetes namespace and application name as inputs.

## scale service

```markdown
Usage:  hyscale scale service [OPTIONS]

Scale a service of an application imperatively.

Options:
  -s --service string         name of service `<serviceName>`
  -n --namespace string       name of namespace `<namespace>`
  -a --application string     name of application `<applicationName>`
  --up integer                scales service up by specified value
  --down integer              scales service down by specified value
  --to integer                scales service to a  specified value    
```

#### Description:

To scale a service of an application


## Tool Options Description:

**_-f `<serviceName>`.hspec_**

HyScale Service Spec file where file name should be same as service name mentioned inside spec. This is the name with which service by default gets deployed. To know how to write service spec click [here](https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md).

**_-p `<profileName>-<serviceName>`.hprof_**

HyScale Service Profile file where file name should follow the pattern `<profileName>-<serviceName>`.hprof. Profile provides flexibility to declare environment specific values for certain parameters. To know what all is supported in profile click [here](https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md#Profile-Files). 
Profile name must consist of alphanumeric characters or '-', with length in the range of 2-30. Regex used for profile name validation is *`([-a-zA-Z0-9]){2,30}`*

**_-n `<namespace>`_**

Kubernetes namespace where service is deployed. `namespace` acts like an isolation for your application services. Generally namespace is used for describing application environments (like QA, STAGE, UAT etc..). If namespace is not present, hyscale will create one with the given name provided kubeconfig permissions.
Namespace must consist of lower case alphanumeric characters or '-', with length in the range of 2-30. Regex used for validation is *`([a-z0-9-]){2,30}`*

**_-a `<applicationName>`_**    

Logical grouping of services together with a given application name. 
Application name must consist of lower case alphanumeric characters or '-' with length in the range of  2-30. Regex used for validation is
*`([a-z0-9-]){2,30}`* 

**_-s `<serviceName>`_**

Basic unit of your application deployment. 
A service name must consist of lower case alphanumeric characters or '-', it should start with an alphabetic character, and can end with an alphanumeric character. Regex used for validation is
*`[a-z]([-a-z0-9]*[a-z0-9])?`*   

## Environment Variables

`HYS_LOG_LEVEL` - Log level for hyscale tool logs. Default is set to `info`. To change log level to `debug` execute `export HYS_LOG_LEVEL=debug`.

`HYS_LOG_SIZE` - Folder size cap for hyscale tool logs. Default is set to `100MB`. To change total size cap execute `export HYS_LOG_SIZE=<size>` (minimum size cap is 10MB).
