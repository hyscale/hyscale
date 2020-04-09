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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.DeploymentStatus.ServiceStatus;

public class StatusUtilTest {

    private DeploymentStatus deploymentStatus = null;

    private static DateTime dateTime = DateTime.now();

    @BeforeEach
    public void createDeploymentStatus() {
        deploymentStatus = new DeploymentStatus();
        deploymentStatus.setAge(dateTime);
        deploymentStatus.setServiceAddress("serviceAddress");
        deploymentStatus.setMessage("test Message");
        deploymentStatus.setServiceName("service");
        deploymentStatus.setServiceStatus(ServiceStatus.RUNNING);
    }

    @Test
    public void testStatusData() {
        String[] rowData = StatusUtil.getRowData(deploymentStatus);

        List<String> rowDataList = Arrays.asList(rowData);

        assertTrue(rowDataList.contains(deploymentStatus.getMessage()));
        assertTrue(rowDataList.contains(deploymentStatus.getServiceAddress()));
        assertTrue(rowDataList.contains(deploymentStatus.getServiceName()));
        assertTrue(rowDataList.contains(deploymentStatus.getServiceStatus().getMessage()));
        assertTrue(rowDataList.contains(StatusUtil.getAge(dateTime)));
    }

    @Test
    public void nullDeployStatus() {
        assertNull(StatusUtil.getRowData(null));
    }

    @Test
    public void nullStatus() {
        deploymentStatus.setServiceStatus(null);
        assertNotNull(StatusUtil.getRowData(deploymentStatus));
    }

    @Test
    public void nullAge() {
        deploymentStatus.setAge(null);
        assertNotNull(StatusUtil.getRowData(deploymentStatus));
    }

    @Test
    public void nullServiceAddress() {
        deploymentStatus.setServiceAddress(null);
        assertNotNull(StatusUtil.getRowData(deploymentStatus));
    }

    @Test
    public void nullMessage() {
        deploymentStatus.setMessage(null);
        assertNotNull(StatusUtil.getRowData(deploymentStatus));
    }

    @Test
    public void nullServiceName() {
        deploymentStatus.setServiceName(null);
        assertNotNull(StatusUtil.getRowData(deploymentStatus));
    }
}
