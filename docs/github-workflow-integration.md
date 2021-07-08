
### Following are the list of steps to integrate HyScale tool with Github workflow:

1. Pass the KUBECONFIG from Github Secrets (you can find Kubeconfig at `~/.kube/config`)
   * Pass the Kubeconfig secret to a environment variable in github action
     ```yaml
     env:
       GCLOUD_SERVICE_ACCOUNT_KEY: ${{ secrets.KUBECONFIG }}
     ```
   * Write the content of the environment variable to ~/.kube/config
     ```
     echo "${GCLOUD_SERVICE_ACCOUNT_KEY}" > config
     sudo mkdir -p ~/.kube
     sudo cp config ~/.kube/
     ```
1. Perform a Docker Login(Username and password are passed as secrets)
   ```
   env:
     DOCKER_USERNAME: ${{ secrets.HYS_DEV_DOCKER_USERNAME }}
     DOCKER_PASSWORD: ${{ secrets.HYS_DEV_DOCKER_PASSWORD }}
   ```
   ```
   docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
   ```
1. Download the hyscale Binary
   ```
   curl -sSL https://get.hyscale.io |  bash
   ```
1. Deploy the service using the hyscale binary which is downloaded 
   ```
   hyscale deploy service -f examples/hrms/hrdatabase/hrdatabase.hspec,examples/hrms/frontend/frontend.hspec -n github-action -a test
   ```
### Github Workflow should look like:

```yaml
# Build workflow gets auto triggered upon code merge to master or release* branches

name: Build

on:
  push:
     branches: 
     - master
     - release/HyScale*

jobs:
  build:
    runs-on: ubuntu-18.04

    steps:
    - uses: actions/checkout@v2

    - name: Download Latest Hyscale Binary
      run: |
          . ./scripts/test.sh
          docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
          curl -sSL https://get.hyscale.io |  bash
      env:
          GCLOUD_SERVICE_ACCOUNT_KEY: ${{ secrets.KUBECONFIG }}
          DOCKER_USERNAME: ${{ secrets.HYS_DEV_DOCKER_USERNAME }}
          DOCKER_PASSWORD: ${{ secrets.HYS_DEV_DOCKER_PASSWORD }}

    - name: Deploy HRMS App
      run: |
              hyscale deploy service -f examples/hrms/hrdatabase/hrdatabase.hspec,examples/hrms/frontend/frontend.hspec -n github-action -a test
              docker logout
```
