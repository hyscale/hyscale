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
package io.hyscale.commons.constants;

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