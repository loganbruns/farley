package org.gedanken.farley.parser

/**
  * 
  * parser/PhraseTransforms.scala
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

object PhraseTransforms {

  def makePossessive(phrase: List[Object]) : List[Object] = {
    val rtail = phrase.reverse.tail
    val rhead = phrase.reverse.head match {
      case l: List[_] => makePossessive(l.asInstanceOf[List[Object]])
      case s: String => s + "'s"
    }

    return (List(rhead) ++ rtail.reverse).reverse
  }

}

