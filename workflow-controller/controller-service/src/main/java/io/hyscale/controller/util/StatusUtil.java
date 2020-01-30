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

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ReplicaInfo;

/**
 * Utility for deployment status commands
 *
 */
public class StatusUtil {
    
	public static String[] getRowData(DeploymentStatus deploymentStatus) {
		if (deploymentStatus == null) {
			return null;
		}
        String age = getAge(deploymentStatus.getAge());
        String statusMsg = deploymentStatus.getStatus() != null ? deploymentStatus.getStatus().getMessage() : null;
        String[] rowData = new String[]{deploymentStatus.getServiceName(), statusMsg, age, deploymentStatus.getServiceAddress(),
                deploymentStatus.getMessage()};
        return rowData;
    }
	
	public static String[] getReplicasData(ReplicaInfo replicaInfo) {
	    if (replicaInfo == null) {
	        return null;
	    }
	    String age = getAge(replicaInfo.getAge());
	    String[] rowData = new String[] {replicaInfo.getName(), replicaInfo.getStatus(), age};
	    return rowData;
	    
	}
	/**
	 * 
	 * @param dateTime
	 * @return time in social-networking style timestamps
	 * @see PrettyTime
	 */
	public static String getAge(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        PrettyTime p = new PrettyTime();
        return p.format(dateTime.toDate());
    }

}
