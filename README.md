Putnami Gradle Plugin
===================

Travis build status [![Build Status](https://travis-ci.org/Putnami/putnami-gradle-plugin.svg?branch=master)](https://travis-ci.org/Putnami/putnami-gradle-plugin)

===================

This plugin helps to build GWT project with Gradle. The main goals is build webapps and libraries.

**Why to use it?**

* DevMode on standalone Jetty 9 container.
* DevMode works with multi module project. 
* Ease of use.
* Gradle is faster and easier than Maven
 
## Requirements ##

* Java 7 or higher.
* Gradle 2 or higher. https://gradle.org

## Quick start ##

* First you need java and gradle installed on your workstation. 
* Init your project with this structure

```
/ build.gradle
/ src
  |-- main
    |-- java
    |-- resources
    |-- webapp
  |-- test
    |-- java
    |-- resources
```

* You can init the project with those commands

```sh
PROJECT_NAME={your-project}
mkdir $PROJECT_NAME
cd $PROJECT_NAME
mkdir -p src/main/java src/main/resources src/main/webapp
mkdir -p src/test/java src/test/resources
touch build.gradle
```


* Apply the plugin **fr.putnami.gwt** in your *build.gradle* file

```groovy
apply plugin: 'fr.putnami.gwt'

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'fr.putnami.gwt:putnami-gradle-plugin:0.1.2'
	}
}

repositories {
	mavenCentral()
}

putnami{
	module 'your.gwt.module.to.compile'
}
```

* Then use the commands below:
  * `gradle gwtRun` To run the webapp
  * `gradle gwtDev` To run the DevMode
  * `gradle build`  To compile GWT and Build the webapp


**Notes**

To use the lastest **snapshot** you can add `maven{ url 'https://oss.sonatype.org/content/repositories/snapshots/' }` to the buildscript repositories.

### On eclipse ###

* Get [Gradle Integration for Eclipse](http://marketplace.eclipse.org/content/gradle-integration-eclipse-44)
* Go to the eclipse navigator by opening eclipse and going to Window>Show View>Navigator
* Create a new java project
* Add a file to the project and name it build.gradle
* In the src folder add the following directory structure:
```
/ src
  |-- main
    |-- java
    |-- webapp
```

* In the build .gradle file put:
```
apply plugin: 'fr.putnami.gwt'
apply plugin: 'eclipse'

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath 'fr.putnami.gwt:putnami-gradle-plugin:0.1.2'
	}
}

repositories {
	mavenCentral()
}

putnami{
	module 'your.gwt.module.to.compile'
}
```

* right click on the project and configure>convert to Gradle project
* Go to Window>Show View>Other>gradle tasks. In the gradle tasks window double click eclipse.
* Make sure that the GPE(Google Plugin for Eclipse) is installed.
* Right click on your project and go to Properties>google>Web Application
* Check the check box labeled this project has a war directory and select src/main/webapp as your war directory.
* Then go to the properties for web toolkit. Check the check box labeled use Google Web Toolkit. Make sure not to use an SDK from before 2.7.0. Add all of your gwt entry point modules. To tell the plugin to gwt compile right click your project and go to google>gwt compile, add all of your modules and click compile. 


## Samples ##

We offer you 3 build samples:

* **Library project** : Simple library project [pgp-sample-lib](https://github.com/Putnami/putnami-gradle-plugin/tree/master/samples/pgp-sample-lib).
* **WebApp project** : Simple GWT project [pgp-sample-webapp](https://github.com/Putnami/putnami-gradle-plugin/tree/master/samples/pgp-sample-webapp).
* **Multi-modules project** : Project composed with a library and a webapp [pgp-sample-multimodules](https://github.com/Putnami/putnami-gradle-plugin/tree/master/samples/pgp-sample-multimodules).

## Advanced usages ##

### Plugins ###

The Putnami Gradle Plugin is usefull either to build GWT's library or webapp.

* apply plugin: **'fr.putnami.gwt-lib'** : To build a library. This plugin installs properly all the GWT dependencies on your project and adds the java sources into the target jar artifact. 
* apply plugin: **'fr.putnami.gwt'** : To build a webapp. This plugin installs properly all the GWT dependencies and provide all the usefull tasks helping you to work efficiently on your projects. 

### Tasks and configuration ###

* Common configuration

```groovy
putnami{
	/** Module to compile, can be multiple */
	module 'fr.putnami.gradle.sample.multimodule.app.App'
	
	/** GWT version */
	gwtVersion = "2.7.0"
	/** Add the gwt-servlet lib */
	gwtServletLib = true
	/** Add the gwt-elemental lib */
	gwtElementalLib = false
	/** Jetty version */
	jettyVersion = "9.2.7.v20150116"
}
```

* **gwtCompile** Compile the GWT webapp.

Can be tuned with the following parametters:

```groovy
putnami{
	compile {
		/** The level of logging detail (ERROR, WARN, INFO, TRACE, DEBUG, SPAM, ALL) */
		logLevel = INFO
		/** Compile a report that tells the "Story of Your Compile". */
		compileReport = true
		/** Compile quickly with minimal optimizations. */
		draftCompile = true
		/** Include assert statements in compiled output. */
		checkAssertions = false
		/** Script output style. (OBF, PRETTY, DETAILED)*/
		style = "OBF"
		/** Sets the optimization level used by the compiler. 0=none 9=maximum. */
		optimize = 5
		/** Fail compilation if any input file contains an error. */
		failOnError = false
		/** Specifies Java source level. ("1.6", "1.7")*/
		sourceLevel = "1.7"
		/** The number of local workers to use when compiling permutations. */
		localWorkers = 2
		/** The number of local workers to use when compiling permutations. */
		localWorkersMem = 2048
		/** Emit extra information allow chrome dev tools to display Java identifiers in many places instead of JavaScript functions. (NONE, ONLY_METHOD_NAME, ABBREVIATED, FULL)*/
		methodNameDisplayMode = "NONE"
		/** Specifies JsInterop mode, either NONE, JS, or CLOSURE. */
		jsInteropMode = "NONE"
		
		/** Java args */
		maxHeapSize="1024m"
		minHeapSize="512m"
		maxPermSize="128m"
		debugJava = true
		debugPort = 8000
		debugSuspend = false
		javaArgs = "-Xmx256m", "-Xms256m"
	}
}
```

* **gwtRun** Compile the GWT webapp and run the jar on Jetty 9


**Note :** This task depends on the gwtCompile task.

Can be tuned with the following parametters:

```groovy
putnami{
	jetty {
		/** interface to listen on. */
		bindAddress = "127.0.0.1"
		/** request log filename. */
		logRequestFile
		/** info/warn/debug log filename. */
		logFile
		/** port to listen on. */
		port = 8080
		/** port to listen for stop command. */
		stopPort = 8089
		/** security string for stop command. */
		stopKey = "JETTY-STOP"
		
		/** Java args */
		maxHeapSize="1024m"
		minHeapSize="512m"
		maxPermSize="128m"
		debugJava = true
		debugPort = 8000
		debugSuspend = false
		javaArgs = "-Xmx256m", "-Xms256m"
	}
}
```

* **gwtDev** Run the CodeServer (SDM) and Jetty 9 

The SDM is tuned with the following parametters, the jetty is configured from the previous configuration.

```groovy
putnami{
	dev {
		/** The ip address of the code server. */
		bindAddress = "127.0.0.1"
		/** Stop compiling if a module has a Java file with a compile error, even if unused. */
		failOnError = false
		/** Precompile modules. */
		precompile = false
		/** The port where the code server will run. */
		port = 9876
		/** EXPERIMENTAL: Don't implicitly depend on "client" and "public" when a module doesn't define anydependencies. */
		enforceStrictResources = false
		/** Specifies Java source level ("1.6", "1.7").
		sourceLevel = "1.6"
		/** The level of logging detail (ERROR, WARN, INFO, TRACE, DEBUG, SPAM, ALL) */
		logLevel = "INFO"
		/** Specifies JsInterop mode (NONE, JS, CLOSURE). */
		jsInteropMode = "JS"
		/** Emit extra information allow chrome dev tools to display Java identifiers in many placesinstead of JavaScript functions. (NONE, ONLY_METHOD_NAME, ABBREVIATED, FULL) */
		methodNameDisplayMode = "NONE"
		/** shown all compile errors */
        strict = false
        /** disable this internal server */
        noServer = false
		
		/** shown all compile errors **/
		strict = false
		
		/** disable this internal server **/
		noServer = false
		
		
		/** Java args */
		maxHeapSize="1024m"
		minHeapSize="512m"
		maxPermSize="128m"
		debugJava = true
		debugPort = 8000
		debugSuspend = false
		javaArgs = "-Xmx256m", "-Xms256m"
	}
}
```


## Help and Contribute ##
We need you! Any help is welcome. And there is many ways to help us:

### Be a nice community member ###
If you tried and you love PWT. We will be glad to count you as community members. So please :

* Star this project and become one famous [stargazers](https://github.com/Putnami/putnami-gradle-plugin/stargazers)
* Become a follower on [twitter](https://twitter.com/PutnamiTeam)
* Join the news group [putnami-web-toolkit](https://groups.google.com/forum/#!forum/putnami-web-toolkit)

Of course you will get all our thankfulness if you blog, share and spread PWT around you. All the backlinks on [http://putnami.org](http://putnami.org) are very welcome.

### Report issues ###

All issues about bugs or enhancement are precious and will be given careful consideration. We're going to help you in the best delay as can (generally in the day time).

To report an issue, please use the project [issue tracker](https://github.com/Putnami/putnami-gradle-plugin/issues)

### Contribute ###

You'd love to contribute your code, nice :)
First be sure that your code respect the project code style and formating. 
All the documentation is [here](https://github.com/Putnami/putnami-gradle-plugin/blob/master/settings/README.md)

If you are an eclipse user, it could be nice if you follow the workspace setup instructions.

Every pullrequest will be review with a great consideration, and with a full open mind. 


## License ##
The framework is delivered under LGPL v 3.0.

The LGPL v 3.0 allows a free usage of PWT for commercial and open source projects.
We equally guarantees that PWT is and will open source for ever.

PWT doesn't affect the license of your application. Using PWT is free of charge so fill free to use and integrate it.

You can have a look at the licence details on a https://www.gnu.org/licenses/lgpl-3.0.txt.


---

---

We hope that this plugin will help you to build great apps. 

Best regards.

[@PutnamiTeam](https://github.com/putnami)
