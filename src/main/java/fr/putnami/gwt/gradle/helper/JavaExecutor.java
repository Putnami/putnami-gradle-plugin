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
package fr.putnami.gwt.gradle.helper;

import com.google.common.base.Joiner;

import org.gradle.internal.jvm.Jvm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaExecutor {
	
	private String javaExec;
	private final String entryPoint;
	private List<String> jvmArgs;
	private String[] classPath;
	private List<String> args;
	
	public JavaExecutor(String entryPoint) {
		this.entryPoint = entryPoint;
	}
	
	public JavaExecutor(String javaExec, List<String> jvmArgs, String[] classPath, String entryPoint, List<String> args) {
		this.jvmArgs = jvmArgs;
		this.classPath = classPath;
		this.entryPoint = entryPoint;
		this.args = args;
	}

	public String getJavaExec() {
		return javaExec;
	}

	public void setJavaExec(String javaExec) {
		this.javaExec = javaExec;
	}

	public List<String> getJvmArgs() {
		return jvmArgs;
	}

	public void setJvmArgs(List<String> jvmArgs) {
		this.jvmArgs = jvmArgs;
	}

	public String[] getClassPath() {
		return classPath;
	}

	public void setClassPath(String[] classPath) {
		this.classPath = classPath;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public String getEntryPoint() {
		return entryPoint;
	}
	
	private String[] getCommand() {
		if (javaExec == null) {
			javaExec = Jvm.current().getJavaExecutable().getAbsolutePath();
		}
		
		List<String> allArgs = new ArrayList<>();
		allArgs.add(javaExec);
		allArgs.addAll(jvmArgs);
		allArgs.add("-cp");
		allArgs.add(Joiner.on(File.pathSeparator).join(classPath));
		allArgs.add(entryPoint);
		allArgs.addAll(args);
		
		String[] command = allArgs.toArray(new String[0]);
		
		return command;
	}
	
	public String getCommandLine() {
		return Joiner.on(' ').join(getCommand());
	}
		
	public Process runProcess() throws IOException {
		return Runtime.getRuntime().exec(getCommand());
	}
	
	public void run() throws Exception {
		URL[] urls = new URL[classPath.length];
		int index = 0;
		for (String path : classPath) {
			File file = new File(path);
			if (file.isDirectory() && !path.endsWith("/")) {
				file = new File(path + "/");
			}
			urls[index++] = file.toURI().toURL();
		}
		ClassLoader childLoader = new URLClassLoader(urls, getClass().getClassLoader());
		
		Class<?> mainClass = Class.forName(entryPoint, true, childLoader);
		Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
			
		mainMethod.invoke(null, new Object[]{ args.toArray(new String[0]) });
	}
	
	@Override
	public String toString() {
		return "SandboxedJava [entryPoint=" + entryPoint + ", jvmArgs=" + jvmArgs + ", classPath="
				+ Arrays.toString(classPath) + ", args=" + args + "] (Java is " + javaExec + ")";
	}
	
}
