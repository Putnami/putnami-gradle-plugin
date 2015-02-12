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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import fr.putnami.gwt.gradle.PwtLibPlugin;
import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.CodeStyle;
import fr.putnami.gwt.gradle.extension.CompilerOptions;
import fr.putnami.gwt.gradle.extension.JsInteropMode;
import fr.putnami.gwt.gradle.extension.LogLevel;
import fr.putnami.gwt.gradle.extension.MethodNameDisplayMode;
import fr.putnami.gwt.gradle.extension.PutnamiExtension;
import fr.putnami.gwt.gradle.util.JavaCommandBuilder;

public class GwtCompileTask extends AbstractTask {

	public static final String NAME = "gwtCompile";

	private List<String> modules;

	private File war;
	private File work;
	private File gen;
	private File deploy;
	private File extra;
	private File missingDepsFile;
	private File saveSourceOutput;

	private LogLevel logLevel;
	private boolean compileReport;
	private boolean draftCompile;
	private boolean checkAssertions;
	private boolean incremental;
	// private String namespace;
	private CodeStyle style;
	private int optimize;
	private boolean overlappingSourceWarnings;
	private boolean saveSource;
	private boolean failOnError;
	private boolean validateOnly;
	private String sourceLevel;
	private int localWorkers;
	private MethodNameDisplayMode methodNameDisplayMode;
	private boolean enforceStrictResources;
	private boolean checkCasts;
	private boolean classMetadata;
	private boolean closureCompiler;
	private JsInteropMode jsInteropMode;

	public GwtCompileTask() {
		setName(NAME);
		setDescription("Compile the GWT modules");

		dependsOn(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaPlugin.PROCESS_RESOURCES_TASK_NAME);

	}

	@TaskAction
	public void exec() {
		Configuration sdmConf = getProject().getConfigurations().getByName(PwtLibPlugin.CONF_GWT_SDM);
		Configuration compileConf = getProject().getConfigurations().getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME);

		PutnamiExtension putnami = getProject().getExtensions().getByType(PutnamiExtension.class);

		JavaCommandBuilder builder = new JavaCommandBuilder();
		builder.configureJavaArgs(putnami.getDev());
		builder.addJavaArgs("-Dgwt.persistentunitcache=false");

		builder.setMainClass("com.google.gwt.dev.Compiler");

		builder.addClassPath("src/main/java");
		builder.addClassPath("src/main/resources");
		builder.addClassPath(compileConf.getAsPath());
		builder.addClassPath(sdmConf.getAsPath());

		builder.addArg("-workDir", getWork());
		builder.addArg("-gen", getGen());
		builder.addArg("-war", getWar());
		builder.addArg("-deploy", getDeploy());
		builder.addArg("-extra", getExtra());

		builder.addArg("-logLevel", getLogLevel());
		builder.addArg("-localWorkers", getLocalWorkers());
		builder.addArgIf(isFailOnError(), "-failOnError", "-nofailOnError");
		builder.addArg("-sourceLevel", getSourceLevel());
		builder.addArgIf(isDraftCompile(), "-draftCompile", "-nodraftCompile");
		builder.addArg("-optimize", getOptimize());
		builder.addArg("-style", getStyle());
		builder.addArgIf(isCompileReport(), "-compileReport", "-nocompileReport");

		if (isIncremental()) {
			builder.addArg("-incremental");
//			builder.addArg("-incrementalCompileWarnings");
		}


		builder.addArgIf(isCheckAssertions(), "-checkAssertions", "-nocheckAssertions");
		builder.addArgIf(isCheckCasts(), "-XcheckCasts", "-XnocheckCasts");
		builder.addArgIf(isEnforceStrictResources(), "-XenforceStrictResources", "-XnoenforceStrictResources");
		builder.addArgIf(isClassMetadata(), "-XclassMetadata", "-XnoclassMetadata");

		builder.addArgIf(isOverlappingSourceWarnings(), "-overlappingSourceWarnings",
			"-nooverlappingSourceWarnings");
		builder.addArgIf(isSaveSource(), "-saveSource", "-nosaveSource");
		builder.addArg("-XmethodNameDisplayMode", getMethodNameDisplayMode());

		builder.addArgIf(isClosureCompiler(), "-XclosureCompiler", "-XnoclosureCompiler");

		builder.addArg("-XjsInteropMode", getJsInteropMode());


		for (String module : getModules()) {
			builder.addArg(module);
		}

