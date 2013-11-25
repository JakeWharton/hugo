package hugo.weaving.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.builder.BuilderConstants
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class HugoPlugin implements Plugin<Project> {
  @Override void apply(Project project) {
    def log = project.logger

    final def variants
    final def plugin
    if (project.plugins.hasPlugin(AppPlugin)) {
      variants = project.android.applicationVariants
      plugin = project.plugins.getPlugin(AppPlugin)
    } else {
      variants = project.android.libraryVariants
      plugin = project.plugins.getPlugin(LibraryPlugin)
    }

    project.dependencies {
      debugCompile 'com.jakewharton.hugo:hugo-runtime:1.0.0-SNAPSHOT'
      // TODO this should come transitively
      debugCompile 'org.aspectj:aspectjrt:1.7.4'
      compile 'com.jakewharton.hugo:hugo-annotations:1.0.0-SNAPSHOT'
    }

    variants.all { variant ->
      if (!BuilderConstants.DEBUG.equals(variant.buildType.name)) {
        log.debug("Skipping non-debug build type '${variant.buildType.name}'.")
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

        new Main().runMain(args, false)
      }
    }
  }
}
