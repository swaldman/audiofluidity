package audiofluidity

import java.nio.file.Path
import scala.collection.*

object Renderer:
  class Basic extends Renderer:
    def generateMainHtml( podcast: Podcast ) : String =
      s"""|<html>
          |  <head>
          |     <title>${podcast.shortestTitle}</title>
          |     <link rel="alternate" type="application/rss+xml" title="${podcast.shortestTitle}" href="${podcast.layout.rssFeedUrl(podcast)}" />
          |     <link href="podcast.css" rel="stylesheet" />
          |   </head>
          |  <body>
          |    <div class="contacts podcast">
          |       [<a href="${podcast.layout.rssFeedUrl(podcast)}">rss</a>]
          |    </div>
          |    <h1 class="maintitle podcast">${podcast.title}</h1>
          |    ${podcast.mbSubtitle.map(st => "<h3 class=\"subtitle podcast\">" + st + "</h3>")}
          |    <hr class="belowtitle podcast"/>
          |    <div class="description podcast">
          |    ${podcast.description}
          |    </div>
          |    <hr class="belowdescription podcast"/>
          |    <ul class="episodelist">
          |      ${podcast.episodes.map(episodeListElement(podcast,_)).mkString("\n")}
          |    </ul>
          |  </body>
          |</html>""".stripMargin
    def generateEpisodeHtml( podcast: Podcast, episode : Episode ) : String =
      s"""|<html>
          |  <head>
          |    <title>${podcast.shortestTitle}: ${episodeSequencePfx(episode)} ${episode.shortestTitle}</title>
          |    <link rel="alternate" type="application/rss+xml" title="${podcast.shortestTitle}" href="${podcast.layout.rssFeedUrl(podcast)}" />
          |    <link href="podcast.css" rel="stylesheet" />
          |  </head>
          |  <body>
          |    <div class="contacts episode">
          |       [<a href="${podcast.layout.rssFeedUrl(podcast)}">rss</a>]
          |    </div>          |
          |    <h1 class="maintitle episode">${podcast.shortestTitle}: ${episode.title}</h1>
          |    ${episode.mbSubtitle.map(st => "<h3 class=\"subtitle episode\">" + st + "</h3>")}
          |    <hr/>
          |    <div class="description episode">
          |    ${episode.description}
          |    </div>
          |    <div class="audiolink"><a href="${podcast.layout.episodeAudioUrl(podcast,episode)}">[audio]</a></div>
          |  </body>
          |</html>""".stripMargin

    def srcStaticResourceBase : Path                = Path.of( "initsite", "src", "docroot" )
    def srcStaticResources    : immutable.Set[Path] = immutable.Set( Path.of("podcast.css") )

    private def mbEpisodeSequencePfx(episode : Episode) : Option[String] =
      episode.mbSeasonNumber.map { s =>
        try
          val n = episode.uid.toInt
          s"S${s}E${n} "
        catch
          case _ : NumberFormatException => ""
      }

    private def episodeSequencePfx(episode : Episode) = mbEpisodeSequencePfx(episode).get

    private def episodeListElement(podcast : Podcast, episode : Episode) : String =
      val epiNumberOrEmpty =
        try "Episode" + episode.uid.toInt + ": "
        catch
          case _ : NumberFormatException => ""
      s"""
         |<li>
         |  <p><b>${epiNumberOrEmpty}<a href="${podcast.layout.episodeUrl(podcast, episode)}">${episode.title}</a></b><p>
         |  ${episode.mbSummary.map(summary =>"<p>" + summary + "</p>")}
         |</li>
         |""".stripMargin
  end Basic
trait Renderer:
  def generateMainHtml( podcast : Podcast )                       : String
  def generateEpisodeHtml( podcast : Podcast, episode : Episode ) : String

  def srcStaticResourceBase : Path
  def srcStaticResources    : immutable.Set[Path]
end Renderer