		JavaAction compileAction = new JavaAction(builder.toString());
		compileAction.execute(this);
		compileAction.join();
		if (compileAction.exitValue() != 0) {
			throw new RuntimeException("Fail to compile GWT modules");
		}
	}

	public void configure(final Project project, final PutnamiExtension extention) {
		final CompilerOptions options = extention.getCompile();

		final File buildDir = new File(project.getBuildDir(), "putnami");

		options.setWar(new File(buildDir, "out"));
		options.setWorkDir(new File(buildDir, "work"));
		options.setGen(new File(buildDir, "extra/gen"));
		options.setDeploy(new File(buildDir, "extra/deploy"));
		options.setExtra(new File(buildDir, "extra"));
		options.setSaveSourceOutput(new File(buildDir, "extra/source"));
		options.setMissingDepsFile(new File(buildDir, "extra/missingDepsFile"));
		options.localWorkers(Runtime.getRuntime().availableProcessors());

		ConventionMapping convention = ((IConventionAware) this).getConventionMapping();

		convention.map("modules", new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return extention.getModule();
			}
		});

		convention.map("work", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return options.getWorkDir();
			}
		});
		convention.map("gen", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return options.getGen();
			}
		});
		convention.map("war", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return options.getWar();
			}
		});
		convention.map("deploy", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return options.getDeploy();
			}
		});
		convention.map("extra", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return options.getExtra();
			}
		});
		convention.map("logLevel", new Callable<LogLevel>() {
			@Override
			public LogLevel call() throws Exception {
				return options.getLogLevel();
			}
		});
		convention.map("compileReport", new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				return options.isCompileReport();
			}
		});
		convention.map("draftCompile", new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				return options.isDraftCompile();
			}
		});
		convention.map("checkAssertions", new
			Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return options.isCheckAssertions();
				}
			});
		convention.map("missingDepsFile", new Callable<File>()
		{
			@Override
			public File call() throws Exception {
				return options.getMissingDepsFile();
			}
		});
		convention.map("optimize", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return options.getOptimize();
			}
		});
		convention.map("overlappingSourceWarnings", new
			Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return options.isOverlappingSourceWarnings();
				}
			});
		convention.map("saveSource", new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return options.isSaveSource();
			}
		});
		convention.map("style", new Callable<CodeStyle>() {
			@Override
			public CodeStyle call() throws Exception {
				return options.getStyle();
			}
		});
		convention.map("failOnError", new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return options.isFailOnError();
			}
		});
		convention.map("validateOnly", new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				return options.isValidateOnly();
			}
		});
		convention.map("sourceLevel", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return options.getSourceLevel();
			}
		});
		convention.map("localWorkers", new Callable<Integer>()
		{
			@Override
			public Integer call() throws Exception {
				return options.getLocalWorkers();
			}
		});
		convention.map("incremental", new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return options.isIncremental();
			}
		});
		convention.map("saveSourceOutput", new Callable<File>()
		{
			@Override
			public File call() throws Exception {
				return options.getSaveSourceOutput();
			}
		});
		convention.map("methodNameDisplayMode",
			new Callable<MethodNameDisplayMode>() {
				@Override
				public MethodNameDisplayMode call() throws Exception {
					return options.getMethodNameDisplayMode();
				}
			});
		convention.map("enforceStrictResources", new
			Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return options.isEnforceStrictResources();
				}
			});
		convention.map("checkCasts", new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return options.isCheckCasts();
			}
		});
		convention.map("classMetadata", new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				return options.isClassMetadata();
			}
		});
		convention.map("closureCompiler", new
			Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return options.isClosureCompiler();
				}
			});
		convention.map("jsInteropMode", new
			Callable<JsInteropMode>() {
				@Override
				public JsInteropMode call() throws Exception {
					return options.getJsInteropMode();
				}
			});
	}

	@OutputDirectory
	public File getWar() {
		return war;
	}

	@OutputDirectory
	public File getWork() {
		return work;
	}

	@OutputDirectory
	public File getGen() {
		return gen;
	}

	@OutputDirectory
	public File getDeploy() {
		return deploy;
	}

	@OutputDirectory
	public File getExtra() {
		return extra;
	}

	@OutputFile
	public File getMissingDepsFile() {
		return missingDepsFile;
	}

	@OutputDirectory
	public File getSaveSourceOutput() {
		return saveSourceOutput;
	}

	@Input
	public List<String> getModules() {
		return modules;
	}

	@Input
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Input
	public boolean isCompileReport() {
		return compileReport;
	}

	@Input
	public boolean isDraftCompile() {
		return draftCompile;
	}

	@Input
	public boolean isCheckAssertions() {
		return checkAssertions;
	}

	// @Input
	// public String getNamespace() {
	// return namespace;
	// }

	@Input
	public CodeStyle getStyle() {
		return style;
	}

	@Input
	public int getOptimize() {
		return optimize;
	}

	@Input
	public boolean isOverlappingSourceWarnings() {
		return overlappingSourceWarnings;
	}

	@Input
	public boolean isSaveSource() {
		return saveSource;
	}

	@Input
	public boolean isFailOnError() {
		return failOnError;
	}

	@Input
	public boolean isValidateOnly() {
		return validateOnly;
	}

	@Input
	public String getSourceLevel() {
		return sourceLevel;
	}

	@Input
	public int getLocalWorkers() {
		return localWorkers;
	}

	@Input
	public boolean isIncremental() {
		return incremental;
	}

	@Input
	public MethodNameDisplayMode getMethodNameDisplayMode() {
		return methodNameDisplayMode;
	}

	@Input
	public boolean isEnforceStrictResources() {
		return enforceStrictResources;
	}

	@Input
	public boolean isCheckCasts() {
		return checkCasts;
	}

	@Input
	public boolean isClassMetadata() {
		return classMetadata;
	}

	@Input
	public boolean isClosureCompiler() {
		return closureCompiler;
	}

	@Input
	public JsInteropMode getJsInteropMode() {
		return jsInteropMode;
	}

}