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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import fr.putnami.gwt.gradle.extension.JavaOption;

public abstract class JavaCommandBuilder {
	
	private static final PathAccumulator NO_PATHING_JAR = new PathAccumulator() {
		private final List<String> paths = new ArrayList<>();
		
		@Override
		public String get() {
			return Joiner.on(File.pathSeparator).join(paths);
		}

		@Override
		public String[] getArray() {
			return paths.toArray(new String[0]);
		}

		@Override
		public void add(String classPath) {
			paths.add(classPath);
		}
		
		@Override
		public void makeJar() throws IOException {
			// noop
		}
	};

	private final List<String> javaArgs = Lists.newArrayList();
	private String mainClass;
	private final List<String> args = Lists.newArrayList();
	private final List<String> separateClassPath = new ArrayList<>();

	private PathAccumulator pathAccumulator;
	
	public JavaCommandBuilder() {
		this.pathAccumulator = NO_PATHING_JAR;
		javaArgs.add("-Dfile.encoding=" + Charset.defaultCharset().name());
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	
	public String getMainClass() {
		return mainClass;
	}

	public void setPathingJar(String pathingJar) {
		if (pathingJar == null) {
			this.pathAccumulator = NO_PATHING_JAR;
		} else {
			this.pathAccumulator = new PathingJarCreator(new File(pathingJar));
		}
	}

	public JavaCommandBuilder addSeparateClassPath(String classPath) {
		this.separateClassPath.add(classPath);
		return this;
	}

	public JavaCommandBuilder addClassPath(String classPath) {
		this.pathAccumulator.add(classPath);
		return this;
	}

	public JavaCommandBuilder addClassPath(Iterable<File> files) {
		for (File file : files) {
			if (file != null && file.exists()) {
				addClassPath(file.getAbsolutePath());
			}
		}
		return this;
	}

	public JavaCommandBuilder addJavaArgs(String javaArg) {
		this.javaArgs.add(javaArg);
		return this;
	}

	public JavaCommandBuilder addArg(String argName) {
		this.args.add(argName);
		return this;
	}

	public JavaCommandBuilder addArg(String argName, File value) {
		if (value != null) {
			this.args.add(argName);
			this.args.add(value.getAbsolutePath());
		}
		return this;
	}

	public JavaCommandBuilder addArg(String argName, Object value) {
		if (value != null) {
			this.args.add(argName);
			this.args.add(value.toString());
		}
		return this;
	}

	public JavaCommandBuilder addArg(String argName, String value) {
		if (!Strings.isNullOrEmpty(value)) {
			this.args.add(argName);
			this.args.add(value);
		}
		return this;
	}

	public void addArgIf(Boolean condition, String ifTrue, String ifFalse) {
		if (condition != null) {
			this.args.add(condition ? ifTrue : ifFalse);
		}
	}

	public void addArgIf(Boolean condition, String value) {
		if (Boolean.TRUE.equals(condition)) {
			this.args.add(value);
		}
	}
	
	public JavaExecutor toJava() {
		try {
			pathAccumulator.makeJar();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<String> fullClassPath = new ArrayList<>();
		fullClassPath.addAll(separateClassPath);
		fullClassPath.add(pathAccumulator.get());
		return new JavaExecutor(null, javaArgs, fullClassPath.toArray(new String[0]), mainClass, args);
	}

	public void configureJavaArgs(JavaOption javaOptions) {
		if (!Strings.isNullOrEmpty(javaOptions.getMinHeapSize())) {
			addJavaArgs("-Xms" + javaOptions.getMinHeapSize());
		}
		if (!Strings.isNullOrEmpty(javaOptions.getMaxHeapSize())) {
			addJavaArgs("-Xmx" + javaOptions.getMaxHeapSize());
		}
		if (!Strings.isNullOrEmpty(javaOptions.getMaxPermSize())) {
			addJavaArgs("-XX:MaxPermSize=" + javaOptions.getMaxPermSize());
		}
		if (!Strings.isNullOrEmpty(javaOptions.getTmpDir())) {
			addJavaArgs("-Djava.io.tmpdir=" + javaOptions.getTmpDir());
		}
		if (!Strings.isNullOrEmpty(javaOptions.getUserDir())) {
			addJavaArgs("-Duser.dir=" + javaOptions.getUserDir());
		}
		if (javaOptions.isDebugJava()) {
			StringBuilder sb = new StringBuilder();
			sb.append("-agentlib:jdwp=server=y,transport=dt_socket,address=");
			sb.append(javaOptions.getDebugPort());
			sb.append(",suspend=");
			sb.append(javaOptions.isDebugSuspend() ? "y" : "n");
			addJavaArgs(sb.toString());
		}
		for (String javaArg : javaOptions.getJavaArgs()) {
			addJavaArgs(javaArg);
		}
	}

}
