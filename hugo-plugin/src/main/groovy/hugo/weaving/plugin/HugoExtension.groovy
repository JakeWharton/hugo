package hugo.weaving.plugin

class HugoExtension {

  def logging = true

  def setLogging(boolean enabled) {
    logging = enabled
  }

  def getLogging() {
    return logging;
  }

}
