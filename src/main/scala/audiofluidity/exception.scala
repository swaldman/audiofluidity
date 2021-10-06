package audiofluidity

class AudiofluidityException(message : String, cause : Throwable = null) extends Exception(message, cause)
class ConfigCompilationErrors(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class MissingScalaConfigDirectory(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class NoExtensionForMediaFile(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class SourceMediaFileNotFound(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class UnsupportedMediaFileType(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
