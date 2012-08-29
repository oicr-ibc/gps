package ca.on.oicr.gps.controller

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import groovy.xml.MarkupBuilder;
import grails.plugins.springsecurity.Secured

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import ca.on.oicr.gps.MissingMutationException;
import ca.on.oicr.gps.model.knowledge.AgentEffectiveness;
import ca.on.oicr.gps.model.knowledge.AgentSensitivity;
import ca.on.oicr.gps.model.knowledge.KnownGene;
import ca.on.oicr.gps.model.knowledge.KnownMutation;
import ca.on.oicr.gps.model.knowledge.ClinicalSignificance;
import ca.on.oicr.gps.model.knowledge.KnownMutationFrequency;
import ca.on.oicr.gps.model.knowledge.KnownTumourType;
import ca.on.oicr.gps.model.knowledge.MutationCharacteristics;
import ca.on.oicr.gps.model.knowledge.MutationConfirmation;

@Secured(['ROLE_GPS-USERS'])
class KnowledgeController implements ApplicationContextAware {

	def messageSource
	def grailsTemplateEngineService
	def springSecurityService
	def userListService
	def currentUserService
	ApplicationContext applicationContext
	
	final static int FREQUENCY_LIMIT = 9
	
	private String getMessage(String) {
		def locale = RequestContextUtils.getLocale(request)
		return messageSource.getMessage(String, [].toArray(), locale)
	}
	
	private String getMessage(String, Object[] args) {
		def locale = RequestContextUtils.getLocale(request)
		return messageSource.getMessage(String, args, locale)
	}
	
    def index = { }
	
	def gene() {
		def gene = KnownGene.findByName(params.gene)
		[gene: gene]
	}
	
	private def getMutationModelFromLabel(String label) {
		
		def mutation
		if (label != null) {
			mutation = KnownMutation.findMutationByLabel(label)
		}
		
		if (mutation == null) {
			throw new MissingMutationException(label)
			assert false
		}
		
		// It is actually helpful to get the COSMIC information ready to hand, assuming
		// there is some, rather than expecting the view to do this.
		
		[mutation: mutation]
	}
	
	private def getMutationModel() {
		return getMutationModelFromLabel(params.mutation)
	}
	
	def edit = {
		return getMutationModel()
	}
	
	def mutation = {
		return getMutationModel()
	}
	
	def editGene = {
		KnownGene gene = KnownGene.findByName(params.gene)
		return [gene: gene]
	}
	
	private String getReportXML(Map model) {
				
		String reportXml = grailsTemplateEngineService.renderView("/knowledge/_mutationReportXML", model)
		return reportXml
	}
	
	private byte[] getReportPDF(String reportXml) {
		FopFactory fopFactory = FopFactory.newInstance();
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

		// Finding the base URL can be hard
		String baseURL = request.contextPath + "/"
		foUserAgent.setBaseURL(baseURL)
		
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		OutputStream out = new BufferedOutputStream(data);

		InputStream xsltStream = applicationContext.getResource("/WEB-INF/xml/fop.xsl").getInputStream()
		Source xslt = new StreamSource(xsltStream)

		try {
			// Step 3: Construct fop with desired output format
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
					  
			// Step 4: Setup JAXP using identity transformer
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(xslt);
		  
			// Step 5: Setup input and output for XSLT transformation
			// Setup input stream
			Source src = new StreamSource(new BufferedReader(new StringReader(reportXml)));
		  
			// Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new SAXResult(fop.getDefaultHandler());
		  
			// Step 6: Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		  
		} finally {
			//Clean-up
			out.close();
		}
		
		return data.toByteArray()
	}
		
	def download = {
		
		if (params.id) {
			MutationConfirmation confirm = MutationConfirmation.get(params.id)
			response.contentType = MimeConstants.MIME_PDF;
			response.outputStream << confirm.pdf
			return
		}
		
		// We used to generate a report here dynamically. We don't actually need to 
		// do that any more. We can just fail if we like
		
		assert params.id
	}
	
