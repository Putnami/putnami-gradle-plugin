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
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.WarPlugin;

import java.io.File;

import fr.putnami.gwt.gradle.PwtLibPlugin;
import fr.putnami.gwt.gradle.action.JavaAction;
import fr.putnami.gwt.gradle.extension.JettyOption;
import fr.putnami.gwt.gradle.util.ResourceUtils;

public class JettyServerBuilder extends JavaCommandBuilder {

	public JettyServerBuilder() {
		setMainClass("org.eclipse.jetty.runner.Runner");
	}

	public void configure(Project project, JettyOption jettyOption, File jettyConf) {
		ConfigurationContainer configs = project.getConfigurations();

		Configuration runtimeConf = configs.getByName(WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME);
		Configuration jettyClassPath = configs.getByName(PwtLibPlugin.CONF_JETTY);

		setPathingJar(jettyOption.getPathingJar());
		configureJavaArgs(jettyOption);

		addClassPath(jettyClassPath.getAsPath());
		addClassPath(runtimeConf.getAsPath());

		if (jettyOption.getLogRequestFile() != null) {
			ResourceUtils.ensureDir(jettyOption.getLogRequestFile().getParentFile());
			addArg("--log", jettyOption.getLogRequestFile());
		}
		if (jettyOption.getLogFile() != null) {
			ResourceUtils.ensureDir(jettyOption.getLogFile().getParentFile());
			addArg("--out", jettyOption.getLogFile());
		}
		addArg("--host", jettyOption.getBindAddress());
		addArg("--port", jettyOption.getPort());
		addArg("--stop-port", jettyOption.getStopPort());
		addArg("--stop-key", jettyOption.getStopKey());

		addArg(jettyConf.getAbsolutePath());
	}

	public JavaAction buildJavaAction() {
		return new JavaAction(this.toJava());
	}
}
