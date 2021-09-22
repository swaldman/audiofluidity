package audiofluidity

import scala.collection.*

object Play:

  val TestEpisodes = immutable.Seq(
    Podcast.Episode(
      uid                 = "1",
      title               = "Hypothetically, a First Episode",
      description         = "<p>If this were not just a test, it would be a first episode</p>",
      publicationDate     = "2021-09-19",
      sourceAudioFileName = "ep1.mp3"
    )
  )

  val TestPodcast = Podcast(    
    mainUrl             = "https://www.audiofluidity.com/",
    title              = "Audiofluidity",
    guidPrefix         = "com.audiofluidty-",
    description        = """|<p><i>Audiofluidity</i> is not a podcast yet, but it might be someday.</p>
                            |
                            |<p>In the meantime, it is indescribable</p>""".stripMargin,
    shortOpaqueName    = "audiofluidity",
    mainImageFileName  = "audiofluidtity-logo-v7.png",
    editorEmail        = "podcast@audiofluidity.com",
    defaultAuthorEmail = "podcast@audiofluidity.com",
    itunesCategories   = immutable.Seq(ItunesCategory.Kids_Family_Pets_Animals),
    episodes           = TestEpisodes
  )

  val TestXml =
    val feed = PodcastFeed(TestPodcast, examineMedia = false)
    feed.asXmlText

end Play