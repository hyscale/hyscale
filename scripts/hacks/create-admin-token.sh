#!/bin/bash

# Handy script to create cluster admin service account and set the token in the current kubeconfig context
# Useful when you have a dynamic token in kubeconfig with user.exec command like eks, gke but wanted to supply kubeconfig having static token to kubernetes deployer tools.

set -e

ekscluster_context=$(kubectl config current-context)

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "taking kubeconfig backup in ${DIR}/kubeconfig"

kubectl config view --flatten > ${DIR}/kubeconfig

admin_sa_file="${DIR}/cluster-admin-sa.yaml"

echo
echo "Generating cluster admin service account Yaml in $admin_sa_file"

cat <<EOM >$admin_sa_file
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kube-system
EOM

echo
echo "Creating cluster admin service account in kubernetes with name admin-user..."
kubectl apply -f $admin_sa_file

kubectl -n kube-system get secret | grep admin-user-token | wc -l
kube_token=$(kubectl -n kube-system get secret $(kubectl -n kube-system get secret | grep admin-user-token | awk '{print $1}') -o go-template='{{ .data.token | base64decode}}{{"\n"}}')

echo
echo "Updating kubeconfig..."
kubectl config unset users.${ekscluster_context}

kubectl config set users.${ekscluster_context}.token $kube_token
echo "Updated kubeconfig !"
