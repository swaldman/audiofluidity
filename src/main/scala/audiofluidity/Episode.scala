package audiofluidity

import audiofluidity.Element.Itunes.ValidEpisodeType

import java.time.ZoneId
import scala.collection.*

final case class Episode(
  uid                   : String,                                         // if number then <itunes:episode>
  title                 : String,
  description           : String,
  sourceAudioFileName   : String,
  publicationDate       : String,                                           // Format: YYYY-MM-DD
  publicationTime       : String                  = "12:00",                // Format HH:MM, 24 hour time
  extraDescription      : String                         = "",              // appended to website descrion, omitted from RSS feed
  block                 : Boolean                 = false,                  // <itunes:block>Yes</itunes:block>
  episodeType           : ValidEpisodeType        = ValidEpisodeType.full,  // <itunes:episodeType>
  explicit              : Boolean                 = false,                  // <itunes:explicit>
  keywords              : immutable.Seq[String]   = immutable.Seq.empty,    // <itunes:keywords>
  mbAuthorEmail         : Option[String]          = None,                   // defaults to podcast.defaultAuthorEmail if not set
  mbCoverImageFileName  : Option[String]          = None,                   // <itunes:image>
  mbExtraData           : Option[Any]             = None,
  mbSeasonNumber        : Option[Int]             = None,                   // <itunes:season>
  mbShortTitle          : Option[String]          = None,                   // <itunes:title>
  mbSubtitle            : Option[String]          = None,                   // <itunes:subtitle>
  mbSummary             : Option[String]          = None,                   // <itunes:summary>
  mbZoneId              : Option[ZoneId]          = None                    // defaults to parent.defaultZoneId
):
  def shortestTitle = mbShortTitle.getOrElse(title)
  def zonedDateTime(podcastZoneId : ZoneId) = audiofluidity.zonedDateTime(publicationDate, publicationTime, mbZoneId.getOrElse(podcastZoneId))


