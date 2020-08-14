### Quickstart with a sample golang application

This is a simple example having a single Go file App.

### To deploy this:
1. Replace `{{username}}` with your dockerhub username.
2. Make sure you have logged in to dockerhub account with your credentials using `docker login`.
3. Make sure you have pointed kubeconfig to the right kubernetes cluster.
4. Execute
```hyscale deploy service -f helloworld-go.hspec -n '<your-namespace>' -a helloworld-go```
