package io.hyscale.ctl.deployer.services.util;

import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeploymentStatusUtilTest {

    @Test
    public void testNotDeployedStatus() {
        DeploymentStatus status = DeploymentStatusUtil.getNotDeployedStatus("myservice");
        Assertions.assertNotNull(status);
        Assertions.assertEquals(status.getServiceName(), "myservice");
        Assertions.assertEquals(status.getStatus(), DeploymentStatus.Status.NOT_DEPLOYED);
    }
}
