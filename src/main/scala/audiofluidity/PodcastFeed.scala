package audiofluidity

import java.io.File
import java.time.{Instant,ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem,NamespaceBinding,Node,TopScope}
import Xmlable.given

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
    case class  Category(text : String, subcategory : Option[Itunes.Category] = None)
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
    val guid = _guid(podcast, episode)
    val author = (episode.authorEmail orElse podcast.defaultAuthorEmail).getOrElse(throw new AudiofluidityException(s"No author available for episode ${episode.uid}. (Podcast constuction should have failed.)"))

    // enclosure info...
    val extension = audioFileExtension(episode.sourceAudioFileName)
    val sourceAudioFileMimeType = mimeTypeForSupportedAudioFileExtension(extension)
    val sourceAudioFile = new File(pathcat(podcast.source.baseDir,podcast.source.mediaDir,episode.sourceAudioFileName))
    if !sourceAudioFile.exists then throw new SourceAudioFileNotFound(s"File '${sourceAudioFile}' not found.")
    val sourceAudioFileLength = sourceAudioFile.length()
    val destinationAudioFileUrl = pathcat(podcast.mainUrl,podcast.format.episodesPath,_mediaFileName(podcast, episode, extension))

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
    val title = Title(podcast.title)
    val link  = Link(podcast.mainUrl)
    val description = Description(podcast.description)
    val channel = Channel(
      title       = title,
      link        = link,
      description = description,
      image       = Some(Image(url=Url(pathcat(podcast.mainUrl,podcast.imagePath)), title=title, link=link, description=Some(description), width=None, height=None)),
      language    = podcast.language.map(lc => Language(lc)),
      copyright   = podcast.copyrightHolder.map(holder => Copyright(notice=s"Copyright ${zdtNow.getYear} ${holder}")),
      generator   = Some( Generator(DefaultGenerator) ),
      items       = podcast.episodes.map( episode => item(podcast,episode) )
    )
    RssFeed(channel)

  private def decorateStandardFeedXml(rssFeedXml : Elem, podcast : Podcast) : Elem =
    val channelElem = uniqueChildElem(rssFeedXml, "channel")
    val itemElems = (channelElem \ "item").collect { case e : Elem => e }
    val orderedEpisodeGuids = itemElems.map( e => uniqueChildElem(e, "guid").text )
    val episodesMap = itemElems.map( e => uniqueChildElem(e, "guid").text -> e ).toMap
    val decoratedItemElems = decorateItemElems(podcast, orderedEpisodeGuids, episodesMap)
    val decoratedChannelElem = decorateChannelElem(podcast, decoratedItemElems, channelElem)
    decorateRssElem(podcast, decoratedChannelElem, rssFeedXml)

  private def decorateItemElems(podcast: Podcast, orderedGuids : immutable.Seq[String], episodesMap : immutable.Map[String,Elem]) : immutable.Seq[Elem] =
    orderedGuids.map(guid => decorateItemElem(podcast, guid, episodesMap(guid)))

  private def decorateItemElem(podcast : Podcast, guid : String, item : Elem) : Elem =
    assert( item.label == "item" )
    val episode = podcast.episodes.find(ep => _guid(podcast, ep) == guid).get // basically an unlabeled assertion it's here
    val mbEpisodeElem =
      try
        Some(episode.uid.toInt).filter(_ >= 0).map(n => Itunes.Episode(n).toElem)
      catch
        case _ : NumberFormatException => None
    val mbSeasonElem = (episode.seasonNumber orElse mbEpisodeElem.map( _ => 1)).map(n => Itunes.Season(n).toElem)
    val mbImageElem = episode.imageUrl.map(u => Itunes.Image(u).toElem)
    val episodeTypeElem = Itunes.EpisodeType(episode.episodeType).toElem
    val mbKeywordsElem = if episode.keywords.nonEmpty then Some(Itunes.Keywords(episode.keywords).toElem) else None
    val mbBlockElem = if episode.block then Some(Itunes.Block.toElem) else None
    val oldKids : immutable.Seq[Node] = item.child
    item.copy(child = (oldKids :+ episodeTypeElem) ++ mbEpisodeElem ++ mbSeasonElem ++ mbImageElem ++ mbKeywordsElem ++ mbBlockElem)

  private def decorateChannelElem(podcast: Podcast, decoratedItemElems: immutable.Seq[Elem], channel : Elem) : Elem =
    val itunesImageElem = Itunes.Image(pathcat(podcast.mainUrl,podcast.imagePath)).toElem
    val mbOwnerElem = podcast.admin.map { case Podcast.Admin(name, email) => Itunes.Owner(Itunes.Name(name),Itunes.Email(email)).toElem }
    val mbAuthorElem = podcast.publisherEmail.map(email => Itunes.Author(email).toElem)
    val itunesTitleElem = Itunes.Title(podcast.shortTitle.getOrElse(podcast.title)).toElem
    val podcastTypeElem = Itunes.Type(podcast.podcastType).toElem
    val explicitElem = Itunes.Explicit(podcast.explicit).toElem
    val itunesCategoriesElems = podcast.itunesCategories.map( _.toElem )

    def isItemElem( node : Node ) =
      node match
        case e : Elem if e.prefix == null && e.label == "item" => true
        case _ => false

    val (nonItems, oldItems) = channel.partition(c => isItemElem(c))
    val newKids = (nonItems :+ itunesImageElem :+ itunesTitleElem :+ podcastTypeElem :+ explicitElem) ++ itunesCategoriesElems ++ mbOwnerElem ++ mbAuthorElem ++ decoratedItemElems
    channel.copy(child = newKids)

  private def decorateRssElem(podcast : Podcast, decoratedChannelElem : Elem, rssFeedXml : Elem) : Elem =
    rssFeedXml.copy(scope=AppleNamespaceBinding, child=decoratedChannelElem)