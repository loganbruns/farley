package org.gedanken.farley.parser

/**
  * 
  * parser/CLI.scala
  * 
  * Copyright 2013 Logan O'Sullivan Bruns
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

object CLI {
  val parser = new Parser(
    "./models/en-sent.bin",
    "./models/en-parser-chunking.bin",
    "/tmp/tdb"
  );

  val system = ActorSystem("CLISystem")

  val context = system.actorOf(Props(new Context), name = "message")

  class Context extends Actor with ActorLogging {
    def receive = {
      case Message(message) =>
	println(message)
    }
  }

  def main(args: Array[String]) {
    println("Hello, I'm ready to help you now.")

    var line : String = readLine
    while (line != null) {
      println(parser.process(line, context))
      line = readLine
    }
  }
}
