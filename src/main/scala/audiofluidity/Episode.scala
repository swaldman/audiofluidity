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
  block                 : Boolean                 = false,                  // <itunes:block>Yes</itunes:block>
  episodeType           : ValidEpisodeType        = ValidEpisodeType.full,  // <itunes:episodeType>
  explicit              : Boolean                 = false,                  // <itunes:explicit>
  keywords              : immutable.Seq[String]   = immutable.Seq.empty,    // <itunes:keywords>
  mbAuthorEmail         : Option[String]          = None,                   // defaults to podcast.defaultAuthorEmail if not set
  mbSeasonNumber        : Option[Int]             = None,                   // <itunes:season>
  mbShortTitle          : Option[String]          = None,                   // <itunes:title>
  mbSourceImageFileName : Option[String]          = None,                   // <itunes:image>
  mbSubtitle            : Option[String]          = None,                   // <itunes:subtitle>
  mbSummary             : Option[String]          = None,                   // <itunes:summary>
  mbZoneId              : Option[ZoneId]          = None                    // defaults to parent.defaultZoneId
):
  def shortestTitle = mbShortTitle.getOrElse(title)


