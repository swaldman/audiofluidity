package audiofluidity.rss

import scala.collection.*
import scala.xml.Elem

/**
 * Decorations are arbitrary XML elements that should be added as _children_
 * of the standard RSS item.
 */
trait Itemable[T]:
  extension( t : T )
    def toItem : Element.Item
    def itemDecorations : immutable.Seq[Elem] = Nil
