package audiofluidity

import java.nio.file.Path
import scala.collection.*

val DefaultGenerator = "Audiofluidity Static Podcast Site Generator"

val SupportedAudioFileExtensions = immutable.Map(
  "mp3" -> "audio/mpeg"
)

val SupportedImageFileExtensions = immutable.Map(
  "jpg" -> "image/jpeg",
  "png" -> "image/png"
)

val DefaultPodcastGeneratorFqcn = "AudiofluidityGenerator" // intentionally in the default package

val BuildClassSysProp = "audiofluidity.build.class"
val BuildClassEnvVar  = "AUDIOFLUIDITY_BUILD_CLASS"

val BaseDirSysProp = "audiofluidity.base.dir"
val BaseDirEnvVar  = "AUDIOFLUIDITY_BASE_DIR"

val AudiofluidityConfigDirName      = ".audiofluidity"
val AudiofluidityPropertiesFileName = "audiofluidity.properties"
val ConfigPropBuildClassName        = "audiofluidity.build.class.name"
