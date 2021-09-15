package audiofluidity

import scala.collection.*
import scala.xml.{Elem,NamespaceBinding,TopScope}

object PodcastFeed:
  val RdfContentModuleNamespaceBinding = new NamespaceBinding("content","http://purl.org/rss/1.0/modules/content/", TopScope)
  val AppleNamespaceBinding = new NamespaceBinding("itunes","http://www.itunes.com/dtds/podcast-1.0.dtd", RdfContentModuleNamespaceBinding)

  // see https://help.apple.com/itc/podcasts_connect/#/itcb54353390
  object Itunes:
    enum ValidPodcastType:
      case episodic, serial
    enum ValidEpisodeType:
      case full, trailer, bonus

    case class  Author(fullName : String)
    case object Block // should contain "Yes"
    case class  Category(text : String, subcategory : Option[Itunes.Category])
    case object Complete // should contain "Yes"
    case class  Duration(seconds : Int)
    case class  Email(email : String)
    case class  Episode(number : Int)
    case class  EpisodeType(validEpisodeType : Itunes.ValidEpisodeType)
    case class  Explicit(isExplicit : Boolean)
    case class  Image(href : String)
    case class  Keywords(keywords : immutable.Seq[String])
    case class  Name(name : String)
    case class  NewFeedUrl(location : String)
    case class  Owner(name : Itunes.Name, email : Itunes.Email)
    case class  Season(number : Int)
    case class  Title(title : String)
    case class  Type(validType : Itunes.ValidPodcastType)

    def decorateStandardFeedXml(rssFeedXml : Elem) : Elem =
      rssFeedXml.copy(scope=AppleNamespaceBinding)





