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
package io.hyscale.deployer.services.predicates;

import io.hyscale.deployer.services.model.PodCondition;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.kubernetes.client.models.V1Pod;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Defines predicated for pod conditions
 *
 */
public class PodPredicates {

	public static Predicate<V1Pod> isPodschedulingCondition() {
		return pod -> K8sPodUtil.checkForPodCondition(pod, PodCondition.POD_SCHEDULED);
	}

	public static Predicate<V1Pod> isPodReady() {
		return pod -> K8sPodUtil.checkForPodCondition(pod, PodCondition.READY);
	}

	public static Predicate<V1Pod> isPodCreated() {
		return pod -> K8sPodUtil.checkForPodCreation(pod);
	}
	
	public static Predicate<List<V1Pod>> isPodAmbiguous() {
	     return podList -> K8sPodUtil.checkForPodAmbiguity(podList);
	}
	
	public static BiPredicate<V1Pod, Map<String, String>> podContainsLabel(){
	    return (pod, labels) -> K8sPodUtil.checkPodLabels(pod, labels);
	}
}
