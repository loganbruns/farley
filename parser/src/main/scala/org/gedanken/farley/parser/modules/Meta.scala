package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/Meta.scala
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
import org.w3.banana._
import org.w3.banana.diesel._
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex

class Meta[Rdf <: RDF, Store](ds : Store)
  (implicit ops: RDFOps[Rdf], rdfStore: RDFStore[Rdf, Try, Store])
    extends Module {
  import ops._
  import rdfStore.graphStoreSyntax._

  val dataset : Store = ds

  val rules : List[Rule] =
    new Rule(new Regex(".*\\(NP \\(PRP\\$ My\\) (.*)\\) \\(VP \\(VBZ is\\) \\(PP \\(IN at\\) \\(NP \\(NN ([^)]*)\\)\\)\\)\\).*", "subject", "location") :: Nil,
      learnLocation) :: 
    new Rule(new Regex(".*\\(SBARQ \\(WHNP \\(WP What\\)\\) \\(SQ \\(VBP do\\) \\(NP \\(PRP you\\)\\)\\) \\(\\. know\\?\\)\\)") :: Nil, describeKnowledge) :: Nil

  def learnLocation(matcher: Regex.Match, context: ActorRef) : String = {
    val subject = matcher group "subject"
    val location = matcher group "location"

    val g: Rdf#Graph = (
      bnode(subject)
        -- URI("http://gedanken.org/farley/location") ->- location
    ).graph

    rdfStore.rw(dataset, { 
      dataset.appendToGraph(URI("http://gedanken.org/farley"), g) 
    })

    return "Remembering that your " + subject + " is at " + location
  }

  def describeKnowledge(matcher: Regex.Match, context: ActorRef) : String = {
    rdfStore.r(dataset, { 
      dataset.getGraph(URI("http://gedanken.org/farley")) match {
        case Success(g) => g.toString
        case Failure(e) => "Unable to retrieve graph: " + e
      }
    }).get
  }
}
