package io.hyscale.ctl.deployer.services.progress;

/**
 * Progress Handler for cluster resources
 * Updates progress based on resource status
 *
 */
public interface ProgressHandler {

	void onPodLaunch(String podName);

	void onPodCompletion(String podName, boolean isSuccess);

	void onContainerLaunch(String containerName, String podName);

	void onContainerCompletion(String containerName, String podName, boolean isSuccess);

}
