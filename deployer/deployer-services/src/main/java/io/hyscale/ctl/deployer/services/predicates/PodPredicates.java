package io.hyscale.ctl.deployer.services.predicates;

import io.hyscale.ctl.deployer.services.model.PodCondition;
import io.hyscale.ctl.deployer.services.util.K8sPodUtil;
import io.kubernetes.client.models.V1Pod;

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
}
