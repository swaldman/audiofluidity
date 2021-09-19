package audiofluidity

import java.io.File
import java.time.{Instant,ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem,NamespaceBinding,Node,TopScope}

import Element.*
import Xmlable.given

object PodcastFeed:
  val RdfContentModuleNamespaceBinding = new NamespaceBinding("content","http://purl.org/rss/1.0/modules/content/", TopScope)
  val AppleNamespaceBinding = new NamespaceBinding("itunes","http://www.itunes.com/dtds/podcast-1.0.dtd", RdfContentModuleNamespaceBinding)

  private def item(podcast : Podcast, episode : Podcast.Episode) : (Item, Decoration.Item) =
    val guid = _guid(podcast, episode)
    val author = episode.mbAuthorEmail.getOrElse(podcast.defaultAuthorEmail)
    val pubDateInstant = parseDateTime(episode.pubDate)

    // enclosure info...
    val audioExtension = mediaFileExtension(episode.sourceAudioFileName)
    val sourceAudioFileMimeType = mimeTypeForSupportedAudioFileExtension(audioExtension)
    val sourceAudioFile = new File(pathcat(podcast.source.srcAudioDir,episode.sourceAudioFileName))
    if !sourceAudioFile.exists then throw new SourceMediaFileNotFound(s"Audio file '${sourceAudioFile}' not found.")
    val sourceAudioFileLength = sourceAudioFile.length()
    val destinationAudioFileUrl = pathcat(podcast.mainUrl,podcast.format.episodesPath,destAudioFileName(podcast, episode, audioExtension))
    val mbDurationInSeconds =
      if audioExtension == "mp3" then Some( mp3FileDurationInSeconds( sourceAudioFile ) ) else None

    val itemOut = Item(
      title       = Title(episode.title),
      link        = Link(pathcat(podcast.mainUrl, podcast.format.episodesPath, s"${episode.uid}.html")),
      description = Description(episode.description),
      author      = Author(author),
      categories  = immutable.Seq.empty,
      comments    = None,
      enclosure   = Some(Enclosure(url=destinationAudioFileUrl,length=sourceAudioFileLength,`type`=sourceAudioFileMimeType)),
      guid        = Some(Guid(false, guid)),
      pubDate     = Some(PubDate(pubDateInstant)),
      source      = None
    )

    // for now, and it seems usually, content:encoded and description are identical
    val contentEncoded = Content.Encoded(episode.description)

    val itunesEpisodeType = Itunes.EpisodeType(episode.episodeType)

    // if the episode UID is a positive int, consider it an episode number
    val mbItunesEpisode =
      try
        Some(episode.uid.toInt).filter(_ >= 0).map(n => Itunes.Episode(n))
      catch
        case _ : NumberFormatException => None

    val mbItunesSeason   = episode.mbSeasonNumber.map(n => Itunes.Season(n))

    val mbItunesKeywords = if episode.keywords.nonEmpty then Some(Itunes.Keywords(episode.keywords)) else None
    val mbItunesBlock    = if episode.block then Some(Itunes.Block) else None

    val mbItunesImage =
      episode.mbSourceImageFileName.map { sourceImageFileName =>
        val imageExtension = mediaFileExtension( sourceImageFileName )
        ensureSupportedImageExtension(imageExtension)
        val sourceImageFile = new File(pathcat(podcast.source.srcImageDir,sourceImageFileName))
        if !sourceImageFile.exists then throw new SourceMediaFileNotFound(s"Image file '${sourceImageFile}' not found.")
        val destinationImageFileUrl = pathcat(podcast.mainUrl,podcast.format.episodesPath,destImageFileName(podcast, episode, imageExtension))
        Itunes.Image(destinationImageFileUrl)  
      }

    val itemD = Decoration.Item(
      mbContentEncoded    = Some(contentEncoded),
      mbItunesBlock       = mbItunesBlock,
      mbItunesDuration    = mbDurationInSeconds.map(secs => Itunes.Duration(secs)),
      mbItunesEpisode     = mbItunesEpisode,
      mbItunesEpisodeType = Some(itunesEpisodeType),
      mbItunesExplicit    = Some(Itunes.Explicit(episode.explicit)),
      mbItunesImage       = mbItunesImage,
      mbItunesKeywords    = if (episode.keywords.nonEmpty) Some(Itunes.Keywords(episode.keywords)) else None,
      mbItunesSeason      = mbItunesSeason,
      mbItunesSubtitle    = episode.mbSubtitle.map(st=>Itunes.Subtitle(st)),
      mbItunesSummary     = episode.mbSummary.map(s=>Itunes.Summary(s)),
      mbItunesTitle       = episode.mbShortTitle.map(t=>Itunes.Title(t)),
    )

    (itemOut, itemD)
      
  end item

  private def channel(podcast : Podcast, items : immutable.Seq[Item] ) : (Channel, Decoration.Channel) =
    val zdtNow = ZonedDateTime.now()
    val title = Title(podcast.title)
    val link  = Link(podcast.mainUrl)
    val description = Description(podcast.description)
    val imageUrl = pathcat(podcast.mainUrl,podcast.mainImagePath)
    val channelOut = Channel(
      title       = title,
      link        = link,
      description = description,
      image       = Some(Image(url=Url(imageUrl), title=title, link=link, description=Some(description), width=None, height=None)),
      language    = podcast.mbLanguage.map(lc => Language(lc)),
      copyright   = podcast.mbCopyrightHolder.map(holder => Copyright(notice=s"\u00A9${zdtNow.getYear} ${holder}")),
      generator   = Some( Generator(DefaultGenerator) ),
      items       = items
    )
    
    val channelD = Decoration.Channel(
      itunesCategories   = podcast.itunesCategories,
      itunesImage        = Itunes.Image(imageUrl),
      itunesExplicit     = Itunes.Explicit(podcast.explicit),
      mbItunesAuthor     = podcast.mbPublisherEmail.map(email => Itunes.Author(email)),
      mbItunesBlock      = if podcast.block then Some(Itunes.Block) else None,
      mbItunesComplete   = if podcast.complete then Some(Itunes.Complete) else None,
      mbItunesKeywords   = if (podcast.keywords.nonEmpty) Some(Itunes.Keywords(podcast.keywords)) else None,
      mbItunesNewFeedUrl = podcast.mbNewFeedUrl.map(url => Itunes.NewFeedUrl(url)),
      mbItunesOwner      = podcast.mbAdmin.map { case Podcast.Admin(name, email) => Itunes.Owner(Itunes.Name(name),Itunes.Email(email)) },
      mbItunesSubtitle   = podcast.mbSubtitle.map(st => Itunes.Subtitle(st)),
      mbItunesSummary    = podcast.mbSummary.map(s => Itunes.Summary(s)),
      mbItunesTitle      = podcast.mbShortTitle.map(t => Itunes.Title(t))    
    )

    (channelOut, channelD)

  end channel  

  def apply(podcast : Podcast) : PodcastFeed =
    val itemItemDs = podcast.episodes.map(e => item(podcast, e) )
    val items = itemItemDs.map( _._1 )
    val (channelIn, channelD) = channel(podcast, items)
    val itemDsMap = itemItemDs.map { case (item, itemD) => (item.guid.get.id, itemD)}.toMap // we always create <guid> elements, so get should always succeed
    PodcastFeed(channelIn, channelD, itemDsMap)

end PodcastFeed // object

case class PodcastFeed(channel : Channel, channelD : Decoration.Channel, itemDs : immutable.Map[String,Decoration.Item]):
  // private def decorateRssElem(podcast : Podcast, decoratedChannelElem : Elem, rssFeedXml : Elem) : Elem =
    // rssFeedXml.copy(scope=AppleNamespaceBinding, child=decoratedChannelElem)

  lazy val asXmlText = ???  
end PodcastFeed
