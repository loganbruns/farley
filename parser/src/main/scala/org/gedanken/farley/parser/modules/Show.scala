package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/Show.scala
  * 
  * Copyright 2015 Logan O'Sullivan Bruns
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
import akka.routing.RoundRobinPool
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.SecureRandom
import org.gedanken.farley.parser.{Message, PhraseTransforms, SExpression}
import org.w3.banana._
import org.w3.banana.diesel._
import scala.io.Source
import scala.sys.process.BasicIO
import scala.util.Try
import scala.util.matching.Regex

class Show[Rdf <: RDF, Store](dataset : Store)
  (implicit 
    ops: RDFOps[Rdf], 
    rdfStore: RDFStore[Rdf, Try, Store],
    sparqlOps: SparqlOps[Rdf],
    sparqlEngine: SparqlEngine[Rdf, Try, Store]
  )
    extends Module {
  import ops._
  import rdfStore.graphStoreSyntax._
  import sparqlEngine.sparqlEngineSyntax._
  import sparqlOps._

  val rules : List[Rule] =
    new Rule(new Regex(".*\\(NP \\(NNP Show\\)\\).*\\((NN|\\.) (http://[^)]*)\\).*", "literal_type", "location") :: Nil, showLiteral) :: 
    new Rule(new Regex(".*\\(NP \\(NNP Show\\)\\).*\\(NP \\(PRP\\$ my\\) (.*)\\)\\)\\)", "location") :: new Regex(".*\\(VP \\(VB Show\\)\\).*\\(NP \\(PRP\\$ my\\) (.*)\\)\\)\\)", "location") :: Nil, show) :: Nil

  val dropboxDir = new File("/var/farley/www")

  val dropboxUrl = "http://docker.gedanken.org/farley"

  val system = ActorSystem("ShowSystem")

  val random = new SecureRandom
    
  val display = system.actorOf(Props(new ShowActor).withRouter(RoundRobinPool(nrOfInstances = 5)))

  def showLiteral(matcher: Regex.Match, context: ModuleContext) : String = {
    val location = matcher group "location"

    display ! ImageRef(context, location, location)

    return "Okay, will show you " + location
  }

  def show(matcher: Regex.Match, context: ModuleContext) : String = {
    val name = matcher group "location"

    Option(showName(matcher group "location", context)) getOrElse 
    Option(showName(Try(SExpression.unapply(
      PhraseTransforms.makePossessive(
        SExpression.apply(name)) ++ List(List("NN", "image"))
    )) getOrElse null, context)).orNull
  }

  def showName(name: String, context: ModuleContext) : String = {
    if (name == null)
      return null

    val query = s"""
      |prefix farley: <http://gedanken.org/farley/>
      |
      |SELECT DISTINCT ?location WHERE {
      |  GRAPH <http://gedanken.org/farley> {
      |    "${name}" farley:location ?location
      |  }
      |}
    """.stripMargin 

    val rows = rdfStore.r(dataset, {
      dataset.executeSelect(parseSelect(query).get).get.iterator.to[List]
    }).get

    if (rows.length == 0)
      return null

    val locations: List[String] = rows.map {
      row => row("location").get.as[String].get
    }

    display ! ImageRef(context, name, locations(0))

    return "Okay, will show you " + name
  }

  case class ImageRef(context: ModuleContext, description: String, url: String)

  class ShowActor extends Actor with ActorLogging {

    def show(context: ModuleContext, description: String, url: String): Unit = {
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
}
