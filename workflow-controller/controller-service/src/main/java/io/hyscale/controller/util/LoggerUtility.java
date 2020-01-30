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
package io.hyscale.controller.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.util.ImageLogUtil;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.utils.HyscaleInputUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.util.DeployerLogUtil;

/**
 * Utility to fetch deployment logs of the service specified.
 */
@Component
public class LoggerUtility {

	private static final Logger logger = LoggerFactory.getLogger(LoggerUtility.class);

	@Autowired
	private ImageLogUtil imageLogUtil;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	@Autowired
	private DeployerLogUtil deployerLogUtil;
	
	@Autowired
	private Deployer deployer;

	public void getLogs(WorkflowContext workflowContext) throws HyscaleException {
	    // Ignore image logs as they can be viewed at the directory
		// imageBuilderLogs(workflowContext);
		deploymentLogs(workflowContext);
	}

	/**
	 * Deployment logs 
	 * @param context
	 * @throws HyscaleException 
	 */
	public void deploymentLogs(WorkflowContext context) throws HyscaleException {

		String appName = context.getAppName();
		String serviceName = context.getServiceName();
		String namespace = context.getNamespace();

		Boolean tail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
		tail = (tail == null) ? false : tail;
		Integer lines = (Integer) context.getAttribute(WorkflowConstants.LINES);
		DeploymentContext deploymentContext = new DeploymentContext();
		deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(namespace);
		deploymentContext.setAppName(appName);
		deploymentContext.setServiceName(serviceName);
		deploymentContext.setTailLogs(tail);
		deploymentContext.setReadLines(lines);

		String selectedPod = null;
		try {
		    selectedPod = getSelectedPod(deploymentContext);
		} catch(HyscaleException ex) {
		    // fail
		    context.setFailed(true);
		    if (ex.getHyscaleErrorCode() != ControllerErrorCodes.VALID_INPUT_NOT_PROVIDED) {
		        WorkflowLogger.error(ControllerActivity.FAILED_TO_STREAM_SERVICE_LOGS, ex.getMessage());
		    }
            throw ex;
		}
		if (StringUtils.isBlank(selectedPod)) {
		    // No service found case
		    WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
		    WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
		    WorkflowLogger.footer();
		    return;
		}
		try {
			WorkflowLogger.header(ControllerActivity.SERVICE_LOGS);
			deployerLogUtil.processLogs(deploymentContext, selectedPod);
		} catch (HyscaleException ex) {
		    logger.error("Error while getting deployment logs for service: {}, in namespace: {}", serviceName, namespace, ex);
			if (ex.getHyscaleErrorCode() == DeployerErrorCodes.FAILED_TO_RETRIEVE_POD) {
				WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
			} else {
			    context.setFailed(true);
				WorkflowLogger.error(ControllerActivity.FAILED_TO_STREAM_SERVICE_LOGS, ex.getMessage());
			}
			WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
		} finally {
			WorkflowLogger.footer();
		}
	}

	/**
	 * 
	 * @param deploymentContext
	 * @return pod name selected by user
	 * @throws HyscaleException 
	 */
	private String getSelectedPod(DeploymentContext deploymentContext) throws HyscaleException {
	    List<ReplicaInfo> replicasInfo = deployer.getReplicas(deploymentContext, false);
	    if (replicasInfo == null || replicasInfo.isEmpty()) {
	        return null;
	    }
	    if (replicasInfo.size() == 1) {
	        return replicasInfo.get(0).getName();
	    }
	    List<String> replicas = replicasInfo.stream().map( each -> each.getName()).collect(Collectors.toList());
	    Map<Integer, ReplicaInfo> indexedReplicasInfo = new HashMap<Integer, ReplicaInfo>();
	    
	    Integer replicaIndex = 1;
	    for (ReplicaInfo replicaInfo : replicasInfo) {
            indexedReplicasInfo.put(replicaIndex++, replicaInfo);
	    }
	    printReplicasInfo(indexedReplicasInfo);
	    
	    WorkflowLogger.input(ControllerActivity.INPUT_REPLICA_DETAIL);
	    int inputAttempt = 0;
	    
	    while(inputAttempt < HyscaleInputUtil.MAX_RETRIES) {
	        inputAttempt++;
	        String input = HyscaleInputUtil.getStringInput();
	        try {
	            replicaIndex = Integer.parseInt(input);
	            if(indexedReplicasInfo.containsKey(replicaIndex)) {
	                return indexedReplicasInfo.get(replicaIndex).getName();
	            } else {
	                WorkflowLogger.warn(ControllerActivity.INVALID_INPUT_RETRY, replicaIndex.toString());
	            }
	        } catch (NumberFormatException e) {
	            if (replicas.contains(input)) {
	                return input;
	            } else {
	                WorkflowLogger.warn(ControllerActivity.INVALID_INPUT_RETRY, input);
	            }
	        }
	    }
	    HyscaleException hex = new HyscaleException(ControllerErrorCodes.VALID_INPUT_NOT_PROVIDED);
	    WorkflowLogger.error(ControllerActivity.INVALID_INPUT, hex.getMessage());
	    WorkflowLogger.footer();
        throw hex;
    }
	
	private static void printReplicasInfo(Map<Integer, ReplicaInfo> indexedReplicasInfo) {
        TableFormatter replicaTable = new TableFormatter.Builder()
                .addField(TableFields.INDEX.getFieldName(), TableFields.INDEX.getLength())
                .addField(TableFields.REPLICA_NAME.getFieldName(), TableFields.REPLICA_NAME.getLength())
                .addField(TableFields.STATUS.getFieldName(), 30)
                .addField(TableFields.AGE.getFieldName(), TableFields.AGE.getLength())
                .build();
        
        indexedReplicasInfo.entrySet().forEach(replicaInfoSet -> {
            String[] replicaData = StatusUtil.getReplicasData(replicaInfoSet.getValue());
            String[] rowData = ArrayUtils.insert(0, replicaData, replicaInfoSet.getKey().toString());
            replicaTable.addRow(rowData);
        });
        
        WorkflowLogger.logTable(replicaTable);
        WorkflowLogger.footer();
        
    }
	    
	/**
	 * Image build and push logs
	 * @param context
	 */
	public void imageBuilderLogs(WorkflowContext context) throws HyscaleException{
		BuildContext buildContext = new BuildContext();
		buildContext.setAppName(context.getAppName());
		buildContext.setServiceName(context.getServiceName());
		Boolean tail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
		logger.debug("Getting Image Builder logs");
		tail = tail == null ? false : tail;
		buildContext.setTail(tail);
		imageLogUtil.handleLogs(buildContext);

	}
}
