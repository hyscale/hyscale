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
package io.hyscale.deployer.services.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.hyscale.commons.annotations.ComponentInterceptor;
import io.hyscale.commons.component.IInterceptorProcessor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.listener.HyscaleContextHelper;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.deployer.services.processor.DeployerInterceptorProcessor;

@Aspect
@Configuration
public class DeployerAspect {

    private static final Logger logger = LoggerFactory.getLogger(DeployerAspect.class);

    /**
     * Pointcut expression to catch methods annotated with {@link ComponentInterceptor} 
     * and has {@link DeploymentContext} as parameter
     * @param interceptor
     * @param context
     */
    @Pointcut("execution(public * io.hyscale.deployer.services.deployer.Deployer.*(..)) "
            + "&& @annotation(interceptor) && args(context)")
    public void deploymentContextPointCut(ComponentInterceptor interceptor, DeploymentContext context) {

    }

    @Before("deploymentContextPointCut(interceptor, context)")
    public void doBefore(JoinPoint jp, ComponentInterceptor interceptor, DeploymentContext context)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DeployerInterceptorProcessor deployerProcessor = validateAndGetProcessorBean(processor);
                if (deployerProcessor != null) {
                    deployerProcessor.preProcess(context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while pre processing for deployer.", ex);
            throw ex;
        }
    }

    @AfterReturning("deploymentContextPointCut(interceptor, context)")
    public void doAfter(JoinPoint jp, ComponentInterceptor interceptor, DeploymentContext context)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DeployerInterceptorProcessor deployerProcessor = validateAndGetProcessorBean(processor);
                if (deployerProcessor != null) {
                    deployerProcessor.postProcess(context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while post processing for deployer.", ex);
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "deploymentContextPointCut(interceptor, context)", throwing = "th")
    public void onError(JoinPoint jp, ComponentInterceptor interceptor, DeploymentContext context, Throwable th)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DeployerInterceptorProcessor deployerProcessor = validateAndGetProcessorBean(processor);
                if (deployerProcessor != null) {
                    deployerProcessor.onError(context, th);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while processing error for deployer.", ex);
            throw ex;
        }
    }

    private DeployerInterceptorProcessor validateAndGetProcessorBean(Class<? extends IInterceptorProcessor> processor) {
        IInterceptorProcessor processorBean = HyscaleContextHelper.getSpringBean(processor);
        if (processorBean == null) {
            logger.debug("Bean not found for Processor {}, ignoring processing", processor.getCanonicalName());
            return null;
        }
        if (!(processorBean instanceof DeployerInterceptorProcessor)) {
            logger.debug("Processor {}, not valid for deployer, ignoring processing", processor.getCanonicalName());
            return null;
        }

        return (DeployerInterceptorProcessor) processorBean;
    }
}
