package audiofluidity

import java.nio.file.{Files,Path}

import scala.collection.*

final case class Build(
  baseDir                 : Path = Path.of(""),            // path to dir, empty String means current working directory

  srcDirName              : String = "src",         // relative to baseDir
  srcStaticDirName        : String = "docroot",     // relative to srcDir, static resources
  srcAudioDirName         : String = "audio",       // relative to srcDir
  srcEpisodeRootName      : String = "episoderoot", // relative to srcDir, contains folders of static resources named by episode UID
  srcImageDirName         : String = "image",       // relative to srcDir
  podcastgenDirName       : String = "podcastgen",  // relative to baseDir
):
  val srcDir              = baseDir.resolve(srcDirName)
  val srcStaticDir        = srcDir.resolve(srcStaticDirName)
  val srcAudioDir         = srcDir.resolve(srcAudioDirName)
  val srcEpisodeRootDir   = srcDir.resolve(srcEpisodeRootName)
  val srcImageDir         = srcDir.resolve(srcImageDirName)
  val podcastgenDir       = baseDir.resolve(podcastgenDirName)

  def srcMainImageFilePath(podcast : Podcast)                         : Path         = srcImageDir.resolve(podcast.mainImageFileName)
  def srcEpisodeAudioFilePath(podcast : Podcast, episode : Episode)   : Path         = srcAudioDir.resolve(episode.sourceAudioFileName)
  def srcEpisodeRootDirPath(podcast : Podcast, episode : Episode)     : Path         = srcEpisodeRootDir.resolve(episode.uid)
  def mbSrcEpisodeImageFilePath(podcast : Podcast, episode : Episode) : Option[Path] = episode.mbSourceImageFileName.map( sifn => srcAudioDir.resolve(sifn) )

  def initDirs() : Unit =
    require(Files.exists(baseDir), s"Podcast build base directory '${baseDir.toAbsolutePath}' must exist before build directories can be created.")
    Files.createDirectories(srcDir)
    Files.createDirectories(srcStaticDir)
    Files.createDirectories(srcAudioDir)
    Files.createDirectories(srcEpisodeRootDir)
    Files.createDirectories(srcImageDir)
    Files.createDirectories(podcastgenDir)

  def buildResourceBase : Path = Path.of("initsite")

  def buildResources : immutable.Set[Path] = immutable.Set( Path.of("dotfile.gitignore"))