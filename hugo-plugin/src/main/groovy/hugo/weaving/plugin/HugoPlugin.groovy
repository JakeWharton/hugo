package hugo.weaving.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class HugoPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def hasApp = project.plugins.withType(AppPlugin)
    def hasLib = project.plugins.withType(LibraryPlugin)
    if (!hasApp && !hasLib) {
      throw new IllegalStateException("'android' or 'android-library' plugin required.")
    }

    def transform = new HugoTransform(project, isEnabled(project))

    if (hasLib) {
      def android = project.extensions.getByType(LibraryExtension)
      android.registerTransform(transform)

      android.libraryVariants.all { BaseVariant variant ->
        configureCompileJavaTask(variant, variant.javaCompile, transform)
      }
    } else {
      def android = project.extensions.getByType(AppExtension)
      android.registerTransform(transform)

      android.applicationVariants.all { BaseVariant variant ->
        configureCompileJavaTask(variant, variant.javaCompile, transform)
      }
    }

    project.dependencies {
      debugCompile 'com.jakewharton.hugo:hugo-runtime:1.2.2-SNAPSHOT'
      // TODO this should come transitively
      debugCompile 'org.aspectj:aspectjrt:1.8.6'
      compile 'com.jakewharton.hugo:hugo-annotations:1.2.2-SNAPSHOT'
    }
    project.extensions.create('hugo', HugoExtension)
  }

  private static boolean isEnabled(Project project) {
    if(project.hasProperty("hugo") && project.hugo.hasProperty("enabled")) {
      return project.hugo.enabled;
    }
    return true;
  }

  private static configureCompileJavaTask(BaseVariant variant, JavaCompile javaCompileTask, HugoTransform transform) {
    transform.putJavaCompileTask(variant.flavorName, variant.buildType.name, javaCompileTask)
  }
}
