package audiofluidity

class AudiofluidityException(message : String, cause : Throwable = null) extends Exception(message, cause)
class DeployerNotConfiguredException(message : String, cause : Throwable = null) extends AudiofluidityException(message,cause)
class InconsistentBuildException(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class PodcastGeneratorCompilationFailed(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class MissingScalaSourceDirectory(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class NoExtensionForMediaFile(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class SourceMediaFileNotFound(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class UnsupportedMediaFileType(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
