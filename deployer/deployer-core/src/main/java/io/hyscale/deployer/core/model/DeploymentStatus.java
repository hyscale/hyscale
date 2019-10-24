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

import org.joda.time.DateTime;

/**
 * Service information on cluster including
 * name, status {@link Status}, message(if any), service Address(If external)
 */
public class DeploymentStatus {

	public enum Status {
		RUNNING("Running"),
		NOT_RUNNING("Not Running"),
		NOT_DEPLOYED("Not Deployed");

		private Status(String message) {
			this.message = message;
		}

		private String message;

		public String getMessage() {
			return this.message;
		}
	}

	private String serviceName;
	private Status status;
	private String message;
	private String serviceAddress;

	private DateTime age;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public DateTime getAge() {
		return age;
	}

	public void setAge(DateTime age) {
		this.age = age;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
