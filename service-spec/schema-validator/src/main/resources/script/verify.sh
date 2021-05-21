#!/bin/bash
VERSION=$1

HSPEC="https://github.com/hyscale/hspec/blob/release/HSpec$VERSION/schema/service-spec.json"
HPROF="https://github.com/hyscale/hspec/blob/release/HSpec$VERSION/schema/profile-spec.json"

if [[ "$VERSION" == "master" ]]; then
  HSPEC="https://github.com/hyscale/hspec/blob/$VERSION/schema/service-spec.json"
  HPROF="https://github.com/hyscale/hspec/blob/$VERSION/schema/profile-spec.json"
fi

echo $HSPEC
echo $HPROF

if curl --head --silent --fail $HSPEC 2> /dev/null;
 then
  echo "HSpec available"
 else
  echo "HSpec not available for link: "$HSPEC 
  exit 1;
fi

if curl --head --silent --fail $HPROF 2> /dev/null;
 then
  echo "HProf available"
 else
  echo "HProf not available for link: "$HPROF
  exit 1; 
fi
