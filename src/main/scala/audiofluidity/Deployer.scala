package audiofluidity

import scala.collection.*
import scala.sys.process.ProcessLogger
import com.mchange.sc.v1.log.MLogger
import com.mchange.sc.v1.log.MLevel.*

import java.nio.file.Files

object Deployer:
  given logger : MLogger = MLogger(this)

  private val processLogger = ProcessLogger(line => INFO.log(s"Deploy command output: ${line}"))

  /*
   *  The preparsed command will be exec'ed from within the podcastgen directory (ie with podcastgen as the current working directory)
   */
  class Exec( preparsedCommand : immutable.Seq[String] = Nil) extends Deployer:
    def deploy( build : Build ) : Unit =
      if preparsedCommand == Nil then throw new DeployerNotConfiguredException("No parsed command has been supplied to the deployer (of type Deployer.Exec)")
      val ppcStr =  preparsedCommand.mkString(", ")
      val cwd = build.podcastgenDir
      if !Files.exists(cwd) || !Files.isDirectory(cwd) then throw new AudiofluidityException(s"No directory '${cwd}' exists from which to deploy a generated podcast.")
      INFO.log("Executing preparsed deployment command: " + ppcStr)
      val exitCode = scala.sys.process.Process(preparsedCommand, cwd.toFile, sys.env.toSeq : _*).!(processLogger)
      if exitCode == 0 then
        INFO.log("Deployment complete.")
      else
        val msg = s"Deployment failed, nonzero exit code returned by preparsed command '${ppcStr}'"
        SEVERE.log(msg)
        throw new AudiofluidityException(msg)

trait Deployer:
  def deploy( build : Build ) : Unit
