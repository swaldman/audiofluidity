package audiofluidity

import audiofluidity.PodcastFeed.Namespaces

import java.io.{ByteArrayOutputStream, File, OutputStreamWriter, StringWriter}
import java.time.{Instant, ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem, NamespaceBinding, Node, PrettyPrinter, TopScope, XML}
import rss.Element.*
import rss.Xmlable.given

object PodcastFeed:

  private val Namespaces = List(
    rss.Namespace.RdfContent,
    rss.Namespace.ApplePodcast
  )

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
      episode.mbCoverImageFileName.flatMap { sourceImageFileName =>
        val imageExtension = mediaFileExtension( sourceImageFileName )
        ensureSupportedImageExtension(imageExtension)
        build.mbSrcEpisodeCoverImageFilePath(podcast, episode).flatMap { sourceImageFilePath =>
          val sourceImageFile = sourceImageFilePath.toFile
          if examineMedia && !sourceImageFile.exists then throw new SourceMediaFileNotFound(s"Image file '${sourceImageFile}' not found.")
          layout.mbEpisodeCoverImagePath(podcast, episode).map( episodeImagePath => Itunes.Image(pathcat(podcast.mainUrl,layout.episodeRoot(podcast,episode), episodeImagePath)) )
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
    val imageUrl = pathcat(podcast.mainUrl, layout.mainCoverImagePath(podcast))
    val channelOut = Channel(
      title       = title,
      link        = link,
      description = description,
      pubDate     = Some(PubDate(zdtNow)),
      image       = Some(Image(url=Url(imageUrl), title=title, link=link, description=Some(description), width=None, height=None)),
      language    = podcast.mbLanguage.map(lc => Language(lc)),
      copyright   = podcast.mbCopyrightHolder.map(holder => Copyright(notice=s"\u00A9${zdtNow.getYear} ${holder}")),
      webMaster   = podcast.mbAdmin.map { case Admin(name, email) => WebMaster(email=email) },
      generator   = Some( Generator(DefaultGenerator) ),
      items       = items
    )
    
    val channelD = Decoration.Channel(
      itunesCategories   = podcast.itunesCategories,
      itunesImage        = Itunes.Image(imageUrl),
      itunesExplicit     = Itunes.Explicit(podcast.explicit),
      mbItunesAuthor     = podcast.mbPublisher.map(fullName => Itunes.Author(fullName)),
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
    val reverseChronologicalEpisodes = podcast.episodes.sortBy( episode => Tuple2(episode.zonedDateTime(podcast.zoneId),System.identityHashCode(episode)) )(summon[Ordering[Tuple2[ZonedDateTime,Int]]].reverse)
    val itemItemDs = reverseChronologicalEpisodes.map(e => item(build, layout, podcast, e, examineMedia) )
    val (items, itemDs) = itemItemDs.foldLeft(Tuple2(Vector.empty[Item],Vector.empty[Decoration.Item])) { (accum,next) =>
      (accum._1 :+ next._1, accum._2 :+ next._2)
    }

    val (channelIn, channelD) = channel(build, layout, podcast, items)
    val itemDsMap = itemItemDs.map { case (item, itemD) => (item.guid.get.id, itemD)}.toMap // we always create <guid> elements, so get should always succeed
    PodcastFeed(channelIn, channelD, itemDs, itemDsMap)

end PodcastFeed // object

case class PodcastFeed private(channelIn : Channel, channelD : Decoration.Channel, itemDs : immutable.Seq[Decoration.Item], itemDsByGuid : immutable.Map[String,Decoration.Item]):

  private def itemGuid( itemElem : Elem ) : String = uniqueChildElem(itemElem, "guid").text

  val rssFeed = rss.RssFeed( channelIn, channelD.decorations, itemDs.map( _.decorations ), Namespaces )

  lazy val asXmlText = rssFeed.asXmlText

  lazy val bytes : immutable.Seq[Byte] = rssFeed.bytes

  def durationInSeconds( podcast : Podcast, episode : Episode ) : Option[Long] =
    itemDsByGuid.get(_guid(podcast, episode)).flatMap( _.mbItunesDuration).map( _.seconds )

  def humanReadableDuration( podcast : Podcast, episode : Episode ) : Option[String] =
    durationInSeconds( podcast : Podcast, episode : Episode ).map( readableDuration )

  private def readableDuration(durationSecs : Long) : String =
    val secondsField = durationSecs % 60
    val durationMins = durationSecs / 60
    val minsField    = durationMins % 60
    val hoursField   = durationMins / 60
    if hoursField > 0 then f"$hoursField%s:$minsField%02d:$secondsField%02d" else f"$minsField%s:$secondsField%02d"

end PodcastFeed
