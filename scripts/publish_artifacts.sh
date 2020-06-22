#!/bin/bash
set -euo pipefail

docker_push()
{
  docker login --username=$DOCKER_USERNAME  --password=$DOCKER_PASSWORD
  docker push $DOCKER_REPO/hyscale:$IMAGE_VERSION
  docker run -v /home/runner/work/hyscale/hyscale/scripts:/var/tmp --entrypoint /bin/cp  $DOCKER_REPO/hyscale:$IMAGE_VERSION /usr/local/bin/hyscale.jar /var/tmp
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
  grep -RiIl '@@HYSCALE_BUILD_VERSION@@' |grep -v publish_artifacts.sh| xargs sed -i "s|@@HYSCALE_BUILD_VERSION@@|$IMAGE_VERSION|g"
  grep -RiIl '@@HYSCALE_URL@@' |grep -v publis_artifacts.sh| xargs sed -i "s|@@HYSCALE_URL@@|https://s3-$AWS_REGION.amazonaws.com/$AWS_S3_BUCKET/hyscale/release/$IMAGE_VERSION/hyscale.jar|g"
  docker_push
  aws_cp_upload $IMAGE_VERSION "hyscale hyscale_osx hyscale.ps1 hyscale.jar" "hyscale mac/hyscale win/hyscale.ps1 hyscale.jar"
elif [ $GITHUB_WORKFLOW == "Release"  ]
then
  grep -RiIl '@@HYSCALE_URL@@' |grep -v publis_artifacts.sh| xargs sed -i "s|@@HYSCALE_URL@@|https://github.com/hyscale/hyscale/releases/download/v$PROJECT_VERSION/hyscale.jar|g"
  grep -RiIl '@@HYSCALE_BUILD_VERSION@@' |grep -v publish_artifacts.sh| xargs sed -i "s|@@HYSCALE_BUILD_VERSION@@|$PROJECT_VERSION|g"
  aws_cp_upload latest "hyscale.ps1 hyscale_osx" "win/hyscale.ps1 mac/hyscale"
fi
