package hugo.weaving.plugin

import groovy.transform.CompileStatic
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Project

@CompileStatic
class HugoExec {

    String inpath;
    String aspectpath;
    String destinationpath;
    String classpath;
    String bootclasspath;

    private final Project project;

    HugoExec(Project project) {
        this.project = project;
    }

    public void exec() {
        final def log = project.logger

        String[] args = [
                "-showWeaveInfo",
                "-1.5",
                "-inpath", inpath,
                "-aspectpath", aspectpath,
                "-d", destinationpath,
                "-classpath", classpath,
                "-bootclasspath", bootclasspath
        ]
//        System.out.println "ajc args: " + Arrays.toString(args.join("\n"))

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