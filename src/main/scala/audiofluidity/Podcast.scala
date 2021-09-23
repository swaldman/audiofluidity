package audiofluidity

import java.nio.file.Path
import java.time.ZoneId

import scala.collection.*

import Element.Itunes  
import Element.Itunes.{ValidEpisodeType, ValidPodcastType}

object Podcast:
  final case class Admin( name : String, email : String)
  final case class Build(
    baseDir                 : Path = Path.of(""),            // path to dir, empty String means current working directory

    configDirName           : String = "config",      // relative to baseDir
    configScalaDirName      : String = "scala",        // relative to configDir
    srcDirName              : String = "src",         // relative to baseDir
    srcStaticDirName        : String = "docroot",     // relative to srcDir, static resources
    srcAudioDirName         : String = "audio",       // relative to srcDir
    srcEpisodeRootName      : String = "episoderoot", // relative to srcDir, contains folders of static resources named by episode UID
    srcImageDirName         : String = "image",       // relative to srcDir
    tmpDirName              : String = "tmp",         // relative to baseDir
    tmpConfigDirName        : String = "config",      // relative to tmpDir
    tmpConfigClassesDirName : String = "classes",     // relative to tmpConfigDir
    podcastgenDirName       : String = "podcastgen",        // relative to baseDir
  ):
    val configDir           = baseDir.resolve(configDirName)
    val configScalaDir      = configDir.resolve(configScalaDirName)
    val srcDir              = baseDir.resolve(srcDirName)
    val srcStaticDir        = srcDir.resolve(srcStaticDirName)
    val srcAudioDir         = srcDir.resolve(srcAudioDirName)
    val srcEpisodeRootDir   = srcDir.resolve(srcEpisodeRootName)
    val srcImageDir         = srcDir.resolve(srcImageDirName)
    val tmpDir              = baseDir.resolve(tmpDirName)
    val tmpConfigDir        = tmpDir.resolve(tmpConfigDirName)
    val tmpConfigClassesDir = tmpConfigDir.resolve(tmpConfigClassesDirName)
    val podcastgenDir       = baseDir.resolve(podcastgenDirName)

    def srcMainImageFilePath(podcast : Podcast)                         : Path         = srcImageDir.resolve(podcast.mainImageFileName)
    def srcEpisodeAudioFilePath(podcast : Podcast, episode : Episode)   : Path         = srcAudioDir.resolve(episode.sourceAudioFileName)
    def srcEpisodeRootDirPath(podcast : Podcast, episode : Episode)     : Path         = srcEpisodeRootDir.resolve(episode.uid)
    def mbSrcEpisodeImageFilePath(podcast : Podcast, episode : Episode) : Option[Path] = episode.mbSourceImageFileName.map( sifn => srcAudioDir.resolve(sifn) )
  end Build
  final case class Episode(
    uid                   : String,                                         // if number then <itunes:episode>
    title                 : String,
    description           : String,
    sourceAudioFileName   : String,
    publicationDate       : String,                                           // Format: YYYY-MM-DD
    publicationTime       : String                  = "12:00",                // Format HH:MM, 24 hour time
    block                 : Boolean                 = false,                  // <itunes:block>Yes</itunes:block>
    episodeType           : ValidEpisodeType        = ValidEpisodeType.full,  // <itunes:episodeType>
    explicit              : Boolean                 = false,                  // <itunes:explicit>
    keywords              : immutable.Seq[String]   = immutable.Seq.empty,    // <itunes:keywords>
    mbAuthorEmail         : Option[String]          = None,                   // defaults to podcast.defaultAuthorEmail if not set
    mbEpisodeRenderer     : Option[EpisodeRenderer] = None,                   // defaults to podcast.episodeRenderer
    mbSeasonNumber        : Option[Int]             = None,                   // <itunes:season>
    mbShortTitle          : Option[String]          = None,                   // <itunes:title>
    mbSourceImageFileName : Option[String]          = None,                   // <itunes:image>
    mbSubtitle            : Option[String]          = None,                   // <itunes:subtitle>
    mbSummary             : Option[String]          = None,                   // <itunes:summary>
    mbZoneId              : Option[ZoneId]          = None                    // defaults to parent.defaultZoneId
  )
  object EpisodeRenderer:
    object Basic extends EpisodeRenderer:
      def generateEpisodeHtml( podcast: Podcast, episode : Episode ) : String =
        s"""|<html>
            |  <head><title>Placeholder</title><head>
            |  <body>
            |    This is just a placeholder for now.
            |  </body>
            |</html>""".stripMargin
  trait EpisodeRenderer:
    def generateEpisodeHtml( podcast : Podcast, episode : Episode ) : String

  object Layout:
    class Basic(
      indexHtmlName   : String = "index.html",
      episodesDirName : String = "episodes",
      rssFeedFileName : String = "feed.rss"
    ) extends Layout:
      def destEpisodeAudioFileName(podcast : Podcast, episode : Podcast.Episode, extension : String) : String =
        s"${podcast.shortOpaqueName}-audio-episode-${episode.uid}.${extension}"
      def destEpisodeImageFileName(podcast : Podcast, episode : Podcast.Episode, extension : String) : String =
        s"${podcast.shortOpaqueName}-coverart-episode-${episode.uid}.${extension}"
      // def mainHtmlPath(podcast : Podcast) : Path = Path.of(indexHtmlName)
      def mainImagePath(podcast : Podcast) : Path =
        val extension = mainImageFileExtension(podcast)
        Path.of(s"${podcast.shortOpaqueName}-coverart.${extension}")
      def rssFeedPath(podcast : Podcast) : Path = Path.of(rssFeedFileName)
      def episodeRoot(podcast : Podcast, episode : Episode) : Path = Path.of(episodesDirName,s"episode-${episode.uid}")
      def episodeAudioPath(podcast : Podcast, episode : Episode) : Path =
        val extension = audioFileExtension(episode)
        val fileName = destEpisodeAudioFileName(podcast, episode, extension)
        episodeRoot(podcast,episode).resolve(fileName)
      def episodeHtmlPath(podcast : Podcast, episode : Episode)  : Path =
        episodeRoot(podcast,episode).resolve(indexHtmlName)
      def mbEpisodeImagePath(podcast : Podcast, episode : Episode) : Option[Path] = // only if there is an episode image
        mbEpisodeImageFileExtension(episode).map { extension =>
          val fileName = destEpisodeImageFileName(podcast, episode, extension)
          episodeRoot(podcast,episode).resolve(fileName)
        }

  trait Layout:
    def destEpisodeAudioFileName(podcast : Podcast, episode : Podcast.Episode, extension : String) : String
    def destEpisodeImageFileName(podcast : Podcast, episode : Podcast.Episode, extension : String) : String

    // paths below are all relative to podcast root
    // def mainHtmlPath(podcast : Podcast)                         : Path
    def mainImagePath(podcast : Podcast)                        : Path
    def rssFeedPath(podcast : Podcast)                          : Path
    def episodeRoot(podcast : Podcast, episode : Episode)       : Path
    def episodeAudioPath(podcast : Podcast, episode : Episode)  : Path
    def episodeHtmlPath(podcast : Podcast, episode : Episode)   : Path

    def mbEpisodeImagePath(podcast : Podcast, episode : Episode) : Option[Path] // only if there is an episode image

    def mainUrl( podcast : Podcast )                          : String = podcast.mainUrl // html file should be directory index
    def mainImageUrl(podcast : Podcast)                      : String = pathcat(podcast.mainUrl,mainImagePath(podcast).toString)
    def rssFeedPathUrl(podcast : Podcast)                     : String = pathcat(podcast.mainUrl,rssFeedPath(podcast).toString)
    def episodeAudioUrl(podcast : Podcast, episode : Episode) : String = pathcat(podcast.mainUrl,episodeAudioPath(podcast,episode).toString)
    def episodeUrl( podcast : Podcast, episode : Episode)     : String = pathcat(podcast.mainUrl,episodeRoot(podcast,episode).toString) // episode html file should be a directory index

    def mbEpisodeImageUrl(podcast : Podcast, episode : Episode) : Option[String] = mbEpisodeImagePath(podcast,episode).map(ip => pathcat(podcast.mainUrl,ip.toString))


