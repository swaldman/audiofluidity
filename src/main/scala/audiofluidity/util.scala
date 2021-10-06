package audiofluidity

import scala.collection.*
import scala.xml.*
import scala.jdk.StreamConverters.*
import java.io.{BufferedInputStream, File, IOException}
import java.net.{URL, URLClassLoader}
import java.nio.file.{FileVisitResult, Files, LinkOption, Path, SimpleFileVisitor, StandardCopyOption}
import java.nio.file.attribute.BasicFileAttributes
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.Locale
import dotty.tools.dotc

import com.mchange.sc.v1.log.MLogger
import com.mchange.sc.v1.log.MLevel.*

given logger : MLogger = MLogger(this)

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

private def _guid(podcast : Podcast, episode : Episode) : String = s"${podcast.guidPrefix}${episode.uid}"

private def mediaFileExtension(filename : String) : String =
  val preindex = filename.lastIndexOf('.')
  if preindex <= 0 || preindex >= (filename.length-1) then throw new NoExtensionForMediaFile(s"Referenced audio or image file '${filename}' lacks a required Extension")
  else filename.substring(preindex + 1)

private def audioFileExtension(episode : Episode) : String = mediaFileExtension(episode.sourceAudioFileName)

private def episodeAudioSourceFilePath( podcast : Podcast, episode : Episode ) : Path = podcast.build.srcAudioDir.resolve(episode.sourceAudioFileName)

private def mbEpisodeImageFileExtension(episode : Episode) : Option[String] = episode.mbSourceImageFileName.map(mediaFileExtension).map(ensureSupportedImageExtension)

private def mbEpisodeImageSourceFilePath( podcast : Podcast, episode : Episode) : Option[Path] =
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

def compileConfig( fqcnPodcastSource : String, classPath : String, config : Config ) : PodcastSource =
  if !Files.exists(config.scalaDir) then throw new MissingScalaConfigDirectory(s"Can't find scala config at '${config.scalaDir}'")
  Files.createDirectories(config.tmpClassesDir)
  val scalaFilePaths = Files.list(config.scalaDir).toScala(List).filter(_.toString.endsWith(".scala"))

  def classFilePath( scalaFilePath : Path) =
    val scalaFilePathStr = scalaFilePath.toString
    val suffixIndex = scalaFilePathStr.lastIndexOf('.')
    val classFilePathStr = scalaFilePathStr.substring(0,suffixIndex) + ".class"
    val classFilePath = Path.of(classFilePathStr)
    config.tmpClassesDir.resolve(config.scalaDir.relativize(classFilePath))

  val pathTups = scalaFilePaths.map( p => Tuple2(p,classFilePath(p)) )

  val changedTups = pathTups.filter {
    case (src, clz) => !Files.exists(clz) || Files.getLastModifiedTime(src).toMillis > Files.getLastModifiedTime(clz).toMillis
  }

  val mustCompileFiles = changedTups.collect { case (src, _) => src.toString }

  if mustCompileFiles.nonEmpty then
    val args =  "-d" :: config.tmpClassesDir.toString :: "-classpath" :: classPath :: mustCompileFiles

    // println(configScalaDir)
    // println(args.mkString(" "))

    // see https://github.com/lampepfl/dotty/blob/master/compiler/src/dotty/tools/dotc/Driver.scala
    val errors = (new dotc.Driver).process(args.toArray).hasErrors
    if errors then throw new ConfigCompilationErrors("Errors occurred while attempting to compile the config")
  end if

  // see https://stackoverflow.com/questions/738393/how-to-use-urlclassloader-to-load-a-class-file
  val configTmpClassesFileUrl =
    val s ="file:"+config.tmpClassesDir.toAbsolutePath
    if s.last == '/' then s else s + '/'

  val classLoader = new URLClassLoader("audiofluidity-config-compiles",Array(URL(configTmpClassesFileUrl)), classOf[Podcast].getClassLoader())
  classLoader.loadClass(fqcnPodcastSource).getDeclaredConstructor().newInstance().asInstanceOf[PodcastSource]
end compileConfig

def generate(podcast : Podcast, examineMedia : Boolean = true) : Unit =
  val podcastFeed = PodcastFeed( podcast, examineMedia = examineMedia )
  val feedPath = podcast.build.podcastgenDir.resolve(podcast.layout.rssFeedPath(podcast))
  val feedParent = feedPath.getParent()
  Files.createDirectories(feedParent)
  Files.writeString(feedPath,podcastFeed.asXmlText, scala.io.Codec.UTF8.charSet)
  val srcMainImageFilePath = podcast.build.srcMainImageFilePath(podcast)
  val destMainImagePath = podcast.build.podcastgenDir.resolve(podcast.layout.mainImagePath(podcast))
  Files.createDirectories(destMainImagePath.getParent)
  Files.copy(srcMainImageFilePath, destMainImagePath, StandardCopyOption.REPLACE_EXISTING)
  val destMainHtmlPath = podcast.build.podcastgenDir.resolve(podcast.layout.mainHtmlPath(podcast))
  Files.writeString(destMainHtmlPath,podcast.renderer.generateMainHtml(podcast), scala.io.Codec.UTF8.charSet)
  podcast.episodes.foreach( episode => generateEpisode(podcast, episode) )
  val srcStaticDirPath = podcast.build.srcStaticDir
  val destStaticDirPath = podcast.build.podcastgenDir
  if Files.exists(srcStaticDirPath) then recursiveCopyDirectory(srcStaticDirPath,destStaticDirPath)
