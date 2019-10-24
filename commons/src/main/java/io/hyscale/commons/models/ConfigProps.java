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

import java.io.Serializable;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigProps implements Serializable, Comparable<ConfigProps>, Comparator<ConfigProps> {

	private static final long serialVersionUID = 1L;

	public enum PropertyInputType {
		FILE, PASSWORD, STRING, ENDPOINT
	}

	private String key;
	private PropertyInputType type = PropertyInputType.STRING;
	private String value;
	private int order; // TODO: 26/2/19 remove - Understand this

	public ConfigProps() {
	}

	public ConfigProps(String key, PropertyInputType type, String value) {
		this.key = key;

		this.type = type;
		this.value = value;
	}

	public ConfigProps(String key, String value) {
		this.key = key;
		this.type = PropertyInputType.STRING;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public PropertyInputType getType() {
		return type;
	}

	public void setType(PropertyInputType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigProps other = (ConfigProps) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int compareTo(ConfigProps obj) {
		return this.getOrder() - obj.getOrder();
	}

	@Override
	public int compare(ConfigProps o1, ConfigProps o2) {
		return o1.getOrder() - o2.getOrder();
	}

	@Override
	public String toString() {
		return "ConfigProps{" + "key='" + key + '\'' + ", type=" + type + ", value='" + value + '\'' + ", order='"
				+ order + '\'' + '}';
	}
}
