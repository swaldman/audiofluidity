package audiofluidity

import java.nio.file.Path

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
end Build
