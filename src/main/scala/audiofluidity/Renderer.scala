package audiofluidity

import java.nio.file.Path
import scala.collection.*

// I'd really rather use ScalaTags, but they're not available yet for Scala 3

object Renderer:
  class Basic extends Renderer:
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
          |      <img src="${layout.mainImagePath(podcast)}" />
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
          |  </body>
          |</html>""".stripMargin
    def generateEpisodeHtml( build : Build, layout : Layout, podcast: Podcast, episode : Episode ) : String =
      s"""|<html>
          |  <head>
          |    <title>${podcast.shortestTitle}: ${episodeSequencePfx(episode)} ${episode.shortestTitle}</title>
          |    <link rel="alternate" type="application/rss+xml" title="${podcast.shortestTitle}" href="../../${layout.rssFeedPath(podcast)}" />
          |    <link href="${layout.episodeBacklinkToRoot(podcast,episode).resolve("podcast.css")} rel="stylesheet" />
          |  </head>
          |  <body class="episode">
          |    <div class="contacts episode">
          |       [<a href="${layout.episodeBacklinkToRoot(podcast,episode).resolve(layout.rssFeedPath(podcast))}">rss</a>]
          |       [<a href="${layout.episodeBacklinkToRoot(podcast,episode)}">home</a>]
          |    </div>
          |    <div class="coverimage episode">
          |      ${layout.mbEpisodeImagePath(podcast,episode).fold("")(p => "<img src=\"" + p + "\" />")}
          |    </div>
          |    <h1 class="maintitle episode">${podcast.shortestTitle}: ${episode.title}</h1>
          |    ${episode.mbSubtitle.fold("")(st => "<h3 class=\"subtitle episode\">" + st + "</h3>")}
          |    <hr/>
          |    <div class="description episode">
          |    ${episode.description}
          |    </div>
          |    <div class="audiolink"><a href="${layout.episodeAudioPath(podcast,episode)}">[audio]</a></div>
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
         |  <p><b>${epiNumberOrEmpty}<a href="${layout.episodeRoot(podcast, episode)}">${episode.title}</a></b><p>
         |  ${episode.mbSummary.fold("")(summary =>"<p>" + summary + "</p>")}
         |</li>
         |""".stripMargin
  end Basic
trait Renderer:
  def generateMainHtml( build : Build, layout : Layout, podcast : Podcast )                       : String
  def generateEpisodeHtml( build : Build, layout : Layout, podcast : Podcast, episode : Episode ) : String

  def staticResourceBase : Path
  def staticResources    : immutable.Set[Path]
end Renderer
