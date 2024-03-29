package audiofluidity

import java.nio.file.Path
import java.time.ZoneId

import scala.collection.*

import rss.Element.Itunes
import rss.Element.Itunes.{ValidEpisodeType, ValidPodcastType}

final case class Podcast(
  mainUrl                : String,
  title                  : String,
  description            : String,
  guidPrefix             : String,
  shortOpaqueName        : String, // to be incorporated in generated filenames
  mainCoverImageFileName : String,
  editorEmail            : String, // managingEditor
  defaultAuthorEmail     : String,
  episodes               : immutable.Seq[Episode],
  itunesCategories       : immutable.Seq[Itunes.Category] = immutable.Seq.empty,
  extraDescription       : String                         = "",                       // appended to website descrion, omitted from RSS feed
  zoneId                 : ZoneId                         = ZoneId.of("US/Pacific"),
  mbLanguage             : Option[rss.LanguageCode]       = None,
  mbAdmin                : Option[Admin]                  = None,                      // <itunes:owner> and <webMaster>
  mbExtraData            : Option[Any]                    = None,
  mbCopyrightHolder      : Option[String]                 = None,
  mbNewFeedUrl           : Option[String]                 = None,                      // <itunes:new-feed-url>
  mbPublisher            : Option[String]                 = None,                      // <itunes:author>
  mbShortTitle           : Option[String]                 = None,                      // <itunes:title>
  mbSubtitle             : Option[String]                 = None,                      // <itunes:subtitle>
  mbSummary              : Option[String]                 = None,                      // <itunes:summary>
  keywords               : immutable.Seq[String]          = immutable.Seq.empty,       // <itunes:keywords>
  podcastType            : ValidPodcastType               = ValidPodcastType.episodic, // <itunes:type>
  explicit               : Boolean                        = false,                     // <itunes:explicit>
  block                  : Boolean                        = false,                     // <itunes:block>
  complete               : Boolean                        = false,                     // <itunes:complete>
):
  require(mainUrl.last == '/', s"mainUrl: '${mainUrl}' must end with /")
  require(nonUniqueEpisodeIds(episodes).isEmpty, s"Duplicate episode UIDs: ${nonUniqueEpisodeIds(episodes).mkString(", ")}")
  def shortestTitle = mbShortTitle.getOrElse(title)


