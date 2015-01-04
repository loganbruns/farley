package org.gedanken.farley.parser.modules

/**
  * 
  * parser/module/Scanner.scala
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
import akka.routing.RoundRobinRouter
import java.io.File
import java.security.SecureRandom
import org.gedanken.farley.parser.Message
import scala.util.matching.Regex

class Scanner extends Module {

  val outputDir = new File("/var/farley/incoming")

  val dropboxDir = new File("/var/farley/www")

  val dropboxUrl = "http://docker.gedanken.org/farley"

  val system = ActorSystem("ScannerSystem")

  val rules : List[Rule] = 
    new Rule(new Regex(".*\\((NNP|VB.?) [sS]can\\).*\\(IN as\\).*\\((NNP?|[.]|CD) \"?([^)]*[^).])\"?\\.?\\).*", "action", "path_type", "path") ::
	     new Regex(".*\\((NNP|VB.?) [sS]can\\).*\\(TO to\\).*\\((NNP?|[.]|CD) \"?([^)]*[^).])\"?\\.?\\).*", "action", "path_type", "path") ::
	     Nil,
	     scan) ::
    new Rule(new Regex(".*\\(NNP [Uu]pdate\\).*\\(\\. [pP][dD][fF]s?.?\\).*") :: Nil, pdf) :: Nil

  val random = new SecureRandom
    
  val scanner = system.actorOf(Props(new ScanActor).withRouter(RoundRobinRouter(nrOfInstances = 10)))

  case class Scan(context: ActorRef, name: String, path: String)

  class ScanActor extends Actor with ActorLogging {

    def scan(context: ActorRef, name: String, path: String): Unit = {
      log.info("Received scan request for " + name)

      val pngpath = path.substring(0, path.length()-4) + ".png"
      
      var process = new ProcessBuilder("timeout", "75", "/app/phantomjs/bin/phantomjs", "/app/scanner/officejet.js", pngpath).start()

      log.info("Initiated scan to " + pngpath)

      process.waitFor()

      log.info("Finished scan to " + pngpath)

      tailor ! Scan(context, name, path)
    }

    def receive = {
      case Scan(context, name, path) =>
	scan(context, name, path)
    }
  }

  val tailor = system.actorOf(Props(new TailorActor).withRouter(RoundRobinRouter(nrOfInstances = 10)))

  class TailorActor extends Actor with ActorLogging {

    def tailor(context: ActorRef, name: String, path: String): Unit = {
      log.info("Received tailor request for " + name)

      val pngpath = path.substring(0, path.length()-4) + ".png"
      
      log.info("Verifying scan to " + pngpath)

      var process = new ProcessBuilder("identify", pngpath).start()

      if (process.waitFor() != 0) {
	log.info("Verification of scan to " + name + " failed. Removing file.")
	context ! Message("Verification of scan to " + name + " failed. Removing file.")
	new File(pngpath).delete()
	return
      }

      process = new ProcessBuilder("scantailor-cli", "--color-mode=color_grayscale", "--margins=2", pngpath, outputDir.getCanonicalPath()).start()

      if (process.waitFor() != 0) {
	log.info("Scan tailoring of scan to " + name + " failed. Removing files.")
	context ! Message("Scan tailoring of scan to " + name + " failed. Removing files.")
	new File(pngpath).delete()
	new File(path).delete()
	return
      }
      new File(pngpath).delete()

      process = new ProcessBuilder("identify", path).start()

      if (process.waitFor() != 0) {
	log.info("Verification of scan to " + name + " failed. Removing file.")
	context ! Message("Verification of scan to " + name + " failed. Removing file.")
	new File(path).delete()
	return
      }

      val buf = new Array[Byte](4096)
      val description = new String(buf, 0, process.getInputStream().read(buf))
      log.info("Verified scan to " + name + " as " + description)
      context ! Message("Verified scan to " + name + " as " + description)

      val previewName = "scan-" + random.nextLong
      val previewDir = new File(dropboxDir, previewName)
      previewDir.mkdir
      val pngname = name.substring(0, name.length()-4) + ".png"
      val preview = new File(previewDir, pngname)

      process = new ProcessBuilder("convert", path, preview.getCanonicalPath()).start()
      if (process.waitFor() == 0)
	context ! Message(dropboxUrl + "/" + previewName + "/" + pngname)
    }

    def receive = {
      case Scan(context, name, path) =>
	tailor(context, name, path)
    }
  }

  def scan(matcher: Regex.Match, context: ActorRef) : String = {
    val path = matcher group "path"
    if (!path.endsWith(".tif"))
      return "Unsupported file format " + path

    val file = new File(outputDir, path)
    if (file.exists())
      return "Output file already exists: " + path

    scanner ! Scan(context, path, file.getCanonicalPath())

    return "Scanning to path " + path 
  }

  val pdfgenerator = system.actorOf(Props(new PDFActor), name = "pdfgenerator")

  case class PDF(context: ActorRef, prefix: String)

  class PDFActor extends Actor with ActorLogging {

    def pdf(context: ActorRef, prefix: String): Unit = {
      val pdfname = prefix + ".pdf"

      log.info("Received pdf generation request for " + pdfname)

      val args = new java.util.ArrayList[String]()

      args.add("convert")

      for (f <- outputDir.list if f.startsWith(prefix)) 
	args.add((new File(outputDir, f)).getCanonicalPath())

      args.add("-units");
      args.add("PixelsPerInch");

      args.add("-density");
      args.add("72");

      args.add((new File(outputDir, pdfname)).getCanonicalPath())

      var process = new ProcessBuilder(args).start()

      log.info("Initiated pdf generation for " + pdfname)

      process.waitFor()

      log.info("Finished pdf generation for " + pdfname)

      log.info("Verifying pdf generation for " + pdfname)

      process = new ProcessBuilder("identify", (new File(outputDir, pdfname)).getCanonicalPath()).start()

      if (process.waitFor() != 0) {
	log.info("Verification of pdf generation for " + pdfname + " failed. Removing file.")
	context ! Message("Verification of pdf generation for " + pdfname + " failed. Removing file.")
	new File(outputDir, pdfname).delete()
	return
      }

      log.info("Verified pdf generation for " + pdfname)
      context ! Message("Verified pdf generation for " + pdfname)

      val previewName = "pdf-" + random.nextLong
      val previewDir = new File(dropboxDir, previewName)
      previewDir.mkdir

      process = new ProcessBuilder("cp", 
				   (new File(outputDir, pdfname)).getCanonicalPath(),
				   (new File(previewDir, pdfname)).getCanonicalPath()).start()
      if (process.waitFor() == 0)
	context ! Message(dropboxUrl + "/" + previewName + "/" + pdfname)
    }

    def receive = {
      case PDF(context, prefix) =>
	pdf(context, prefix)
    }
  }

  def pdf(matcher: Regex.Match, context: ActorRef) : String = {
    val baseRegex = new Regex("(.*)_[0-9][0-9]?.tif", "base")

    val bases = 
      (for (f <- outputDir.list if f.matches(".*_[0-9][0-9]?.tif")) 
         yield baseRegex findFirstMatchIn f match { case Some(m) => m group "base" case None => None } 
      ) distinct

    val pdfbases = for (base <- bases if !(new File(outputDir, base + ".pdf")).exists()) yield base
    if (pdfbases.isEmpty == true)
      return "No new PDFs to create."

    for (base <- pdfbases)
      pdfgenerator ! PDF(context, base.toString)

    return "Creating " + pdfbases.size + " new PDFs."
  }
}
