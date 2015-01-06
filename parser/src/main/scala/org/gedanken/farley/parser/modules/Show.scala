package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/Show.scala
  * 
  * Copyright 2014 Logan O'Sullivan Bruns
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *  http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import akka.actor._
import akka.routing.RoundRobinRouter
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.SecureRandom
import org.gedanken.farley.parser.Message
import org.w3.banana._
import org.w3.banana.diesel._
import scala.io.Source
import scala.sys.process.BasicIO
import scala.util.Try
import scala.util.matching.Regex

class Show[Rdf <: RDF, Store](dataset : Store)
  (implicit ops: RDFOps[Rdf], rdfStore: RDFStore[Rdf, Try, Store])
    extends Module {
  import ops._
  import rdfStore.graphStoreSyntax._

  val rules : List[Rule] =
    new Rule(new Regex(".*\\(NP \\(NNP Show\\)\\).*\\((NN|\\.) (http://[^)]*)\\).*", "literal_type", "location") :: Nil, showLiteral) :: Nil

  val dropboxDir = new File("/var/farley/www")

  val dropboxUrl = "http://docker.gedanken.org/farley"

  val system = ActorSystem("ShowSystem")

  val random = new SecureRandom
    
  val display = system.actorOf(Props(new ShowActor).withRouter(RoundRobinRouter(nrOfInstances = 5)))

  def showLiteral(matcher: Regex.Match, context: ActorRef) : String = {
    val location = matcher group "location"

    display ! ImageRef(context, location, location)

    return "Okay, will show you " + location
  }

  case class ImageRef(context: ActorRef, description: String, url: String)

  class ShowActor extends Actor with ActorLogging {

    def show(context: ActorRef, description: String, url: String): Unit = {
      log.info("Received show request for " + description)

      val name = "show-" + random.nextLong
      val previewDir = new File(dropboxDir, name)
      previewDir.mkdir
      val pngname = name + ".png"
      val download = new File(previewDir, name)
      val preview = new File(previewDir, pngname)

      BasicIO.transferFully(new URL(url).openStream(), new FileOutputStream(download))

      val process = new ProcessBuilder(
        "convert", 
        download.getCanonicalPath(), 
        preview.getCanonicalPath()
      ).start()

      if (process.waitFor() == 0)
	context ! Message(dropboxUrl + "/" + name + "/" + pngname)
    }

    def receive = {
      case ImageRef(context, description, url) =>
        show(context, description, url)
    }
  }

  // TODO: look up what a non-literal such as my front door is defined
  // as in the rdf store
}
