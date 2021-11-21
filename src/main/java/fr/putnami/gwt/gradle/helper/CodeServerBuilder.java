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

import com.google.common.collect.Lists;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.Collection;
import java.util.List;

import fr.putnami.gwt.gradle.PwtLibPlugin;
import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.DevOption;
import fr.putnami.gwt.gradle.extension.PutnamiExtension;
import fr.putnami.gwt.gradle.task.GwtCompileTask;
import fr.putnami.gwt.gradle.util.ResourceUtils;

public class CodeServerBuilder extends JavaCommandBuilder {

	public CodeServerBuilder() {
		setMainClass("com.google.gwt.dev.codeserver.CodeServer");
	}

	public void configure(Project project, DevOption devOption, Collection<String> modules) {
		ConfigurationContainer configs = project.getConfigurations();
		Configuration sdmConf = configs.getByName(PwtLibPlugin.CONF_GWT_SDM);

		PutnamiExtension putnami = project.getExtensions().getByType(PutnamiExtension.class);

		SourceSet mainSourceSet = project.getConvention()
			.getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		FileCollection sources = project.files(mainSourceSet.getAllJava().getSrcDirs());

		setPathingJar(devOption.getPathingJar());
		configureJavaArgs(devOption);

		addClassPath(mainSourceSet.getOutput().getAsPath());
		addClassPath(mainSourceSet.getAllJava().getSrcDirs());
		addClassPath(mainSourceSet.getCompileClasspath().getAsPath());
		addClassPath(sdmConf.getAsPath());
		
		Configuration gwtSource = project.getConfigurations().getByName(GwtCompileTask.GWT_SOURCE_CONFIG);
		addSeparateClassPath(gwtSource.getAsPath());

		addSrc(sources);
//		addSrc(listProjectDepsSrcDirs(project));
		
		addArg("-bindAddress", devOption.getBindAddress());
		addArgIf(devOption.getFailOnError(), "-failOnError", "-nofailOnError");
		addArgIf(devOption.getPrecompile(), "-precompile", "-noprecompile");
		addArg("-port", devOption.getPort());
		addArgIf(devOption.getStrict(), "-strict");
		addArgIf(devOption.getEnforceStrictResources(), "-XenforceStrictResources ", "-XnoenforceStrictResources");
		addArg("-workDir", ResourceUtils.ensureDir(devOption.getWorkDir()));
		addArgIf(devOption.getIncremental(), "-incremental", "-noincremental");
		addArg("-sourceLevel", devOption.getSourceLevel());
		if (!putnami.getGwtVersion().startsWith("2.6")) {
			addArg("-logLevel", devOption.getLogLevel());
		}
		addArg("-XmethodNameDisplayMode", devOption.getMethodNameDisplayMode());
		addArg("-XjsInteropMode", devOption.getJsInteropMode());
		addArgIf(devOption.getGenerateJsInteropExports(), "-generateJsInteropExports");

		if (devOption.getExtraArgs() != null) {
			for (String arg : devOption.getExtraArgs()) {
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

	private Collection<File> listProjectDepsSrcDirs(Project project) {
		ConfigurationContainer configs = project.getConfigurations();
		Configuration compileConf = configs.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

		final List<File> result = Lists.newArrayList();
		compileConf.getAllDependencies().withType(ProjectDependency.class, new Action<ProjectDependency>() {
			@Override
			public void execute(ProjectDependency dep) {
				Project projectDependency = dep.getDependencyProject();
				if (projectDependency.getPlugins().hasPlugin(JavaPlugin.class)) {
					JavaPluginConvention javaConvention = projectDependency.getConvention().getPlugin(JavaPluginConvention.class);
					SourceSet mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
					
					result.addAll(mainSourceSet.getAllSource().getSrcDirs());
				}
			}
		});
		return result;
	}

	private void addSrc(Iterable<File> sources) {
		for (File srcDir : sources) {
			if (srcDir.isDirectory()) {
				addArg("-src", srcDir);
			}
		}
	}

}
