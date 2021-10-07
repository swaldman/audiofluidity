package audiofluidity

object PodcastGenerator {
  abstract class Base extends PodcastGenerator:
    val layout   : Layout = Layout.Basic()
    def renderer : Renderer = new Renderer.Basic()
}
trait PodcastGenerator:
  def layout   : Layout
  def podcast  : Podcast
  def renderer : Renderer
