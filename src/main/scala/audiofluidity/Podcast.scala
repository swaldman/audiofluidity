package audiofluidity

import scala.collection.*
import Element.Itunes  
import Element.Itunes.{ValidEpisodeType, ValidPodcastType}

object Podcast:
  case class Admin( name : String, email : String)
  case class Source(
    baseDir          : String = "",            // path to dir, empty String means current working directory
    srcDirName       : String = "src",         // relative to baseDir
    srcStaticDirName : String = "docroot",     // relative to srcDir, static resources
    srcAudioDirName  : String = "audio",       // relative to srcDir
    srcEpisodeRoot   : String = "episoderoot", // relative to srcDir, place folders of static resources named by episode UID
    srcImageDirName  : String = "image",       // relative to srcDir 
  ):
    val srcDir       = pathcat(baseDir,srcDirName)
    val srcStaticDir = pathcat(srcDir,srcStaticDirName)
    val srcAudioDir  = pathcat(srcDir,srcAudioDirName)
    val srcImageDir  = pathcat(srcDir,srcImageDirName)
  end Source

  case class Format (
    episodesPath    : String          = "episodes/",
    episodeRenderer : EpisodeRenderer = new EpisodeRenderer.Default,
    feedPath        : String          = "feed.rss"
  )
  case class Episode(
    uid                   : String,                                        // if number then <itunes:episode>
    title                 : String,
    description           : String,
    pubDate               : String,                                        // Format: Sun, 19 May 2002 15:21:36 GMT
    sourceAudioFileName   : String,
    block                 : Boolean               = false,                 // <itunes:block>Yes</itunes:block>
    episodeType           : ValidEpisodeType      = ValidEpisodeType.full, // <itunes:episodeType>
    explicit              : Boolean               = false,                 // <itunes:explicit>
    keywords              : immutable.Seq[String] = immutable.Seq.empty,   // <itunes:keywords>
    mbAuthorEmail         : Option[String]        = None,                  // defaults to parent.defaultAuthorEmail if not set
    mbSeasonNumber        : Option[Int]           = None,                  // <itunes:season>
    mbShortTitle          : Option[String]        = None,                  // <itunes:title>
    mbSourceImageFileName : Option[String]        = None,                  // <itunes:image>
    mbSubtitle            : Option[String]        = None,                  // <itunes:subtitle>
    mbSummary             : Option[String]        = None                   // <itunes:summary>
  )
  object EpisodeRenderer:
    class Default extends EpisodeRenderer:
      def generateEpisodeHtml( episode : Episode ) : String =
        s"""|<html>
            |  <head><title>Placeholder</title><head>
            |  <body>
            |    This is just a placeholder for now.
            |  </body>
            |</html>""".stripMargin
  trait EpisodeRenderer:
    def generateEpisodeHtml( episode : Episode ) : String

import Podcast.*

case class Podcast(
  source               : Source = new Source(),
  format               : Format = new Format(),                
  mainUrl              : String,
  title                : String,
  guidPrefix           : String,
  audioFilePrefix      : String,
  imageFilePrefix      : String,
  description          : String,
  editorEmail          : String,                                                     // managingEditor
  mainImagePath        : String,                                                     // relative to mainUrl or full URL, for both image and itunes:image
  defaultAuthorEmail   : String,
  itunesCategories     : immutable.Seq[Itunes.Category] = immutable.Seq.empty,
  mbLanguage           : Option[LanguageCode]           = None,
  mbAdmin              : Option[Admin]                  = None,                      // <itunes:owner> and <webmaster>
  mbPublisherEmail     : Option[String]                 = None,                      // <itunes:author>
  mbCopyrightHolder    : Option[String]                 = None,
  mbNewFeedUrl         : Option[String]                 = None,                      // <itunes:new-feed-url>
  mbShortTitle         : Option[String]                 = None,                      // <itunes:title>
  mbSubtitle           : Option[String]                 = None,                      // <itunes:subtitle>
  mbSummary            : Option[String]                 = None,                      // <itunes:summary>
  keywords             : immutable.Seq[String]          = immutable.Seq.empty,       // <itunes:keywords>
  podcastType          : ValidPodcastType               = ValidPodcastType.episodic, // <itunes:type>
  explicit             : Boolean = false,                                            // <itunes:explicit>
  block                : Boolean = false,                                            // <itunes:block>
  complete             : Boolean = false,
  episodes             : immutable.Seq[Episode]
)
