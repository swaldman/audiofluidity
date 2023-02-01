package audiofluidity

import audiofluidity.PodcastFeed.Namespaces

import java.io.{ByteArrayOutputStream, File, OutputStreamWriter, StringWriter}
import java.time.{Instant, ZonedDateTime}
import scala.collection.*
import scala.xml.{Elem, NamespaceBinding, Node, PrettyPrinter, TopScope, XML}
import rss.Element.*

object PodcastFeed:

  private val Namespaces = List(
    rss.Namespace.RdfContent,
    rss.Namespace.ApplePodcast
  )

  private def item(build : Build, layout : Layout, podcast : Podcast, episode : Episode, examineMedia : Boolean) : Item =
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

    val standardItem = Item(
      title       = Title(episode.title),
      link        = Link(pathcat(podcast.mainUrl, layout.episodeRoot(podcast,episode))),
      description = Description(episode.description),
      author      = Author(author),
      categories  = immutable.Seq.empty,
      comments    = None,
      enclosure   = Some(Enclosure(url=destinationAudioFileUrl,length=sourceAudioFileLength,`type`=sourceAudioFileMimeType)),
      guid        = Some(Guid(false, guid)), // we depend on a Guid for every item!
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
    val mbItunesBlock    = if episode.block then Some(Itunes.Block()) else None

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

    val extras =
      List.newBuilder[rss.Element[?]]
        .addOne(contentEncoded)
        .addAll(mbItunesBlock)
        .addAll(mbDurationInSeconds.map(secs => Itunes.Duration(secs)))
        .addAll(mbItunesEpisode)
        .addOne(itunesEpisodeType)
        .addOne(Itunes.Explicit(episode.explicit))
        .addAll(mbItunesImage)
        .addAll(mbItunesKeywords)
        .addAll(mbItunesSeason)
        .addAll(episode.mbSubtitle.map(st=>Itunes.Subtitle(st)))
        .addAll(episode.mbSummary.map(s=>Itunes.Summary(s)))
        .addAll(episode.mbShortTitle.map(t=>Itunes.Title(t)))
        .result()

    standardItem.withExtras(extras)

  end item

  private def channel(build : Build, layout : Layout, podcast : Podcast, items : immutable.Seq[Item]) : Channel =
    val zdtNow = ZonedDateTime.now(podcast.zoneId)
    val title = Title(podcast.title)
    val link  = Link(podcast.mainUrl)
    val description = Description(podcast.description)
    val imageUrl = pathcat(podcast.mainUrl, layout.mainCoverImagePath(podcast))
    val standardChannel = Channel(
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

    val extras =
      List.newBuilder[rss.Element[?]]
        .addAll(podcast.itunesCategories)
        .addOne(Itunes.Image(imageUrl))
        .addOne(Itunes.Explicit(podcast.explicit))
        .addAll(podcast.mbPublisher.map(fullName => Itunes.Author(fullName)))
        .addAll(if podcast.block then Some(Itunes.Block()) else None)
        .addAll(if podcast.complete then Some(Itunes.Complete()) else None)
        .addAll(if (podcast.keywords.nonEmpty) Some(Itunes.Keywords(podcast.keywords)) else None)
        .addAll(podcast.mbNewFeedUrl.map(url => Itunes.NewFeedUrl(url)))
        .addAll(podcast.mbAdmin.map { case Admin(name, email) => Itunes.Owner(Itunes.Name(name),Itunes.Email(email)) })
        .addAll(podcast.mbSubtitle.map(st => Itunes.Subtitle(st)))
        .addAll(podcast.mbSummary.map(s => Itunes.Summary(s)))
        .addAll(podcast.mbShortTitle.map(t => Itunes.Title(t)))
        .result()

    standardChannel.withExtras(extras)

  end channel  

  def apply(build : Build, layout : Layout, podcast : Podcast, examineMedia : Boolean = true) : PodcastFeed =
    val reverseChronologicalEpisodes = podcast.episodes.sortBy( episode => Tuple2(episode.zonedDateTime(podcast.zoneId),System.identityHashCode(episode)) )(summon[Ordering[Tuple2[ZonedDateTime,Int]]].reverse)
    val items = reverseChronologicalEpisodes.map(e => item(build, layout, podcast, e, examineMedia) )
    val c = channel(build, layout, podcast, items)
    PodcastFeed(c)

end PodcastFeed // object

case class PodcastFeed private(channel : Channel):

  val rssFeed = Rss( channel ).overNamespaces( Namespaces )

  lazy val asXmlText = rssFeed.asXmlText

  lazy val bytes : immutable.Seq[Byte] = rssFeed.bytes

  lazy val itemsByGuid =
    channel.items
      .map( item => (item.guid, item) )
      .collect { case (Some(guid), item) => (guid.id, item)}
      .toMap

  def durationInSeconds( podcast : Podcast, episode : Episode ) : Option[Long] =
    val guid = _guid(podcast, episode)
    val item = itemsByGuid.get(guid).getOrElse{
      throw AssertionError(
        s"All episodes should map by guid to an item. no item for guid '${guid}' found in channel: ${channel}")
    }
    val durations = item.extraElements.collect { case duration : Itunes.Duration => duration }
    assert( durations.length <= 1, "Multiple durations in item, audiofluidity bug: " + item )
    durations.headOption.map( _.seconds )

  def humanReadableDuration( podcast : Podcast, episode : Episode ) : Option[String] =
    durationInSeconds( podcast : Podcast, episode : Episode ).map( readableDuration )

  private def readableDuration(durationSecs : Long) : String =
    val secondsField = durationSecs % 60
    val durationMins = durationSecs / 60
    val minsField    = durationMins % 60
    val hoursField   = durationMins / 60
    if hoursField > 0 then f"$hoursField%s:$minsField%02d:$secondsField%02d" else f"$minsField%s:$secondsField%02d"

end PodcastFeed
