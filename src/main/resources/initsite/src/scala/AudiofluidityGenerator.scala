import audiofluidity.*
import audiofluidity.Element.Itunes

import java.time.ZoneId
import scala.collection.*

class AudiofluidityGenerator extends PodcastGenerator.Base:

  // only mandatory parameters are shown in the generated template.
  // Many more parameters can and usually should be provided.
  // See the source, Episode.scala and Podcast.scala

  val episodes : List[Episode] =
    Episode(
      uid                  = ???, // String
      title                = ???, // String
      description          = ???, // String
      sourceAudioFileName  = ???, // String
      publicationDate      = ???  // String, Format: YYYY-MM-DD
    ) :: Nil

  val podcast : Podcast =
    Podcast(
      mainUrl             = ???, // String
      title               = ???, // String
      description         = ???, // String
      guidPrefix          = ???, // String
      shortOpaqueName     = ???, // String
      mainImageFileName   = ???, // String
      editorEmail         = ???, // String
      defaultAuthorEmail  = ???, // String
      itunesCategories    = immutable.Seq( ??? ),             //immutable.Seq[ItunesCategory], ??? is one or several ItunesCategory values, only first was is used, not mandatory as RSS, but strongly recommended by Apple Podcast
      mbAdmin             = Some(Admin(name=???, email=???)), //Option[Admin], ??? are Strings, not mandatory as RSS, but strongly recommended by Apple Podcast
      mbLanguage          = Some(???),                        //Option[LanguageCode], not mandatory as RSS, but strongly recommended by Apple Podcast
      mbPublisher         = Some(???),                        //Option[String], not mandatory as RSS, but strongly recommended by Apple Podcast
      episodes            = episodes
    )