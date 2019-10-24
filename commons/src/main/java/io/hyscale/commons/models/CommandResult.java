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
