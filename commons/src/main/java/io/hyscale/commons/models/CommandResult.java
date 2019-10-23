package io.hyscale.commons.models;

public class CommandResult {

	private String commandOutput;
	private Integer exitCode;

	public String getCommandOutput() {
		return commandOutput;
	}

	public void setCommandOutput(String commandOutput) {
		this.commandOutput = commandOutput;
	}

	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	@Override
	public String toString() {
		return "Command Result: \nExit Code: " + exitCode + "\n Output: " + commandOutput;
	}

}
