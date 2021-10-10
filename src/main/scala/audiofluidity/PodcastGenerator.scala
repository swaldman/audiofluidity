package audiofluidity

object PodcastGenerator {
  abstract class Base extends PodcastGenerator:
    val layout   : Layout = Layout.Basic()
    def renderer : Renderer = new DefaultRenderer()
}
trait PodcastGenerator:
  def layout   : Layout
  def podcast  : Podcast
  def renderer : Renderer
  def deployer : Deployer
