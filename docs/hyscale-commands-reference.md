# HyScale Tool Command Reference

> Version 1.0B <br /> Last Updated 30th September 2019

---

## deploy

```markdown
Usage:  hyscale deploy service [OPTIONS] 

Deploy an application service

Options:
  -f, --file string          service spec file `<serviceName>.hspec`. Can be repeated for multiple service deployment.
  -n, --namespace string     name of namespace `<nameSpace>`
  -a, --application string   Application Name
  -v  --verbose              `Optional` verbose mode
```

#### Description

To deploy an application service which is defined in the hspec file, use the "deploy" command. The command requires hspec file, Kubernetes namespace and the application name as  inputs.  Multiple hspec files (-f `<serviceName1>`.hspec -f `<serviceName2>`.hspec `<serviceNameN>`.hspec) can be provided to deploy multiple services.

## service status

```markdown
Usage:  hyscale get service status [OPTIONS]

Status of an Application Service.

Options:
  -s --service string         name of service `<serviceName>`
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`   
```

#### Description

To get the status of a particular deployed service, use the "get service status". The command requires the service name , Kubernetes namespace and application name as inputs.  Multiple service names (-s `<serviceName1>` -s `<serviceName2>` -s `<serviceNameN>`)  can be provided to get status of multiple services.


## logs

```markdown
Usage: hyscale get service logs [OPTIONS]

Logs of an application Service

Options:
  -s --service string         name of service `<serviceName>`
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`
  -l --lines int              `Optional` output the last given lines
  -t --tail                   `Optional` follow the logs
```

#### Description

To get the stdout logs of the deployed service , use the "get service logs" command as below. The command requires servicename, Kubernetes namespace and the application name as inputs. The command can tail the logs with the specified number of lines using -t and -l options to the command.

## undeploy service

```markdown
Usage: hyscale undeploy service [OPTIONS]   

Undeploy an existing deployed application service.

Options: 
  -s --service string         name of service `<serviceName>`
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

To undeploy a particular application service which was deployed by HyScale, use the "undeploy service" command. The undeploy command requires the service name , Kubernetes namespace and the application name as inputs. Multiple service names (-s `<serviceName1>` -s `<serviceName2>` -s `<serviceNameN>`) can be provided to undeploy multiple services.

## undeploy app

```markdown
Usage: hyscalectl undeploy app [OPTIONS]

Undeploy an existing deployed application.

Options:
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

HyScale deletes all the services of an application using the "undeploy app" command. The command requires the Kubernetes namespace and the application name as inputs.

## generate

```markdown
Usage: hyscalectl generate service manifests [OPTIONS]

Generate kubernetes manifests for the specified services.

Options:
  -f, --file string          service spec file `<serviceName>.hspec`. Can be repeated for multiple service specifications.
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`
```
#### Description

HyScale abstracts the generation of Kubernetes manifests for deployments, however a user can generate the Kubernetes manifests without deployment using the "generate service manifests" command. The command requires service name , Kubernetes namespace and the application name as inputs. Multiple service names (-f `<serviceName1>`.hspec -f `<serviceName2>`.hspec `<serviceNameN>`.hspec) can be provided to generate Kubernetes manifests for multiple services.

## app status

```markdown
Usage: hyscalectl get app status [OPTIONS]

Status of Deployed Application.

Options:
  -n --namespace string       name of namespace `<nameSpace>`
  -a --application string     name of application `<applicationName>`
```

#### Description

To get the status of all the deployed services in an application , use the "get app status". The command requires the Kubernetes namespace and application name as inputs.


## Tool Options Description:

**_-f `<serviceName>`.hspec_**

HyScale Service Spec file where file name should be same as service name mentioned inside spec. This is the name with which service by default gets deployed

**_-n `<nameSpace>`_**

Kubernetes namespace where service is deployed

**_-a `<applicationName>`_**    

Logical grouping of services together with a given application name. 
