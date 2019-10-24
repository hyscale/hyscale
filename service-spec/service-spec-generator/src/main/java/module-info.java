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
module service_spec_generator {
	requires jackson.annotations;
	requires org.glassfish.java.json;
	requires json.path;
	requires service_spec_commons;
	requires com.fasterxml.jackson.databind;
	requires commons;
	requires java.sql;
	requires com.fasterxml.jackson.core;
	requires org.apache.commons.lang3;

	exports io.hyscale.servicespec.generator;
	exports io.hyscale.servicespec.model;
}