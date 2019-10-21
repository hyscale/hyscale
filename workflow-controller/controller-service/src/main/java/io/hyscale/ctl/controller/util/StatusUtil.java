package io.hyscale.ctl.controller.util;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import io.hyscale.ctl.deployer.core.model.DeploymentStatus;

/**
 * Utility for deployment status commands
 *
 */
public class StatusUtil {
	
	public static String[] getRowData(DeploymentStatus deploymentStatus) {
        String age = getAge(deploymentStatus.getAge());
        String statusMsg = deploymentStatus.getStatus() != null ? deploymentStatus.getStatus().getMessage() : null;
        String[] rowData = new String[]{deploymentStatus.getServiceName(), statusMsg, age, deploymentStatus.getServiceAddress(),
                deploymentStatus.getMessage()};
        return rowData;
    }
	
	/**
	 * 
	 * @param dateTime
	 * @return time in social-networking style timestamps
	 * @see PrettyTime
	 */
	private static String getAge(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        PrettyTime p = new PrettyTime();
        return p.format(dateTime.toDate());
    }

}
