package ca.on.oicr.gps.controller

import java.sql.Timestamp;
import java.text.MessageFormat
import java.util.Date;

import grails.converters.XML
import grails.converters.JSON

import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import ca.on.oicr.gps.model.data.Decision;
import ca.on.oicr.gps.model.data.Sample;
import ca.on.oicr.gps.model.data.Subject;
import ca.on.oicr.gps.model.data.Summary;

import grails.plugins.springsecurity.Secured
import groovy.xml.MarkupBuilder;
import groovy.xml.StreamingMarkupBuilder;

/**
 * SubjectController acts as the controller base for access to the subject domain
 * information. 
 * 
 * @author swatt
 */

// TODO Some serious refactoring of the type coercion needed here. Some of this
// is due to the complexity of date handling in jqgrid, which is not exactly
// clean in some areas. There is still a lot of room for improvement. 

@Secured(['ROLE_GPS-USERS'])
class SubjectController {
	
	def grailsTemplateEngineService

	/**
	 * This implements a sortable key from a patient identifier. The sortable key ought to be
	 * a value which can be compared. 
	 */
	static final def patientIdKey(Subject a) {
		String pid = a.patientId;
		if (pid.startsWith("GEN2-")) {
			return (pid.substring(5, 8)).toInteger()
		} else if (pid.startsWith("GEN-")) {
			Integer division = pid.length() - 2
			return (pid.substring(division)).toInteger()
		} else {
			return Integer.MAX_VALUE;
	    }
	}
		
    def index = {
        redirect(action: "list", params: params)
    }

	def list = {
		
		// Query part: we need to filter down and sort according to the request and 
		// all its delightful paging parameters
		
		// First unpack the request parameters we might want (Ok, need)
		def requestPage = params.page?.toInteger() ?: 1
		def requestRows = params.rows?.toInteger() ?: 10
		def requestSortIndex = params.sidx ?: 'patientId'
		def requestSortOrder = params.sord ?: 'asc'
		def requestStart = requestRows * (requestPage - 1)
		def format = params.format ?: "json"

		// The number of subjects is simple		
		def subjectCount = Subject.count()
		def pageCount = Math.ceil(subjectCount / requestRows).toInteger()
		
		// Now, the sort criteria might be in the Summary or in the Subject, depending
		// on things. We need to decide which. 
		
		def criteria = Subject.createCriteria()
		
		def subjectList = criteria.list {
			if (requestSortIndex != 'patientId') {
				maxResults(requestRows)
				firstResult(requestStart)
			}
			if (Subject.metaClass.getProperties().find { it.getName().equals(requestSortIndex) }) {
				order(requestSortIndex, requestSortOrder)
			} else {
				summary {
					order(requestSortIndex, requestSortOrder)
				}
			}
		}
		
		// Manual hack of sorting by patient identifier required here. This can't be done
		// in either HQL or GORM. 
		
		if (requestSortIndex == 'patientId') {
			if (requestSortOrder == 'asc') {
				subjectList = subjectList.sort { a, b -> patientIdKey(a).compareTo(patientIdKey(b)) }
			} else {
				subjectList = subjectList.sort { a, b -> patientIdKey(b).compareTo(patientIdKey(a)) }
			}
			def first = requestStart
			def last = requestStart + requestRows - 1
			def limit = subjectList.size() - 1
			if (last > limit) {
				last = limit
			}
			subjectList = subjectList.getAt(first..last)
		}

		renderSubjectsJSON(subjectList, subjectCount, requestPage, pageCount)
	}
		
	def export = {
		def subjectList = Subject.findAll().sort { a, b -> patientIdKey(a).compareTo(patientIdKey(b)) }
		renderSubjectsExcel(subjectList)
	}
	
	private String formatDate (date) {
		
		final DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyyy");
		
		if (date) {
			return fmt.print(new DateTime(date))
		} else {
			return date
		}
	}

	private String formatInteger (i) {
		if (i) {
			return i.toString()
		} else {
			return i
		}
	}
	
