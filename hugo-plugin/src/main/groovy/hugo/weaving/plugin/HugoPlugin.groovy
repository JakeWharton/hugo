package hugo.weaving.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class HugoPlugin implements Plugin<Project> {
  @Override void apply(Project project) {
    def hasApp = project.plugins.hasPlugin(AppPlugin)
    def hasLib = project.plugins.hasPlugin(LibraryPlugin)
    if (!hasApp && !hasLib) {
      throw new IllegalStateException("'android' or 'android-library' plugin required.")
    }

    final def log = project.logger
    final def variants
    final def plugin
    if (hasApp) {
      variants = project.android.applicationVariants
      plugin = project.plugins.getPlugin(AppPlugin)
    } else {
      variants = project.android.libraryVariants
      plugin = project.plugins.getPlugin(LibraryPlugin)
    }

    project.dependencies {
      debugCompile 'com.jakewharton.hugo:hugo-runtime:1.0.1'
      // TODO this should come transitively
      debugCompile 'org.aspectj:aspectjrt:1.7.4'
      compile 'com.jakewharton.hugo:hugo-annotations:1.0.1'
    }

    variants.all { variant ->
      if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
      }

      JavaCompile javaCompile = variant.javaCompile
      javaCompile.doLast {
        String[] args = [
            "-showWeaveInfo",
            "-1.5",
            "-inpath", javaCompile.destinationDir.toString(),
            "-aspectpath", javaCompile.classpath.asPath,
            "-d", javaCompile.destinationDir.toString(),
            "-classpath", javaCompile.classpath.asPath,
            "-bootclasspath", plugin.runtimeJarList.join(File.pathSeparator)
        ]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
          switch (message.getKind()) {
            case IMessage.ABORT:
            case IMessage.ERROR:
            case IMessage.FAIL:
              log.error message.message, message.thrown
              break;
            case IMessage.WARNING:
              log.warn message.message, message.thrown
              break;
            case IMessage.INFO:
              log.info message.message, message.thrown
              break;
            case IMessage.DEBUG:
              log.debug message.message, message.thrown
              break;
          }
        }
      }
    }
  }
}
