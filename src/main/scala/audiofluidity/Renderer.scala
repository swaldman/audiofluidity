package audiofluidity

import java.nio.file.Path
import scala.collection.*

// any resources that the renderer depends upon can be added to src/docroot (or
// more accurately, build.srcStaticDir) by including the paths relative to that
// directory in staticResources,
//
// they must be provided as ClassLoader resources, and will be loaded from beneath
// the path specified staticResourceBase

trait Renderer:
  def generateMainHtml( build : Build, layout : Layout, podcast : Podcast )                       : String
  def generateEpisodeHtml( build : Build, layout : Layout, podcast : Podcast, episode : Episode ) : String

  def staticResourceBase : Path
  def staticResources    : immutable.Set[Path]
end Renderer