end generate

private def generateEpisode( podcast: Podcast, episode : Episode ) : Unit =
  def root( path : Path ) = podcast.build.podcastgenDir.resolve(path)
  val episodeRoot = root(podcast.layout.episodeRoot(podcast,episode))
  Files.createDirectories(episodeRoot)
  val srcEpisodeAudioPath = podcast.build.srcEpisodeAudioFilePath(podcast,episode)
  val destEpisodeAudioPath = root(podcast.layout.episodeAudioPath(podcast,episode))
  Files.createDirectories(destEpisodeAudioPath.getParent)
  Files.copy(srcEpisodeAudioPath, destEpisodeAudioPath, StandardCopyOption.REPLACE_EXISTING)
  for
    srcEpisodeImagePath  <- podcast.layout.mbEpisodeImagePath(podcast, episode).map(root)
    destEpisodeImagePath <- podcast.build.mbSrcEpisodeImageFilePath(podcast,episode).map(root)
  yield
    Files.createDirectories(destEpisodeImagePath.getParent)
    Files.copy(srcEpisodeImagePath,destEpisodeImagePath)
  val episodeIndexHtmlPath = root(podcast.layout.episodeHtmlPath(podcast,episode))
  val episodeRenderer = episode.mbOverrideRenderer.getOrElse( podcast.renderer )
  val episodeIndexHtml = episodeRenderer.generateEpisodeHtml(podcast, episode)
  Files.writeString(episodeIndexHtmlPath, episodeIndexHtml, scala.io.Codec.UTF8.charSet)
  val srcEpisodeRootPath = podcast.build.srcEpisodeRootDirPath(podcast,episode)
  if Files.exists(srcEpisodeRootPath) then recursiveCopyDirectory(srcEpisodeRootPath,episodeRoot)
end generateEpisode

private def recursiveCopyDirectory( srcRoot : Path, destRoot : Path ) =
  val absSrcRoot = srcRoot.toAbsolutePath
  val absDestRoot = destRoot.toAbsolutePath

  def destDirPath( absSrcPath : Path ) =
    val relPath = absSrcRoot.relativize(absSrcPath)
    absDestRoot.resolve(relPath)

  val visitor = new SimpleFileVisitor[Path]: // path will be absolute in sourceDir
    override def preVisitDirectory(dir : Path, attrs : BasicFileAttributes) : FileVisitResult =
      Files.createDirectories(destDirPath(dir))
      FileVisitResult.CONTINUE

    override def visitFile(file : Path, attrs : BasicFileAttributes) : FileVisitResult =
      Files.copy(file, destDirPath(file), StandardCopyOption.REPLACE_EXISTING)
      FileVisitResult.CONTINUE

  Files.walkFileTree(absSrcRoot, visitor)
end recursiveCopyDirectory

private def recursiveDeleteDirectory( deleteDir : Path, leaveTop : Boolean = false ) : Unit =
  if !Files.exists( deleteDir ) then return

  require( Files.isDirectory(deleteDir), s"'${deleteDir}' is not a directory, recursiveDeleteDirectory can't delete!" )

  // println(s"Deleting ${deleteDir.toAbsolutePath}")

  def canonicalize(dirpath : Path) : Path = dirpath.toFile.getCanonicalFile.toPath

  val canonicalDeleteDir = canonicalize(deleteDir)

  val visitor = new SimpleFileVisitor[Path]: // path will be absolute in sourceDir
    override def postVisitDirectory(dir : Path, exc : IOException) : FileVisitResult =
      // println(s"${canonicalDeleteDir} =?= ${canonicalize(dir)}")
      if (!leaveTop || canonicalize(dir) != canonicalDeleteDir) Files.delete(dir)
      FileVisitResult.CONTINUE

    override def visitFile(file : Path, attrs : BasicFileAttributes) : FileVisitResult =
      Files.delete(file)
      FileVisitResult.CONTINUE

  Files.walkFileTree(deleteDir, visitor)
end recursiveDeleteDirectory

// expects directories already created
private def fillInResources(resourceBase : Path, resources : immutable.Set[Path], destDir : Path, cl : ClassLoader ) : Unit =
  resources.foreach( resource => fillInResource(resourceBase, resource, destDir, cl) )

// expects directories already created
private def fillInResource(resourceBase : Path, resource : Path, destDir : Path, cl : ClassLoader ) : Unit =
  TRACE.log(s"fillInResource(resourceBase=${resourceBase}, resource=${resource}, destDir=${destDir}, cl=${cl}")
  val inPath  = resourceBase.resolve(resource)
  val outPath =
    val xformResource =
      val filename = resource.getFileName.toString
      if filename.indexOf("dotfile.") == 0 then
        val newFileName = filename.replace("dotfile.",".")
        Option(resource.getParent).fold(Path.of(newFileName))(parent => parent.resolve(newFileName))
      else
        resource
    destDir.resolve(xformResource)
  TRACE.log(s"Copying resource '${inPath}' to file '${outPath}'")
  if (Files.notExists(outPath)) then
    val is = new BufferedInputStream(cl.getResourceAsStream(inPath.toString))
    try Files.copy(is, outPath) finally is.close()
  else
    INFO.log(s"File '${outPath}' exists already. Leaving as-is.")
