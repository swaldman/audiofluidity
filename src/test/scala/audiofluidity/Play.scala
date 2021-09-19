package audiofluidity

import scala.collection.*

object Play:

  val TestEpisodes = immutable.Seq(
    Podcast.Episode(
      uid                 = "1",
      title               = "Hypothetically, a First Episode",
      description         = "<p>If this were not just a test, it would be a first episode</p>",
      pubDate             = "Sat, 18 Sep 2021 23:36:04 -0700",
      sourceAudioFileName = "ep1.mp3"
    )
  )


  val TestPodcast = Podcast(    
    mainUrl             = "https://www.audiofluidity.com/",
    title              = "Audiofluidity",
    guidPrefix         = "com.audiofluidty-",
    audioFilePrefix    = "audiofluidity-episode-",
    imageFilePrefix    = "audiofluidity-coverart-episode-",
    description        = """|<p><i>Audiofluidity</i> is not a podcast yet, but it might be someday.</p>
                            |
                            |<p>In the meantime, it is indescribable</p>""".stripMargin,
    editorEmail        = "podcast@audiofluidity.com",
    mainImagePath      = "main-cover.png",
    defaultAuthorEmail = "podcast@audiofluidity.com",
    itunesCategories   = immutable.Seq(ItunesCategory.Kids_Family_Pets_Animals),
    episodes           = TestEpisodes
  )

