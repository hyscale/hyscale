package io.hyscale.ctl.commons.constants;

import java.util.Arrays;
import java.util.List;

public class K8SRuntimeConstants {

	public static final String K8s_DEPLOYMENT_POD_TEMPLATE_HASH = "pod-template-hash";

	public static final String K8s_STS_CONTROLLER_REVISION_HASH = "controller-revision-hash";

	public static final String DOMAIN_NAME = "DOMAIN_NAME";

	public static final String HYSCALE_SYSTEM_COMPONENT = "system";

	public static final String DEFAULT_BACKEND_SERVICE_NAME = "ingress-default-backend-svc";

	public static final String DEFAULT_BACKEND_COMPONENT_NAME = "hyscale-default-backend";

	public static final String DEFAULT_INGRESS_CONTROLLER_COMPONENT_NAME = "hyscale-ingress-controller";

	public static final String INGRESS_GROUP = "hyscale-ingress";

	public static final String CLIENTIP = "ClientIP";

	public static final String NONE = "None";

	public static final String COOKIE = "cookie";

	public static final String INGRESS_LEADER_CONFIG_MAP_NAME_PREFIX = "ingress-controller-leader-";

	public static final String POD_READY_STATE_CONDITION = "Ready";

	public static final String POD_SCHEDULED_CONDITION = "PodScheduled";

	public static final String POD_RUNING_STATE_CONDITION = "Running";

	public static final String DEFAULT_NAMESPACE = "default";

	public static final List<String> POD_ERROR_SATES = Arrays.asList("ErrImagePull", "CrashLoopBackOff",
			"ImagePullBackOff", "Error", "Terminating");

	public static final String DEFAULT_VOLUME_SIZE = "1Gi";

}