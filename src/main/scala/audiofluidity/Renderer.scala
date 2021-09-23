package audiofluidity

object Renderer:
  object Basic extends Renderer:
    def generateMainHtml( podcast: Podcast ) : String =
      s"""|<html>
          |  <head><title>Placeholder Main Page: ${podcast.title}</title><head>
          |  <body>
          |    <h1>${podcast.title}: Main Page</h1>
          |    <hr/>
          |    This is just a placeholder for now.
          |  </body>
          |</html>""".stripMargin
    def generateEpisodeHtml( podcast: Podcast, episode : Episode ) : String =
      s"""|<html>
          |  <head><title>PlaceholderEpisode </title><head>
          |  <body>
          |    <h1>Episode Page: ${episode.uid}</h1>
          |    <hr/>
          |    This is just a placeholder for now.
          |  </body>
          |</html>""".stripMargin
trait Renderer:
  def generateMainHtml( podcast : Podcast ) : String
  def generateEpisodeHtml( podcast : Podcast, episode : Episode ) : String

