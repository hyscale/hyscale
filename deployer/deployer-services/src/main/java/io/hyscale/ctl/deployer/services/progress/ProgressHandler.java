package io.hyscale.ctl.deployer.services.progress;

public interface ProgressHandler {

	void onPodLaunch(String podName);

	void onPodCompletion(String podName, boolean isSuccess);

	void onContainerLaunch(String containerName, String podName);

	void onContainerCompletion(String containerName, String podName, boolean isSuccess);

}
