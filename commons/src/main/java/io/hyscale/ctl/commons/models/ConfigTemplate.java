package io.hyscale.ctl.commons.models;

import java.io.File;

public class ConfigTemplate {

	private String rootPath;
	private String templateName;

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getTemplatePath() {
		return rootPath + File.separator + templateName;
	}

}
