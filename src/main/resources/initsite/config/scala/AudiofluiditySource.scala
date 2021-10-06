import audiofluidity.*
import audiofluidity.Element.Itunes

import java.time.ZoneId
import scala.collection.*

class AudiofluiditySource extends PodcastSource:

  // only mandatory parameters are shown in the generated template.
  // Many more parameters can and usually should be provided.
  // See the source, Episode.scala and Podcast.scala

  val episodes : List[Episode] =
    Episode(
      uid                  = ???,    // String
      title                = ???,    // String
      description          = ???,    // String
      sourceAudioFileName  = ???,    // String
      publicationDate      = ???     // String, Format: YYYY-MM-DD
    ) :: Nil

  val podcast : Podcast =
    Podcast(
      mainUrl             = ???,   // String
      title               = ???,   // String
      description         = ???,   // String
      guidPrefix          = ???,   // String
      shortOpaqueName     = ???,   // String, to be incorporated in generated filenames
      mainImageFileName   = ???,   // String
      editorEmail         = ???,   // String, managingEditor
      defaultAuthorEmail  = ???,   // String
      episodes            = episodes
    )

  def toPodcast : Podcast = podcast
