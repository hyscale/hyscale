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
package io.hyscale.builder.cleanup.services.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.ImageCleanupProcessor;
import io.hyscale.builder.services.util.DockerImageUtil;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class DeleteAfterBuild implements ImageCleanupProcessor {

	@Autowired
	private ImageCommandProvider imageCommandProvider;
	
	@Autowired
	private DockerImageUtil dockerImageUtil;

	@Override
	public void clean(ServiceSpec serviceSpec) {
		String image=dockerImageUtil.getImageName(serviceSpec);
		List<String> imageIds = Arrays
				.asList(CommandExecutor.executeAndGetResults(imageCommandProvider.getAllImageCommandByImageName(image))
						.getCommandOutput().split("\\s+"));
		
		CommandExecutor.execute(imageCommandProvider.getAllImageDeleteCommand(new HashSet<String>(imageIds)));

	}
	
	public static void main(String args[]) {
		ImageCommandProvider imageCommandProvider=new ImageCommandProvider();
		
	}
}
