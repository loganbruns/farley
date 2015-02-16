package org.gedanken.farley.parser

/**
  * 
  * parser/SExpression.scala
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

import scala.language.postfixOps
import scala.util.parsing.combinator.RegexParsers

object SExpression extends RegexParsers {
  def constant = """[^() ]+""".r ^^ { _.toString }

  def group : Parser[List[Object]] = "(" ~> terms <~ ")"

  def terms = term*

  def term = constant | group

  def apply(input: String): List[Object] = parseAll(terms, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }

  def unapply(x: Any, wrap: Boolean = false): String = x match {
    case s: String => s
    case l: List[_] => {
      val expr = l.foldLeft("")((left, right) => {
        (unapply(left, true) + " " + unapply(right, true)).trim
      })
      if (wrap)
        "(" + expr + ")"
      else
        expr
    }
  }
}