	/**
	 * An action that exports all the data from the knowledge base, in an XML format, 
	 * approximately anyway. 
	 */
	def export() {
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		
		xml.data {
			
			xml.genes {
				for(KnownGene gene : KnownGene.list()) {
					xml.gene(name: gene.name) {
						xml.chromosome(gene.chromosome)
						xml.start(gene.start)
						xml.stop(gene.stop)
						xml.geneSize(gene.geneSize)
						
						if (gene.characteristics != null) {
							xml.characteristics {
								xml.fullName(gene.characteristics.fullName)
								xml.somaticTumorTypes(gene.characteristics.somaticTumorTypes)
								xml.germlineTumorTypes(gene.characteristics.germlineTumorTypes)
								xml.cancerSyndrome(gene.characteristics.cancerSyndrome)
								xml.description(gene.characteristics.description)
							}
						}
					}
				}
			}
			
			xml.mutations {
				for(KnownMutation mut : KnownMutation.list()) {
					Map args = [:]
					args.chromosome = mut.chromosome
					args.gene = mut.gene
					args.start = mut.start
					args.stop = mut.stop
					args.mutation = mut.mutation
					args.refAllele = mut.refAllele
					args.varAllele = mut.varAllele
					xml.mutation(args) {
						
						// Now, let's handle the annotation information.
						xml.lastEditedBy(mut.lastEditedBy)
						xml.lastUpdated(mut.lastUpdated)
						
						xml.annotation() {
							
							for(ClinicalSignificance significance : mut.significance) {
								if (significance.significance != ClinicalSignificance.SIGNIFICANCE_UNKNOWN ||
									significance.significanceComment ||
									significance.significanceReference) {
									xml.significance {
										xml.tumourType(significance.tumourType.name)
										xml.type(significance.getSignificanceCode())
										xml.significanceComment(significance.significanceComment)
										xml.significanceReference(significance.significanceReference)
										xml.significanceEvidence(significance.significanceEvidence)
									}
								}
							}
	
							for(AgentSensitivity sensitivity : mut.sensitivity) {
								if (sensitivity.agentName) {
									xml.sensitivity {
										xml.agents(sensitivity.agentName)
										xml.sensitivityType(sensitivity.getSensitivityCode())
									}
								}
							}
	
							for(AgentEffectiveness effectiveness : mut.effectiveness) {
								if (effectiveness.agents) {
									xml.effectiveness {
										xml.agents(effectiveness.agents)
										xml.effectiveness(effectiveness.getEffectivenessCode())
									}
								}
							}
							
							if (mut.characteristics != null) {
								xml.characteristics {
									xml.action(mut.characteristics.getActionCode())
									xml.actionReference(mut.characteristics.actionReference)
									xml.actionComment(mut.characteristics.actionComment)
									xml.agentsAvailable(mut.characteristics.getAgentsAvailableCode())
								}
							}
							
							for(MutationConfirmation confirmation : mut.confirmations) {
								xml.confirmation {
									xml.date(confirmation.date)
									xml.userName(confirmation.userName)
									xml.name(confirmation.name)
									xml.comment(confirmation.comment)
									xml.body(confirmation.body?.encodeBase64()?.toString())
									xml.pdf(confirmation.pdf?.encodeBase64()?.toString())
								}
							}
						}
					}
				}
			}
		}
		
		render(text: writer.toString(),contentType:"text/xml",encoding:"UTF-8")
	}
	
	private void buildPopupMenu(Map args, MarkupBuilder xml) {
		List values = args.remove("values")
		Map labels = args.remove("labels")
		def defaultValue = args.remove("defaultValue")
		xml.select(args) {
			for(value in values) {
				Map options = [:]
				if (value.equals(defaultValue)) {
					options.selected = "selected"
				}
				if (labels && labels.containsKey(value)) {
					options.label = labels[value]
				}
				option(options, value)
			}
		}
	}
	
	private void buildSignificanceMenu(MarkupBuilder xml, String id, String defaultValue) {
		buildPopupMenu(xml, id: "${id}_clinical_significance", name: "clinical_significance", defaultValue: defaultValue,
			values: ["-", "prospective", "retrospective", "preclinical", "case", "observational", "unknown"],
			labels: ["prospective": 'has been examined by PROSPECTIVE clinical trials',
					 "retrospective": 'has been examined by RETROSPECTIVE clinical trials',
					 "preclinical":   'has been examined by PRECLINICAL study',
					 "case":          'has been examined by RETROSPECTIVE case studies',
					 "observational": 'has been examined by PROSPECTIVE observational studies',
					 "unknown":       'is UNKNOWN'],
            onchange: "adjust_significance_visibility('#${id}_details', this.value)"
		)
	}
	
	private void buildEvidenceMenu(MarkupBuilder xml, String defaultValue) {
		buildPopupMenu(xml, name: "evidence_code", defaultValue: defaultValue,
			values: ["-", "IA", "IB", "IIB", "IIC", "IIIC", "IVD", "VD"]
		)
	}
	
	private void buildActionMenu(MarkupBuilder xml, String defaultValue) {
		buildPopupMenu(xml, name: "mutation_action", defaultValue: defaultValue,
			values: ["-", "activating", "inactivating", "none", "other"],
			labels: [
				"activating": "Activating",
				"inactivating": "Inactivating",
				"none": "None",
				"other": "Other"
			],
			onchange: "if (this.value=='other') {jQuery('#action_comment_div').show()} else {jQuery('#action_comment_div').hide();jQuery('#mutation_action_comment').value=''}"
		)
	}
	
	private void buildSensitivityMenu(MarkupBuilder xml, String defaultValue) {
		buildPopupMenu(xml, name: "_sensitivity_resistance", defaultValue: defaultValue,
			values: ["-", "sensitivity", "resistance", "maybe_sensitivity", "maybe_resistance"],
			labels: [
				"sensitivity": 'confers sensitivity',
				"resistance": 'confers resistance',
				"maybe_sensitivity": 'MAY confer sensitivity',
				"maybe_resistance": 'MAY confer resistance'
			],
			onchange: "insert_sensitivity(this.value);this.value='-'"
		);
	}
	
