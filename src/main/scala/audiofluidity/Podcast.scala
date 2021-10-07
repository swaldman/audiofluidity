package audiofluidity

import java.nio.file.Path
import java.time.ZoneId

import scala.collection.*

import Element.Itunes  
import Element.Itunes.{ValidEpisodeType, ValidPodcastType}

final case class Podcast(
  mainUrl                : String,
  title                  : String,
  description            : String,
  guidPrefix             : String,
  shortOpaqueName        : String, // to be incorporated in generated filenames
  mainImageFileName      : String,
  editorEmail            : String, // managingEditor
  defaultAuthorEmail     : String,
  episodes               : immutable.Seq[Episode],
  itunesCategories       : immutable.Seq[Itunes.Category] = immutable.Seq.empty,
  zoneId                 : ZoneId                         = ZoneId.of("US/Pacific"),
  mbLanguage             : Option[LanguageCode]           = None,
  mbAdmin                : Option[Admin]                  = None,                      // <itunes:owner> and <webmaster>
  mbExtraData            : Option[Any]                    = None,
  mbCopyrightHolder      : Option[String]                 = None,
  mbNewFeedUrl           : Option[String]                 = None,                      // <itunes:new-feed-url>
  mbPublisherEmail       : Option[String]                 = None,                      // <itunes:author>
  mbShortTitle           : Option[String]                 = None,                      // <itunes:title>
  mbSubtitle             : Option[String]                 = None,                      // <itunes:subtitle>
  mbSummary              : Option[String]                 = None,                      // <itunes:summary>
  keywords               : immutable.Seq[String]          = immutable.Seq.empty,       // <itunes:keywords>
  podcastType            : ValidPodcastType               = ValidPodcastType.episodic, // <itunes:type>
  explicit               : Boolean                        = false,                     // <itunes:explicit>
  block                  : Boolean                        = false,                     // <itunes:block>
  complete               : Boolean                        = false,                     // <itunes:complete>
):
  def shortestTitle = mbShortTitle.getOrElse(title)
