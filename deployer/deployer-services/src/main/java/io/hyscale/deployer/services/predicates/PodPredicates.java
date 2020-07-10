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
import io.kubernetes.client.openapi.models.V1Pod;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Defines predicated for pod conditions
 *
 */
public class PodPredicates {
    
    private PodPredicates() {}

	public static Predicate<V1Pod> isPodschedulingCondition() {
		return pod -> K8sPodUtil.checkForPodCondition(pod, PodCondition.POD_SCHEDULED);
	}

	public static Predicate<V1Pod> isPodReady() {
		return pod -> K8sPodUtil.checkForPodCondition(pod, PodCondition.READY);
	}
	
	public static Predicate<V1Pod> isPodInitialized() {
		return pod -> K8sPodUtil.checkForPodCondition(pod, PodCondition.INITIALIZED);
	}

	public static Predicate<V1Pod> isPodCreated() {
		return K8sPodUtil::checkForPodCreation;
	}
	
	/**
	 * Pods are ambiguous if pods have different owner kinds, or same owner with different uid
	 * @return {@link Predicate} which checks for ambiguity condition
	 */
	public static Predicate<List<V1Pod>> isPodAmbiguous() {
	    return K8sPodUtil::checkForPodAmbiguity;
	}
	
	public static BiPredicate<V1Pod, Map<String, String>> podContainsLabel(){
	    return K8sPodUtil::checkPodLabels;
	}
	
	/**
	 * Pods are in failed state or not
	 * @return {@link Predicate} return true if pod is in failed state, else false
	 */
	public static Predicate<V1Pod> isPodFailed(){
	    return K8sPodUtil::checkForPodFailure;
	}
	
	public static BiPredicate<V1Pod, Long> isPodRestarted(){
	    return K8sPodUtil::checkForPodRestart;
	}
}
