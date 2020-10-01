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

import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FileListCompressor {
	private static final String VAR_PREFIX = "V";
	private static final String SEARCH_STRING = "/modules-2/files-2.1/";
	private static final String SEARCH_STRING_WINDOWS = "\\modules-2\\files-2.1\\";

	private final boolean isWindows;
	private final String searchString;
	private final String pathSeparator;
	
	private final Map<String, String> vars;
	private int varIndex = 0;
	
	
	public FileListCompressor() {
		this.vars = new HashMap<>(1);
		this.varIndex = 0;
		
		this.isWindows = OperatingSystem.current().isWindows();
		this.searchString = isWindows ? SEARCH_STRING_WINDOWS : SEARCH_STRING;
		this.pathSeparator = System.getProperty("path.separator");
	}

	public String compressFileSet(Set<File> fileSet) {
		List<String> files = new ArrayList<>(fileSet.size());
		for (File file : fileSet) {
			files.add(file.getAbsolutePath());
		}
		return compressFileList(files);
	}
	
	public String compressFileList(List<String> fileList) {
		// Get a map of the unique leading parts of the arguments
		// that contain the SEARCH_STRING. These will be set up as
		// environment variables for compiles.
		for (String file : fileList) {
			String prefix = null;
			int found = file.indexOf(searchString);
			if (found >= 0) {
				prefix = file.substring(0, found) + searchString;
				if (!vars.containsKey(prefix)) {
					vars.put(prefix, VAR_PREFIX + varIndex);
					++ varIndex;
				}
			}
		}
		
		List<String> replacedFiles = new ArrayList<>(fileList.size());
		for (String file : fileList) {
			String newFile = file;
			for (Entry<String, String> entry : vars.entrySet()) {
				if (file.startsWith(entry.getKey())) {
					newFile = varReference(entry.getValue()) + file.substring(entry.getKey().length());
					break;
				}
			}
			replacedFiles.add(newFile);
		}
		
		return Joiner.on(pathSeparator).join(replacedFiles);
	}
	
	private String varReference(String var) {
		return isWindows ? ("%" + var + "%") : ("${" + var + "}");
	}
	
	public String[] getEnvironmentVariables() {
		String[] envp = new String[vars.size()];
		int index = 0;
		for (Entry<String, String> entry : vars.entrySet()) {
			envp[index++] = entry.getValue() + "=" + entry.getKey();
		}
		return envp;
	}
	
}
