package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/Module.scala
  * 
  * Copyright 2013, 2015 Logan O'Sullivan Bruns
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

import akka.actor.ActorRef
import scala.util.matching.Regex

trait Module {

  case class Rule(regexes: List[Regex], process: (Regex.Match, ModuleContext) => String)

  val rules : List[Rule]

  def evaluate(parse: String, context: ModuleContext) : String = {
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

  def evaluate(parses: Array[String], context: ModuleContext) : String = {
    for (parse <- parses) {
      val response = evaluate(parse, context)
      if (response != null)
	return response
    }

    return null
  }

}
