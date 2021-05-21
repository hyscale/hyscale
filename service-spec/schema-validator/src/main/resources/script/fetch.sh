#!/bin/bash

VERSION=$1

HSPEC="https://raw.githubusercontent.com/hyscale/hspec/release/HSpec${VERSION}/schema/service-spec.json"
HPROF="https://raw.githubusercontent.com/hyscale/hspec/release/HSpec${VERSION}/schema/profile-spec.json"

if [[ "$VERSION" == "master" ]]; then
  HSPEC="https://raw.githubusercontent.com/hyscale/hspec/master/schema/service-spec.json"
  HPROF="https://raw.githubusercontent.com/hyscale/hspec/master/schema/profile-spec.json"
fi

echo $HSPEC
echo $HPROF

mkdir src/main/resources/hspec/${VERSION}
mkdir src/main/resources/hprof/${VERSION}
cd src/main/resources/hspec/${VERSION}
wget $HSPEC -O service-spec.json

cd ../../hprof/${VERSION}
wget $HPROF -O profile-spec.json

