package audiofluidity

import scala.collection._

class AudiofluidityException(message : String, cause : Throwable = null) extends Exception(message, cause)
class NoExtensionForAudioFile(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class UnsupportedAudioFileType(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)
class SourceAudioFileNotFound(message : String, cause : Throwable = null) extends AudiofluidityException(message, cause)

val DefaultGenerator = "Audiofluidity Static Podcast Site Generator"

val SupportedExtensions = immutable.Map(
  "mp3" -> "audio/mpeg"
)

private def makeGuid(podcast : Podcast, episode : Podcast.Episode) : String = s"${podcast.guidPrefix}/${episode.uid}"

private def audioFileExtension(filename : String) : String =
  val preindex = filename.lastIndexOf('.')
  if preindex <= 0 || preindex >= (filename.length-1) then throw new NoExtensionForAudioFile(s"Supposed audio file '${filename}' lacks a required Extension")
  else filename.substring(preindex + 1)

private def mimeTypeForSupportedAudioFileExtension(extension : String) : String =
  SupportedExtensions.getOrElse (
    extension,
    throw UnsupportedAudioFileType(s"""Audio files with extension '${extension}' are not yet supported. Supported extensions: ${SupportedExtensions.keySet.mkString(",")}""")
  )


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

