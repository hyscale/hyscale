package io.hyscale.ctl.controller.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.TableFields;
import io.hyscale.ctl.commons.logger.TableFormatter;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.util.StatusUtil;
import io.hyscale.ctl.deployer.services.deployer.Deployer;
import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command to get Deployment status for an application
 *
 * @author tushart
 */
@Command(name = "status", description = "Get App Deployment status")
public class HyscaleAppStatusCommand implements Runnable {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message")
    private boolean helpRequested = false;

    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace")
    private String namespace;

    @Option(names = {"-a", "--app"}, required = true, description = "Application name.")
    private String appName;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

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

        try {
        	List<DeploymentStatus> deploymentStatusList = deployer.getDeploymentStatus(deploymentContext);
        	
            if (deploymentStatusList != null && !deploymentStatusList.isEmpty()) {
                deploymentStatusList.stream().forEach(each -> {
                	String[] tableRow = StatusUtil.getRowData(each);
                    table.addRow(tableRow);
                });
                WorkflowLogger.logTable(table);
            } else {
                WorkflowLogger.info(ControllerActivity.NO_SERVICE_DEPLOYED);
            }
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
        }

    }

}