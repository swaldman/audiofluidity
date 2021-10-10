# Audiofluidity

A simple Scala-cenric static site generator for podcasts

  * [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Initialize a project directory](#initialize-a-project-directory)
    * [Supply initial resources](#supply-initial-resources)
    * [Define the PodcastGenerator](#define-the-podcastgenerator)
      * [Podcast\-level values](#podcast-level-values)
      * [Episode\-level values](#episode-level-values)
      * [Putting it all together](#putting-it-all-together)
    * [Generating your podcast](#generating-your-podcast)
    * [Customizing your podcast website](#customizing-your-podcast-website)
    * [Deploying your podcast](#deploying-your-podcast)
    * [Testing and submitting your podcast](#testing-and-submitting-your-podcast)
  * [Example podcast](#example-podcast)
  * [Developer Resources](#developer-resources)
      * [Podcast RSS](#podcast-rss)
      * [Podcast RSS Validators](#podcast-rss-validators)
      * [Scala 3 compilation at runtime](#scala-3-compilation-at-runtime)
      * [Scala XML](#scala-xml)

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

### Define the PodcastGenerator

You have to supply a fair amount of information to generate a podcast in the style Apple and other podcast indexers now expect. With `audiofluidity`, this information is defined in Scala.

In `src/scala`, we define a class called `AudiofluidityGenerator` that implements the [`PodcastGenerator`](src/main/scala/audiofluidity/PodcastGenerator.scala) trait.
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

* `mainUrl` is the URL to which your podcast's root directory will eventually be deployed. It should end with a `/` character.
We'll use `https://superpodcast.audiofluidity.com/` for this example. Eventually we'll upload the site generated by `audiofluidity`
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
      mainUrl                = "https://superpodcast.audiofluidity.com/",
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

There are many more, optional, values we could have supplied in defining our `Podcast`. See the [source code](src/main/scala/audiofluidity/Podcast.scala) for all the rest!

#### Episode-level values

We have lots fewer values that we _have_ to supply for an episode (although here too, there are [many optional fields](src/main/scala/audiofluidity/Episode.scala) we can supply).
Let's look back at our template, and go through them.

* `uid` is a unique identifier of each episode _within this podcast_. It doesn't have to be globally unique. Usually we'll want numbered episodes, so this
should just be an episode number, like `"1"`.

* `title` is just the title of your episode. Let's use `The Fish is Dead`, because why not?

* `description` is the HTML text that podcasters often refer to as "show notes". This can and usually should contain links to related resources!

* `sourceAudioFileName` is the name of the episode mp3 file in `src/audio`. Ours was called `something.mp3`.

* A `publicationDate` is required, in `YYYY-MM-DD` format. We'll use `2021-10-10`.

#### Putting it all together

Putting it all together, here is our filled-in file:

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
      uid                  = "1",
      title                = "The Fish is Dead",
      description          = """|<p>It's true.</p>
                                |
                                |<p>The fish is dead.</p>
                                |
                                |<p><b>Related Links</b></p>
                                |
                                |<ul>
                                |  <li><a href="https://symbolismandmetaphor.com/dead-fish-meaning-symbolism/">Dead Fish Meaning and Symbolism</a></li>
                                |</ul>""".stripMargin,
      sourceAudioFileName  = "something.mp3",
      publicationDate      = "2021-10-10"
    ) :: Nil

  val podcast : Podcast =
    Podcast(
      mainUrl                = "https://superpodcast.audiofluidity.com/",
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

  // Optionally uncomment and customize the preparsedCommand if you wish audiofluidity to deploy for you.
  val deployer = new Deployer.Exec(/* preparsedCommand = immutable.Seq("rsync", "-av", ".", "user@host:/web/server/root") */)
```

### Generating your podcast

Once your cover art and audio have been supplied and your `AudiofluidityGenerator.scala` file is defined,
generating your podcast is easy.

```bash
$ audiofluidity generate
2021-10-10T00:49:35.084-0700 [INFO] audiofluidity.Audiofluidity: Compiling scala-defined podcast generator... 
2021-10-10T00:49:37.275-0700 [INFO] audiofluidity.Audiofluidity: Compilation of scala-defined podcast generator succeeded. 
2021-10-10T00:49:37.277-0700 [INFO] audiofluidity.Audiofluidity: Adding renderer-defined documents to 'src/docroot' directory before generation, if overriding documents are not already defined. 
2021-10-10T00:49:37.281-0700 [INFO] audiofluidity.Audiofluidity: Generating podcast website and RSS feed. 
2021-10-10T00:49:37.394-0700 [INFO] audiofluidity.Audiofluidity: Successfully generated podcast 'Superpodcast'.
```

You'll find that _audiofluidity_ will have defined two new directories, `tmp` and `podcastgen`. Your podcast static site lives at `podcastgen`.

### Customizing your podcast website

The core of your site is its rss feed, defined as `feed.rss` in that directory. However, this is also a website, which you can open in a browser. You'll see that, for now, it is a very rudimentary
and ugly website. You can make it less awful via CSS. Elements of the generated HTML files contain class attributes to help you style the output. You'll find an initial
CSS file that you can augment and modify in `src/docroot`.

If you want to control the structure of the generated HTML, rather than merely style the default documents, you'll have to define your own [`Renderer`](src/main/scala/audiofluidity/Renderer.scala).
If you do this, just add
```scala
  override val renderer : Renderer = new MyCustomRenderer()
```
to your `AudiofluidityGenerator.scala` file.

### Deploying your podcast

You can always deploy your podcast by hand, uploading it however you upload it to your webserver.

But if there is a simple command you can run to deply, you can provide that in your `AudiofluidityGenerator.scala` file. Uncomment and replace the `preparsedCommand` in the following line:
```scala
  val deployer = new Deployer.Exec(/* preparsedCommand = immutable.Seq("rsync", "-av", ".", "user@host:/web/server/root") */)
```
The command you provide (the first item of the `Seq`) will be run, _with your `podcastgen` output directory as its current working directory_. So the directory you will want to upload is just `.`
The items in the `Seq` after the command names are the arguments to the command you wish to run.

Once you provide a deployment command, `audiofuidity deploy` will ensure that your site is generated from its current source, and then run the deployment command. Here's an example.

```bash
$ audiofluidity deploy
2021-10-10T00:02:41.138-0700 [INFO] audiofluidity.Audiofluidity: Compiling scala-defined podcast generator... 
2021-10-10T00:02:41.188-0700 [INFO] audiofluidity.Audiofluidity: Compilation of scala-defined podcast generator succeeded. 
2021-10-10T00:02:41.189-0700 [INFO] audiofluidity.Audiofluidity: Adding renderer-defined documents to 'src/docroot' directory before generation, if overriding documents are not already defined. 
2021-10-10T00:02:41.203-0700 [INFO] audiofluidity.util.package: File 'src/docroot/podcast.css' exists already. Leaving as-is, NOT overwriting with classloader resource 'initsite/podcastgen/podcast.css'. 
2021-10-10T00:02:41.204-0700 [INFO] audiofluidity.Audiofluidity: Generating podcast website and RSS feed. 
2021-10-10T00:02:41.339-0700 [INFO] audiofluidity.util.package: Skipping copy of 'HelloScratchfluidity.mp3' as destination is newer. 
2021-10-10T00:02:41.347-0700 [INFO] audiofluidity.util.package: Skipping copy of 'podcast.css' as destination is newer. 
2021-10-10T00:02:41.348-0700 [INFO] audiofluidity.util.package: Skipping copy of 'notebook.gif' as destination is newer. 
2021-10-10T00:02:41.348-0700 [INFO] audiofluidity.util.package: Skipping copy of 'readme.txt' as destination is newer. 
2021-10-10T00:02:41.349-0700 [INFO] audiofluidity.util.package: Skipping copy of 'double-bubble-dark.png' as destination is newer. 
2021-10-10T00:02:41.350-0700 [INFO] audiofluidity.Audiofluidity: Successfully generated podcast 'Scratchfluidity', will now deploy. 
2021-10-10T00:02:41.351-0700 [INFO] audiofluidity.Audiofluidity: Deploying generated podcast from 'podcastgen' 
2021-10-10T00:02:41.352-0700 [INFO] audiofluidity.Deployer: Executing preparsed deployment command: "rsync", "-av", ".", "swaldman@tickle.mchange.com:/home/web/public/audiofluidity-scratch" 
2021-10-10T00:02:42.407-0700 [INFO] audiofluidity.Deployer: Deploy command output: building file list ... done 
2021-10-10T00:02:42.468-0700 [INFO] audiofluidity.Deployer: Deploy command output: ./ 
2021-10-10T00:02:43.043-0700 [INFO] audiofluidity.Deployer: Deploy command output: feed.rss 
2021-10-10T00:02:43.043-0700 [INFO] audiofluidity.Deployer: Deploy command output: index.html 
2021-10-10T00:02:43.043-0700 [INFO] audiofluidity.Deployer: Deploy command output: podcast.css 
2021-10-10T00:02:43.044-0700 [INFO] audiofluidity.Deployer: Deploy command output: scratchfluidity-coverart.jpg 
2021-10-10T00:02:43.050-0700 [INFO] audiofluidity.Deployer: Deploy command output: episodes/ 
2021-10-10T00:02:43.050-0700 [INFO] audiofluidity.Deployer: Deploy command output: episodes/episode-1/ 
2021-10-10T00:02:43.050-0700 [INFO] audiofluidity.Deployer: Deploy command output: episodes/episode-1/index.html 
2021-10-10T00:02:43.050-0700 [INFO] audiofluidity.Deployer: Deploy command output: episodes/episode-1/scratchfluidity-audio-episode-1.mp3 
2021-10-10T00:02:43.059-0700 [INFO] audiofluidity.Deployer: Deploy command output: image/ 
2021-10-10T00:02:43.060-0700 [INFO] audiofluidity.Deployer: Deploy command output: image/notebook.gif 
2021-10-10T00:02:43.060-0700 [INFO] audiofluidity.Deployer: Deploy command output: image/double-bubble-dark/ 
2021-10-10T00:02:43.060-0700 [INFO] audiofluidity.Deployer: Deploy command output: image/double-bubble-dark/double-bubble-dark.png 
2021-10-10T00:02:43.060-0700 [INFO] audiofluidity.Deployer: Deploy command output: image/double-bubble-dark/readme.txt 
2021-10-10T00:02:43.300-0700 [INFO] audiofluidity.Deployer: Deploy command output:  
2021-10-10T00:02:43.301-0700 [INFO] audiofluidity.Deployer: Deploy command output: sent 10835 bytes  received 13032 bytes  9546.80 bytes/sec 
2021-10-10T00:02:43.301-0700 [INFO] audiofluidity.Deployer: Deploy command output: total size is 2177221  speedup is 91.22 
2021-10-10T00:02:43.302-0700 [INFO] audiofluidity.Deployer: Deployment complete. 
2021-10-10T00:02:43.302-0700 [INFO] audiofluidity.Audiofluidity: Deployment complete. 
```

### Testing and submitting your podcast

Once deployed, you can use resources like https://castfeedvalidator.com/ and https://podba.se/validate/ to validate your podcast feed.

Users of apps that accept podcast RSS feed URLs will immediately be able to subscribe to your podcast!

When your feed validates, follow the directions under [Submit an RSS feed](https://podcasters.apple.com/support/897-submit-a-show) to get your podcast into Apple Podcasts.
See also [Spotify](https://podcasters.spotify.com/) and [Google](https://support.google.com/podcast-publishers/answer/10315648).

## Example podcast

_audiofluidity_ is very fresh software, But you can see a test site (not yet submitted to Apple or any other podcast aggregators) at https://scratch.audiofluidity.com/

You can subscribe to the podcast with apps that accept Podcast RSS feeds.

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