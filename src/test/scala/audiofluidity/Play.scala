package audiofluidity

import audiofluidity.rss.ItunesCategory

import scala.collection.*

object Play:

  val TestEpisodes = immutable.Seq(
    Episode(
      uid                 = "1",
      title               = "Hypothetically, a First Episode",
      description         = "<p>If this were not just a test, it would be a first episode</p>",
      publicationDate     = "2021-09-19",
      sourceAudioFileName = "Opening-Pokemon-2021-09-21.mp3"
    )
  )

  val TestPodcast = Podcast(    
    mainUrl             = "https://test.audiofluidity.com/",
    title              = "Test Audiofluidity",
    guidPrefix         = "com.audiofluidty.test-",
    description        = """|<p><i>Audiofluidity</i> is not a podcast yet, but it might be someday.</p>
                            |
                            |<p>In the meantime, it is indescribable</p>""".stripMargin,
    shortOpaqueName    = "test-audiofluidity",
    mainImageFileName  = "246138187.jpg",
    editorEmail        = "podcast@test.audiofluidity.com",
    defaultAuthorEmail = "podcast@test.audiofluidity.com",
    itunesCategories   = immutable.Seq(ItunesCategory.Kids_Family_Pets_Animals),
    episodes           = TestEpisodes
  )

  val TestXml =
    val feed = PodcastFeed(TestPodcast, examineMedia = false)
    feed.asXmlText

  val GenerateTestPodcast = TestPodcast.copy(build = TestPodcast.build.copy(baseDir = java.nio.file.Path.of("/Users/swaldman/tmp/test-audiofluidity")))

end Play