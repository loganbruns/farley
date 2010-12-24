package org.gedanken.farley.parser.modules

import akka.actor.ActorRef
import scala.util.matching.Regex

trait Module {

  case class Rule(regexes: List[Regex], process: (Regex.Match, ActorRef) => String)

  val rules : List[Rule]

  def evaluate(parse: String, context: ActorRef) : String = {
    for (rule <- rules) {
      for (regex <- rule.regexes) {
	val response = regex findFirstMatchIn parse match { 
	  case Some(m) => rule.process(m, context)
	  case None => null
	}

	if (response != null)
	  return response
      }
    }

    return null
  }

  def evaluate(parses: Array[String], context: ActorRef) : String = {
    for (parse <- parses) {
      val response = evaluate(parse, context)
      if (response != null)
	return response
    }

    return null
  }

}
