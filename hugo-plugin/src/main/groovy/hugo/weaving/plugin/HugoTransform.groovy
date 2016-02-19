package hugo.weaving.plugin

import com.android.build.api.transform.*
import com.android.utils.Pair
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.compile.JavaCompile

import static com.android.build.api.transform.Status.*

/**
 * Created by williamwebb on 2/16/16.
 */
@CompileStatic
class HugoTransform extends Transform {

    private final Project project
    private final Map<Pair<String, String>, JavaCompile> javaCompileTasks = new HashMap<>()
    private final boolean enabled;
    public HugoTransform(Project project, boolean enabled) {
        this.project = project
        this.enabled = enabled;
    }

    /**
     * We need to set this later because the classpath is not fully calculated until the last
     * possible moment when the java compile task runs. While a Transform currently doesn't have any
     * variant information, we can guess the variant based off the input path.
     */
    public void putJavaCompileTask(String flavorName, String buildTypeName, JavaCompile javaCompileTask) {
        javaCompileTasks.put(Pair.of(flavorName, buildTypeName), javaCompileTask)
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        boolean debug = context.path.toLowerCase().endsWith("debug");

        inputs.each { TransformInput input ->
            def outputDir = outputProvider.getContentLocation("hugo", outputTypes, scopes, Format.DIRECTORY)

            input.directoryInputs.each { DirectoryInput directoryInput ->
                File inputFile = directoryInput.file

                // All classes need to be copied regardless for some reason. So if we want to
                // disable hugo simply copy files.
                if(!enabled || !debug) {
                    FileUtils.copyDirectory(inputFile,outputDir)
                    return
                }

                String inputDirs;
                if (isIncremental) {
                    FileCollection changed = new SimpleFileCollection(project.files().asList())
                    directoryInput.changedFiles.each { File file, Status status ->
                        if (status == ADDED || status == CHANGED) {
                            changed += project.files(file.parent);
                        }
                    }
                    inputDirs = changed.asPath
                } else {
                    inputDirs = inputFile.path
                }

                JavaCompile javaCompileTask = getJavaCompile(inputFile)

                String classpath = (getClasspath(javaCompileTask, referencedInputs) + project.files(inputFile)).asPath
                String bootClasspath = getBootClassPath(javaCompileTask).asPath

                def exec = new HugoExec(project)
                exec.inpath = inputDirs
                exec.aspectpath = classpath
                exec.destinationpath = outputDir
                exec.classpath = classpath
                exec.bootclasspath = bootClasspath
                exec.exec()
            }
        }
    }

    private FileCollection getBootClassPath(JavaCompile javaCompileTask) {

        def bootClasspath = javaCompileTask.options.bootClasspath
        if (bootClasspath) {
            return project.files(bootClasspath.tokenize(File.pathSeparator))
        } else {
            // If this is null it means the javaCompile task didn't need to run, however, we still
            // need to run but can't without the bootClasspath. Just fail and ask the user to rebuild.
            throw new ProjectConfigurationException("Unable to obtain the bootClasspath. This may happen if your javaCompile tasks didn't run but hugo did. You must rebuild your project or otherwise force javaCompile to run.", null)
        }
    }

    private FileCollection getClasspath(JavaCompile javaCompileTask, Collection<TransformInput> referencedInputs) {

        def classpathFiles = javaCompileTask.classpath
        referencedInputs.each { TransformInput input -> classpathFiles += project.files(input.directoryInputs*.file) }

        // bootClasspath isn't set until the last possible moment because it's expensive to look
        // up the android sdk path.
        def bootClasspath = javaCompileTask.options.bootClasspath
        if (bootClasspath) {
            classpathFiles += project.files(bootClasspath.tokenize(File.pathSeparator))
        } else {
            // If this is null it means the javaCompile task didn't need to run, however, we still
            // need to run but can't without the bootClasspath. Just fail and ask the user to rebuild.
            throw new ProjectConfigurationException("Unable to obtain the bootClasspath. This may happen if your javaCompile tasks didn't run but hugo did. You must rebuild your project or otherwise force javaCompile to run.", null)
        }
        return classpathFiles
    }

    private JavaCompile getJavaCompile(File inputFile) {
        String buildName = inputFile.name
        String flavorName = inputFile.parentFile.name

        // If either one starts with a number or is 'folders', it's probably the result of a transform, keep moving
        // up the dir structure until we find the right folders.
        // Yes I know this is bad, but hopefully per-variant transforms will land soon.
        File current = inputFile
        while (Character.isDigit(buildName.charAt(0)) || Character.isDigit(flavorName.charAt(0)) || buildName.equals("folders") || flavorName.equals("folders")) {
            current = current.parentFile
            buildName = current.name
            flavorName = current.parentFile.name
        }

        def javaCompileTask = javaCompileTasks.get(Pair.of(flavorName, buildName))
        if (javaCompileTask == null) {
            // Flavor might not exist
            javaCompileTask = javaCompileTasks.get(Pair.of("", buildName))
        }

        return javaCompileTask;
    }

    @Override
    public String getName() {
        return "hugo"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return Collections.singleton(QualifiedContent.Scope.PROJECT)
    }

    @Override
    public boolean isIncremental() {
        return true
    }
}