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
package io.hyscale.troubleshooting.integration.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.deployer.services.model.PodExitStatus;
import io.hyscale.deployer.services.model.PodStatusUtil;
import io.hyscale.troubleshooting.integration.models.AbstractedErrorMessage;
import io.hyscale.troubleshooting.integration.models.ActionNode;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.FailedResourceKey;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1Pod;

@Component
public class FixCrashingApplication extends ActionNode<TroubleshootingContext> {

	private static final Logger logger = LoggerFactory.getLogger(FixCrashingApplication.class);
	private static final String EXIT_CODE="exit code ";

	@Override
	public void process(TroubleshootingContext context) {
		Object obj = context.getAttribute(FailedResourceKey.FAILED_POD);
		String lastState = null;
		V1Pod pod = null;
		if (obj != null) {
			pod = (V1Pod) FailedResourceKey.FAILED_POD.getKlazz().cast(obj);
			lastState = pod != null ? PodStatusUtil.lastStateOf(pod) : null;
		}

		DiagnosisReport report = new DiagnosisReport();
		if (lastState == null) {
			report.setReason(AbstractedErrorMessage.APPLICATION_CRASH.getReason());
			report.setRecommendedFix(AbstractedErrorMessage.APPLICATION_CRASH.getMessage());
			context.addReport(report);
			return;
		}

		if (lastState.equals(PodStatus.OOMKILLED.getStatus())) {
			report.setReason(AbstractedErrorMessage.NOT_ENOUGH_MEMORY_FOUND
					.formatReason(context.getServiceMetadata().getServiceName()));
			report.setRecommendedFix(AbstractedErrorMessage.NOT_ENOUGH_MEMORY_FOUND.getMessage());
		} else if (lastState.equals(PodStatus.COMPLETED.getStatus())) {
			report.setReason(AbstractedErrorMessage.INVALID_STARTCOMMANDS_FOUND.getReason());
			report.setRecommendedFix(AbstractedErrorMessage.INVALID_STARTCOMMANDS_FOUND.getMessage());
		} else {
			V1ContainerState v1ContainerState = PodStatusUtil.getLastState(pod);
			Integer statusCode = PodStatusUtil.getExitCode(v1ContainerState);
			logger.debug("Pod status code: {}", statusCode);
			if (statusCode!=null) {
				PodExitStatus podExitStatus = PodExitStatus.fromCode(statusCode);
				String exitCode = podExitStatus != null ? podExitStatus.getSignal() : EXIT_CODE + statusCode.toString();
				report.setReason(AbstractedErrorMessage.SERVICE_COMMANDS_FAILURE.formatReason(exitCode));
			}else {
				report.setReason(AbstractedErrorMessage.APPLICATION_CRASH.getReason());
			}
			report.setRecommendedFix(AbstractedErrorMessage.APPLICATION_CRASH.getMessage());
		}
		context.addReport(report);

	}

	@Override
	public String describe() {
		return "Fix your crashing application";
	}

}
