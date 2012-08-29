package org.obiba.heliotrope

import java.io.File

import scala.xml.XML

import org.specs.Specification
import org.specs.runner.JUnit4

import net.liftweb.common.Full
import net.liftweb.util.PCDataXmlParser
import net.liftweb.util.Html5

class HtmlSourceSpecsTest extends JUnit4(HtmlSourceSpecs)

object HtmlSourceSpecs extends Specification {
  
  "HTML Sources" should {
    "be well-formed" in {
      /**
       * Tests to make sure the project's XML files are well-formed.
       *
       * Finds every *.html and *.xml file in src/main/webapp (and its
       * subdirectories) and tests to make sure they are well-formed.
       */
      var failed: List[File] = Nil
      
      def handledHtml(file: String) =
	    file.endsWith(".html") || file.endsWith(".htm")
      
      def wellFormed(file: File) {
	    if (file.isDirectory)
	      for (f <- file.listFiles) wellFormed(f)
        
	    if (file.isFile && handledHtml(file.getName)) {
	      Html5.parse(new java.io.FileInputStream(file.getAbsolutePath)) match {
	        case Full(_) => // file is ok
	          case _ => failed = file :: failed
	      }
	    }
      }
      
      wellFormed(new File("src/main/webapp"))
      
      val numFails = failed.size
      if (numFails > 0) {
	    val fileStr = if (numFails == 1) "file" else "files"
	    val msg = "Malformed HTML in " + numFails + " " + fileStr + ": " + failed.mkString(", ")
	    fail(msg)
      }
      
      numFails must_== 0
    }
  }
}
