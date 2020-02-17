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
package io.hyscale.dockerfile.gen.services.aspect;

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
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.processor.DockerfileGenInterceptorProcessor;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Aspect
@Configuration
public class DockerfileGenAspect {

    private static final Logger logger = LoggerFactory.getLogger(DockerfileGenAspect.class);

    /**
     * Pointcut expression to catch methods annotated with {@link ComponentInterceptor} 
     * and has {@link ServiceSpec} and {@link DockerfileGenContext} as parameter
     * @param interceptor
     * @param context
     */
    @Pointcut("execution(* io.hyscale.dockerfile.gen.services.generator.DockerfileGenerator.*(..)) "
            + "&& @annotation(interceptor) && args(serviceSpec, context)")
    public void dockerfileGenPointCut(ComponentInterceptor interceptor, ServiceSpec serviceSpec,
            DockerfileGenContext context) {
    }

    @Before("dockerfileGenPointCut(interceptor, serviceSpec, context)")
    public void doBefore(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec,
            DockerfileGenContext context) throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DockerfileGenInterceptorProcessor processorBean = validateAndGetProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.preProcess(serviceSpec, context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while pre processing for Dockerfile generator.", ex);
            throw ex;
        }
    }

    @AfterReturning("dockerfileGenPointCut(interceptor, serviceSpec, context)")
    public void doAfter(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec,
            DockerfileGenContext context) throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DockerfileGenInterceptorProcessor processorBean = validateAndGetProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.postProcess(serviceSpec, context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while post processing for Dockerfile generator.", ex);
            throw ex;
        }
    }

    @AfterThrowing(pointcut = "dockerfileGenPointCut(interceptor, serviceSpec, context)", throwing = "th")
    public void onError(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec,
            DockerfileGenContext context, Throwable th) throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                DockerfileGenInterceptorProcessor processorBean = validateAndGetProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.onError(serviceSpec, context, th);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while processing error for Dockerfile generator.", ex);
            throw ex;
        }
    }

    private DockerfileGenInterceptorProcessor validateAndGetProcessorBean(Class<? extends IInterceptorProcessor> processor) {
        IInterceptorProcessor processorBean = HyscaleContextUtil.getSpringBean(processor);
        if (processorBean == null) {
            logger.debug("Bean not found for Processor {}, ignoring processing", processor.getCanonicalName());
            return null;
        }
        if (!(processorBean instanceof DockerfileGenInterceptorProcessor)) {
            logger.debug("Processor {}, not valid for dockerfile generator, ignoring processing",
                    processor.getCanonicalName());
            return null;
        }

        return (DockerfileGenInterceptorProcessor) processorBean;
    }

}
