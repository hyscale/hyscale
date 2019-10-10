package io.hyscale.ctl.plugin;

import javax.validation.constraints.NotNull;

/**
 *
 */
public class ManifestSnippet {

	/**
	 * snippet : should be yaml formatted string
	 */
	private String snippet;
	private String kind;
	private String path;
	private String name;
	private SnippetType type;

	@NotNull
	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	@NotNull
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	@NotNull
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SnippetType getType() {
		return type != null ? type : SnippetType.JSON;
	}

	public void setType(SnippetType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public enum SnippetType {
		JSON, YAML;
	}

	@Override
	public String toString() {
		return "ManifestSnippet{" + "kind='" + kind + '\'' + ", path='" + path + '\'' + ", name='" + name + '\''
				+ ", type=" + type + '}';
	}
}
