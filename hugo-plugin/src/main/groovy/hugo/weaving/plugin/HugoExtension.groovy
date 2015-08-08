package hugo.weaving.plugin

class HugoExtension {

    def logging = true

    def logging(boolean enabled) {
        logging = enabled
    }

    def logging() {
        return logging;
    }

}