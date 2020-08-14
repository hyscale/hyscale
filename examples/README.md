## README

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

#### To get started quickly, you can start with [quickstart](https://github.com/hyscale/hyscale/tree/master/examples/quickstart) examples:

For instance to deploy [go-lang](https://github.com/hyscale/hyscale/tree/master/examples/quickstart/go-lang) sample application.
```
cd quickstart/go-lang
hyscale deploy service -f helloworld-go.hspec -n '<your-namespace>' -a helloworld-go
```