	private Map renderSubjectJSON(Subject subject) {

		// Get the basic data		
		Map data = subject.toMap()

		// Acquire and render the most recent study sample decision text
		Decision studyDecision = subject.findDecision(Sample.SOURCE_STUDY_SAMPLE)
		String string = subject.summary.expertPanelDecision
		if (studyDecision != null) {
			string = grailsTemplateEngineService.renderView("/decision/_gridSummary", [decision: studyDecision])
		}
		string = (string == null) ? "" : string;
		string = string.replaceAll("\\s+", " ")
		string = string.trim()
		data.putAt("renderedDecision", string)
		
		// Acquire and render the most recent archival decision text
		Decision archivalDecision = subject.findDecision(Sample.SOURCE_ARCHIVAL_SAMPLE)
		string = ""
		if (archivalDecision != null) {
			string = grailsTemplateEngineService.renderView("/decision/_gridSummary", [decision: archivalDecision])
		}
		string = string.replaceAll("\\s+", " ")
		string = string.trim()
		data.putAt("renderedArchivalDecision", string)

		// Acquire the earliest non-withdrawn expert panel decision date
		Date date = subject.summary.expertPanelDecisionDate
		Decision firstStudyDecision = subject.findFirstDecision(Sample.SOURCE_STUDY_SAMPLE)
		if (firstStudyDecision != null) {
			date = firstStudyDecision.date
		}
		data.putAt("firstDecisionDate", date)
		
		return data
	}

	private void renderSubjectsJSON(subjectList, subjectCount, requestPage, pageCount) {
		// Rendering part: we transform for jqGrid. There is little rendering here,
		// as it is better if we push that into the view as far as we can, especially
		// for stuff like dates. 
		render(contentType: "text/json") { [ 
			page: 		requestPage, 
			total:	 	pageCount, 
			records:	subjectCount,
			subjects:   subjectList.collect { renderSubjectJSON(it) }
		] }
	}
	

	/**
	 * Renders a subject list to something that can be handed off to Excel. That could be
	 * either a comma-separated or tab-separated file, or it could be HTML. 
	 * 
	 * @param subjectList
	 */
	private void renderSubjectsExcel(subjectList) {
		
		StreamingMarkupBuilder mb = new StreamingMarkupBuilder()
		mb.encoding = "UTF-8"
		
		def output = mb.bind { builder->
			mkp.xmlDeclaration()
			
			'ss:Workbook'('xmlns:ss': 'urn:schemas-microsoft-com:office:spreadsheet') {
				'ss:Styles'() {
					'ss:Style'('ss:ID': "date1") {
						'ss:NumberFormat'('ss:Format': "Short Date")
					}
				}
				'ss:Worksheet'('ss:Name': "Data") {
					'ss:Table'() {
						GrailsDomainClass d = new DefaultGrailsDomainClass(Summary.class)
						List<GrailsDomainClassProperty> properties = d.getPersistentProperties()
						properties = properties.findAll { it.getName() != "subject" }
						'ss:Row'() {
							buildCell(builder, "patientId")
							buildCell(builder, "gender")
							buildCell(builder, "elapsedWorkingDays")
							buildCell(builder, "mutations")
							for(GrailsDomainClassProperty property in properties) {
								buildCell(builder, property.getName())
							}
						}
						for(Subject subject in subjectList) {
							'ss:Row'() {
								buildCell(builder, subject.patientId)
								buildCell(builder, subject.gender)

								Summary summary = subject.summary
								buildCell(builder, summary.elapsedWorkingDays)
								
								buildCell(builder, subject.mutations)

								for(GrailsDomainClassProperty property in properties) {
									String name = property.getName()
									String clazz = property.getType()
									Object value = summary.getProperty(name)
									buildCell(builder, value)
								}
							}
						}
					}
				}
			}
		}
		
		response.setHeader("Content-Disposition", "attachment; filename=file.xls")
		response.setHeader("Pragma", "no-cache")

		render(text: output,
			contentType: "application/vnd.ms-excel",
			encoding: "UTF-8");
	}
	
	private void buildCell(builder, Object value) {
		buildCell(builder, (String) value?.toString())
	}
	
	private void buildCell(builder, String value) {
		builder.'ss:Cell'() {
			builder.'ss:Data'('ss:Type': "String") {
				if (value != null) {
					builder.mkp.yield(value)
				}
			}
		}
	}
	
	private void buildCell(builder, Timestamp value) {
		builder.'ss:Cell'('ss:StyleID': "date1") {
			builder.'ss:Data'('ss:Type': "DateTime") {
				if (value != null) {
					String timestamp = DateFormatUtils.ISO_DATETIME_FORMAT.format(value);
					builder.mkp.yield(timestamp)
				}
			}
		}
	}
	
	private void buildCell(builder, Integer value) {
		builder.'ss:Cell'() {
			builder.'ss:Data'('ss:Type': "Number") {
				if (value != null) {
					builder.mkp.yield(value)
				}
			}
		}
	}
	
	def query = {
		def subjectList = Subject.findAllByPatientIdIlike(params.term + "%")
		subjectList.sort { it.patientId }
		
		render(contentType: "text/json") {
			subjectList.collect { [patientId: it.patientId, gender: it.gender] };
		}
	}
	
	/*
	 * The /subject/edit action requires at least the managers role. This is where
	 * cell editing lands. 
	 */

	/*
	 * The /subject/update action requires at least the managers role. This is where the
	 * record insertion lands.
	 */
	
