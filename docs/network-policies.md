# Network Policies in HyScale
#### Description

HyScale generates K8s manifest yamls with respect to hspec that satisfies the majority of application use cases with exception to few kubernetes concepts. The thought behind network policies plugin is to allow the user to regulate network traffic communication between services, hence increasing the overall application security. 

The user can now mention network traffic rules in the hspec for each service specifying the list of ports on which traffic will be accepted or denied for the given list of services.

### Introducing 'Network Policies'

#### Pre-Requisites 

A Container Network Interface (CNI) Plugin has to be installed on your cluster before you start using the network policies feature. 
Refer to [network-plugin](https://kubernetes.io/docs/concepts/extend-kubernetes/compute-storage-net/network-plugins/) for installation process.

#### Introduction

We have introduced a new field "allowTraffic" in hspec where the user will provide rules in the hspec for each service specifying the list of ports on which traffic will be accepted or denied for the given list of services. Refer allowTraffic in [Hyscale Spec Reference](https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md#allowtraffic).
The external field of hspec should be false or not provided in order to apply a network policy.

### Constructing a Network Traffic Rule

In the below example we have taken the [book-info](https://istio.io/latest/docs/examples/bookinfo) application, in this the details service should be accessible from the product page service.

To construct a traffic rule for such a requirement we specify the port to allow traffic in 'port' field under 'allowTraffic'. The service from which it is accessible is specified under the 'from' field.

A simple hspec with network traffic rules looks like :

```yaml
name: details

image:
  registry: registry.hub.docker.com
  name: istio/examples-bookinfo-details-v1
  tag: 1.16.2

external: false

ports:
  - port: 9080/http

allowTraffic:
  - ports:
      - 9080/tcp
    from:
      - productpage
```

Here is a sample output of k8s deployment manifest which is generated post applying network policy :
```yaml
kind: "NetworkPolicy"
apiVersion: "networking.k8s.io/v1"
metadata:
  labels:
    hyscale.io/service-name: "details"
    hyscale.io/environment-name: "dev"
    hyscale.io/app-name: "details"
  name: "details-details"
spec:
  podSelector:
    matchLabels:
      hyscale.io/service-name: "details"
  policyTypes:
    - "Ingress"
  ingress:
    - from:
        - podSelector:
            matchLabels:
              hyscale.io/service-name: "productpage"
      ports:
        - port: 9080
```

### Different scenarios for constructing network traffic rules

We can add multiple rules, each containing multiple ports and multiple 'from' services.

#### Allow all incoming traffic 
```yaml
allowTraffic:
  - ports:
      - []
    from:
      - []
```

#### Deny all incoming traffic 
```yaml
allowTraffic:
  - ports:
      - []
```

#### Allow traffic from all services on a specific port
```yaml
  - ports:
      - 9080/tcp
    from:
      - []
```

#### Deny traffic on a specific port
```yaml
  - ports:
      - 9080/tcp
```

#### Allow traffic to all ports from specific services
```yaml
allowTraffic:
  - ports:
      - []
    from:
      - service-a
      - service-b
```

