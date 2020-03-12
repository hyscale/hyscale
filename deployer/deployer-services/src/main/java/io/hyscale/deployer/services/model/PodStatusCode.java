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
package io.hyscale.deployer.services.model;

import java.util.EnumMap;

public class PodStatusCode {
	// System Signals
	public static enum Signals {
		SIGKILL(143), SIGTERM(15),FAILURE(1);

		private Integer code = null;

		private Signals(Integer code) {
			this.code = code;
		}

		public Integer getStatusCode() {
			return this.code;
		}

		public static Signals fromCode(Integer code) {
			for (Signals type : values()) {
				if (type.getStatusCode() == code) {
					return type;
				}
			}
			return null;
		}

		public static EnumMap<Signals, String> statusCodeVsMessage = new EnumMap<Signals, String>(Signals.class);

		static {
			statusCodeVsMessage.put(PodStatusCode.Signals.SIGTERM, "SIGTERM");
			statusCodeVsMessage.put(PodStatusCode.Signals.SIGKILL, "SIGKILL");
			statusCodeVsMessage.put(PodStatusCode.Signals.FAILURE, "FAILURE");


		}
	}
}
