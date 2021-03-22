# Load Balancer Support in HyScale
#### Description
Kubernetes provides a variety of means to expose a service to the outside world for bringing external traffic into a cluster. 
HyScale currently allows exposing a service externally by utilizing Kubernetes service type 'LoadBalancer'.
Although you can use it for HTTP(S) traffic, they operate in OSI layers 3/4 and are not aware of HTTP connections or 
individual HTTP requests and responses.

There are multiple methods (Ingress, Service Mesh)  and multiple providers for each method (Nginx, Traefik, Istio) that
are offering OSI Layer 7 Load balancing capabilities but with a shortcoming of having different configurations for each provider.
With so many options, there cannot be only one right choice for services to be exposed externally. 
The choice varies depending upon the usability and functional requirements of a service/application.

We abstracted load balancing support in K8s by defining basic requirements to a layer 7 load balancer such as how to route 
requests to backend services based on HTTP URL/Host, TLS key, certification configuration and which provider to use. 
Thereby generating required manifests based on the provider and simplifying the configuration across different providers.


### Unification of Ingress and Service Mesh
Ingress can provide basic layer 7 load balancing capabilities. Whereas Service mesh holds additional functionalities like advanced routing rules, distributed tracing, policy checking and metrics collections.
But both Ingress and Service mesh commonly provide the primary functionality for Load balancing, SSL termination and virtual hosting and also share the majority of use cases.
Thus unification of Ingress and Service Mesh makes more sense from a load balancing perspective.

### How it works?

### Prerequisites

### Introducing the 'loadBalancer' field 

### Enabling Ingress for routing with HyScale

### Enabling Service Mesh for routing with Hyscale
