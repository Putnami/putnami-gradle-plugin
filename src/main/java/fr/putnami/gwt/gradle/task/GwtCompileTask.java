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
package fr.putnami.gwt.gradle.task;

import com.google.common.base.Strings;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.file.UnionFileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.concurrent.Callable;

import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.CompilerOption;
import fr.putnami.gwt.gradle.extension.PutnamiExtension;
import fr.putnami.gwt.gradle.helper.CompileCommandBuilder;

public class GwtCompileTask extends AbstractTask {

	public static final String NAME = "gwtCompile";
	public static final String GWT_SOURCE_CONFIG = "gwtSource";

	private List<String> modules;
	private File war;
	private FileCollection src;

	public GwtCompileTask() {
		setDescription("Compile the GWT modules");

		dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
	}

	@TaskAction
	public void exec() {

		PutnamiExtension putnami = getProject().getExtensions().getByType(PutnamiExtension.class);
		CompilerOption compilerOptions = putnami.getCompile();
		if (!Strings.isNullOrEmpty(putnami.getSourceLevel()) &&
			Strings.isNullOrEmpty(compilerOptions.getSourceLevel())) {
			compilerOptions.setSourceLevel(putnami.getSourceLevel());
		}

		CompileCommandBuilder commandBuilder = new CompileCommandBuilder();
		commandBuilder.configure(getProject(), compilerOptions, getSrc(), getWar(), getModules());
		JavaAction compileAction = commandBuilder.buildJavaAction();
		compileAction.execute(this);
		compileAction.join();
		if (compileAction.exitValue() != 0) {
			throw new RuntimeException("Failed to compile GWT modules");
		}

		getProject().getTasks().getByName(GwtCheckTask.NAME).setEnabled(false);
	}

	public void configure(final Project project, final PutnamiExtension extention) {
		final CompilerOption options = extention.getCompile();
		options.init(project);
		options.setLocalWorkers(evalWorkers(options));
		
		final FileCollection sourceJars = new UnionFileCollection();

		final FileCollection sources = new UnionFileCollection(project.files());
		addSourceSet(sources, sourceJars, project, SourceSet.MAIN_SOURCE_SET_NAME);

		Configuration compileClasspath = project.getConfigurations().getByName(
				JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
		compileClasspath.getAllDependencies().withType(ProjectDependency.class, new Action<ProjectDependency>() {
			@Override
			public void execute(ProjectDependency dep) {
				addSourceSet(sources, sourceJars, dep.getDependencyProject(), SourceSet.MAIN_SOURCE_SET_NAME);
			}
		});

		ConventionMapping mapping = ((IConventionAware) this).getConventionMapping();

		mapping.map("modules", new Callable<List<String>>() {
			@Override
			public List<String> call()  {
				return extention.getModule();
			}
		});
		mapping.map("war", new Callable<File>() {
			@Override
			public File call()  {
				return options.getWar();
			}
		});
		mapping.map("src", new Callable<FileCollection>() {
			@Override
			public FileCollection call()  {
				return sources;
			}
		});
	}

	private void addSourceSet(FileCollection sources, FileCollection sourceJars, Project project, String sourceSetName) {
		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet sourceSet = javaConvention.getSourceSets().getByName(sourceSetName);
		sources.add(project.files(sourceSet.getOutput().getResourcesDir()))
			.add(project.files(sourceSet.getOutput().getClassesDirs()))
			.add(project.files(sourceSet.getAllSource().getSrcDirs()));
	}

	private int evalWorkers(CompilerOption options) {
		long workers = Runtime.getRuntime().availableProcessors();
		OperatingSystemMXBean osMBean = ManagementFactory.getOperatingSystemMXBean();
		if (osMBean instanceof com.sun.management.OperatingSystemMXBean) {
			com.sun.management.OperatingSystemMXBean sunOsMBean = (com.sun.management.OperatingSystemMXBean) osMBean;
			long memPerWorker = 1024L * 1024L * options.getLocalWorkersMem();
			long nbFreeMemInGb = sunOsMBean.getFreePhysicalMemorySize() / memPerWorker;

			if (nbFreeMemInGb < workers) {
				workers = nbFreeMemInGb;
			}
			if (workers < 1) {
				workers = 1;
			}
		}
		return (int) workers;
	}

	@OutputDirectory
	public File getWar() {
		return war;
	}

	@Input
	public List<String> getModules() {
		return modules;
	}

	@InputFiles
	public FileCollection getSrc() {
		return src;
	}
}
