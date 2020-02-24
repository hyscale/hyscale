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
package io.hyscale.dockerfile.gen.services.constants;

public class DockerfileGenConstants {

	public static final String DOCKER_FILE_REPO_TMP_FILE_PREFIX = "dockerfileRepo";

	public static final String STACK_IMAGE = "STACK_IMAGE";

	public static final String STACK_AS_SERVICE_IMAGE = "STACK_AS_SERVICE_IMAGE";

	public static final String ARTIFACTS = "artifacts";

	public static final String SHELL_START_FIELD = "SHELL_START";

	public static final String CONFIGURE_SCRIPT_DIR_FIELD = "CONFIGURE_SCRIPT_DIR";

	public static final String CONFIGURE_SCRIPT_FILE_FIELD = "CONFIGURE_SCRIPT_FILE";

	public static final String RUN_SCRIPT_FILE_FIELD = "RUN_SCRIPT_FILE";

	public static final String CONFIGURE_COMMANDS_FIELD = "CONFIGURE_COMMANDS";

	public static final String RUN_COMMANDS_FIELD = "RUN_COMMANDS";

	public static final String SCRIPT_DIR_FIELD = "SCRIPT_DIR";

	public static final String PERMISSION_COMMAND_FIELD = "PERMISSION_COMMAND";
	
	public static final String WINDOWS_NEW_LINE_CHANGE_COMMAND = "sed -i 's/\\r$//'";
	
	public static final String PERMISSION_COMMAND = "chmod -R 755";

}