package org.gedanken.farley.parser

import akka.actor.ActorRef
import com.hp.hpl.jena.tdb.TDBFactory
import com.hp.hpl.jena.query.Dataset
import opennlp.tools.parser._
import opennlp.tools.sentdetect._
import opennlp.tools.cmdline.parser.ParserTool
import org.gedanken.farley.parser.modules._
import org.w3.banana.jena.Jena
import java.io.FileInputStream

class Parser(
  sentenceModelPath : String, 
  parserModelPath : String,
  dataSetPath : String) {

  println("Loading sentence model.")
  private val sentenceModel = new SentenceModel(new FileInputStream(sentenceModelPath))
  private val detector = new SentenceDetectorME(sentenceModel)
  println("Done loading sentence model.")

  println("Loading parser model.")
  private val parserModel = new ParserModel(new FileInputStream(parserModelPath))
  private val parser = ParserFactory.create(parserModel)
  println("Done loading parser model.")

  println("Loading dataset.")
  private val dataset = TDBFactory.createDataset(dataSetPath)
  println("Done loading dataset.")

  println("Loading modules.")
  private val modules = 
    new Help() :: new Meta[Jena, Dataset](dataset) :: new Scanner() :: Nil
  println("Done loading modules.")

  def process(input: String, context: ActorRef) : String = {

    var nlpOnly = false;
    var sentences =
      if (!input.startsWith("?")) 
	detector.sentDetect(input) 
      else { 
	nlpOnly = true
	detector.sentDetect(input.substring(1))
      }

    val response = new StringBuilder();
    val buffer = new StringBuffer();
    for (sentence <- sentences) {
      var parses = ParserTool.parseLine(sentence, parser, 5)

      val variants : Array[String] =
	for (parse <- parses) yield {
	  buffer.setLength(0)
	  parse.show(buffer);

	  buffer.toString()
	}

      if (nlpOnly)
	for (variant <- variants) {
	  response.append(variant)
	  response.append("\n")
	}
      else {
	for (module <- modules) {
	  val result = module.evaluate(variants, context)
	  if (result != null) {
	    response.append(result)
	    response.append("\n")
	  }
	}
      }
    }

    if (response.length() > 0)
      return response.toString()
    else
      return "I'm sorry I didn't understand."
  }
}
