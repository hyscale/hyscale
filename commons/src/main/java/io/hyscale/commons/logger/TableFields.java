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
package io.hyscale.commons.logger;

import io.hyscale.commons.constants.ValidationConstants;

public enum TableFields {

	SERVICE("Service Name", 30), STATUS("Status", 15), AGE("Age"), REASON("Reason"), MESSAGE("Message"),
	SERVICE_ADDRESS("Service Address", 40), SERVICE_ADDRESS_LARGE("Service Address", 75), REPLICA_NAME("Replica name", 50), 
	INDEX("Index", 7), NAMESPACE("Namespace", ValidationConstants.NAMESPACE_LENGTH_MAX), 
	APPLICATION("Application", ValidationConstants.APP_NAME_LENGTH_MAX), SERVICES("Services", 40),
	PROFILE("Profile",ValidationConstants.PROFILE_NAME_LENGTH_MAX), SERVICE_URL("Service URL", 40);

	private TableFields(String fieldName, Integer length) {
		this.fieldName = fieldName;
		this.length = length;
	}

	private TableFields(String fieldName) {
		this.fieldName = fieldName;
		this.length = TableField.DEFAULT_FIELD_LENGTH;
	}

	private String fieldName;
	private Integer length;

	public String getFieldName() {
		return fieldName;
	}

	public Integer getLength() {
		return length;
	}

}
