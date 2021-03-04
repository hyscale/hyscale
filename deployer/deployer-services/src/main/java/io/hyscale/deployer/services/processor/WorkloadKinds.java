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
package io.hyscale.deployer.services.processor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.hyscale.deployer.core.model.CustomResourceKind;
import io.hyscale.deployer.core.model.ResourceKind;

@Component
public class WorkloadKinds {

	private List<CustomResourceKind> kinds;
	private static final String POD_PARENT_API_VERSION = "apps/v1";

	@PostConstruct
	public void init() {
		kinds = new ArrayList<>();
		kinds.add(new CustomResourceKind(ResourceKind.DEPLOYMENT.getKind(), POD_PARENT_API_VERSION));
		kinds.add(new CustomResourceKind(ResourceKind.STATEFUL_SET.getKind(), POD_PARENT_API_VERSION));
	}

	public List<CustomResourceKind> get() {
		return kinds;
	}

	public void register(CustomResourceKind kind) {
		kinds.add(kind);
	}
}
