package audiofluidity

import java.nio.file.{Files,Path}

import scala.collection.*
import scala.jdk.StreamConverters.*

class Build(
  val baseDir             : Path,   // path to dir, empty String means current working directory
  srcDirName              : String, // relative to baseDir
  srcScalaDirName         : String, // relative to srcDir
  srcStaticDirName        : String, // relative to srcDir, static resources
  srcAudioDirName         : String, // relative to srcDir
  srcEpisodeRootName      : String, // relative to srcDir, contains folders of static resources named by episode UID
  srcCoverImageDirName    : String, // relative to srcDir
  libDirName              : String, // relative to baseDir
  tmpDirName              : String, // relative to baseDir
  tmpClassesDirName       : String, // relative to tmpDir
  tmpStaticDirName        : String, // relative to tmpDir
  podcastgenDirName       : String  // relative to baseDir
):
  def this( baseDir : Path ) = this(
    baseDir,
    srcDirName           = "src",             // relative to baseDir
    srcScalaDirName      = "scala",           // relative to srcDir
    srcStaticDirName     = "docroot",         // relative to srcDir, static resources
    srcAudioDirName      = "audio",           // relative to srcDir
    srcEpisodeRootName   = "episoderoot",     // relative to srcDir, contains folders of static resources named by episode UID
    srcCoverImageDirName = "coverimage",           // relative to srcDir
    libDirName           = "lib",             // relative to baseDir
    tmpDirName           = "tmp",             // relative to baseDir
    tmpClassesDirName    = "classes",         // relative to tmpDir
    tmpStaticDirName     = "docroot",         // relative to tmpDir
    podcastgenDirName    = "podcastgen",      // relative to baseDir
  )

  val srcDir              = baseDir.resolve(srcDirName)
  val srcScalaDir         = srcDir.resolve(srcScalaDirName)
  val srcStaticDir        = srcDir.resolve(srcStaticDirName)
  val srcAudioDir         = srcDir.resolve(srcAudioDirName)
  val srcEpisodeRootDir   = srcDir.resolve(srcEpisodeRootName)
  val srcCoverImageDir    = srcDir.resolve(srcCoverImageDirName)
  val libDir              = baseDir.resolve(libDirName)
  val tmpDir              = baseDir.resolve(tmpDirName)
  val tmpClassesDir       = tmpDir.resolve(tmpClassesDirName)
  val podcastgenDir       = baseDir.resolve(podcastgenDirName)

  def srcMainCoverImageFilePath(podcast : Podcast)                         : Path         = srcCoverImageDir.resolve(podcast.mainImageFileName)
  def srcEpisodeAudioFilePath(podcast : Podcast, episode : Episode)        : Path         = srcAudioDir.resolve(episode.sourceAudioFileName)
  def srcEpisodeRootDirPath(podcast : Podcast, episode : Episode)          : Path         = srcEpisodeRootDir.resolve(episode.uid)
  def mbSrcEpisodeCoverImageFilePath(podcast : Podcast, episode : Episode) : Option[Path] = episode.mbSourceImageFileName.map( sifn => srcAudioDir.resolve(sifn) )

  def initDirs() : Unit =
    require(Files.exists(baseDir), s"Podcast build base directory '${baseDir.toAbsolutePath}' must exist before build directories can be created.")
    Files.createDirectories(srcScalaDir) // creates srcDir along the way
    Files.createDirectories(srcStaticDir)
    Files.createDirectories(srcAudioDir)
    Files.createDirectories(srcEpisodeRootDir)
    Files.createDirectories(srcCoverImageDir)
    Files.createDirectories(libDir)

  def libJars : List[Path] =
    if Files.exists(libDir) then
      Files.list(libDir).toScala(List).filter(_.toString.endsWith(".jar"))
    else
      Nil

  def expectedPodcastGeneratorPath = srcScalaDir.resolve("AudiofluidityGenerator.scala")

  def buildResourceBase : Path = Path.of("initsite")

  def buildResources : immutable.Set[Path] = immutable.Set( Path.of("dotfile.gitignore"), Path.of("src","scala","AudiofluidityGenerator.scala"))