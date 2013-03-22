package ca.on.oicr.gps.pages

import java.util.Date;

import grails.test.*
import org.htmlparser.Parser
import org.htmlparser.filters.TagNameFilter

/**
 * @author swatt
 * 
 * This test class does some round-trip testing between the data and the results
 * of the GSP rendering. The actual tests are fairly limited, as most of the actual 
 * test - apart from the data - can be mangled by internationalization. 
 */
class MailGroovyPagesTests extends GroovyPagesTestCase {
	
    public void setUp() {
        super.setUp()

	}

    public void tearDown() {
        super.tearDown()
    }

	/**
	 * Test the submission controller's ability to set up to create a new
	 * submission.
	 */
    void testMailInformation() {
		def file = new File(System.properties['base.dir'], "grails-app/views/mail/information.gsp")
		
		def model = [changes: [insert: 2, update: 1], modifiedSince: new Date()]
		
		def textString = applyTemplate(file.text, model)
		
		// Oddly, I never thought I'd be got by a context bug in a language which isn't Perl
		// I was wrong!
		assertTrue((textString =~ /New patients: 2/).find())
		assertTrue((textString =~ /Updated patients: 1/).find())
		assertTrue((textString =~ /http:/).find())
    }
}
