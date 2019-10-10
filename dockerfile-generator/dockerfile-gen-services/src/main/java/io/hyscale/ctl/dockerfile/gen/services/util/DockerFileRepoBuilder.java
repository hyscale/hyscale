package io.hyscale.ctl.dockerfile.gen.services.util;

import io.hyscale.ctl.commons.utils.ServiceUtil;
import io.hyscale.ctl.commons.utils.ZipUtils;
import io.hyscale.ctl.dockerfile.gen.services.constants.DockerfileGenConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerFileRepoBuilder {

	// TODO move all logic to finish build instead of appending command every time

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final String DOCKER_FILE_REPO_DEFAULT_BASE_PATH = "/tmp/DockerFileRepo";
	private static final String DOCKER_FILE_NAME = "Dockerfile";
	private static final Object EQUALS = "=";

	private DockerFileRepoContext dockerFileRepoContext;
	private Map<String, String> envs;

	private DockerFileRepoBuilder() {
		dockerFileRepoContext = new DockerFileRepoContext();
		String baseDir = DOCKER_FILE_REPO_DEFAULT_BASE_PATH + FILE_SEPARATOR
				+ ServiceUtil.getRandomKey(DockerfileGenConstants.DOCKER_FILE_REPO_TMP_FILE_PREFIX);
		dockerFileRepoContext.setBaseDir(new File(baseDir));
		dockerFileRepoContext.getBaseDir().mkdirs();
		File file = new File(baseDir + FILE_SEPARATOR + DOCKER_FILE_NAME);
		dockerFileRepoContext.setFile(file);
		dockerFileRepoContext.setDockerFileBuilder(new StringBuilder());
		this.envs = new HashMap<String, String>();
	}

	public DockerFileRepoContext get() {
		return dockerFileRepoContext;
	}

	public static DockerFileRepoBuilder startBuild() {
		DockerFileRepoBuilder repoBuilder = new DockerFileRepoBuilder();
		return repoBuilder;
	}

	public DockerFileRepoBuilder from(String baseImageName, String version) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.FROM).append(baseImageName).append(":").append(version).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder from(String baseImageName) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.FROM).append(baseImageName).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder maintainer(String maintainer) {
		StringBuilder buidler = this.get().getDockerFileBuilder();
		buidler.append(DockerFileComands.MAINTAINER).append(maintainer).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder add(String srcPath, String destPath) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.ADD).append(srcPath).append(" ").append(destPath).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder run(String command) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.RUN).append(command).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder expose(String args) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.EXPOSE).append(args).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder workDir(String workDir) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.WORK_DIR).append(workDir).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder copy(String user, String group, String destPath, String... args) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(DockerFileComands.COPY);
		if (user != null && group != null) {
			builder.append("--chown=").append(user).append(":").append(group).append(" ");
		}
		builder.append(getPathsAsString(args)).append(destPath).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder env(String key, String value) {
		envs.put(key, value);
		return this;
	}

	public DockerFileRepoBuilder addLine(String dockerFileCommand) {
		StringBuilder builder = this.get().getDockerFileBuilder();
		builder.append(dockerFileCommand).append(LINE_SEPARATOR);
		return this;
	}

	public DockerFileRepoBuilder addFile(File srcFile) throws IOException {
		FileUtils.copyFileToDirectory(srcFile, this.get().getBaseDir());
		return this;
	}

	public DockerFileRepoBuilder addFolder(File srcFile) throws IOException {
		FileUtils.copyDirectoryToDirectory(srcFile, this.get().getBaseDir());
		return this;
	}

	public DockerFileRepoBuilder addMultipleFiles(List<File> srcFiles) throws IOException {
		if (srcFiles != null) {
			for (File eachFile : srcFiles) {
				FileUtils.copyFileToDirectory(eachFile, this.get().getBaseDir());
			}
		}
		return this;
	}

	/**
	 *
	 * @param file
	 * @param folderPath
	 * @return
	 * @throws IOException
	 */

	public DockerFileRepoBuilder addFileToFolder(File file, String folderPath) throws IOException {
		File destFolder = new File(this.get().getBaseDir().getAbsolutePath() + folderPath);
		if (file != null) {
			if (!file.exists() && !file.isDirectory()) {
				file.mkdirs();
			}
			FileUtils.copyFileToDirectory(file, destFolder);
		}
		return this;
	}

	/**
	 *
	 * @param content
	 * @param fileName
	 * @return
	 * @throws IOException
	 */

	public DockerFileRepoBuilder addFileByContent(String content, String fileName) throws IOException {
		return addFileByContent(content, fileName, true);
	}

	public DockerFileRepoBuilder addFileByContent(String content, String fileName, boolean addCommonContent)
			throws IOException {
		File file = new File(this.get().getBaseDir().getAbsolutePath() + FILE_SEPARATOR + fileName);
		if (addCommonContent && !file.exists()) {
			content = getCommonScriptContent() + content;
		}
		FileUtils.writeStringToFile(file, content, true);
		return this;
	}

	public DockerFileRepoBuilder addFileByContent(byte[] content, String fileName) throws IOException {
		File file = new File(this.get().getBaseDir().getAbsolutePath() + FILE_SEPARATOR + fileName);
		FileUtils.writeByteArrayToFile(file, content, true);
		return this;
	}

	private String getPathsAsString(String... args) {
		StringBuilder builder = new StringBuilder();
		for (String eachPath : args) {
			builder.append(eachPath).append(" ");
		}
		return builder.toString();
	}

	/**
	 *
	 * @return
	 * @throws IOException
	 */

	public DockerFileRepoBuilder finishBuild() throws IOException {
		// add envs
		StringBuilder builder = this.get().getDockerFileBuilder();
		if (envs != null && !envs.isEmpty()) {
			builder.append(DockerFileComands.ENV);

			for (String key : envs.keySet()) {
				builder.append(key).append(EQUALS).append(envs.get(key)).append(" ");
			}
			builder.append(LINE_SEPARATOR);
		}

		FileUtils.write(this.get().getFile(), this.get().getDockerFileBuilder().toString(), true);
		return this;
	}

	public FileMeta zipDockerFileRepo() throws Exception {
		String fileName = this.get().getBaseDir().getName() + ".zip";
		String zipDestFilePath = DOCKER_FILE_REPO_DEFAULT_BASE_PATH + FILE_SEPARATOR + fileName;
		ZipUtils.zipFolder(this.get().getBaseDir().getAbsolutePath(), zipDestFilePath);
		return new FileMeta(fileName, DOCKER_FILE_REPO_DEFAULT_BASE_PATH);
	}

	private StringBuilder getCommonScriptContent() {
		return new StringBuilder("#!/bin/bash").append(LINE_SEPARATOR).append("set -e").append(LINE_SEPARATOR);
	}

	public class FileMeta {

		private String name;
		private String directory;

		public FileMeta(String name, String directory) {
			this.name = name;
			this.directory = directory;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDirectory() {
			return directory;
		}

		public void setDirectory(String directory) {
			this.directory = directory;
		}
	}

	private class DockerFileRepoContext {
		private File baseDir;
		private File File;
		private StringBuilder dockerFileBuilder;

		public File getFile() {
			return File;
		}

		public void setFile(File file) {
			File = file;
		}

		public StringBuilder getDockerFileBuilder() {
			return dockerFileBuilder;
		}

		public void setDockerFileBuilder(StringBuilder dockerFileBuilder) {
			this.dockerFileBuilder = dockerFileBuilder;
		}

		public File getBaseDir() {
			return baseDir;
		}

		public void setBaseDir(File baseDir) {
			this.baseDir = baseDir;
		}
	}

}