	private void buildSignificance(Map args, MarkupBuilder xml) { 
		String id = "x" + Math.random().toString().replace(".", "")
		def studyType = args.studyType
		def comment = args.comment
		def ref = args.ref
		def evidence = args.evidence
		
		xml.div(id: "${id}_container") {
			mkp.yield('In this tumour type, the clinical significance of this mutation: ')
			buildSignificanceMenu(xml, id,  studyType)
			span(class: "link indent", onclick: "delete_clinical_significance('${id}_container')", "Remove")
			br()
			div(id: "${id}_details", 
				class: "indented", 
				style: (studyType ==~ /prospective|retrospective|preclinical|case|observational/) ? "display:inline" : "display:none") {
				span(class: "prompt", 'Comment:')
				br()
				textarea(name: "significance_comment", cols: 80, valign: 'top') {
					mkp.yield(comment ?: "")
				}
				br()
				mkp.yield('Reference (PMID): ')
				input(type: "text", name: "significance_pmid", value: ref ?: "")
				mkp.yield('Evidence: ')
				buildEvidenceMenu(xml, evidence)
			}
		}
	}
	
	private void buildEffectivenessMenu(MarkupBuilder xml, String id, String defaultValue) {
		buildPopupMenu(xml, id: "${id}_agents_effectiveness", name: "agents_effectiveness", defaultValue: defaultValue,
			values: ["-", "unknown", "effective", "ineffective"],
			labels: ["unknown":       'Unknown',
					 "effective":     'Known effective',
					 "ineffective":   'Known inffective'],
			onchange: "if (this.value != '-') {jQuery('#${id}_details').show()} else {jQuery('#${id}_details').hide()}"
		)
	}
	
	private void buildAgent(Map args, MarkupBuilder xml) {
		String id = "x" + Math.random().toString().replace(".", "")
		
		xml.div(id: "${id}_container") {
			div(class: "indented", id: "${id}_details") {
				mkp.yield('The available investigational agents ')
				input(type: "text", name: "agents", value: args.agents)
				mkp.yield(' have documented efficacy ')
				buildEffectivenessMenu(xml, id,  args.agentsEffective)
				span(class: "link indent", onclick: "jQuery('#${id}_container').remove()", "Remove")
				br()
			}
		}
	}
	
	private void buildSensitivity(Map args, MarkupBuilder xml) {
		String id = "x" + Math.random().toString().replace(".", "")
		
		def locale = RequestContextUtils.getLocale(request)
		def messageKey = "sensitivity." + (args.type ?: "unknown")
		
		xml.div(id: "${id}") {
			if (args.type != "-") {
				mkp.yieldUnescaped(getMessage(messageKey, [""].toArray()))
				input(type: "hidden", name: "sensitivity", value: "${args.type}")
				input(type: "text", name: "sensitivity_agent", value: "${args.agent}")
			}
			div(class: 'link indented', onclick: "jQuery('#${id}').remove()", 'Remove')
		}
	}
	
	/**
	 * Builds a sensitivity information control
	 */
	private void buildSensitivityInformation(MarkupBuilder xml) {
		
		xml.div(id: 'sensitivity_resistance')
		xml.div(class: 'prompt') {
			mkp.yield('Add sensitivity/resistance information: ')
			buildPopupMenu(xml, 
				name: "sensitivity_resistance",
				values: ['-', 'sensitivity', 'resistance', 'maybe_sensitivity', 'maybe_resistance'],
				labels: ["sensitivity":       'confers sensitivity',
						 "resistance":        'confers resistance',
						 "maybe_sensitivity": 'MAY confer sensitivity',
						 "maybe_resistance":  'MAY confer resistance'],
				onchange: "insert_sensitivity(this.value);this.value='-'"
			)
		}
	}
	
	
	//, String type, String name, String value, String width, String onchange
	private void buildTumourTypeAutocomplete(Map args, MarkupBuilder xml, KnownMutation mutation) {
		def type = args.remove("type")
		def id = args.getAt("id")
		def action = args.remove("action")
		args.type = "text"
		args.putAt("class", "${type}_autocomplete")
		if (! args.getAt("size")) {
			args.putAt("size", "20")
		}
		args.putAt("id", id)
		xml.input(args)
		xml.script {
			mkp.yield("""
jQuery(function() {
    jQuery("#${id}").autocomplete({
        source: tumour_type_autocomplete_callback,
        minLength: 2,
        close: function(event, ui) {
			adjust_tumour_sections('${id}');
		}
	}).keypress(function(event) {
        var code = (event.keyCode ? event.keyCode : event.which);
        if (code == 13) { jQuery("#${id}").autocomplete("close"); return false; };
    })
});
			""")
		}
	}
	
