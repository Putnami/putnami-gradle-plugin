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
package fr.putnami.gwt.gradle.extension;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class JavaOption {

	private final List<String> javaArgs = Lists.newArrayList();

	private String tmpDir;
	private String userDir;
	private String pathingJar;
	private String maxHeapSize;
	private String minHeapSize;
	private String maxPermSize;
	private boolean debugJava = false;
	private int debugPort = 8000;
	private boolean debugSuspend = false;

	public List<String> getJavaArgs() {
		return javaArgs;
	}

	public void setJavaArgs(String... javaArgs) {
		this.javaArgs.addAll(Arrays.asList(javaArgs));
	}

	public String getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public String getUserDir() {
		return userDir;
	}

	public void setUserDir(String userDir) {
		this.userDir = userDir;
	}

	public String getPathingJar() {
		return pathingJar;
	}

	public void setPathingJar(String pathingJar) {
		this.pathingJar = pathingJar;
	}

	public String getMaxHeapSize() {
		return maxHeapSize;
	}

	public void setMaxHeapSize(String maxHeapSize) {
		this.maxHeapSize = maxHeapSize;
	}

	public String getMinHeapSize() {
		return minHeapSize;
	}

	public void setMinHeapSize(String minHeapSize) {
		this.minHeapSize = minHeapSize;
	}

	public String getMaxPermSize() {
		return maxPermSize;
	}

	public void setMaxPermSize(String maxPermSize) {
		this.maxPermSize = maxPermSize;
	}

	public boolean isDebugJava() {
		return debugJava;
	}

	public void setDebugJava(boolean debugJava) {
		this.debugJava = debugJava;
	}

	public int getDebugPort() {
		return debugPort;
	}

	public void setDebugPort(int debugPort) {
		this.debugPort = debugPort;
	}

	public void setDebugPort(String debugPort) {
		this.debugPort = Integer.valueOf(debugPort);
	}

	public boolean isDebugSuspend() {
		return debugSuspend;
	}

	public void setDebugSuspend(boolean debugSuspend) {
		this.debugSuspend = debugSuspend;
	}

	public void setDebugSuspend(String debugSuspend) {
		this.debugSuspend = Boolean.valueOf(debugSuspend);
	}
}
