package audiofluidity

import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import scala.collection.*

// I'd really rather use ScalaTags, but they're not available yet for Scala 3

class DefaultRenderer extends Renderer:
  def generateMainHtml( build : Build, layout : Layout, podcast: Podcast ) : String =
    s"""|<html>
        |  <head>
        |     <title>${podcast.shortestTitle}</title>
        |     <link rel="alternate" type="application/rss+xml" title="${podcast.shortestTitle}" href="${layout.rssFeedPath(podcast)}" />
        |     <link href="podcast.css" rel="stylesheet" />
        |   </head>
        |  <body class="podcast">
        |    <div class="contacts podcast">
        |       [<a href="${layout.rssFeedPath(podcast)}">rss</a>]
        |    </div>
        |    <div class="coverimage podcast">
        |      <img src="${layout.mainCoverImagePath(podcast)}" />
        |    </div>
        |    <h1 class="maintitle podcast">${podcast.title}</h1>
        |    ${podcast.mbSubtitle.fold("")(st => "<h3 class=\"subtitle podcast\">" + st + "</h3>")}
        |    <hr class="belowtitle podcast"/>
        |    <div class="description podcast">
        |    ${podcast.description}
        |    </div>
        |    <hr class="belowdescription podcast"/>
        |    <ul class="episodelist">
        |      ${podcast.episodes.map(episodeListElement(layout,podcast,_)).mkString("\n")}
        |    </ul>
        |    <hr class="belowepisodelist podcast"/>
        |    <div class="endcredits">
        |    ${podcast.mbPublisher.fold("")(p => s"<p class=\"publishedby\">Published by ${p}.</p>")}
        |    ${podcast.mbAdmin.fold("")(admin => s"<p class=\"admin\">Administrative contact: <a href=\"mailto:${admin.email}\">${admin.name}</a></p>")}
        |    ${podcast.mbCopyrightHolder.fold("")(holder => "<p class=\"copyright\">&copy; " + ZonedDateTime.now.getYear + " " + holder + "</p>")}
        |    </div>
        |  </body>
        |</html>""".stripMargin
  def generateEpisodeHtml( build : Build, layout : Layout, podcast: Podcast, episode : Episode ) : String =
    val when = episode.zonedDateTime(podcast.zoneId)

    extension (p : Path)
      def suffix : Option[String] =
        val fn = p.getFileName.toString
        val lastDot = fn.lastIndexOf('.')
        if lastDot <= 0 || lastDot == fn.length-1 then None else Some(fn.substring(lastDot+1))

    s"""|<html>
        |  <head>
        |    <title>${podcast.shortestTitle}: ${episodeSequencePfx(episode)} ${episode.shortestTitle}</title>
        |    <link rel="alternate" type="application/rss+xml" title="${podcast.shortestTitle}" href="../../${layout.rssFeedPath(podcast)}" />
        |    <link href="${layout.episodeBacklinkToRoot(podcast,episode).resolve("podcast.css")}" rel="stylesheet" />
        |  </head>
        |  <body class="episode">
        |    <div class="contacts episode">
        |       [<a href="${layout.episodeBacklinkToRoot(podcast,episode).resolve(layout.rssFeedPath(podcast))}">rss</a>]
        |       [<a href="${layout.episodeBacklinkToRoot(podcast,episode)}">home</a>]
        |    </div>
        |    <div class="coverimage episode">
        |      ${layout.mbEpisodeCoverImagePath(podcast,episode).fold("")(p => "<img src=\"" + p + "\" />")}
        |    </div>
        |    <h1 class="maintitle episode">${podcast.shortestTitle}: ${episode.title}</h1>
        |    ${episode.mbSubtitle.fold("")(st => "<h3 class=\"subtitle episode\">" + st + "</h3>")}
        |    <hr class="belowtitle episode"/>
        |    <div class="description episode">
        |    ${episode.description}
        |    </div>
        |    <div class="audiolink">Episode audio: [<a href="${layout.episodeAudioPath(podcast,episode)}">${layout.episodeAudioPath(podcast,episode).suffix.getOrElse("???")}</a>]</div>
        |    <hr class="belowdescription episode"/>
        |    ${episode.mbAuthorEmail.fold("")(email => s"<p class=\"author\">Author: <a href=\"mailto:${email}\">${email}</a></p>")}
        |    <p class="publishedwhen">Published ${when.format(DateTimeFormatter.RFC_1123_DATE_TIME)}.</p>
        |    ${podcast.mbCopyrightHolder.fold("")(holder => "<p class=\"copyright\">&copy; " + when.getYear + " " + holder + "</p>")}</p>
        |  </body>
        |</html>""".stripMargin

  def staticResourceBase : Path = Path.of("initsite", "podcastgen")
  def staticResources : immutable.Set[Path] = immutable.Set( Path.of("podcast.css") )

  // this is terrible
  private def mbEpisodeSequencePfx(episode : Episode) : Option[String] =
    episode.mbSeasonNumber.map { s =>
      try
        val n = episode.uid.toInt
        s"S${s}E${n} "
      catch
        case _ : NumberFormatException => ""
    } orElse Some("")

  private def episodeSequencePfx(episode : Episode) = mbEpisodeSequencePfx(episode).get

  private def episodeListElement(layout : Layout, podcast : Podcast, episode : Episode) : String =
    val epiNumberOrEmpty =
      try "Episode " + episode.uid.toInt + ": "
      catch
        case _ : NumberFormatException => ""
    s"""
       |<li>
       |  <p><b><span class="episodenumber">${epiNumberOrEmpty}</span><a href="${layout.episodeRoot(podcast, episode)}">${episode.title}</a></b> [<a href="${layout.episodeRoot(podcast,episode).resolve(layout.episodeAudioPath(podcast,episode))}">audio</a>] <span class="pubdate">${episode.publicationDate}</span><p>
       |  ${episode.mbSummary.fold("")(summary =>"<p>" + summary + "</p>")}
       |</li>
       |""".stripMargin
end DefaultRenderer
