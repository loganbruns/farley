package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/ModuleContext.scala
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

import org.gedanken.farley.parser.Message

import akka.actor.{Actor, ActorRef}
import Actor._

class ModuleContext(actor : ActorRef) {

  def ! (message: Any):Unit = {
    actor ! message
  }
}
