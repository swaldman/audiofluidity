package audiofluidity

import java.io.{BufferedInputStream, FileNotFoundException}
import java.nio.file.{Files, Path}
import java.util.Properties

import com.mchange.sc.v1.log.MLogger
import com.mchange.sc.v1.log.MLevel.*

object Audiofluidity {

  java.util.logging.LogManager.getLogManager.readConfiguration(this.getClass.getClassLoader.getResourceAsStream("logging.properties"))

  given logger : MLogger = MLogger(this)

  private val PrimordialBuildFqcn = "audiofluidity.Build"

  @main def go(args : String*) : Unit =

    val buildClassFqcn =
      (Option( sys.props(BuildClassSysProp) ) orElse sys.env.get(BuildClassEnvVar)).getOrElse(PrimordialBuildFqcn)

    val baseDirPath =
      (Option( sys.props(BaseDirSysProp) ) orElse sys.env.get(BaseDirEnvVar)).map(p => Path.of(p)).getOrElse(Path.of("")) // default to current working dir

    val build =
      try Class.forName(buildClassFqcn).getDeclaredConstructor(classOf[Path]).newInstance(baseDirPath).asInstanceOf[Build]
      catch
        case t : Throwable =>
          SEVERE.log(s"Could not load build class '${buildClassFqcn}'", t)
          throw t

    val podcastSourceFqcn = DefaultPodcastGeneratorFqcn

    val sep = sys.props("path.separator")
    val libJars = build.libJars.mkString(sep)
    val appClassPath = sys.props("java.class.path") + sep + build.tmpClassesDir + sep + libJars

    def buildPodcastGenerator() : PodcastGenerator =
      try
        INFO.log("Compiling scala-defined podcast generator...")
        val podcastGenerator = compileGenerator( podcastSourceFqcn, appClassPath, build )
        INFO.log("Compilation of scala-defined podcast generator succeeded.")
        podcastGenerator
      catch
        case ite : java.lang.reflect.InvocationTargetException if ite.getCause.isInstanceOf[scala.NotImplementedError] =>
          SEVERE.log("Please replace all fields marked '???' with valid values in your podcast generator!")
          throw ite.getCause

    val cl = this.getClass.getClassLoader

    if args.nonEmpty then
      val command = args.head
      command match
        case "init" =>
          build.initDirs()
          fillInResources(build.buildResourceBase, build.buildResources, build.baseDir, cl)
          println("Podcast template initialized.")
        case "clean" =>
          recursiveDeleteDirectory(build.tmpDir, leaveTop = true)
          recursiveDeleteDirectory(build.podcastgenDir, leaveTop = true)
        case "generate" =>
          val generator = buildPodcastGenerator()
          val layout   = generator.layout
          val renderer = generator.renderer
          val podcast  = generator.podcast
          INFO.log(s"Adding renderer-defined documents to '${build.podcastgenDir}' directory before generation, if overriding documents are not already defined.")
          fillInResources(renderer.staticResourceBase, renderer.staticResources, build.podcastgenDir, cl, overwrite=true)
          INFO.log("Generating podcast website and RSS feed.")
          generate( build, layout, renderer, podcast )
          INFO.log(s"Successfully generated podcast '${podcast.title}'.")
        case _ => usage()
    else usage()

  def usage() : Unit =
    println("Usage: audiocity <command>")
    println("Commands:")
    println("  clean    -- Deletes generated artifacts from build directory (usually the current working directory)")
    println("  generate -- Compiles and generates a configured podcast")
    println("  init     -- Creates template for a podcast in the build directory (usually the current working directory)")
}
