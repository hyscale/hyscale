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

public enum TableFields {

	SERVICE("Service Name", 30), STATUS("Status"), AGE("Age"), REASON("Reason"), MESSAGE("Message"),
	SERVICE_ADDRESS("Service Address", 40), REPLICA_NAME("Replica name", 40), INDEX("Index", 7);

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
