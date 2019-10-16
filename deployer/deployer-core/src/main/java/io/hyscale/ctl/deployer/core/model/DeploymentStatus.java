package io.hyscale.ctl.deployer.core.model;

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

	private DateTime dateTime;

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

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
