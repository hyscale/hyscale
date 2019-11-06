 README
 -------

### To deploy these applications
1. Replace dockerRegistryUrl with your docker registry or dockerhub. 

   For dockerhub replace it with registry.hub.docker.com
2. Replace imageName with your image. 

   For dockerhub replace it with dockerhub-username/myservice

Example for dockerhub:
```yaml
image:
    registry: registry.hub.docker.com
    image: dockerhub-username/myservice
    tag: 1.0
``` 
