/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.gwt.gradle.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;

import com.google.common.base.Joiner;

public class JavaAction implements Action<Task> {

	public static class ProcessLogger extends Thread {
		private InputStream stream;
		private LogLevel level;
		private boolean quit = false;

		public void setStream(InputStream stream) {
			this.stream = stream;
		}

		public void setLevel(LogLevel level) {
			this.level = level;
		}

		@Override
		public void run() {
			try (BufferedReader input = new BufferedReader(new InputStreamReader(stream))) {
				String line = input.readLine();
				while (!quit && line != null) {
					printLine(line);
					line = input.readLine();
				}
				stream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		protected void printLine(String line) {
			if (level == LogLevel.ERROR) {
				System.err.println(line);
			} else {
				System.out.println(line);
			}
		}

		public void quitLogger() {
			quit = true;
		}
	}

	private final String[] javaCommand;

	private Process process;

	private ProcessLogger errorLogger = new ProcessLogger();
	private ProcessLogger infoLogger = new ProcessLogger();

	public JavaAction(String[] javaCommand) {
		super();
		this.javaCommand = javaCommand;
	}

	@Override
	public void execute(Task task) {
		try {
			task.getLogger().info(Joiner.on(" ").join(javaCommand));
			process = Runtime.getRuntime().exec(javaCommand);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					kill();
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		errorLogger.setStream(process.getErrorStream());
		errorLogger.setLevel(LogLevel.ERROR);
		errorLogger.start();
		infoLogger.setStream(process.getInputStream());
		errorLogger.setLevel(LogLevel.INFO);
		infoLogger.start();
	}

	public void setErrorLogger(ProcessLogger errorLogger) {
		this.errorLogger = errorLogger;
	}

	public void setInfoLogger(ProcessLogger infoLogger) {
		this.infoLogger = infoLogger;
	}

	public void kill() {
		errorLogger.quitLogger();
		infoLogger.quitLogger();
		process.destroy();
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void join() {
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public int exitValue() {
		return process.exitValue();
	}

	public boolean isAlive() {
		try {
			process.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

}
