package audiofluidity

import java.io.File
import java.time.{Instant,ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem,NamespaceBinding,TopScope}

object PodcastFeed:
  val RdfContentModuleNamespaceBinding = new NamespaceBinding("content","http://purl.org/rss/1.0/modules/content/", TopScope)
  val AppleNamespaceBinding = new NamespaceBinding("itunes","http://www.itunes.com/dtds/podcast-1.0.dtd", RdfContentModuleNamespaceBinding)

  // see https://help.apple.com/itc/podcasts_connect/#/itcb54353390
  object Itunes:
    enum ValidPodcastType:
      case episodic, serial
    enum ValidEpisodeType:
      case full, trailer, bonus

    case class  Author(fullName : String)
    case object Block // should contain "Yes"
    case class  Category(text : String, subcategory : Option[Itunes.Category])
    case object Complete // should contain "Yes"
    case class  Duration(seconds : Int)
    case class  Email(email : String)
    case class  Episode(number : Int)
    case class  EpisodeType(validEpisodeType : Itunes.ValidEpisodeType)
    case class  Explicit(isExplicit : Boolean)
    case class  Image(href : String)
    case class  Keywords(keywords : immutable.Seq[String])
    case class  Name(name : String)
    case class  NewFeedUrl(location : String)
    case class  Owner(name : Itunes.Name, email : Itunes.Email)
    case class  Season(number : Int)
    case class  Title(title : String)
    case class  Type(validType : Itunes.ValidPodcastType)

  private def item(podcast : Podcast, episode : Podcast.Episode) =
    import RssFeed.*
    val guid = makeGuid(podcast, episode)
    val author = (episode.authorEmail orElse podcast.defaultAuthorEmail).getOrElse(throw new AudiofluidityException(s"No author available for episode ${episode.uid}. (Podcast constuction should have failed.)"))

    // enclosure info...
    val extension = audioFileExtension(episode.sourceAudioFileName)
    val sourceAudioFileMimeType = mimeTypeForSupportedAudioFileExtension(extension)
    val sourceAudioFile = new File(pathcat(podcast.source.baseDir,podcast.source.mediaDir,episode.sourceAudioFileName))
    if !sourceAudioFile.exists then throw new SourceAudioFileNotFound(s"File '${sourceAudioFile}' not found.")
    val sourceAudioFileLength = sourceAudioFile.length()
    val destinationAudioFileUrl = pathcat(podcast.mainUrl,podcast.format.episodesPath,s"${episode.uid}.${audioFileExtension}")

    Item(
      title = Title(episode.title),
      link  = Link(pathcat(podcast.mainUrl, podcast.format.episodesPath, s"${episode.uid}.html")),
      description = Description(episode.description),
      author = Author(author),
      categories = immutable.Seq.empty,
      comments = None,
      enclosure = Some(Enclosure(url=destinationAudioFileUrl,length=sourceAudioFileLength,`type`=sourceAudioFileMimeType)),
      guid = Some(Guid(guid)),
      pubDate = Some(PubDate(Instant.now)),
      source = None
    )

  private def rssFeedFromPodcast(podcast : Podcast) : RssFeed =
    import RssFeed.*
    val zdtNow = ZonedDateTime.now()
    val channel = Channel(
      title       = Title(podcast.title),
      link        = Link(podcast.mainUrl),
      description = Description(podcast.description),
      language    = podcast.language.map(lc => Language(lc)),
      copyright   = podcast.copyrightHolder.map(holder => Copyright(notice=s"Copyright ${zdtNow.getYear} ${holder}")),
      generator   = Some( Generator(DefaultGenerator) ),
      items       = podcast.episodes.map( episode => item(podcast,episode) )
    )
    RssFeed(channel)

  def decorateStandardFeedXml(rssFeedXml : Elem) : Elem =
    rssFeedXml.copy(scope=AppleNamespaceBinding)





