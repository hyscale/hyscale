package io.hyscale.ctl.deployer.services.model;

public enum PodCondition {

	READY("Ready"), POD_SCHEDULED("PodScheduled"), INITIALIZED("Initialized"), CONTAINERS_READY("ContainersReady");

	private String podCondition;

	PodCondition(String podCondition) {
		this.podCondition = podCondition;
	}

	public String getPodCondition() {
		return podCondition;
	}

	public static PodCondition fromString(String podCondition) {
		if (podCondition == null) {
			return null;
		}
		for (PodCondition condition : PodCondition.values()) {
			if (condition.getPodCondition().equalsIgnoreCase(podCondition)) {
				return condition;
			}
		}
		return null;
	}
}
