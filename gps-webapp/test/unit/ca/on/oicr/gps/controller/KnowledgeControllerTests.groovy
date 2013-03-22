package ca.on.oicr.gps.controller

import grails.test.*
import groovy.xml.MarkupBuilder;

class KnowledgeControllerTests extends ControllerUnitTestCase {
    public void setUp() {
        super.setUp()
    }

    public void tearDown() {
        super.tearDown()
    }

	/** 
	 * Test the popup menu builder.
	 */
    void testPopupMenu() {
		def ctr = new KnowledgeController()
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		
		ctr.buildPopupMenu(xml, 
			name: "menu", 
			id: "identifier", 
			values: ["-", "prospective", "retrospective", "preclinical", "case", "observational", "unknown"], 
			labels: ["prospective": 'has been examined by PROSPECTIVE clinical trials',
					 "retrospective": 'has been examined by RETROSPECTIVE clinical trials',
					 "preclinical":   'has been examined by PRECLINICAL study',
					 "case":          'has been examined by RETROSPECTIVE case studies',
					 "observational": 'has been examined by PROSPECTIVE observational studies',
					 "unknown":       'is UNKNOWN'],
			defaultValue: "prospective",
			onchange: "if (this.value!='-' && this.value!='unknown') {k=jQuery('details').show()} else {jQuery('details').hide()}")
		
		String output = writer.toString();
		
		this.assertTrue((output =~ /<option[^>]+selected='selected'/).find())
		this.assertTrue((output =~ /<select[^>]+name='menu'/).find())
		this.assertTrue((output =~ /<select[^>]+id='identifier'/).find())
    }
	
	/**
	 * Test building an effectiveness menu 
	 */
	void testEffectivenessMenu() {
		def ctr = new KnowledgeController()
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		ctr.buildEffectivenessMenu(xml, "myid", "")
		String output = writer.toString();
		
		this.assertTrue((output =~ /<select[^>]+name='agents_effectiveness'/).find())
		this.assertFalse((output =~ /<option[^>]+selected='selected'/).find())
		this.assertFalse((output =~ /<option[^>]+label=''/).find())
		this.assertTrue((output =~ /<option[^>]+label='Known effective'/).find())
		
		writer = new StringWriter()
		xml = new MarkupBuilder(writer)
		ctr.buildEffectivenessMenu(xml, "myid", "effective")
		output = writer.toString();
		this.assertTrue((output =~ /<option[^>]+selected='selected'/).find())
	}

	/**
	 * Test the code that inserts a clinical significance through AJAX
	 */
    void testInsertSignificance() {
		def ctr = new KnowledgeController()
		
		// No set study type, display should be none
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		ctr.buildSignificance(xml, tumourType: "my tumour", studyType: "", comment: "my comment", ref: "my ref", evidence: "my evidence")
		String output = writer.toString();
		this.assertTrue((output =~ /<div[^>]+style='display:none'/).find())
		this.assertTrue((output =~ /<textarea[^>]+>my comment<\/textarea>/).find())
		this.assertTrue((output =~ /<input[^>]+type='text'[^>]+value='my ref'/).find())
		
		// Valid study type, display should be inline
		writer = new StringWriter()
		xml = new MarkupBuilder(writer)
		ctr.buildSignificance(xml, tumourType: "my tumour", studyType: "preclinical", comment: "", ref: "", evidence: "")
		output = writer.toString();
		this.assertTrue((output =~ /<div[^>]+style='display:inline'/).find())
    }
	
	/**
	 * Tests the code that builds an agent widget set
	 */
	void testBuildAgent() {
		def ctr = new KnowledgeController()
		
		// No set study type, display should be none
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		ctr.buildAgent(xml, agents: "my agent", agentsEffective: "")
		String output = writer.toString();

		this.assertTrue((output =~ /<input[^>]+type='text'[^>]+name='agents'[^>]+value='my agent'/).find())
		this.assertTrue((output =~ /<select[^>]+name='agents_effectiveness'/).find())
	}
	
	/**
	 * Tests the code that builds a sensitivity interface. 
	 */
	void testBuildSensitivity() {
		
		def ctr = new KnowledgeController()
		
		// Mock the messageSource
		ctr.messageSource = [getMessage: { String msg, args, locale -> msg }]
		
		// No set study type, display should be none
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		ctr.buildSensitivity(xml, type: "sensitive", agent: "my agent")
		String output = writer.toString();
		
		this.assertTrue((output =~ /<input[^>]+type='hidden'[^>]+name='sensitivity'[^>]+value='sensitive'[^>]*>/).find())
		this.assertTrue((output =~ /<input[^>]+type='text'[^>]+name='sensitivity_agent'[^>]+value='my agent'[^>]*>/).find())
	}
	
	/**
	 * Tests the code that builds the sensitivity control interface.
	 */
	/**
	 * Tests the code that builds a sensitivity interface. 
	 */
	void testBuildSensitivityInformation() {
		
		def ctr = new KnowledgeController()
		
		// Mock the messageSource
		ctr.messageSource = [getMessage: { String msg, args, locale -> msg }]
		
		// No set study type, display should be none
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		ctr.buildSensitivityInformation(xml)
		String output = writer.toString();
		
		this.assertTrue((output =~ /<select[^>]+name='sensitivity_resistance'[^>]*>/).find())
		this.assertTrue((output =~ /<option[^>]+label='confers resistance'[^>]*>/).find())
	}
}
