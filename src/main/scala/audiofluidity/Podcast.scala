package audiofluidity

import scala.collection.*
import PodcastFeed.Itunes.{ValidEpisodeType, ValidPodcastType}

object Podcast:
  case class Admin( name : String, email : String)
  case class Source(
    baseDir   : String,
    staticDir : String = "html",
    mediaDir  : String = "media"
  )
  case class Format (
    episodesPath    : String          = "episodes/",
    episodeRenderer : EpisodeRenderer = new EpisodeRenderer.Default,
    feedPath        : String          = "rss.xml"
  )
  case class Episode(
    title                : String,
    uid                  : String,                                    // if number then <itunes:episode>
    description          : String,
    pubDate              : String,                                    // Format: Sun, 19 May 2002 15:21:36 GMT
    sourceAudioFileName  : String,
    authorEmail          : Option[String],                            // defaults to parent.defaultAuthorEmail or fails if not set
    seasonNumber         : Option[Int],                               // <itunes:season>
    imageUrl             : Option[String],                            // <itunes:image>
    episodeType          : ValidEpisodeType = ValidEpisodeType.full,  // <itunes:episodeType>
    block                : Boolean          = false,                  // <itunes:block>Yes</itunes:block>
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
  source             : Source,
  format             : Format,                
  mainUrl            : String,
  title              : String,
  guidPrefix         : String,
  description        : String,
  editorEmail        : String,                                               // managingEditor
  imagePath          : String,                                               // relative to mainUrl or full URL
  language           : Option[LanguageCode]     = None,
  admin              : Option[Admin]            = None,                      // <itunes:owner> and <webmaster>
  defaultAuthorEmail : Option[String],
  publisherEmail     : Option[String]           = None,                      // <itunes:author>
  copyrightHolder    : Option[String]           = None,
  shortTitle         : Option[String]           = None,                      // <itunes:title>
  podcastType        : ValidPodcastType         = ValidPodcastType.episodic, // <itunes:type>
  explicit           : Boolean = false,                                      // <itunes:explicit>
  episodes           : immutable.Seq[Episode]
):
  require(defaultAuthorEmail.nonEmpty || episodes.forall(_.authorEmail.nonEmpty), "No default author email set, and at least one episode is missing an author.")
