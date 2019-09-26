package io.hyscale.ctl.deployer.services.provider.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.constants.ToolConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.hyscale.ctl.commons.models.K8sBasicAuth;
import io.hyscale.ctl.commons.models.K8sConfigFileAuth;
import io.hyscale.ctl.commons.models.K8sConfigReaderAuth;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.provider.K8sClientProvider;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;

@Component
public class K8sClientProviderImpl implements K8sClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(K8sClientProviderImpl.class);

    private ApiClient from(K8sConfigFileAuth authConfig) throws HyscaleException {
        String mountedKubeConfigName = authConfig.getK8sConfigFile() != null ? SetupConfig.getMountPathOfKubeConf(authConfig.getK8sConfigFile().getName()) : ToolConstants.EMPTY_STRING;
        try (FileInputStream fis = new FileInputStream(authConfig.getK8sConfigFile())) {
            return Config.fromConfig(fis);
        } catch (FileNotFoundException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.KUBE_CONFIG_NOT_FOUND, mountedKubeConfigName);
            logger.error("Failed to find kube config ", ex);
            throw ex;
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.UNABLE_TO_READ_KUBE_CONFIG);
            logger.error("Failed to initialize k8s client ", ex);
            throw ex;
        } catch (Exception e) {
            // yaml parsing exception
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.INVALID_KUBE_CONFIG, mountedKubeConfigName);
            logger.error("Failed to initialize k8s client ", ex);
            throw ex;
        }
    }

    private ApiClient from(K8sConfigReaderAuth authConfig) throws HyscaleException {
        try {
            return Config.fromConfig(authConfig.getK8sConfigReader());
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.UNABLE_TO_READ_KUBE_CONFIG);
            logger.error("Failed to initialize k8s client ", ex);
            throw ex;
        } catch (Exception e) {
            // yaml parsing exception
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.INVALID_KUBE_CONFIG, ToolConstants.EMPTY_STRING);
            logger.error("Failed to initialize k8s client ", ex);
            throw ex;
        }
    }

    private ApiClient from(K8sBasicAuth authConfig) throws HyscaleException {
        if (authConfig.getToken() == null) {
            return Config.fromUserPassword(authConfig.getMasterURL(), authConfig.getUserName(),
                    authConfig.getPassword(), authConfig.getCaCert() != null ? true : false);
        } else {
            return Config.fromToken(authConfig.getMasterURL(), authConfig.getToken(),
                    authConfig.getCaCert() != null ? true : false);
        }
    }

    @Override
    public ApiClient get(K8sAuthorisation authConfig) throws HyscaleException {
        ApiClient apiClient = null;
        switch (authConfig.getK8sAuthType()) {
            case KUBE_CONFIG_FILE:
                apiClient = from((K8sConfigFileAuth) authConfig);
                break;
            case KUBE_CONFIG_READER:
                apiClient = from((K8sConfigReaderAuth) authConfig);
                break;
            case BASIC_AUTH:
                apiClient = from((K8sBasicAuth) authConfig);
                break;
        }
        return apiClient;
    }

}
