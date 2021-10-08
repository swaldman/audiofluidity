package audiofluidity

import java.io.{ByteArrayOutputStream, File, StringWriter, OutputStreamWriter}
import java.time.{Instant, ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem, NamespaceBinding, Node, PrettyPrinter, TopScope, XML}
import Element.*
import Xmlable.given

object PodcastFeed:
  private val RdfContentModuleNamespaceBinding = new NamespaceBinding("content","http://purl.org/rss/1.0/modules/content/", TopScope)
  private val AppleNamespaceBinding = new NamespaceBinding("itunes","http://www.itunes.com/dtds/podcast-1.0.dtd", RdfContentModuleNamespaceBinding)

  private def item(build : Build, layout : Layout, podcast : Podcast, episode : Episode, examineMedia : Boolean) : (Item, Decoration.Item) =
    val guid       = _guid(podcast, episode)
    val author     = episode.mbAuthorEmail.getOrElse(podcast.defaultAuthorEmail)
    val zoneId     = episode.mbZoneId.getOrElse( podcast.zoneId )
    val pubDateZdt = zonedDateTime( episode.publicationDate, episode.publicationTime, zoneId )

    // enclosure info...
    val audioExtension = audioFileExtension(episode)
    val sourceAudioFileMimeType = mimeTypeForSupportedAudioFileExtension(audioExtension)
    val sourceAudioFile = build.srcEpisodeAudioFilePath(podcast, episode).toFile
    if examineMedia && !sourceAudioFile.exists then throw new SourceMediaFileNotFound(s"Audio file '${sourceAudioFile}' not found.")
    val sourceAudioFileLength = if examineMedia then sourceAudioFile.length() else 0
    val destinationAudioFileUrl = pathcat(podcast.mainUrl, layout.episodeRoot(podcast,episode), layout.episodeAudioPath(podcast, episode))
    val mbDurationInSeconds =
      if examineMedia && audioExtension == "mp3" then Some( mp3FileDurationInSeconds( sourceAudioFile ) ) else None

    val itemOut = Item(
      title       = Title(episode.title),
      link        = Link(pathcat(podcast.mainUrl, layout.episodeRoot(podcast,episode))),
      description = Description(episode.description),
      author      = Author(author),
      categories  = immutable.Seq.empty,
      comments    = None,
      enclosure   = Some(Enclosure(url=destinationAudioFileUrl,length=sourceAudioFileLength,`type`=sourceAudioFileMimeType)),
      guid        = Some(Guid(false, guid)),
      pubDate     = Some(PubDate(pubDateZdt)),
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
      episode.mbSourceImageFileName.flatMap { sourceImageFileName =>
        val imageExtension = mediaFileExtension( sourceImageFileName )
        ensureSupportedImageExtension(imageExtension)
        build.mbSrcEpisodeImageFilePath(podcast, episode).flatMap { sourceImageFilePath =>
          val sourceImageFile = sourceImageFilePath.toFile
          if examineMedia && !sourceImageFile.exists then throw new SourceMediaFileNotFound(s"Image file '${sourceImageFile}' not found.")
          layout.mbEpisodeImagePath(podcast, episode).map( episodeImagePath => Itunes.Image(pathcat(podcast.mainUrl,layout.episodeRoot(podcast,episode), episodeImagePath)) )
        }
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

  private def channel(build : Build, layout : Layout, podcast : Podcast, items : immutable.Seq[Item]) : (Channel, Decoration.Channel) =
    val zdtNow = ZonedDateTime.now(podcast.zoneId)
    val title = Title(podcast.title)
    val link  = Link(podcast.mainUrl)
    val description = Description(podcast.description)
    val imageUrl = pathcat(podcast.mainUrl, layout.mainImagePath(podcast))
    val channelOut = Channel(
      title       = title,
      link        = link,
      description = description,
      pubDate     = Some(PubDate(zdtNow)),
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
      mbItunesOwner      = podcast.mbAdmin.map { case Admin(name, email) => Itunes.Owner(Itunes.Name(name),Itunes.Email(email)) },
      mbItunesSubtitle   = podcast.mbSubtitle.map(st => Itunes.Subtitle(st)),
      mbItunesSummary    = podcast.mbSummary.map(s => Itunes.Summary(s)),
      mbItunesTitle      = podcast.mbShortTitle.map(t => Itunes.Title(t))    
    )

    (channelOut, channelD)

  end channel  

  def apply(build : Build, layout : Layout, podcast : Podcast, examineMedia : Boolean = true) : PodcastFeed =
    val itemItemDs = podcast.episodes.map(e => item(build, layout, podcast, e, examineMedia) )
    val items = itemItemDs.map( _._1 )
    val (channelIn, channelD) = channel(build, layout, podcast, items)
    val itemDsMap = itemItemDs.map { case (item, itemD) => (item.guid.get.id, itemD)}.toMap // we always create <guid> elements, so get should always succeed
    PodcastFeed(channelIn, channelD, itemDsMap)

end PodcastFeed // object

case class PodcastFeed private(channelIn : Channel, channelD : Decoration.Channel, itemDs : immutable.Map[String,Decoration.Item]):

  private def itemGuid( itemElem : Elem ) : String = uniqueChildElem(itemElem, "guid").text

  private def decorateItem( itemElem : Elem ) : Elem =
    val guid  = itemGuid( itemElem )
    val itemD = itemDs( guid )
    itemD.decorate( itemElem )

  lazy val undecoratedRss = Rss(channel=channelIn)

  lazy val decoratedRssElem : Elem =
    val undecoratedRssElem = undecoratedRss.toElem.copy(scope=PodcastFeed.AppleNamespaceBinding)
    val undecoratedChannelElem = uniqueChildElem(undecoratedRssElem, "channel")
    val oldItems = undecoratedChannelElem.child.collect { case e : Elem if e.prefix == null && e.label == "item" => e }
    val nonItems = undecoratedChannelElem.child.filter( n => !oldItems.contains(n) )
    val undecoratedNoItemChannelElem = undecoratedChannelElem.copy( child = nonItems )
    val decoratedNoItemChannelElem = channelD.decorate( undecoratedNoItemChannelElem )
    val decoratedChannelElem = decoratedNoItemChannelElem.copy( child = decoratedNoItemChannelElem.child ++ oldItems.map( decorateItem ) )
    undecoratedRssElem.copy( child = decoratedChannelElem :: Nil )

  lazy val asXmlText =
    // to autogenerate the XML declaration, but no pretty print...
    // thanks https://stackoverflow.com/questions/8965025/how-do-you-add-xml-document-info-with-scala-xml
    //val sw = new StringWriter()
    //XML.write(sw,decoratedRssElem,"UTF-8",true,null) 
    //sw.toString
    val pp = new PrettyPrinter(80,2)
    val noXmlDeclarationPretty = pp.format(decoratedRssElem)
    s"<?xml version='1.0' encoding='UTF-8'?>\n\n${noXmlDeclarationPretty}"

  lazy val bytes : immutable.Seq[Byte] =
    val baos = new ByteArrayOutputStream()
    val osw = new OutputStreamWriter( baos, scala.io.Codec.UTF8.charSet )
    try
      osw.write(asXmlText)
    finally
      osw.close()
    immutable.ArraySeq.ofByte(baos.toByteArray)

end PodcastFeed