	@Secured(['ROLE_GPS-MANAGERS'])
	def update = {
		log.info("Updating: " + params)

		def subjectId = params.id
		def operation = params.oper
		def parsedValue = params.parsedValue
		
		params.remove('id')
		params.remove('oper')
		params.remove('parsedValue')
		
		def propertyCount = params.size()
		def propertyKey = params.keySet().toList().get(0)
		def propertyValue = params.get(propertyKey)
		
		def subject = Subject.get(subjectId)
		
		setSubjectProperty(subject, propertyKey, propertyValue)
		updateSaveAndRespond(subject)
	}

	/*
	 * The /subject/edit action requires at least the managers role. This is where
	 * cell editing lands. 
	 */

	@Secured(['ROLE_GPS-MANAGERS'])
	def edit = {

		// Get the operation, right now we only want 'new'
		// Never try and write a primary key for a new object
		def ignoreParameters = ["oper", "action", "controller", "id"]
		
		def operation = request.getParameter("oper")
		def subjectId = request.getParameter("id")
		def parameterNames = request.getParameterNames().toList()
		parameterNames.removeAll(ignoreParameters)
		
		assert parameterNames.contains("action") == false
		assert parameterNames.contains("id") == false
		
		if (log.traceEnabled) {
			for(param in parameterNames) {
				log.trace(param + " => " + request.getParameter(param))
			}
		}
		
		def subject
		if (operation == 'add') {
			
			subject = new Subject()
			subject.summary = new Summary()
			subject.summary.subject = subject
			
			for(propertyKey in parameterNames) {
				setSubjectProperty(subject, propertyKey, request.getParameter(propertyKey))
			}
			
			log.trace("Saving the new subject")
			subject.save()
		} else if (operation == 'del') {
		
			subject = Subject.get(subjectId)
			subject.delete()
			log.trace("Deleting the subject")
		}
		
		
		// And we're done!
		render(contentType: "text/json") {
			[done: true]
		}
	}

	private void updateSaveAndRespond(Subject subject) {
		if (subject.save(flush:true)) {
			render(contentType: "text/json") {
				[done: true]
			}
		} else {
			// Send back an HTTP error, but with a message that could
			// actually be useful.
			response.setStatus(response.SC_NOT_IMPLEMENTED)
			def messages = subject.errors.allErrors.collect {
				MessageFormat.format(it.getDefaultMessage(), it.getArguments())
			}
			log.error(messages)
			render(contentType: "text/json") {
				[caption: "Error", messages: messages]
			}
		}
	}

	/**
	 * setSubjectProperty does some interesting MOP glossing. The propertyKey and 
	 * the value are both strings. Essentially, the method looks to see whether the
	 * propertyKey is defined as part of Subject or Summary, and looks up the 
	 * appropriate data type, and then does a little guess at coercion before 
	 * writing the value into the appropriate property. 
	 * 
	 * @param subject
	 * @param propertyKey
	 * @param propertyValue
	 * @return
	 */
	
	private def setSubjectProperty(Subject subject, String propertyKey, propertyValue) {
		
		log.info("Setting property: " + propertyKey + ", value: " + propertyValue)
		
		Object place = subject
		MetaClass owner
		MetaProperty property
		
		def matcher = (propertyKey =~ /\w+/).iterator()
		while(matcher.hasNext()) {
			String propertyName = matcher.next()
			owner = place.class.metaClass
			log.trace("Searching for property $propertyName in $place")
			property = owner.getProperties().find { it.getName().equals(propertyName) }
			assert property
			Object result = property.getProperty(place)
			if (matcher.hasNext()) {
				
				log.trace("Setting place to $place")
				place = result
				property = null
			}
		}
		
		assert place
		assert owner
		assert property
		
		// Handle any coercions needed from strings, which is all we will get. Again
		// we use MOP stuff.
		if (property.getType().getCanonicalName().equals("java.util.Date")) {
			if (! propertyValue) {
				propertyValue = null
			} else {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
				propertyValue = formatter.parseDateTime(propertyValue).toDate()
			}
		} else if (property.getType().getCanonicalName().equals("java.lang.Integer")) {
			if (! propertyValue) {
				propertyValue = null
			} else {
				propertyValue = Integer.parseInt(propertyValue);
			}
		} else if (property.getType().getCanonicalName().equals("java.lang.Boolean")) {
			if (! propertyValue) {
				propertyValue = 0
			} else {
				propertyValue = Integer.parseInt(propertyValue);
			}
		}
		
		// At this point, we can do it all in a single call. This allows all type coercion
		// to be handled for all classes and types in one pass.

		owner.setProperty(place, property.getName(), propertyValue)
	}
}
