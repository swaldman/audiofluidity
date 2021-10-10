# Audiofluidity

A simple Scala 3 static site generator for podcasts


## Getting Started

### Prerequisites

You'll need a Java virtual machine installed (_audiofluidity_ is developed against a Java 11 VM).
You'll need to download the latest _audiofluidity_ release, unpack it, and place its `bin` directory in your execution `PATH`.

### Initialize a project directory

```bash
$ cd ~/tmp
$ mkdir superpodcast
$ cd superpodcast/
$ audiofluidity init
2021-10-09T12:07:15.072-0700 [INFO] audiofluidity.Audiofluidity: Podcast template initialized. 
$ 
```

That's it!

Now our `superpodcast` directory has the following structure:

```
superpodcast/
  |
  +-src/
  |  |
  |  +-scala                            <-- your podcast will be defined by Scala source files in this directory
  |  |   |
  |  |   +-AudiofluidityGenerator.scala <-- a template for your PodcastGenerator instance
  |  |
  |  +-audio                            <-- place your mp3 audio files here
  |  |
  |  +-coverimage                       <-- place your podcast cover image and optionally episode cover images here
  |  |
  |  +-docroot                          <-- place anything you want here, it will be merged with generated artifacts to form your podcast website
  |  |
  |  +-episoderoot                      <-- create subdirectories that match episode UIDs, and the content will be merged with generated episode directories
  |
  +-lib/                                <-- optionally place jar files that your podcast definition depends upon here
  |
  +-.gitignore                          <-- excludes tmp dirs and the generated output directory from version control
  |
  +-.audiofluidity                      <-- for internal use by the audiofluidity app, at least for now
        |
        +-audiofluidity.properties      <-- keeps track of the "build" that generated this directory to ensure consistency
```

### Supply initial resources

