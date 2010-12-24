package org.gedanken.farley.parser.modules

import akka.actor.ActorRef
import scala.util.matching.Regex

class Help extends Module {

  val rules : List[Rule] = 
    new Rule(new Regex("\\(TOP \\(VB help\\)\\)") :: Nil, process) :: Nil

  def process(matcher: Regex.Match, context: ActorRef) : String = {
    return "Help for " + matcher
  }
}
