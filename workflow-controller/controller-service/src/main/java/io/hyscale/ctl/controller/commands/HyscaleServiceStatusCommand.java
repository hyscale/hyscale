package io.hyscale.ctl.controller.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.TableFields;
import io.hyscale.ctl.commons.logger.TableFormatter;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.util.StatusUtil;
import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import io.hyscale.ctl.deployer.services.deployer.Deployer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command to get Deployment Status for Services
 *
 * @author tushart
 */
@Command(name = "status", description = "Get the status of the deployment")
public class HyscaleServiceStatusCommand implements Runnable {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service")
    private String namespace;

    @Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @Option(names = {"-s", "--service"}, required = true, description = "Service names.")
    private String[] serviceList;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    private static final Logger logger = LoggerFactory.getLogger(HyscaleServiceStatusCommand.class);

    @Override
    public void run() {

        WorkflowLogger.header(ControllerActivity.APP_NAME, appName);

        TableFormatter table = new TableFormatter.Builder()
                .addField(TableFields.SERVICE.getFieldName(), TableFields.SERVICE.getLength())
                .addField(TableFields.STATUS.getFieldName())
                .addField(TableFields.AGE.getFieldName(), TableFields.AGE.getLength())
                .addField(TableFields.SERVICE_ADDRESS.getFieldName(), TableFields.SERVICE_ADDRESS.getLength())
                .addField(TableFields.MESSAGE.getFieldName(), TableFields.MESSAGE.getLength()).build();

        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(namespace.trim());
        deploymentContext.setAppName(appName.trim());
        deploymentContext.setWaitForReadiness(false);

        List<DeploymentStatus> deploymentStatusList = new ArrayList<>();

        try {
        	Set<String> services = new HashSet<String>(Arrays.asList(serviceList));
        	WorkflowLogger.logTableFields(table);
        	for (String serviceName : services) {
        		deploymentContext.setServiceName(serviceName);
        		DeploymentStatus serviceStatus = deployer.getServiceDeploymentStatus(deploymentContext);
        		if (serviceStatus != null) {
        			String[] tableRow = StatusUtil.getRowData(serviceStatus);
        			table.addRow(tableRow);
					WorkflowLogger.logTableRow(table, tableRow);
        		}
				deploymentStatusList.add(serviceStatus);
        		
        	}
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
        }

    }

}