	private void buildMutationFrequencies(MarkupBuilder xml, KnownMutation mutation) {

		def i
		i = 0
		xml.h2("Frequency of ${mutation.mutation} mutation in ${mutation.knownGene.name} in the top tumour types (from COSMIC)")
		def allFrequencies = mutation.frequencies.sort { a,b -> b.frequency.compareTo(a.frequency) }
		for(frequency in allFrequencies.take(10)) {
			i++
			xml.p {
				input(type: "checkbox",
					id: "checkbox_${i}",
					name: "tumour_selected_cosmic",
					label: '',
					disabled: "disabled",
					checked: "checked")
				b("${i}. Tumour")
				this.buildTumourTypeAutocomplete(xml, mutation, 
					type: 'tumour', 
					name: "tumour_cosmic", 
					id: "tumour_cosmic_${i}", 
					value: frequency?.tumourType?.name, 
					size: "60", 
					disabled: "disabled",
					onclick: 'adjust_tumour_sections(this.id)',
					action: 'autocompleteTumourType')
				b('Frequency')
				input(
					name: "frequency_cosmic",
					id: "frequency_cosmic_${i}",
					value: (frequency?.frequency ? sprintf("%4.3f",frequency?.frequency*100) : ''),
					size: 6,
					disabled: "disabled",
					onchange: 'adjust_tumour_sections(this.id)',
					onkeyup: 'adjust_tumour_sections(this.id)'
				)
				if (frequency != null) {
					mkp.yield(' (')
					mkp.yield(frequency.affected)
					mkp.yield('/')
					mkp.yield(frequency.samples)
					mkp.yield(' samples)')
				}
				br()
			}
		}
	}
	
	private void buildClinicalSignificance(MarkupBuilder xml, KnownMutation mutation) {
		xml.h2('Clinical and Preclinical Studies')
    	
		def mutationName = mutation.toLabel()
		
		xml.div(class: 'prompt', '(Add a study if you need to.)')
		
		xml.div(id: "clinicalsignificances") {
			mkp.yield("")
			Integer i = 1;
			List<ClinicalSignificance> significances = mutation.getOrderedSignificances()
			for(ClinicalSignificance significance : significances) {
				KnownMutationFrequency frequency = mutation.getSignificanceFrequency(significance)
				String div_name = "tumour_div_${i}"
				xml.div(id: div_name, style: 'display:inline') {
					xml.h3(class: "tumour_type_header") {
						span(class: "tumour_type_index", "${i}.") {
							mkp.yield("")
						}
						span(class: "tumour_type_name", significance?.tumourType?.name)  {
							mkp.yield("")
						}
						span(class: "tumour_type_frequency", (frequency?.samples ? sprintf('%4.3f%%', (frequency?.frequency ?: 0.0)*100) : ''))  {
							mkp.yield("")
						}
					}
	
					xml.div(class: "significances") {
						this.buildSignificance(xml,
							studyType: significance.significanceCode, 
							tumourType: significance.tumourType.name, 
							comment: significance.significanceComment, 
							ref: significance.significanceReference,
							evidence: significance.significanceEvidence)
						xml.input(type: "hidden", name: 'significance_tumour_type', value: significance?.tumourType?.name ?: "")
					}
				}
				
				i++;
			}
		}
		xml.span(class: 'link insert_link', 
			onclick: "insert_clinical_significance('${mutationName}', 'clinicalsignificances')" ,'Add a study'
		);
	}
	
	private void buildGeneProperties(MarkupBuilder xml, KnownGene gene) {
		xml.h3 {
			mkp.yield(gene.name)
			mkp.yield(' Characteristics')
		}
		
		xml.table("class": "propertysheet") {
			tbody {
				tr {
					th("class": "nobackground labelcolumn", getMessage("gene.fullname.label") + ":")
					td {
						input(type: "text", "class": "value", name: "full_name", value: gene.characteristics.fullName)
					}
				}
				tr {
					th("class": "nobackground labelcolumn", getMessage("gene.somatic.tumour.types.label") + ":")
					td {
						input(type: "text", "class": "value", name: "somatic_tumor_types", value: gene.characteristics.somaticTumorTypes)
					}
				}
				tr {
					th("class": "nobackground labelcolumn", getMessage("gene.germline.tumour.types.label") + ":")
					td {
						input(type: "text", "class": "value", name: "germline_tumor_types", value: gene.characteristics.germlineTumorTypes)
					}
				}
				tr {
					th("class": "nobackground labelcolumn", getMessage("gene.cancer.syndrome.label") + ":")
					td {
						input(type: "text", "class": "value", name: "cancer_syndrome", value: gene.characteristics.cancerSyndrome)
					}
				}
				tr {
					th("class": "nobackground", colspan: 2, getMessage("gene.description.label") + ":")
				}
				tr {
					td(colspan: 2) {
						textarea(name: "description", "class": "description", gene.characteristics.description)
					}
				}
			}
		}
	}
	
