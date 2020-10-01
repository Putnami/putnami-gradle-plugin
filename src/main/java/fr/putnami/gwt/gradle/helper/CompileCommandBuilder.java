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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;

import java.io.File;
import java.util.Collection;

import fr.putnami.gwt.gradle.PwtLibPlugin;
import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.CompilerOption;
import fr.putnami.gwt.gradle.task.GwtCompileTask;

public class CompileCommandBuilder extends JavaCommandBuilder {

	public CompileCommandBuilder() {
		super();
		setMainClass("com.google.gwt.dev.Compiler");
	}

	public void configure(Project project, CompilerOption compilerOptions, FileCollection sources, File war,
		Collection<String> modules) {
		Configuration sdmConf = project.getConfigurations().getByName(PwtLibPlugin.CONF_GWT_SDM);
		Configuration compileConf = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
		Configuration gwtSource = project.getConfigurations().getByName(GwtCompileTask.GWT_SOURCE_CONFIG);
		
		setPathingJar(compilerOptions.getPathingJar());
		configureJavaArgs(compilerOptions);
		addJavaArgs("-Dgwt.persistentunitcachedir=" + project.getBuildDir() + "/putnami/work/cache");

		for (File sourceDir : sources) {
			addClassPath(sourceDir.getAbsolutePath());
		}

		addClassPath(compileConf.getAsPath());
		addClassPath(sdmConf.getAsPath());
		addSeparateClassPath(gwtSource.getAsPath());

		addArg("-war", war);
		addArg("-extra", compilerOptions.getExtra());
		addArg("-workDir", compilerOptions.getWorkDir());
		addArg("-gen", compilerOptions.getGen());
		addArg("-deploy", compilerOptions.getDeploy());

		addArg("-logLevel", compilerOptions.getLogLevel());
		addArg("-localWorkers", compilerOptions.getLocalWorkers());
		addArgIf(compilerOptions.getStrict(), "-strict");
		addArgIf(compilerOptions.getFailOnError(), "-failOnError", "-nofailOnError");
		addArg("-sourceLevel", compilerOptions.getSourceLevel());
		addArgIf(compilerOptions.getDraftCompile(), "-draftCompile", "-nodraftCompile");
		addArg("-optimize", compilerOptions.getOptimize());
		addArg("-style", compilerOptions.getStyle());
		addArgIf(compilerOptions.getCompileReport(), "-compileReport", "-nocompileReport");
		addArgIf(compilerOptions.getIncremental(), "-incremental");
		addArgIf(compilerOptions.getCheckAssertions(), "-checkAssertions", "-nocheckAssertions");
		addArgIf(compilerOptions.getCheckCasts(), "-XcheckCasts", "-XnocheckCasts");
		addArgIf(compilerOptions.getEnforceStrictResources(), "-XenforceStrictResources",
			"-XnoenforceStrictResources");
		addArgIf(compilerOptions.getClassMetadata(), "-XclassMetadata", "-XnoclassMetadata");

		addArgIf(compilerOptions.getOverlappingSourceWarnings(), "-overlappingSourceWarnings",
			"-nooverlappingSourceWarnings");
		addArgIf(compilerOptions.getSaveSource(), "-saveSource", "-nosaveSource");
		addArg("-XmethodNameDisplayMode", compilerOptions.getMethodNameDisplayMode());

		addArg("-XjsInteropMode", compilerOptions.getJsInteropMode());
		addArgIf(compilerOptions.getGenerateJsInteropExports(), "-generateJsInteropExports");

		if (compilerOptions.getExtraArgs() != null) {
			for (String arg : compilerOptions.getExtraArgs()) {
				if (arg != null && arg.length() > 0) {
					addArg(arg);
				}
			}
		}

		for (String module : modules) {
			addArg(module);
		}
	}

	public JavaAction buildJavaAction() {
		return new JavaAction(this.toJava());
	}
}
