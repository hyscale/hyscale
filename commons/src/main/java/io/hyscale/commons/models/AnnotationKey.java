/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.commons.models;

public enum AnnotationKey {

    PVC_TEMPLATE_STORAGE_CLASS("volume.beta.kubernetes.io/storage-class"),
    K8S_HYSCALE_LAST_APPLIED_CONFIGURATION("hyscale.io/last-applied-configuration"),
    K8S_DEFAULT_STORAGE_CLASS("storageclass.beta.kubernetes.io/is-default-class"),
    K8S_STORAGE_CLASS("volume.beta.kubernetes.io/storage-class"),
    K8S_DEPLOYMENT_REVISION("deployment.kubernetes.io/revision"),
    K8S_STS_POD_NAME("statefulset.kubernetes.io/pod-name"),
    HYSCALE_SERVICE_SPEC("hyscale.io/service-spec"),
    DEFAULT_STORAGE_CLASS("storageclass.kubernetes.io/is-default-class"),
    DEFAULT_BETA_STORAGE_CLASS("storageclass.beta.kubernetes.io/is-default-class"),
    LAST_UPDATED_AT("hyscale.io/last-updated-at"),
    CHECKSUM("hyscale.io/checksum"),
    HYSCALE_APPLIED_KINDS("hyscale.io/applied-kinds");
    

    private String annotation;

    AnnotationKey(String annotationKey) {
        this.annotation = annotationKey;
    }

    public String getAnnotation() {
        return annotation;
    }
}
