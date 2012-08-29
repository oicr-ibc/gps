package org.obiba.heliotrope

import java.io.File

import scala.xml.XML

import org.specs.Specification
import org.specs.runner.JUnit4

import net.liftweb.common.Full
import net.liftweb.util.PCDataXmlParser
import net.liftweb.util.Html5

class XmlSourceSpecsTest extends JUnit4(XmlSourceSpecs)

object XmlSourceSpecs extends Specification {
  
  "XML Sources" should {
    "be well-formed" in {
      /**
       * Tests to make sure the project's XML files are well-formed.
       *
       * Finds every *.html and *.xml file in src/main/webapp (and its
       * subdirectories) and tests to make sure they are well-formed.
       */
      var failed: List[File] = Nil
      
      def handledXml(file: String) =
	    file.endsWith(".xml")
      
      def wellFormed(file: File) {
	    if (file.isDirectory)
	      for (f <- file.listFiles) wellFormed(f)
        
	    if (file.isFile && handledXml(file.getName)) {
	      try {
	        XML.loadFile(file)
	      } catch {
	        case e: _root_.org.xml.sax.SAXParseException => failed = file :: failed
	      }
	    }
      }
      
      wellFormed(new File("src/main/webapp"))
      
      val numFails = failed.size
      if (numFails > 0) {
	    val fileStr = if (numFails == 1) "file" else "files"
	    val msg = "Malformed XML in " + numFails + " " + fileStr + ": " + failed.mkString(", ")
	    fail(msg)
      }
      
      numFails must_== 0
    }
  }
}