	private void buildMutationCharacteristics(MarkupBuilder xml, KnownMutation mutation) {
		MutationCharacteristics characteristics = mutation.characteristics
		
		xml.h2 {
			i(mutation.toLabel())
			mkp.yield(' Characteristics')
		}
		xml.p {
			mkp.yield('The functional consequence of this mutation is')
			buildActionMenu(xml, characteristics?.actionCode ?: 'unknown')
			mkp.yield('Reference (PMID):')
			input(type: "text", name: 'mutation_action_pmid', value: characteristics?.actionReference ?: '')
			div(id: 'action_comment_div', style: (characteristics?.action == characteristics?.ACTION_OTHER ? 'display:inline' : 'display:none')) {
				span(class: 'prompt', 'Comment:')
				br()
				textarea(id: 'mutation_action_comment', name: "mutation_action_comment", cols: 80) {
					mkp.yield(characteristics?.actionComment ?: '')
				}
			}
		}
	}
			
	private void buildInvestigationalAgents(MarkupBuilder xml, KnownMutation mutation) {
		xml.h2('Availability of Investigational Agents');
		xml.div(class: 'prompt', 'Fill in available investigational agents.');
		Set<AgentEffectiveness> agents = mutation.effectiveness
		MutationCharacteristics characteristics = mutation.characteristics
		String mutationName = mutation.toLabel()
		String divName = "investigational_agents"
		Map inputArgs = [
			type:      "checkbox",
			name:      'agents_available',
			value:     1,
			onclick:   "if (this.checked) jQuery('#${divName}').show(); else jQuery('#${divName}').hide();"
		]
		if (characteristics?.agentsAvailable != characteristics?.AVAILABLE_NO) {
			inputArgs.checked = "checked"
		}
		xml.label {
			input(inputArgs)
			mkp.yield(' investigational agent(s) are available')
		}
		xml.div(id: divName,
			    style: characteristics?.agentsAvailable != characteristics?.AVAILABLE_NO ? 'display:inline' : 'display:none') {
			if (agents) {
				for(AgentEffectiveness agent in agents) {
					buildAgent(xml, agents: agent.agents, agentsEffective: agent.getEffectivenessCode())
				}
			} else {
				buildAgent(xml, agents: "", agentsEffective: null)
			}
			span(
				class: 'link indented',
				id: "${divName}_insert",
				onclick: "insert_mutation_agent('${mutationName}','${divName}_insert')",
				'Add investigational agents')
		}
	}
	
	private void buildSensitivityAndResistance(MarkupBuilder xml, KnownMutation mutation) {
		Set<AgentSensitivity> sensitivity = mutation.sensitivity
		xml.h2('Sensitivity and Resistance Conferred by Mutation');
		xml.div(class: 'prompt', 'Fill in agent sensitivity/resistance conferred by mutation, if any.');
		for (AgentSensitivity s in sensitivity) {
			buildSensitivity(xml, type: s.sensitivityCode, agent: s.agentName)
		}
		xml.div(id: 'sensitivity_resistance','')
		xml.div(class: 'prompt', 'Add sensitivity/resistance information: ')
		buildSensitivityMenu(xml, "")
	}
	
	private void buildUpdateInformation(MarkupBuilder xml, KnownMutation mutation) {
		xml.h2("Update information")
		
		def complete = mutation.isComplete()
		def confirmed = mutation.isConfirmed()
		def who = mutation.lastEditedBy
		def when = mutation.lastUpdated

		xml.p {
			span("class": (complete ? (confirmed ? 'statuscomplete' : 'statusnotconfirmed') : 'statusincomplete')) {
				mkp.yield('Status: ')
				mkp.yield(complete ? (confirmed ? 'complete' : 'not confirmed') : 'incomplete')
				mkp.yield(".")
			}
			if (who) {
				mkp.yield(" Last saved by: ${who}")
				xml.span(class: 'prompt', id: 'date_changed', when ?: 'not saved')
			} else {
				mkp.yield(" Not yet saved")
			}
			input(type: "submit", name: 'Update', value: 'Save', onclick: "confirm_submit('save')");
		}
	}
	
	private void buildReports(MarkupBuilder xml, KnownMutation mutation) {
		xml.h2("Reports available")

		def confirmed = mutation.confirmations
		def mostRecentConfirmation = confirmed.size() > 0 ? confirmed.first() : null
		def when = mutation.lastUpdated

		xml.div {
			if (! mostRecentConfirmation || when > mostRecentConfirmation.date) {
				ul {
					li {
						a(href: request.contextPath + "/" + controllerName + "/mutation=" + mutation.toLabel(), 'Preview current draft')
					}
				}
				span(class: 'prompt', "Enter confirmer's name:")
				br()
				input(type: "text", id: 'confirmed_by', name: 'confirmed_by', disabled: "disabled", value: currentUserService.currentUserName() ?: "")
				input(type: "submit", name: 'Confirm and Save', value: 'Confirm and Save', onclick: "confirm_submit('confirm_save')")
				br()
				span(class: 'prompt', 'Confirmation comment:')
				br()
				textarea(name: "confirmation_comment", cols: 80, valign: 'top') {
					mkp.yield('')
				}
				br()
			}
		}
	}
	
