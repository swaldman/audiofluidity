package audiofluidity

import scala.collection.*
import scala.xml.*
import java.io.File
import java.nio.file.{Files, Path, SimpleFileVisitor}
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.Locale

class AudiofluidityException(message : String, cause : Throwable = null) extends Exception(message, cause)
class NoExtensionForMediaFile(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class UnsupportedMediaFileType(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class SourceMediaFileNotFound(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)

val DefaultGenerator = "Audiofluidity Static Podcast Site Generator"

val SupportedAudioFileExtensions = immutable.Map(
  "mp3" -> "audio/mpeg"
)

val SupportedImageFileExtensions = immutable.Map(
  "jpg" -> "image/jpeg",
  "png" -> "image/png"
)

val RssDateTimeFormatter = RFC_1123_DATE_TIME

// private val AnnoyingDateTimeFormatter = RFC_1123_DATE_TIME.withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault())

// private def formatDateTime(i : Instant) : String = AnnoyingDateTimeFormatter.format(i)

// private def parseDateTime(s : String) : ZonedDateTime = ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME)

private def zonedDateTime( dateString : String, timeString : String, zoneId : ZoneId ) : ZonedDateTime =
  val ld  = LocalDate.parse( dateString )
  val lt  = LocalTime.parse( timeString )
  val ldt = LocalDateTime.of( ld, lt )
  ZonedDateTime.of( ldt, zoneId )

// see...
//    https://github.com/mpatric/mp3agic
//    https://github.com/mpatric/mp3agic-examples
//
// do I need to worry that there is no API that closes the file?
// is this library tidy internally?

private def mp3FileDurationInSeconds( f : File ) : Long =
  import com.mpatric.mp3agic.Mp3File
  val mp3file = new Mp3File(f)
  mp3file.getLengthInSeconds()   

private def uniqueChildElem(node : Node, elemName : String) : Elem =
  (node \ elemName).collect{ case e : Elem => e }.ensuring(_.length == 1, s"Expected unique child '${elemName}' of Node, found multiple or nonr.").head

private def _guid(podcast : Podcast, episode : Podcast.Episode) : String = s"${podcast.guidPrefix}${episode.uid}"

private def mediaFileExtension(filename : String) : String =
  val preindex = filename.lastIndexOf('.')
  if preindex <= 0 || preindex >= (filename.length-1) then throw new NoExtensionForMediaFile(s"Referenced audio or image file '${filename}' lacks a required Extension")
  else filename.substring(preindex + 1)

private def audioFileExtension(episode : Podcast.Episode) : String = mediaFileExtension(episode.sourceAudioFileName)

private def episodeAudioSourceFilePath( podcast : Podcast, episode : Podcast.Episode ) : Path = podcast.build.srcAudioDir.resolve(episode.sourceAudioFileName)

private def mbEpisodeImageFileExtension(episode : Podcast.Episode) : Option[String] = episode.mbSourceImageFileName.map(mediaFileExtension).map(ensureSupportedImageExtension)

private def mbEpisodeImageSourceFilePath( podcast : Podcast, episode : Podcast.Episode) : Option[Path] =
  episode.mbSourceImageFileName.map(sourceImageFileName => podcast.build.srcAudioDir.resolve(sourceImageFileName))

private def mainImageFileExtension(podcast : Podcast) : String = ensureSupportedImageExtension( mediaFileExtension(podcast.mainImageFileName) )


private def mimeTypeForSupportedAudioFileExtension(extension : String) : String =
  SupportedAudioFileExtensions.getOrElse (
    extension,
    throw UnsupportedMediaFileType(s"""Audio files with extension '${extension}' are not yet supported. Supported extensions: ${SupportedAudioFileExtensions.keySet.mkString(",")}""")
  )

private def ensureSupportedImageExtension(extension : String) : String =
  SupportedImageFileExtensions.getOrElse (
    extension,
    throw UnsupportedMediaFileType(s"""Image files with extension '${extension}' are not yet supported. Supported extensions: ${SupportedImageFileExtensions.keySet.mkString(",")}""")
  )
  extension

private def isStartOnlyPath( path : String ) : Boolean = path.indexOf(':') >= 0

private def pathcat(a : String, b : String) : String =
  if a.isEmpty then b
  else if b.isEmpty then a
  else if isStartOnlyPath(b) then b
  else (a.last == '/', b.head == '/') match
    case (true,true)                 => a + b.tail
    case (true,false) | (false,true) => a+b
    case (false,false)               => s"$a/$b"

private def pathcat(s : String*) : String =
  s.foldLeft("")((last,next)=>pathcat(last,next))

/*
private def generate(podcast : Podcast) : Unit =
  val feedPath = Path.of(pathcat(podcast.build.showDir, podcast.format.feedPath))
  val feedParent = feedPath.getParent()
  Files.createDirectories(feedParent)

private def generateEpisode( podcast: Podcast, episode : Podcast.Episode ) : Unit =
  val guid = _guid(podcast, episode)

private def recursiveCopyDirectory( srcRoot : Path, destRoot : Path ) =
  val absSrcRoot = srcRoot.toAbsolutePath
  val absDestRoot = destRoot.toAbsolutePath

  def destDirPath( absSrcPath : Path ) =
    val relPath = absSrcRoot.relativize(absSrcPath)
    absDestRoot.resolve(relPath)

  val visitor = new SimpleFileVisitor[Path]: // path will be absolute in sourceDir
    def preVisitDirectory(dir : Path, attrs : BasicFileAttributes) : FileVisitResult =
      Files.createDirectories(destDirPath(path))
      FileVisitResult.CONTINUE

    def visitFile(file : Path, attrs : BasicFileAttributes) : FileVisitResult =
      Files.copy(file, desdtDirPath(file))
      FileVisitResult.CONTINUE

  File.walkFileTree(absSrcRoot, visitor)
end recursiveCopyDirectory
*/

