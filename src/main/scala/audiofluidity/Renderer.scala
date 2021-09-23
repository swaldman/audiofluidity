package audiofluidity

object Renderer:
  object Basic extends Renderer:
    def generateEpisodeHtml( podcast: Podcast, episode : Episode ) : String =
      s"""|<html>
          |  <head><title>Placeholder</title><head>
          |  <body>
          |    This is just a placeholder for now.
          |  </body>
          |</html>""".stripMargin
trait Renderer:
  def generateEpisodeHtml( podcast : Podcast, episode : Episode ) : String

