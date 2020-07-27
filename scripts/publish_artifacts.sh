#!/bin/bash
set -euo pipefail

docker_push()
{
  docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
  docker push $DOCKER_REPO/hyscale:$IMAGE_VERSION
  docker run -v `pwd`:/var/tmp --entrypoint /bin/cp  $DOCKER_REPO/hyscale:$IMAGE_VERSION /usr/local/bin/hyscale.jar /var/tmp
  docker logout
}

aws_cp_upload()
{
  dir=$1
  scripts=$2
  upload_to=$3
  set -- $upload_to
  for script in $scripts
  do 
    aws s3 cp $script s3://$AWS_S3_BUCKET/hyscale/release/$dir/$1
    aws s3api put-object-tagging --bucket $AWS_S3_BUCKET  --key hyscale/release/$dir/$1 --tagging 'TagSet=[{Key=hyscalepubliccontent,Value=true}]'
    shift
  done
}



sed -i "s|@@IMAGE_NAME@@|$DOCKER_REPO/hyscale|g" invoker/container/container.go
sed -i "s|@@IMAGE_TAG@@|$IMAGE_VERSION|g" invoker/container/container.go

#Changing the directory to invoker to build the go binary(Package main.go is available in the invoker directory)
cd invoker

export GOPATH=$HOME/go
export GOBIN=$(go env GOPATH)/bin

package=main.go
if [[ -z "$package" ]]; then
  echo "usage: $0 <package-name>"
  exit 1
fi
package_split=(${package//\// })
package_name=${package_split[-1]}

platforms=("windows/amd64" "darwin/amd64" "linux/amd64")

for platform in "${platforms[@]}"
do
    platform_split=(${platform//\// })
    GOOS=${platform_split[0]}
    GOARCH=${platform_split[1]}
    output_name=hyscale'-'$GOOS'-'$GOARCH
    if [ $GOOS = "windows" ]; then
        output_name+='.exe'
    fi

    env GOOS=$GOOS GOARCH=$GOARCH go build -o $output_name $package
    if [ $? -ne 0 ]; then
        echo 'An error has occurred! Aborting the script execution...'
        exit 1
    fi
done


docker_push
aws_cp_upload $IMAGE_VERSION "hyscale-linux-amd64 hyscale-windows-amd64.exe hyscale-darwin-amd64 hyscale.jar" "hyscale win/hyscale.exe mac/hyscale hyscale.jar"
