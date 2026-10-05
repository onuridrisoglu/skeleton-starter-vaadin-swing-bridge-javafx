# Vaadin Swing Bridge: JavaFX starter

## Overview
This minimal skeleton shows how to use the [**JavaFX Extension for Vaadin Swing Bridge**](https://github.com/vaadin/vaadin-javafx-bridge) to run an existing JavaFX application inside a **Vaadin** application, so that end users can reach it through the web in no time.

It runs **WarehousePro**, a sample JavaFX inventory application, whose jar is in `applibs/`. Every browser session gets its own instance of the application. Sign in with `admin / admin` or `manager / manager`.

This project uses Maven for managing the dependencies and configurations.
Similar to any other Vaadin project, you can run it from the command line with `mvn clean spring-boot:run`.
For production builds, see the [Production Build](#production-build) section.

To keep the initial setup simple, the bridge assumes a few things are in place, mostly where the JavaFX application's jar file(s) are and where JavaFX itself comes from.
Please read [Important Directories](#important-directories) for a smooth experience. :+1:

## Important Directories

### applibs
This is the directory that is automatically scanned for the jar files of the JavaFX application.
JavaFX applications are packaged in different ways, from one fat jar to a light main jar with its dependencies next to it.
In any case, all the jar files should be in this directory so that they are available to the bridge.
**Do not put JavaFX itself (the `javafx-*` jars) here**: the bridge provides JavaFX, see below.

> **_NOTE:_** The `applibs` name of this directory is important and case-sensitive.
If you wish to change it, you can pass the path to a directory that contains all the jar files with `-Dapplibs.dir` on the command line.
It is also possible to configure this in the `pom.xml`. There is a commented placeholder for this in the `<systemPropertyVariables>` section of the `pom.xml`.

### Swing Bridge
The JavaFX extension builds on Vaadin Swing Bridge, so `pom.xml` declares both: `swing-bridge-flow` (Swing Bridge, version `swing-bridge.version`) and `swing-bridge-javafx-flow` (the extension). Swing Bridge is a commercial product and needs a Swing Bridge license.

Keep `swing-bridge.version` at the version the extension release was built against (1.3.0 for this release): the extension uses Swing Bridge internals that can change between versions.

Unlike the Swing starter, no `java.desktop` patch and no Swing Bridge jars on the boot class path are needed; JavaFX does not render through AWT.

### target/javafx-modules and target/javafx-bridge
The build (`process-resources` phase, so any `mvn spring-boot:run` or `mvn package`) copies the JavaFX jars for your operating system to `target/javafx-modules`, and the bridge's two JVM-level jars to `target/javafx-bridge`. The JVM flags point at these directories.

JavaFX is not bundled into the application: it has to be on the JVM's **module path**, because the bridge patches the `javafx.graphics` module. That is also why it is in `provided` scope and excluded from the Spring Boot jar.

## Prerequisites
- **JDK 21 or later.**
- **OpenJFX** comes from Maven Central; nothing to install. The version is the `openjfx.version` property in `pom.xml`: 21.x works on JDK 21 and newer, while OpenJFX 23 and newer need JDK 23 or newer.
- **Build on the platform you run on.** The OpenJFX jars are platform-specific (they contain native libraries), and a Maven profile picks them for the operating system the build runs on. To build for another platform, pass `-Djavafx.platform=linux` (or `linux-aarch64`, `mac`, `mac-aarch64`, `win`).
- **No native windows.** The bridge renders JavaFX into the browser with JavaFX's software renderer (`-Dprism.order=sw`) and its own windowing platform (`-Dglass.platform=Vaadin`); the application never opens a window on the server.

Maven itself does not need to be installed: the project ships the Maven wrapper, so `./mvnw` (or `mvnw.cmd` on Windows) works out of the box. The commands below use `mvn`; substitute `./mvnw` if you prefer the wrapper.

## How to Run
The `pom.xml` is configured with the necessary command-line arguments and configurations, so you can run it in development mode simply by running:
```
mvn clean spring-boot:run
```

Then open http://localhost:8888. To attach a debugger, connect your IDE to `localhost:5005`.

## Running your own JavaFX application
1. Replace the jar in `applibs/` with your application's jar(s), without JavaFX.
2. Set `MAIN_CLASS` in `src/main/java/com/example/javafxbridge/ui/WarehouseView.java` to your `javafx.application.Application` subclass.
3. If your application uses JavaFX modules beyond `javafx.controls` (for example `javafx.fxml` or `javafx.web`), add them as `provided` dependencies in `pom.xml` and to `--add-modules` in the JVM flags.

Keep in mind that every user's application runs in the same JVM:
- Never call `System.exit()` or `Platform.exit()`; close the window instead.
- The primary stage fills the page in the browser; any other stage opens as a dialog.

See [Writing applications for the bridge](https://github.com/vaadin/vaadin-javafx-bridge#writing-applications-for-the-bridge) for the details.

## Configuring the bridge
The bridge reads its optional settings from JVM system properties. The `<systemPropertyVariables>` section of the `pom.xml` lists them as commented-out entries with a one-line explanation each: screen size, HiDPI rendering, keyboard focus, frame interval, and the launch-failure error view. Uncomment what you need.

All settings are described in the extension's [Configuration](https://github.com/vaadin/vaadin-javafx-bridge#configuration) section. `swingbridge.screenSize`, `swingbridge.hidpi` and `swingbridge.errorReporting.enabled` work as in Swing Bridge; see also the [Swing Bridge documentation](https://vaadin.com/docs/latest/tools/modernization-toolkit/swing-bridge/configuration).

## Production Build
Similar to any Spring Boot Vaadin application, you can build it with:
```
mvn clean package
```

The result is a Spring Boot fat jar in `target/`, next to the `target/javafx-modules` and `target/javafx-bridge` directories the build also creates. Run it from the project directory with the commands below; to deploy elsewhere, copy the jar, both directories and `applibs` together and adjust the paths.

> **_NOTE:_** These flags are the same set the `pom.xml` passes to `spring-boot:run`, minus the debug agent. Keep the two in sync if you change one.

**Linux/macOS:**
```bash
java \
  --module-path target/javafx-modules \
  --add-modules javafx.controls \
  --patch-module javafx.graphics=target/javafx-bridge/swing-bridge-javafx-patch.jar \
  -Xbootclasspath/a:target/javafx-bridge/swing-bridge-javafx-graphics.jar \
  --add-reads javafx.graphics=ALL-UNNAMED,java.logging \
  --add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.glass.ui.vaadin=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.glass.events=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED \
  --add-exports=javafx.graphics/com.sun.prism=ALL-UNNAMED \
  --enable-native-access=javafx.graphics \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  -Dglass.platform=Vaadin \
  -Dprism.order=sw \
  -XX:+UseStringDeduplication \
  -Dapplibs.dir=./applibs \
  -jar target/skeleton-starter-vaadin-swing-bridge-javafx-1.0-SNAPSHOT.jar
```

**Windows (Command Prompt):**
```cmd
java ^
  --module-path target\javafx-modules ^
  --add-modules javafx.controls ^
  --patch-module javafx.graphics=target\javafx-bridge\swing-bridge-javafx-patch.jar ^
  -Xbootclasspath/a:target\javafx-bridge\swing-bridge-javafx-graphics.jar ^
  --add-reads javafx.graphics=ALL-UNNAMED,java.logging ^
  --add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED ^
  --add-exports=javafx.graphics/com.sun.glass.ui.vaadin=ALL-UNNAMED ^
  --add-exports=javafx.graphics/com.sun.glass.events=ALL-UNNAMED ^
  --add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED ^
  --add-exports=javafx.graphics/com.sun.prism=ALL-UNNAMED ^
  --enable-native-access=javafx.graphics ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  -Dglass.platform=Vaadin ^
  -Dprism.order=sw ^
  -XX:+UseStringDeduplication ^
  -Dapplibs.dir=.\applibs ^
  -jar target\skeleton-starter-vaadin-swing-bridge-javafx-1.0-SNAPSHOT.jar
```

`-XX:+UseStringDeduplication` is optional but recommended: every user's copy of the application holds many equal strings, which the garbage collector then merges.

## Learn more
- [JavaFX Extension for Vaadin Swing Bridge](https://github.com/vaadin/vaadin-javafx-bridge): features, configuration and known limitations.
- [Vaadin Swing Bridge documentation](https://vaadin.com/docs/latest/tools/modernization-toolkit/swing-bridge).
- [Vaadin documentation](https://vaadin.com/docs/latest).
