package io.hyscale.ctl.commons.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class TailLogFile implements Runnable {

	private int delayMillis = 100;
	private long lastKnownPosition = 0;
	private boolean run = true;
	private File logFile = null;
	private TailHandler handler = null;

	private static final String READ_ONLY = "r";

	public TailLogFile(File logFile, int wait, TailHandler handler) {
		this.delayMillis = wait;
		this.logFile = logFile;
		this.handler = handler;
	}

	public TailLogFile(File logFile, TailHandler handler) {
		this.logFile = logFile;
		this.handler = handler;
	}

	public void stopRunning() {
		run = false;
	}

	public boolean isRunning() {
		return run;
	}

	private void handle(String line) {
		System.out.println(line);
	}

	@Override
	public void run() {
		try {
			while (run) {
				long fileLength = logFile.length();
				if (fileLength > lastKnownPosition) {

					// Reading
					RandomAccessFile readWriteFileAccess = new RandomAccessFile(logFile, READ_ONLY);
					readWriteFileAccess.seek(lastKnownPosition);
					String fileLine = null;
					while ((fileLine = readWriteFileAccess.readLine()) != null) {
						if (handler == null) {
							handle(fileLine);
							continue;
						}
						handler.handleLine(fileLine);
						if (handler.handleEOF(fileLine)) {
							stopRunning();
						}

					}
					lastKnownPosition = readWriteFileAccess.getFilePointer();
					readWriteFileAccess.close();
				}
				sleep();
			}
		} catch (FileNotFoundException ex) {
			sleep();
		} catch (Exception e) {
			stopRunning();
		}
	}

	private void sleep() {
		try {
			Thread.sleep(delayMillis);
		} catch (InterruptedException e) {
		}
	}

}
