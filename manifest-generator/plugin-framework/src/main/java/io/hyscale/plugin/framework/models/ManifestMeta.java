package io.hyscale.plugin.framework.models;

import org.apache.commons.lang3.StringUtils;

public class ManifestMeta {

	private String kind;
	private String identifier;

	public ManifestMeta(String kind) {
		this.kind = kind;
	}

	public String getKind() {
		return kind;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return !StringUtils.isBlank(identifier) ? identifier + "_" + kind : kind;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
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
		ManifestMeta other = (ManifestMeta) obj;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;

		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;

		return true;
	}
}
