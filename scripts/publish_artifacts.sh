#!/bin/bash
set -euo pipefail
project_version=`cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}'`
artifactory_version=$project_version.$GITHUB_RUN_NUMBER
echo ::set-env name=IMAGE_VERSION::$(echo $artifactory_version)
sed -i "s|@@HYSCALE_DOCKER_REPO_PATH@@|$DOCKER_REPO|g" scripts/hyscale
sed -i "s|@@HYSCALE_BUILD_VERSION@@|$artifactory_version|g" scripts/hyscale

docker_build_push()
{
  docker build -t $DOCKER_REPO/hyscale:$artifactory_version .
  docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
  docker push $DOCKER_REPO/hyscale:$artifactory_version
  docker logout
}

aws_cp_upload()
{
  aws s3 cp scripts/hyscale s3://$AWS_S3_BUCKET/hyscale/release/$1/hyscale
  aws s3api put-object-tagging --bucket $AWS_S3_BUCKET  --key hyscale/release/$1/hyscale --tagging 'TagSet=[{Key=hyscalepubliccontent,Value=true}]'
}

docker_build_push
aws_cp_upload $artifactory_version