	private void buildTumourTypeMenu(MarkupBuilder xml) {
		List<String> values = KnownTumourType.findAll().collect { it.name }.sort { a,b -> a.compareTo(b) }.plus(0, ["-"])
		
		buildPopupMenu(xml, name: "significance_tumour_type", 
			values: values
		)
	}
	
	def insertSignificance = {
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		xml.h3 {
			mkp.yield("Add pop-up tumour type here")
			buildTumourTypeMenu(xml)
		}
		this.buildSignificance([:], xml)
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def insertMutationAgent = {
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		this.buildAgent([:], xml)
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def insertSensitivity = {
		
		def theType = params.type
		log.trace("Received type: " + theType)
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		this.buildSensitivity(xml, type: theType, agent: "")
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def getTumourTypeFrequency = {
		
		def tumourTypeString
		if (params.tumour_type) {
			tumourTypeString = params.tumour_type
			log.trace("Tumour type: " + tumourTypeString)
		} else {
			throw new RuntimeException("Missing tumour type")
		}
		
		if (! KnownTumourType.findByName(tumourTypeString)) {
			response.sendError(404, "Can't find tumour type: ${tumourTypeString}");
			return
		}
		
		def model = getMutationModel()	
		
		render(contentType:"text/json",encoding:"UTF-8") {
			[frequency: 0.0, samples: 0]
		}
	}
	
	def autocompleteTumourType = {
		def term = params.term
		def mutation = params.mutation
		
		// This is now somewhat tricky. We want to offer an autocomplete on all tumour types apart
		// from those already in the COSMIC list, which should have been displayed anyway. That
		// prevents the COSMIC ones from being intentionally overwritten. The data model for
		// this is all hideously complex, mainly because we are shadowing data from COSMIC and 
		// relational systems are not good at that. In a document-oriented database, this would
		// be far easier. 
		
		def model = getMutationModelFromLabel(mutation)
		def mutationTumourTypes = model.mutation.frequencies.collect { it.tumourType.name }
		
		def matched = KnownTumourType.findAllByNameLike("%" + term + "%").collect { it.name }
		matched.removeAll(mutationTumourTypes)
				
		render(contentType:"text/json",encoding:"UTF-8") { matched }
	}
	
	def setGene = {
		KnownGene gene = KnownGene.findByName(params.gene)
		assert(gene)
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		
		xml.div {
			
			buildGeneProperties(xml, gene)
			input(type: "hidden", name: "gene", value: gene.name, id: "gene")
			input(type: "submit", name: 'Update', value: 'Save', onclick: "confirm_submit('save')");
			input(type: "submit", name: 'Cancel', value: 'Cancel', onclick: "confirm_submit('cancel')");
		}
		
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def setMutation = {
		
		def model = getMutationModel()
		def mutation = model.mutation
		
		assert(mutation)
		
		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		
		xml.div {
			buildUpdateInformation(xml, mutation)
			hr()
			buildMutationFrequencies(xml, mutation)
			hr()
			buildMutationCharacteristics(xml, mutation)
			hr()
			buildClinicalSignificance(xml, mutation)
			hr()
			buildInvestigationalAgents(xml, mutation)
			hr()
			buildSensitivityAndResistance(xml, mutation)
			hr()
			input(type: "hidden", name: "mutation", value: mutation.toLabel(), id: "mutation")
			input(type: "submit", name: 'Update', value: 'Save', onclick: "confirm_submit('save')");
			input(type: "submit", name: 'Cancel', value: 'Cancel', onclick: "confirm_submit('cancel')");
		}
		
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def findGene = {
		def knownGenes = KnownGene.findGenes(params.gene)
		render(contentType: "text/xml") {
			for(gene in knownGenes) {
                tr {
					td {
						a(href: request.contextPath + "/" + controllerName + "/gene?gene=" + gene.name, gene.name)
					}
                }
            }
		}
	}

	def findMutation = {
		
		def matcher = params.mutation =~ /(\w+)\s+(\w+)/
		if (matcher.matches()) {
			
			def genePart = matcher.group(1)
			def mutationPart = matcher.group(2)

			def criteria = KnownMutation.createCriteria()
			def mutationList = criteria.list {
				like("mutation", mutationPart.trim() + "%")
				knownGene {
					eq("name", genePart.trim())
				}
			}

			render(contentType: "text/xml") {
				for(mut in mutationList) {
					def mutationName = mut.knownGene.name + " " + mut.mutation
					tr {
						td {
							a(href: request.contextPath + "/" + controllerName + 
								"/mutation?mutation=" + mutationName.encodeAsURL(), mutationName)
						}
					}
				}
			}
	
		} else {
			throw new RuntimeException("Internal error: invalid mutation term")
		}
	}
	
	private void ifSet(String name, Closure code) {
		String value = request.getParameter(name)
		if (value != null) {
			log.trace("Writing ${name}: " + value);
			code.call(value)
		}
	}
	
	def preview = {
		def model = getMutationModel()
		
		model
	}
	
	def confirm = {
		
		def model = getMutationModel()
		KnownMutation mutation = model.mutation
		
		// Now to handle the confirmation option, should we have selected it. 
		def username = springSecurityService.authentication.name
		def user = userListService.getUser(username)		

		MutationConfirmation confirm = new MutationConfirmation()
		confirm.userName = username
		confirm.name = (user.givenName != null && user.familyName != null) ? (user.givenName + " " + user.familyName) : username
		confirm.comment = params.confirmation_comment
		confirm.date = new Date()

		log.info("Recording confirmation; mutation: ${mutation.toLabel()}, user: ${confirm.userName}, date: ${confirm.date}")
		mutation.addToConfirmations(confirm)
		
		// At this stage, the model is complete even if not yet formally persisted. This
		// should allow us to generate the XML and PDF, before we store them in the confirmation
		// record, then we can continue to save
		
		String reportXml = getReportXML(model)
		byte[] reportPdf = getReportPDF(reportXml)
		confirm.body = reportXml.getBytes()
		confirm.pdf = reportPdf
		
		if (mutation.save(validate: true, flush: true)) {
			redirect(action: "mutation", params: [mutation: mutation.toLabel()], fragment: "reportcontainer")
		} else {
			mutation.errors.each {
				log.warn(it)
			}
			redirect(action: "mutation", params: [mutation: mutation.toLabel()], fragment: "reportcontainer")
		}
	}
	
	def saveGene = {
		KnownGene gene = KnownGene.findByName(params.gene)
		
		if (params.submit_action == "cancel") {
			redirect(action: "gene", params: [gene: gene.name], fragment: "summarycontainer")
			return
		}
		
		ifSet("full_name", { gene.characteristics.fullName = it })
		ifSet("somatic_tumor_types", { gene.characteristics.somaticTumorTypes = it })
		ifSet("germline_tumor_types", { gene.characteristics.germlineTumorTypes = it })
		ifSet("cancer_syndrome", { gene.characteristics.cancerSyndrome = it })
		ifSet("description", { gene.characteristics.description = it })

		if (gene.save(validate: true, flush: true)) {
			redirect(action: "gene", params: [gene: gene.name], fragment: "summarycontainer")
		} else {
			mutation.errors.each {
				log.warn(it)
			}
			redirect(action: "gene", params: [mgene: gene.name], fragment: "summarycontainer")
		}
	}
	
	/**
	 * Save a mutation, following the form action when editing a mutation's report.
	 */
	// As far as the implementation goes, we can't use params because it's a hashmap,
	// so you can't rely on the order of submitted data. Using request.getParameterValues()
	// allows order to be preserved. 
	def saveMutation = {
		
		def model = getMutationModel()
		KnownMutation mutation = model.mutation
		Map parameters = [:]
		
		// Don't bother doing anything that actually modifies anything if the cancel action has been
		// selected. And yes, this could also be done by hacking around the URL, but this is good
		// enough for now. 
		if (params.submit_action == "cancel") {
			redirect(action: "mutation", params: [mutation: mutation.toLabel()], fragment: "summarycontainer")
			return
		}
		
		Enumeration<String> e = request.getParameterNames()
		while (e.hasMoreElements()) {
			String name = e.nextElement()
			log.trace("Parameter name: ${name}")
			List values = request.getParameterValues(name) as List
			if (values.size() > 0) {
				parameters.putAt(name, values)
			}
		}
		
		// Tragically, we messed up. If we don't have a characteristics object, or some of the 
		// other related components, we should make sure we have them now.
		
		if (mutation.characteristics == null) {
			mutation.characteristics = new MutationCharacteristics(action: MutationCharacteristics.ACTION_UNKNOWN)
			mutation.characteristics.actionComment = "";
			mutation.characteristics.mutation = mutation
		}
		
		// OK, there is a fair bit to be done saving this information, in that there
		// are a fair few objects that need to be managed. All the annotations are
		// attached to the mutation object. 
		ifSet("agents_available", { mutation.characteristics.agentsAvailable = it == "1" ? MutationCharacteristics.AVAILABLE_YES : MutationCharacteristics.AVAILABLE_NO })
		ifSet("mutation_action_pmid", { mutation.characteristics.actionReference = it })
		ifSet("mutation_action_comment", { mutation.characteristics.actionComment = it })
		ifSet("mutation_action", { mutation.characteristics.action = MutationCharacteristics.toAction(it) })
		
		/*
		 * Handle the tumour types that the user may have manually added into the report. 
		 */
		
		List tumours = request.getParameterValues("tumour") as List
		List tumourSelected = request.getParameterValues("tumour_selected") as List
		
		// We can, and probably should, remove all existing ClinicalSignificance's, because that's
		// much simpler than working out which ones might have been deleted. 		
		def listSignificances = []
		listSignificances += mutation.significance
		listSignificances.each { significance ->
			significance.tumourType.removeFromSignificance(significance)
			mutation.removeFromSignificance(significance)
			significance.delete()
		}
		
		List significanceTumourTypes = request.getParameterValues("significance_tumour_type") as List
		List significanceComments = request.getParameterValues("significance_comment") as List
		List significancePmids = request.getParameterValues("significance_pmid") as List
		List significances = request.getParameterValues("clinical_significance") as List
		List significanceEvidences = request.getParameterValues("evidence_code") as List
		
		significanceTumourTypes.eachWithIndex { obj, i -> 
			KnownTumourType tumourType = KnownTumourType.findByName(obj)
			assert tumourType != null
			ClinicalSignificance significance = new ClinicalSignificance()
			significance.significance = significance.toSignificance(significances.get(i))
			significance.significanceComment = significanceComments.get(i)
			significance.significanceReference = significancePmids.get(i)
			significance.significanceEvidence = significanceEvidences.get(i)
			tumourType.addToSignificance(significance)
			mutation.addToSignificance(significance)
			significance.save()
		}
		
		/* 
		 * Handle the AgentEffectiveness aspect
		 * Yawn: because we have modeled the relation as bidirectional, we have to do this the tedious
		 * way #unimpressed
		 */

		List agentNames = request.getParameterValues("agents") as List
		List agentEffectiveness = request.getParameterValues("agents_effectiveness") as List		

		def listEffectiveness = []
		listEffectiveness += mutation.effectiveness
		listEffectiveness.each { effectiveness ->
			mutation.removeFromEffectiveness(effectiveness)
		}
		agentNames.eachWithIndex { obj, i ->
			mutation.addToEffectiveness(new AgentEffectiveness(agents: obj, agentsEffective: AgentEffectiveness.toAgentsEffective(agentEffectiveness.get(i))))
		}
		
		/*
		 * Handle the AgentSensitivity aspect
		 * Yawn: because we have modeled the relation as bidirectional, we have to do this the tedious
		 * way #unimpressed
		 */
		
		List sensitivityTypes = request.getParameterValues("sensitivity") as List
		List sensitivityAgents = request.getParameterValues("sensitivity_agent") as List

		def listSensitivity = []
		listSensitivity += mutation.sensitivity
		listSensitivity.each { sensitivity ->
			mutation.removeFromSensitivity(sensitivity)
		}
		sensitivityTypes.eachWithIndex { obj, i ->
			mutation.addToSensitivity(new AgentSensitivity(agentName: sensitivityAgents.get(i), sensitivityType: AgentSensitivity.toSensitivity(obj)))
		}
		
		// Almost the very last thing, if we have to, let's change the modified date. Only do this if we 
		// wrote anything. This also depends on the kind of authentication system we are allowing, which
		// might not be LDAP. 
		
		def username = springSecurityService.authentication.name
		def user = userListService.getUser(username)
		mutation.lastEditedBy = user.givenName + " " + user.familyName
		
		mutation.lastUpdated = new Date()
		log.trace("Updating the lastUpdated time to ${mutation.lastUpdated}")
		
		if (mutation.save(validate: true, flush: true)) {
			redirect(action: "mutation", params: [mutation: mutation.toLabel()], fragment: "summarycontainer")
		} else {
			mutation.errors.each {
				log.warn(it)
			}
			redirect(action: "mutation", params: [mutation: mutation.toLabel()], fragment: "summarycontainer")
		}
	}

	/**
	 * JSON-returning action, used to do the dynamic drop-down for a gene name. 
	 */
	// TODO some limiting count would be a good plan
	def queryGene = {
		String term = params.term.trim()
		def geneList = KnownGene.findGenes(term)
		
		render(contentType: "text/json") {
			geneList.collect { [name: it.name] };
		}
	}
	
	def queryGeneHtml = {
		String term = params.term.trim()
		def geneList = KnownGene.findGenes(term)

		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		
		xml.ul {
			for(gene in geneList) {
				li {
					mkp.yieldUnescaped(link(action: "gene", params: [gene: gene.name]) { gene.name }.toString())
				}	
			};
		}
		
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}
	
	def queryMutationHtml = {
		String term = params.term.trim()
		def mutList = KnownMutation.findMutations(term)

		def writer = new StringWriter()
		def xml = new MarkupBuilder(writer)
		xml.setDoubleQuotes(true)
		
		xml.ul {
			for(mut in mutList) {
				li {
					String url = createLink(action: "mutation", params: [mutation: mut.toLabel()])
					a(href: url) {
						mkp.yield(mut.toLabel())
						mkp.yield(" - ")
						if (! mut.isComplete()) {
							span("class": "statusincomplete", "incomplete")
						} else if (! mut.isConfirmed()){
							span("class": "statusnotconfirmed", "not confirmed")
						} else {
							span("class": "statusconfirmed", "complete and confirmed")
						}
					}
				}	
			};
		}
		
		render(text: writer.toString(),contentType:"text/html",encoding:"UTF-8")
	}	
}
