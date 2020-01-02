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
package io.hyscale.builder.services.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.processor.BuilderInterceptorProcessor;
import io.hyscale.commons.annotations.ComponentInterceptor;
import io.hyscale.commons.component.IInterceptorProcessor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Aspect
@Configuration
public class ImageBuilderAspect {

    private static final Logger logger = LoggerFactory.getLogger(ImageBuilderAspect.class);

    /**
     *  Pointcut expression to catch methods annotated with {@link ComponentInterceptor} 
     * and has {@link ServiceSpec} and {@link BuildContext} as parameter
     * @param interceptor 
     * @param serviceSpec
     * @param context BuildContext
     */
    @Pointcut("execution(* io.hyscale.builder.services.service.ImageBuildPushService.*(..)) "
            + "&& @annotation(interceptor) && args(serviceSpec, context)")
    public void manifestPointCut(ComponentInterceptor interceptor, ServiceSpec serviceSpec, BuildContext context) {

    }

    @Before("manifestPointCut(interceptor, serviceSpec, context)")
    public void doBefore(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec, BuildContext context)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                BuilderInterceptorProcessor processorBean = getProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.preProcess(serviceSpec, context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while pre processing for Image builder.", ex);
            throw ex;
        }
    }

    @AfterReturning("manifestPointCut(interceptor, serviceSpec, context)")
    public void doAfter(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec, BuildContext context)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                BuilderInterceptorProcessor processorBean = getProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.postProcess(serviceSpec, context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while post processing for Image builder.", ex);
            throw ex;
        }
    }

    @AfterThrowing("manifestPointCut(interceptor, serviceSpec, context)")
    public void onError(JoinPoint jp, ComponentInterceptor interceptor, ServiceSpec serviceSpec, BuildContext context)
            throws HyscaleException {
        try {
            for (Class<? extends IInterceptorProcessor> processor : interceptor.processors()) {
                BuilderInterceptorProcessor processorBean = getProcessorBean(processor);
                if (processorBean != null) {
                    processorBean.onError(serviceSpec, context);
                }
            }
        } catch (HyscaleException ex) {
            logger.error("Error while processing error for Image builder.", ex);
            throw ex;
        }
    }

    private BuilderInterceptorProcessor getProcessorBean(Class<? extends IInterceptorProcessor> processor) {
        IInterceptorProcessor processorBean = HyscaleContextUtil.getSpringBean(processor);
        if (processorBean == null) {
            logger.debug("Bean not found for Processor {}, ignoring processing", processor.getCanonicalName());
            return null;
        }
        if (!(processorBean instanceof BuilderInterceptorProcessor)) {
            logger.debug("Processor {}, not valid for image builder, ignoring processing",
                    processor.getCanonicalName());
            return null;
        }

        return (BuilderInterceptorProcessor) processorBean;
    }
}
