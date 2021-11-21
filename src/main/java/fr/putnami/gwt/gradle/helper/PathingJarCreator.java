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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class PathingJarCreator implements PathAccumulator {
	private final File resultingFile;
	private final List<String> paths;

	public PathingJarCreator(File resultingFile) {
		this.resultingFile = resultingFile;
		this.paths = new ArrayList<>();
	}
	
	@Override
	public void add(String classPath) {
		paths.addAll(Arrays.asList(classPath.split(System.getProperty("path.separator"))));
	}
	
	@Override
	public String get() {
		return resultingFile.getAbsolutePath();
	}
	
	@Override
	public String[] getArray() {
		return paths.toArray(new String[0]);
	}

	public void makeJar() throws IOException {
		List<String> completed = new ArrayList<>(paths.size());
		Set<String> seen = new HashSet<>(paths.size());
		
		Path myPath = resultingFile.getParentFile().toPath();
		for (String path : paths) {
			if (! seen.contains(path) && !Strings.isNullOrEmpty(path)) {
				File pathFile = new File(path);
				Path relativePath = myPath.relativize(pathFile.toPath());
				String extra = pathFile.isDirectory() ? "/" : "";
				completed.add(relativePath.toString().replace(File.separator, "/") + extra);
				seen.add(path);
			}
		}
		
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
		manifest.getMainAttributes().putValue(Attributes.Name.CLASS_PATH.toString(), Joiner.on(" ").join(completed));
		
		new JarOutputStream(new FileOutputStream(resultingFile), manifest).close();
	}
	
}
