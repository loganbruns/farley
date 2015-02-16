package org.gedanken.org.farley.parser

/**
  * 
  * parser/SExpressionTest.scala
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

import org.gedanken.farley.parser.SExpression
import org.scalatest._

class SExpressionTest extends FlatSpec with Matchers {

  behavior of "S-Expression"

  val input = "(NP (PRP$ my) (NN front) (NN door's) (NN image))"

  var sexpr : List[Object] = null

  it should "parse an s-expression into a list of lists" in {
    sexpr = SExpression.apply(input)

    (sexpr) should not be (null)
    (sexpr.length) should not be (0)
  }

  it should "turn a list of lists back into an s-expression" in {
    (SExpression.unapply(sexpr)) should be (input)
  }

}
