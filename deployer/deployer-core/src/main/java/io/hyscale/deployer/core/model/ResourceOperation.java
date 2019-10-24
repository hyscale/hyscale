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
package io.hyscale.deployer.core.model;

public enum ResourceOperation {

	GET("Get"), CREATE("Create"), UPDATE("Update"), DELETE("Delete"), PATCH("Patch"),
	GET_BY_SELECTOR("Get by Selector"), DELETE_BY_SELECTOR("Delete by selector");

	private String operation;

	ResourceOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return this.operation;
	}
}
