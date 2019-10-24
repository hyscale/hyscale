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
package io.hyscale.builder.services.constants;

public class DockerImageConstants {

	public static final String IMAGE_PUSH_LOG = "image-push.log";

	public static final String file_separator = System.getProperty("file.separator");

	public static final String BASE_DIR = "/tmp/hyscale-ctl/";

	public static final String DOCKERFILE_DIR = "dockerfiles";

	public static final String END_OF_FILE = "END OF FILE";

	public static final long TAIL_LOG_MAX_WAIT_TIME = 10000l;

}
