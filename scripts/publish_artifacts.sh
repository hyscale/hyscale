#!/bin/bash
set -euo pipefail
project_version=`cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}'`
artifactory_version=$project_version.$GITHUB_RUN_NUMBER
echo ::set-env name=IMAGE_VERSION::$(echo $artifactory_version)

docker_build_push()
{
  docker build -t $DOCKER_REPO/hyscale:$artifactory_version .
  docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
  docker push $DOCKER_REPO/hyscale:$artifactory_version
  docker run -v /home/runner/work/hyscale/hyscale/scripts:/var/tmp --entrypoint /bin/cp  $DOCKER_REPO/hyscale:$artifactory_version /usr/local/bin/hyscale.jar /var/tmp
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
    aws s3 cp scripts/$script s3://$AWS_S3_BUCKET/hyscale/release/$dir/$1
    aws s3api put-object-tagging --bucket $AWS_S3_BUCKET  --key hyscale/release/$dir/$1 --tagging 'TagSet=[{Key=hyscalepubliccontent,Value=true}]'
    shift
  done
}


if [ $GITHUB_WORKFLOW == "Build"  ] #Here Build is the name of the github workflow that gets triggered on the merge to master, release branch or On the BranchCut.`
then
  sed -i "s|@@HYSCALE_DOCKER_REPO_PATH@@|$DOCKER_REPO|g" scripts/hyscale
  grep -RiIl '@@HYSCALE_BUILD_VERSION@@' |grep -v publish_artifacts.sh| xargs sed -i "s|@@HYSCALE_BUILD_VERSION@@|$artifactory_version|g"
  grep -RiIl '@@HYSCALE_URL@@' |grep -v publis_artifacts.sh| xargs sed -i "s|@@HYSCALE_URL@@|https://s3-$AWS_REGION.amazonaws.com/$AWS_S3_BUCKET/hyscale/release/$artifactory_version/hyscale.jar|g"
  docker_build_push
  aws_cp_upload $artifactory_version "hyscale hyscale_osx hyscale.ps1 hyscale.jar" "hyscale mac/hyscale win/hyscale.ps1 hyscale.jar"
elif [ $GITHUB_WORKFLOW == "Release"  ]
then
  grep -RiIl '@@HYSCALE_URL@@' |grep -v publis_artifacts.sh| xargs sed -i "s|@@HYSCALE_URL@@|https://github.com/hyscale/hyscale/releases/download/v$project_version/hyscale.jar|g"
  grep -RiIl '@@HYSCALE_BUILD_VERSION@@' |grep -v publish_artifacts.sh| xargs sed -i "s|@@HYSCALE_BUILD_VERSION@@|$project_version|g"
  aws_cp_upload latest "hyscale.ps1 hyscale_osx" "win/hyscale.ps1 mac/hyscale"
fi
