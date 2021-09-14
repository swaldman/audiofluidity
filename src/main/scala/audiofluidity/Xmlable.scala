package audiofluidity

import scala.xml.{Elem,Node,MetaData,Null,Text,TopScope,UnprefixedAttribute}

import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

import PodcastFeed.*

object Xmlable:
  private def elem(label : String, attributes1 : MetaData, children : Node*) : Elem =
    new Elem(prefix=null, label=label, attributes1=attributes1, scope=TopScope, minimizeEmpty=true, children : _*)
  private def elem(label : String, children : Node*) : Elem = elem(label, Null, children : _*)

  given Xmlable[Author] with
    extension(x : Author) def toNode : Node = elem("author", new Text(x.email))
  given Xmlable[Category] with
    extension(x : Category) def toNode : Node =
      elem("category", new UnprefixedAttribute("domain", x.domain, Null), new Text(x.text))
  given Xmlable[Channel] with
    extension(x : Channel) def toNode : Node =
      val itemNodes = x.items.map( _.toNode )
      val kids =
         itemNodes ++ x.skipDays.map( _.toNode ) ++ x.skipHours.map( _.toNode ) ++ x.textInput.map( _.toNode ) ++
          x.rating.map( _.toNode ) ++ x.image.map( _.toNode ) ++ x.ttl.map( _.toNode ) ++ x.cloud.map( _.toNode ) ++ x.docs.map( _.toNode ) ++
          x.generator.map( _.toNode ) ++ x.categories.map( _.toNode ) ++ x.lastBuildDate.map( _.toNode ) ++ x.pubDate.map( _.toNode ) ++
          x.webMaster.map( _.toNode ) ++ x.managingEditor.map( _.toNode ) ++ x.copyright.map( _.toNode ) ++ x.language.map( _.toNode ) +
          x.description.toNode + x.link.toNode + x.title.toNode
      elem("channel",kids.toSeq : _*)
  given Xmlable[Cloud] with
    extension(x : Cloud) def toNode : Node =
      val attributes = new UnprefixedAttribute("domain", x.domain,
        new UnprefixedAttribute("port", x.port.toString,
          new UnprefixedAttribute("path", x.path,
            new UnprefixedAttribute("registerProcedure", x.registerProcedure,
              new UnprefixedAttribute("protocol", x.protocol, Null)
            )
          )
        )
      )
      elem("cloud", attributes)
  given Xmlable[Comments] with
    extension(x : Comments) def toNode : Node = elem("comments", new Text(x.url))
  given Xmlable[Copyright] with
    extension(x : Copyright) def toNode : Node = elem("copyright", new Text(x.notice))
  given Xmlable[Day] with
    extension(x : Day) def toNode : Node = elem("day", new Text(x.day.toString))
  given Xmlable[Description] with
    extension(x : Description) def toNode : Node = elem("description", new Text(x.text))
  given Xmlable[Docs] with
    extension(x : Docs) def toNode : Node = elem("docs", new Text(x.url))
  given Xmlable[Enclosure] with
    extension(x : Enclosure) def toNode : Node =
      val attributes = UnprefixedAttribute("url", x.url,
        new UnprefixedAttribute("length", x.length.toString,
          new UnprefixedAttribute("type", x.`type`, Null)
        )
      )
      elem("enclosure", attributes)
  given Xmlable[Generator] with
    extension(x : Generator) def toNode : Node = elem("generator", new Text(x.description))
  given Xmlable[Guid] with
    extension(x : Guid) def toNode : Node = elem("guid", new Text(x.contents))
  given Xmlable[Hour] with
    extension(x : Hour) def toNode : Node = elem("hour", new Text(x.hour.toString))
  given Xmlable[Height] with
    extension(x : Height) def toNode : Node = elem("height", new Text(x.pixels.toString))
  given Xmlable[Image] with
    extension(x : Image) def toNode : Node =
      val reverseKids : List[Node] =
        x.description.map(_.toNode) ++: x.height.map(_.toNode) ++: x.width.map( _.toNode ) ++: (x.link.toNode :: x.title.toNode :: x.url.toNode :: Nil)
      elem("image", reverseKids.reverse : _*)
  given Xmlable[Item] with
    extension(x : Item) def toNode : Node =
      val kids =
        x.categories.map( _.toNode ) ++ x.source.map( _.toNode ) ++ x.pubDate.map( _.toNode ) ++ x.guid.map( _.toNode ) ++ x.enclosure.map( _.toNode ) ++
          x.comments.map( _.toNode) + x.author.toNode + x.description.toNode + x.link.toNode + x.title.toNode
      elem("item", kids.toSeq : _*)
  given Xmlable[Language] with
    extension(x : Language) def toNode : Node = elem("language", new Text(x.code.rendered))
  given Xmlable[LastBuildDate] with
    extension(x : LastBuildDate) def toNode : Node =
      val dateStr = RFC_1123_DATE_TIME.format(x.date)
      elem("lastBuildDate", new Text(dateStr))
  given Xmlable[Link] with
    extension(x : Link) def toNode : Node = elem("link", new Text(x.location))
  given Xmlable[ManagingEditor] with
    extension(x : ManagingEditor) def toNode : Node = elem("managingEditor", new Text(x.email))
  given Xmlable[Name] with
    extension(x : Name) def toNode : Node = elem("name", new Text(x.text))
  given Xmlable[PubDate] with
    extension(x : PubDate) def toNode : Node =
      val dateStr = RFC_1123_DATE_TIME.format(x.date)
      elem("pubDate", new Text(dateStr))
  given Xmlable[Rating] with
    extension(x : Rating) def toNode : Node = elem("rating", new Text(x.contents))
  given Xmlable[SkipDays] with
    extension(x : SkipDays) def toNode : Node = elem("skipDays", x.days.map( _.toNode ).toSeq : _*)
  given Xmlable[SkipHours] with
    extension(x : SkipHours) def toNode : Node = elem("skipHours", x.hours.map( _.toNode ).toSeq : _*)
  given Xmlable[Source] with
    extension(x : Source) def toNode : Node = elem("source", new UnprefixedAttribute("url",x.url,Null), new Text(x.title))
  given Xmlable[TextInput] with
    extension(x : TextInput) def toNode : Node = elem("textInput", x.title.toNode, x.description.toNode, x.name.toNode, x.link.toNode)
  given Xmlable[Title] with
    extension(x : Title) def toNode : Node = elem("title", new Text(x.text))
  given Xmlable[Ttl] with
    extension(x : Ttl) def toNode : Node = elem("ttl", new Text(x.minutes.toString))
  given Xmlable[Url] with
    extension(x : Url) def toNode : Node = elem("url", new Text(x.location))
  given Xmlable[Webmaster] with
    extension(x : Webmaster) def toNode : Node = elem("webmaster", new Text(x.email))
  given Xmlable[Width] with
    extension(x : Width) def toNode : Node = elem("width", new Text(x.pixels.toString))






trait Xmlable[T]:
  extension(x : T) def toNode : Node


