package audiofluidity

import java.nio.file.Path
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import scala.collection.*

val DefaultGenerator = "Audiofluidity Static Podcast Site Generator"

val SupportedAudioFileExtensions = immutable.Map(
  "mp3" -> "audio/mpeg"
)

val SupportedImageFileExtensions = immutable.Map(
  "jpg" -> "image/jpeg",
  "png" -> "image/png"
)

val RssDateTimeFormatter = RFC_1123_DATE_TIME

val DefaultPodcastSourceFqcn = "AudiofluiditySource" // intentionally in the default package

val ConfigDirSysProp = "audiofluidity.config.dir"
val ConfigDirEnvVar  = "AUDIOFLUIDITY_CONFIG_DIR"