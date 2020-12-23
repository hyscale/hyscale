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
package io.hyscale.controller.commands.get.app;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.impl.ClusterValidator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.TableFormatter.Builder;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.model.WorkflowContextBuilder;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.services.deployer.Deployer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This class executes 'hyscale get apps' command.
 * It is a sub-command of the 'hyscale get' command
 *
 * @author tushar
 * @see io.hyscale.controller.commands.get.HyscaleGetCommand .
 * It also act as parent command to perform app level operation like get status
 * @see HyscaleAppStatusCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 * <p>
 * Eg: hyscale get apps
 * <p>
 * Displays all the apps along with namespace deployed on the cluster.
 * Ignores system namespace {@link K8SRuntimeConstants#SYSTEM_NAMESPACE} on the cluster
 */
@Command(name = "apps", aliases = "app", subcommands = {HyscaleAppStatusCommand.class}, description = "Operates on the application specified.")
@Component
public class HyscaleGetAppsCommand implements Callable<Integer> {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    //    @Option(names = { "--wide" }, required = false, description = "Display additional information like services.")
    private boolean wide = false;

    @Autowired
    private ClusterValidator clusterValidator;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Override
    public Integer call() throws Exception {
        WorkflowContext context = new WorkflowContextBuilder(null).
                withAuthConfig(authConfigBuilder.getAuthConfig()).get();

        if (!clusterValidator.validate(context)) {
            WorkflowLogger.logPersistedActivities();
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        WorkflowLogger.header(ControllerActivity.APPLICATION_DETAILS);
        List<AppMetadata> appInfoList = null;
        try {
            appInfoList = deployer.getAppsMetadata(context.getAuthConfig());
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_DEPLOYMENTS);
            throw e;
        }

        if (appInfoList == null || appInfoList.isEmpty()) {
            WorkflowLogger.info(ControllerActivity.NO_DEPLOYMENTS);
            WorkflowLogger.footer();
            return ToolConstants.HYSCALE_SUCCESS_CODE;
        }

        appInfoList = appInfoList.stream().filter(appInfo -> {
            if (appInfo == null || StringUtils.isBlank(appInfo.getAppName())
                    || K8SRuntimeConstants.SYSTEM_NAMESPACE.contains(appInfo.getNamespace())) {
                return false;
            }
            return true;
        }).sorted(Comparator.comparing(AppMetadata::getAppName)).collect(Collectors.toList());

        if (appInfoList.isEmpty()) {
            WorkflowLogger.info(ControllerActivity.NO_DEPLOYMENTS);
            WorkflowLogger.footer();
            return ToolConstants.HYSCALE_SUCCESS_CODE;
        }


        Builder tableBuilder = new TableFormatter.Builder()
                .addField(TableFields.APPLICATION.getFieldName())
                .addField(TableFields.PROFILE.getFieldName())
                .addField(TableFields.NAMESPACE.getFieldName());

        if (wide) {
            tableBuilder.addField(TableFields.SERVICES.getFieldName(), TableFields.SERVICES.getLength());
        }
        TableFormatter table = tableBuilder.build();

        appInfoList.forEach(appInfo -> {
            String services = appInfo.getServices() == null || appInfo.getServices().isEmpty() ? null
                    : appInfo.getServices().toString().replace("[", "").replace("]", "");
            String[] row = new String[]{appInfo.getAppName(), appInfo.getEnvName(), appInfo.getNamespace(), services};
            table.addRow(row);
        });
        WorkflowLogger.logTable(table);
        WorkflowLogger.footer();
        return ToolConstants.HYSCALE_SUCCESS_CODE;
    }

}
