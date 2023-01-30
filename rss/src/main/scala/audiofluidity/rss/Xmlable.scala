package audiofluidity.rss

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

import scala.xml.{Elem, MetaData, Node, Null, PCData, Text, TopScope, UnprefixedAttribute}
import Element.*
import Element.{Content, Itunes}

object Xmlable:
  private val RssDateTimeFormatter = RFC_1123_DATE_TIME

  private def elem(label : String, attributes1 : MetaData, children : Node*) : Elem =
    new Elem(prefix=null, label=label, attributes1=attributes1, scope=TopScope, minimizeEmpty=true, children : _*)
  private def elem(label : String, children : Node*) : Elem = elem(label, Null, children : _*)

  // Standard RSS Elements
  given Xmlable[Author] with
    extension(x : Author) def toElem : Elem = elem("author", new Text(x.email))
  given Xmlable[Category] with
    extension(x : Category) def toElem : Elem =
      elem("category", new UnprefixedAttribute("domain", x.domain, Null), new Text(x.text))
  given Xmlable[Channel] with
    extension(x : Channel) def toElem : Elem =
      val itemElems = x.items.map( _.toElem )
      val kids =
         itemElems ++ x.skipDays.map( _.toElem ) ++ x.skipHours.map( _.toElem ) ++ x.textInput.map( _.toElem ) ++
          x.rating.map( _.toElem ) ++ x.image.map( _.toElem ) ++ x.ttl.map( _.toElem ) ++ x.cloud.map( _.toElem ) ++ x.docs.map( _.toElem ) ++
          x.generator.map( _.toElem ) ++ x.categories.map( _.toElem ) ++ x.lastBuildDate.map( _.toElem ) ++ x.pubDate.map( _.toElem ) ++
          x.webMaster.map( _.toElem ) ++ x.managingEditor.map( _.toElem ) ++ x.copyright.map( _.toElem ) ++ x.language.map( _.toElem ) :+
          x.description.toElem :+ x.link.toElem :+ x.title.toElem
      elem("channel",kids.toSeq : _*)
  given Xmlable[Cloud] with
    extension(x : Cloud) def toElem : Elem =
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
    extension(x : Comments) def toElem : Elem = elem("comments", new Text(x.url))
  given Xmlable[Copyright] with
    extension(x : Copyright) def toElem : Elem = elem("copyright", new Text(x.notice))
  given Xmlable[Day] with
    extension(x : Day) def toElem : Elem = elem("day", new Text(x.day.toString))
  given Xmlable[Description] with
    extension(x : Description) def toElem : Elem = elem("description", new PCData(x.text))
  given Xmlable[Docs] with
    extension(x : Docs) def toElem : Elem = elem("docs", new Text(x.url))
  given Xmlable[Enclosure] with
    extension(x : Enclosure) def toElem : Elem =
      val attributes = UnprefixedAttribute("url", x.url,
        new UnprefixedAttribute("length", x.length.toString,
          new UnprefixedAttribute("type", x.`type`, Null)
        )
      )
      elem("enclosure", attributes)
  given Xmlable[Generator] with
    extension(x : Generator) def toElem : Elem = elem("generator", new Text(x.description))
  given Xmlable[Guid] with
    extension(x : Guid) def toElem : Elem = elem("guid", new UnprefixedAttribute("isPermalink", x.isPermalink.toString, Null), new Text(x.id))
  given Xmlable[Hour] with
    extension(x : Hour) def toElem : Elem = elem("hour", new Text(x.hour.toString))
  given Xmlable[Height] with
    extension(x : Height) def toElem : Elem = elem("height", new Text(x.pixels.toString))
  given Xmlable[Image] with
    extension(x : Image) def toElem : Elem =
      val reverseKids : List[Elem] =
        x.description.map(_.toElem) ++: x.height.map(_.toElem) ++: x.width.map( _.toElem ) ++: (x.link.toElem :: x.title.toElem :: x.url.toElem :: Nil)
      elem("image", reverseKids.reverse : _*)
  given Xmlable[Item] with
    extension(x : Item) def toElem : Elem =
      val kids =
        x.categories.map( _.toElem ) ++ x.source.map( _.toElem ) ++ x.pubDate.map( _.toElem ) ++ x.guid.map( _.toElem ) ++ x.enclosure.map( _.toElem ) ++
          x.comments.map( _.toElem) :+ x.author.toElem :+ x.description.toElem :+ x.link.toElem :+ x.title.toElem
      elem("item", kids : _*)
  given Xmlable[Language] with
    extension(x : Language) def toElem : Elem = elem("language", new Text(x.code.rendered))
  given Xmlable[LastBuildDate] with
    extension(x : LastBuildDate) def toElem : Elem =
      val dateStr = x.date.format(RssDateTimeFormatter)
      elem("lastBuildDate", new Text(dateStr))
  given Xmlable[Link] with
    extension(x : Link) def toElem : Elem = elem("link", new Text(x.location))
  given Xmlable[ManagingEditor] with
    extension(x : ManagingEditor) def toElem : Elem = elem("managingEditor", new Text(x.email))
  given Xmlable[Name] with
    extension(x : Name) def toElem : Elem = elem("name", new Text(x.text))
  given Xmlable[PubDate] with
    extension(x : PubDate) def toElem : Elem =
      val dateStr = x.date.format(RssDateTimeFormatter)
      elem("pubDate", new Text(dateStr))
  given Xmlable[Rating] with
    extension(x : Rating) def toElem : Elem = elem("rating", new Text(x.contents))
  given Xmlable[Rss] with
    extension(x : Rss) def toElem : Elem =
      elem("rss", new UnprefixedAttribute("version", RssVersion, Null), x.channel.toElem)
  given Xmlable[SkipDays] with
    extension(x : SkipDays) def toElem : Elem = elem("skipDays", x.days.map( _.toElem ) : _*)
  given Xmlable[SkipHours] with
    extension(x : SkipHours) def toElem : Elem = elem("skipHours", x.hours.map( _.toElem ) : _*)
  given Xmlable[Source] with
    extension(x : Source) def toElem : Elem = elem("source", new UnprefixedAttribute("url",x.url,Null), new Text(x.title))
  given Xmlable[TextInput] with
    extension(x : TextInput) def toElem : Elem = elem("textInput", x.title.toElem, x.description.toElem, x.name.toElem, x.link.toElem)
  given Xmlable[Title] with
    extension(x : Title) def toElem : Elem = elem("title", new Text(x.text))
  given Xmlable[Ttl] with
    extension(x : Ttl) def toElem : Elem = elem("ttl", new Text(x.minutes.toString))
  given Xmlable[Url] with
    extension(x : Url) def toElem : Elem = elem("url", new Text(x.location))
  given Xmlable[WebMaster] with
    extension(x : WebMaster) def toElem : Elem = elem("webMaster", new Text(x.email))
  given Xmlable[Width] with
    extension(x : Width) def toElem : Elem = elem("width", new Text(x.pixels.toString))

  // Apple-specific elements
  private def ielem(label : String, attributes1 : MetaData, children : Node*) : Elem =
    new Elem(prefix="itunes", label=label, attributes1=attributes1, scope=TopScope, minimizeEmpty=true, children : _*)
  private def ielem(label : String, children : Node*) : Elem = ielem(label, Null, children : _*)

  given given_Xmlable_Itunes_Author : Xmlable[Itunes.Author] with
    extension(x : Itunes.Author) def toElem : Elem = ielem("author", new Text(x.fullName))
  given Xmlable[Itunes.Block.type] with
    extension(x : Itunes.Block.type ) def toElem : Elem = ielem("block", new Text("Yes"))
  given given_Xmlable_Itunes_Category : Xmlable[Itunes.Category] with
    extension(x : Itunes.Category) def toElem : Elem =
      ielem("category", new UnprefixedAttribute("text", x.text, Null), x.subcategory.map( _.toElem ).toSeq : _*)
  given Xmlable[Itunes.Complete.type] with
    extension(x : Itunes.Complete.type ) def toElem : Elem = ielem("complete", new Text("Yes"))
  given Xmlable[Itunes.Duration] with
    extension(x : Itunes.Duration) def toElem : Elem = ielem("duration", new Text(x.seconds.toString))
  given Xmlable[Itunes.Email] with
    extension(x : Itunes.Email) def toElem : Elem = ielem("email", new Text(x.email))
  given Xmlable[Itunes.Episode] with
    extension(x : Itunes.Episode) def toElem : Elem = ielem("episode", new Text(x.number.toString))
  given Xmlable[Itunes.EpisodeType] with
    extension(x : Itunes.EpisodeType) def toElem : Elem = ielem("episodeType", new Text(x.validEpisodeType.toString))
  given Xmlable[Itunes.Explicit] with
    extension(x : Itunes.Explicit) def toElem : Elem = ielem("explicit", new Text(x.isExplicit.toString))
  given given_Xmlable_Itunes_Image : Xmlable[Itunes.Image] with
    extension(x : Itunes.Image) def toElem : Elem =
      ielem("image", new UnprefixedAttribute("href", x.href, Null))
  given Xmlable[Itunes.Keywords] with
    extension(x : Itunes.Keywords) def toElem : Elem = ielem("keywords", new Text(x.keywords.mkString(",")))
  given given_Xmlable_Itunes_Name : Xmlable[Itunes.Name] with
    extension(x : Itunes.Name) def toElem : Elem = ielem("name", new Text(x.name))
  given Xmlable[Itunes.NewFeedUrl] with
    extension(x : Itunes.NewFeedUrl) def toElem : Elem = ielem("new-feed-url", new Text(x.location))
  given Xmlable[Itunes.Owner] with
    extension(x : Itunes.Owner) def toElem : Elem = ielem("owner", x.name.toElem, x.email.toElem)
  given Xmlable[Itunes.Season] with
    extension(x : Itunes.Season) def toElem : Elem = ielem("season", new Text(x.number.toString))
  given Xmlable[Itunes.Subtitle] with
    extension(x : Itunes.Subtitle) def toElem : Elem = ielem("subtitle", new Text(x.text))
  given Xmlable[Itunes.Summary] with
    extension(x : Itunes.Summary) def toElem : Elem = ielem("summary", new Text(x.text))
  given given_Xmlable_Itunes_Title : Xmlable[Itunes.Title] with
    extension(x : Itunes.Title) def toElem : Elem = ielem("title", new Text(x.title))
  given Xmlable[Itunes.Type] with
    extension(x : Itunes.Type) def toElem : Elem = ielem("type", new Text(x.validType.toString))

  // RDF content elements
  given Xmlable[Content.Encoded] with
    extension(x : Content.Encoded ) def toElem : Elem = new Elem(prefix="content", label="encoded", attributes1=Null, scope=TopScope, minimizeEmpty=true, new PCData(x.text))

trait Xmlable[T]:
  extension(x : T) def toElem : Elem


