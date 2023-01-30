package audiofluidity.rss

import scala.xml.{NamespaceBinding,TopScope}

object Namespace:
  val RdfContent   = Namespace("content", "http://purl.org/rss/1.0/modules/content/")
  val ApplePodcast = Namespace("itunes", "http://www.itunes.com/dtds/podcast-1.0.dtd")

  private def toBinding( parentScope : NamespaceBinding, list : List[Namespace] ) : NamespaceBinding =
    list match
      case head :: tail => toBinding(new NamespaceBinding(head.prefix, head.uri, parentScope), tail)
      case Nil          => parentScope
  def toBinding( namespaces : List[Namespace]) : NamespaceBinding = toBinding(TopScope, namespaces)
case class Namespace(prefix : String, uri : String)