Under the [informal](https://podcasters.apple.com/support/823-podcast-requirements) [standard](https://help.apple.com/itc/podcasts_connect/#/itcb54353390)
defined by Apple, every podcast must have at least a main cover image (a square JPG or PNG between 1400 x 1400 and 3000 x 3000 pixels), and at least one
episode with an mp3 audio file. Optionally, episodes may also define cover images.

Before we can generate a podcast, we'll need to provide those resources.

```bash
$ cp ~/somewhere/some-cover-art.jpg ./src/coverimage/
$ cp ~/somewhere/something.mp3 ./src/audio
```

### Define our `PodcastGenerator`

The core of our podcast is defined in Scala. In `src/scala`, we define a class called `AudiofluidityGenerator` that implements the [`PodcastGenerator`](src/main/scala/audiofluidity/PodcastGenerator.scala) trait.
A template of this class is already defined. Let's take a look:

```scala
import audiofluidity.*
import audiofluidity.Element.Itunes

import java.time.ZoneId
import scala.collection.*

class AudiofluidityGenerator extends PodcastGenerator.Base:

  // only mandatory and near-mandatory parameters are shown in the generated template.
  // Many more parameters can and usually should be provided.
  // See the source, Episode.scala and Podcast.scala

  val episodes : List[Episode] =
    Episode(
      uid                  = ???, // String
      title                = ???, // String
      description          = ???, // String
      sourceAudioFileName  = ???, // String
      publicationDate      = ???  // String, Format: YYYY-MM-DD
    ) :: Nil

  val podcast : Podcast =
    Podcast(
      mainUrl                = ???, // String
      title                  = ???, // String
      description            = ???, // String
      guidPrefix             = ???, // String
      shortOpaqueName        = ???, // String
      mainCoverImageFileName = ???, // String
      editorEmail            = ???, // String
      defaultAuthorEmail     = ???, // String
      itunesCategories       = immutable.Seq( ??? ),             //immutable.Seq[ItunesCategory], ??? is one or several ItunesCategory values, only first was is used, not mandatory as RSS, but strongly recommended by Apple Podcast
      mbAdmin                = Some(Admin(name=???, email=???)), //Option[Admin], ??? are Strings, not mandatory as RSS, but strongly recommended by Apple Podcast
      mbLanguage             = Some(???),                        //Option[LanguageCode], not mandatory as RSS, but strongly recommended by Apple Podcast
      mbPublisher            = Some(???),                        //Option[String], not mandatory as RSS, but strongly recommended by Apple Podcast
      episodes               = episodes
    )

  // Optionally uncomment and customize the preparsedCommand if you wish audiofluidity to deploy for you.
  val deployer = new Deployer.Exec(/* preparsedCommand = immutable.Seq("rsync", "-av", ".", "user@host:/web/server/root") */)
```

Basically, we'll want to fill in all of the blanks marked `???`. We are writing Scala code here, so Strings should be provided
as double-quoted string literals. Very helpfully here, we can also use thrice-double-quoted multiline string literals.

#### Podcast-level values

Let's start with the podcast values.

* `mainUrl` is the URL to which your podcast's root directory will eventually be deployed.
We'll use `https://superpodcast.audiofluidity.com` for this example. Eventually we'll upload the site generated by `audiofluidity`
to your web server, which will be configured to serve it from this URL.
* Our title will just be `Superpodcast`.
* `description` will be the heart of your podcast's cover page. It should contain HTML, not just a short string. We'll bullshit something here,
as a multiline Scala String.
* Each episode is going to be given what is supposed to be a _global_ unique ID (UID). To do this, _audiofluidity_ will prepend a `guidPrefix` to each episode's
within-podcast UID (which will usually just be an episode number like `"1"`, so hardly globally unique. To ensure this combination yields a globally unique UID,
let's use the usual trick of basing our prefix on DNS we control. We'll use `com.audiofluidity.superpodcast-` as our prefix.
* Some generated files may want to include the name of our podcast, but since the podcast title may be long and contain spaces and punctuation, it's not
necesarily appropriate. `shortOpaqueName` is a kind of mini-title suitable for inclusion in generated file names. We'll just use `superpodcast`.
* In the prior step, we say that our `mainCoverImageFileName` was `some-cover-art.jpg`. We don't have to supply any path information. It's expected in `src/coverimage`.
* `editorEmail` is an e-mail address of the podcast editor. We'll just use `asshole@audiofluidity.com`. This becomes `managingEditor` in the podcast's RSS feed.
* Each episode should have an author (which is incorporated into the RSS feed). The podcast's `defaultAuthourEmail` becomes that author if an author is not provided
at the episode level. (It's optional there.) We'll use `asshole@audiofluidity.com` again here.
* Apple requires a category (with an optional subcategory) for each podcast it indexes. More than one catgory can be provided, but for now all but the first are ignore.
You can see all the available categories [here](src/main/audiofluidity/ItunesCategory.scala) We'll use `ItunesCategory.Comedy`.
* Apple wants an administrative contact to be provided with each podcast (which defines `webMaster` and `itunes:owner` in the generated RSS).
We'll use `Asshole` and `asshole@audiofluidity.com`.
* Apple wants a language code supplied in the RSS feed. See [LanguageCode.scala](src/main/scala/audiofluidity/LanguageCode.scala). We'll use `LanguageCode.EnglishUnitedStates`.
* Apple wants a publisher defined, just the name of an entity (which becomes `itunes:author` in the Apple-ified RSS). We'll use `Does Not Exist, LLC`.

So, filling it all in, we have...

```scala
  val podcast : Podcast =
    Podcast(
      mainUrl                = "https://superpodcast.audiofluidity.com",
      title                  = "Superpodcast",
      description            = """|<p>Superpodcast is the best podcast you've ever heard.</p>
                                  |
                                  |<p>In fact, you will never hear it.</p>""".stripMargin,
      guidPrefix             = "com.audiofluidity.superpodcast-",
      shortOpaqueName        = "superpodcast",
      mainCoverImageFileName = "some-cover-art.jpg",
      editorEmail            = "asshole@audiofluidity.com",
      defaultAuthorEmail     = "asshole@audiofluidity.com",
      itunesCategories       = immutable.Seq( ItunesCategory.Comedy ),
      mbAdmin                = Some(Admin(name="Asshole", email="asshole@audiofluidity.com")),
      mbLanguage             = Some(LanguageCode.EnglishUnitedStates),
      mbPublisher            = Some("Does Not Exist, LLC"),
      episodes               = episodes
    )
```

Note the use of Scala's multiline string and related utilities in defining the description.

This is just ordinary scala code; you can reorganize it any way the Scala language would allow.
You could define a separate variable, something like...
```scala
  val podcastDescription =
    """|<p>Superpodcast is the best podcast you've ever heard.</p>
       |
       |<p>In fact, you will never hear it.</p>""".stripMargin,  
```
for example, and then in your podcast definition...
```scala
      description            = podcastDescription,
```
in the `Podcast` constructor. But for now, we'll keep it simple and in-line.

## Developer Resources

#### Podcast RSS

* RSS 2.0 Spec https://cyber.harvard.edu/rss/rss.html
* W3C RSS2 Spec https://validator.w3.org/feed/docs/rss2.html
* Apple Podcast RSS Feed Requirements https://podcasters.apple.com/support/823-podcast-requirements
* Apple Podcaster's Guide to RSS https://help.apple.com/itc/podcasts_connect/#/itcb54353390
* Spotify Podcast Delivery Specification https://podcasters.spotify.com/terms/Spotify_Podcast_Delivery_Specification_v1.6.pdf
* RSS feed guidelines for Google Podcasts https://support.google.com/podcast-publishers/answer/9889544#required_podcast
* RDF Site Summary 1.0 Modules: Content https://web.resource.org/rss/1.0/modules/content/

Thanks https://stackoverflow.com/questions/8389872/where-is-the-official-podcast-dtd

#### Podcast RSS Validators

* https://podba.se/validate/
* https://castfeedvalidator.com/

Thanks https://stackoverflow.com/questions/55577690/podcast-validator-and-giving-the-audio-file/60586619#60586619

#### Scala 3 compilation at runtime

* https://github.com/com-lihaoyi/Ammonite/blob/master/amm/compiler/src/main/scala-3/ammonite/compiler/Parsers.scala
* https://github.com/lampepfl/dotty/blob/master/compiler/src/dotty/tools/dotc/Main.scala
* https://users.scala-lang.org/t/compile-at-runtime-using-run-to-specified-folder/6963

#### Scala XML

* https://github.com/scala/scala-xml/wiki
* https://javadoc.io/doc/org.scala-lang.modules/scala-xml_2.13/latest/
* https://index.scala-lang.org/scala/scala-xml/scala-xml/2.0.1?target=_3.x