import Podcast.*

final case class Podcast(
  build                  : Build  = new Build(),
  layout                 : Layout = new Layout.Basic(),
  mainUrl                : String,
  title                  : String,
  description            : String,
  guidPrefix             : String,
  shortOpaqueName        : String,                                                     // to be incorporated in generated filename
  mainImageFileName      : String,
  editorEmail            : String,                                                     // managingEditor
  defaultAuthorEmail     : String,
  defaultEpisodeRenderer : EpisodeRenderer                = EpisodeRenderer.Basic,
  itunesCategories       : immutable.Seq[Itunes.Category] = immutable.Seq.empty,
  zoneId                 : ZoneId                         = ZoneId.of("US/Pacific"),
  mbLanguage             : Option[LanguageCode]           = None,
  mbAdmin                : Option[Admin]                  = None,                      // <itunes:owner> and <webmaster>
  mbPublisherEmail       : Option[String]                 = None,                      // <itunes:author>
  mbCopyrightHolder      : Option[String]                 = None,
  mbNewFeedUrl           : Option[String]                 = None,                      // <itunes:new-feed-url>
  mbShortTitle           : Option[String]                 = None,                      // <itunes:title>
  mbSubtitle             : Option[String]                 = None,                      // <itunes:subtitle>
  mbSummary              : Option[String]                 = None,                      // <itunes:summary>
  keywords               : immutable.Seq[String]          = immutable.Seq.empty,       // <itunes:keywords>
  podcastType            : ValidPodcastType               = ValidPodcastType.episodic, // <itunes:type>
  explicit               : Boolean = false,                                            // <itunes:explicit>
  block                  : Boolean = false,                                            // <itunes:block>
  complete               : Boolean = false,
  episodes               : immutable.Seq[Episode]
)
