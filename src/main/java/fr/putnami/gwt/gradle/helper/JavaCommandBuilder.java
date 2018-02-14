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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.gradle.internal.jvm.Jvm;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import fr.putnami.gwt.gradle.extension.JavaOption;

public abstract class JavaCommandBuilder {

	private final String javaExec;
	private final List<String> javaArgs = Lists.newArrayList();
	private final List<String> classPaths = Lists.newArrayList();
	private String mainClass;
	private final List<String> args = Lists.newArrayList();

	public JavaCommandBuilder() {
		this.javaExec = Jvm.current().getJavaExecutable().getAbsolutePath();
		javaArgs.add("-Dfile.encoding=" + Charset.defaultCharset().name());
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public JavaCommandBuilder addClassPath(String classPath) {
		this.classPaths.add(classPath);
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

//	@Override
//	public String toString() {
//		return StringUtils.join(" ", toStringArray());
//	}

	public String[] toStringArray() {
		List<String> pieces = new ArrayList<String>();
		pieces.add(javaExec);

		for (String arg : javaArgs) {
			pieces.add(arg);
		}

		if (classPaths.size() > 0) {
			pieces.add("-cp");
			int i = 0;
			StringBuilder sb = new StringBuilder();
			for (String classPath : classPaths) {
				if (!Strings.isNullOrEmpty(classPath.trim())) {
					if (sb.length() > 0) {
						sb.append(System.getProperty("path.separator"));
					}
					sb.append(classPath.trim());
					i++;
				}
			}
			pieces.add(sb.toString());
		}

		pieces.add(mainClass);

		for (String arg : args) {
			if (!Strings.isNullOrEmpty(arg)) {
				pieces.add(arg);
			}
		}

		return pieces.toArray(new String[0]);
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
		if (javaOptions.isDebugJava()) {
			StringBuilder sb = new StringBuilder();
			sb.append("-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=");
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
