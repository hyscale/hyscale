#!/bin/bash

appName=$1;
namespace=$2;


declare -a services=('zipkin' 'mysql' 'redis' 'mongodb' 'rabbitmq' 'cart' 'user' 'catalogue' 'shipping' 'dispatch' 'ratings' 'payments' 'loadgen' 'web');
for i in "${services[@]}"
do
	hyscale deploy service -f $i/$i.hspec.yaml -a $appName -n $namespace
done

