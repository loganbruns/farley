package org.gedanken.farley.parser

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
