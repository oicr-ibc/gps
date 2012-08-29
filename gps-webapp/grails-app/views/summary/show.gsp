<%@ page import="ca.on.oicr.gps.model.data.Subject" %>
<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<%@ page import="ca.on.oicr.gps.model.data.Decision" %>
<%@ page import="ca.on.oicr.gps.model.data.ReportableMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'subject.label', default: 'Subject')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /> ${fieldValue(bean: subjectInstance, field: "patientId")}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            	<h3>Patient summary</h3>
                <table class="info">
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.patientId.label" default="Patient ID" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance, field: "patientId")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.lastUpdated" default="Last Updated" />:</td>
                            <td valign="top" class="value"><g:dateAndTimeFormat format="dd-MM-yyyy hh:mm" date="${subjectInstance.lastUpdated}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.elapsedWorkingDays.label" default="Elapsed Working Days" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "elapsedWorkingDays")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.institution.label" default="Institution" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "institution")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.primaryPhysician.label" default="Primary Physician" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "primaryPhysician")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.primaryTumorSite.label" default="Primary Tumor Site" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "primaryTumorSite")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.consentDate.label" default="Consent Date" />:</td>
                            <td valign="top" class="value"><g:dateFormat format="dd-MM-yyyy" date="${subjectInstance.summary.consentDate}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.biopsyDate.label" default="Biopsy Date" />:</td>
                            <td valign="top" class="value"><g:dateFormat format="dd-MM-yyyy" date="${subjectInstance.summary.biopsyDate}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.biopsySite.label" default="Biopsy Site" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "biopsySite")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.biopsyCores.label" default="Biopsy Cores" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "biopsyCores")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.expertPanelDecisionDate.label" default="Expert Panel Decision Date" />:</td>
                            <td valign="top" class="value"><g:dateFormat format="dd-MM-yyyy" date="${subjectInstance.summary.expertPanelDecisionDate}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.expertPanelDecision.label" default="Expert Panel Decision" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "expertPanelDecision")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="subject.comment.label" default="Comments" />:</td>
                            <td valign="top" class="value">${fieldValue(bean: subjectInstance.summary, field: "comment")}</td>
                        </tr>
                        
                    </tbody>
                </table>
                
                <div id="tabs">
					<ul>
						<li><a href="#samples">Samples</a></li>
						<li><a href="#processes">Experiments</a></li>
						<li><a href="#mutations">Observed Mutations</a></li>
						<li><a href="#decisions">Expert Panel Decisions</a></li>
					</ul>
					<div id="samples">
						<table class="info" style="width: auto">
							<thead>
                        		<tr>
                        			<g:sortableColumn property="barcode" style="width: 15em" title="${message(code: 'sample.barcode.label', default: 'Sample Barcode')}" />
                        			<th style="width: 12em"><g:message code="sample.registered.label" default="Registered" /></th>
                        			<th style="width: 10em"><g:message code="sample.type.label" default="Sample type" /></th>
                        			<th style="width: 10em"><g:message code="sample.source.label" default="Sample source" /></th>
                        			<th style="width: 15em"><g:message code="sample.concentration.label" default="DNA concentration" /></th>
                        		</tr>
                        	</thead>
                    		<tbody>
								<g:each in="${subjectInstance.samples.sort { a,b -> a.barcode.compareTo(b.barcode) }}" var="sample" status="index">
   									<tr>
   										<td class="value"><g:link controller="sample" action="show" id="${fieldValue(bean: sample, field: 'id')}">${fieldValue(bean: sample, field: "barcode")}</g:link></td>
   										<td class="value"><g:dateAndTimeFormat format="dd-MM-yyyy" date="${sample.dateCreated}" /></td>
   										<td class="value">${fieldValue(bean: sample, field: "type")}</td>
   										<td class="value"><g:message code="${ 'sample.source.' + (sample?.source ?: 'none') }" default="${sample?.source}" /></td>
   										<td class="value">${fieldValue(bean: sample, field: "dnaConcentration")}</td>
   									</tr>
   								</g:each>
							</tbody>
						</table>
					</div>
					<div id="processes">
						<table class="info" style="width: auto">
							<thead>
                        		<tr>
                        			<g:sortableColumn property="barcode" style="width: 15em" title="${message(code: 'sample.barcode.label', default: 'Sample Barcode')}" />
                        			<th style="width: 10em" ><g:message code="sample.source.label" default="Sample source" /></th>
                        			<th style="width: 10em" ><g:message code="process.date.label" default="Processed" /></th>
                        			<th style="width: 10em" ><g:message code="assay.technology.label" default="Technology" /></th>
                        		</tr>
                        	</thead>
                    		<tbody>
								<g:each in="${subjectInstance.samples.sort { a,b -> a.barcode.compareTo(b.barcode) }}" var="sample" status="index">
                        			<g:each in="${sample.runSamples}" var="runSample" status="i">
	   									<tr>
	   										<td class="value"><g:link controller="sample" action="show" id="${fieldValue(bean: sample, field: 'id')}">${fieldValue(bean: sample, field: "barcode")}</g:link></td>
	   										<td class="value"><g:message code="${ 'sample.source.' + (sample?.source ?: 'none') }" default="${sample?.source}" /></td>
	   										<td class="value"><g:dateFormat format="dd-MM-yyyy" date="${runSample.process.date}" /></td>
                            	    		<td class="value">${fieldValue(bean: runSample.process.panel, field: "technology")}</td>
	   									</tr>
	   								</g:each>
	   								<g:if test="${! sample.runSamples}">
	   									<tr>
	   										<td class="value"><g:link controller="sample" action="show" id="${fieldValue(bean: sample, field: 'id')}">${fieldValue(bean: sample, field: "barcode")}</g:link></td>
	   										<td class="value"><g:message code="${ 'sample.source.' + (sample?.source ?: 'none') }" default="${sample?.source}" /></td>
	   										<td class="value" colspan="2"><g:message code="sample.notanalyzed" default="-" /></td>
	   									</tr>
	   								</g:if>
   								</g:each>
							</tbody>
						</table>
					</div>
					<div id="mutations">
						<table class="info" style="width: auto">
							<thead>
                        		<tr>
                        			<th style="width: 15em"><g:message code="mutation.mutation.label" default="Mutation" /></th>
                        			<th style="width: 15em"><g:message code="sample.barcode.label" default="Sample barcode" /></th>
                        			<th style="width: 10em"><g:message code="sample.type.label" default="Sample type" /></th>
                        			<th style="width: 10em"><g:message code="sample.source.label" default="Sample source" /></th>
                        			<th style="width: 10em"><g:message code="assay.technology.label" default="Technology" /></th>
                        			<th style="width: 8em"><g:message code="mutation.frequency.label" default="Frequency" /></th>
                        			<th style="width: 10em"><g:message code="process.date.label" default="Processed" /></th>
                        		</tr>
                        	</thead>
                        	<tbody>
                        		<g:each in="${subjectInstance.samples.sort { a,b -> a.barcode.compareTo(b.barcode) }}" var="sample" status="index">
                        			<g:each in="${sample.runSamples}" var="runSample">
                        				<g:each in="${runSample.mutations}" var="mutation">
                            	    			<tr>
                            	    				<td>
                            	    					<g:link action="show" controller="observedMutation" params="${ [ mutation: mutation.knownMutation.toLabel() ]}">
                            	    						${mutation.knownMutation.toLabel()}
                            	    					</g:link>
                            	    				</td>
                            	    				<td><g:link controller="sample" action="show" id="${fieldValue(bean: sample, field: 'id')}">${fieldValue(bean: sample, field: "barcode")}</g:link></td>                            	    			
                            	    				<td>${fieldValue(bean: sample, field: "type")}</td>                            	    			
			   										<td class="value"><g:message code="${ 'sample.source.' + (sample?.source ?: 'none') }" default="${sample?.source}" /></td>
                            	    				<td>${fieldValue(bean: runSample.process.panel, field: "technology")}</td>
                            	    				<td>${fieldValue(bean: mutation, field: "frequency")}</td>                          	    			
                            	    				<td><g:dateFormat format="dd-MM-yyyy" date="${runSample.process.date}" /></td>                          	    			
                            	    			</tr>
                            	    		</g:each>
                            	    </g:each>
                        		</g:each>
                        	</tbody>
                        </table>
					</div>
					<div id="decisions">
						<g:if test="${subjectInstance.decisions}">
						<h4>Previous decisions</h4>
						<table class="info" width="100%">
							<thead>
								<th style="width: 16em"><g:message code="decision.decision.label" default="Decision" /></th>	
								<th style="width: 12em"><g:message code="mutation.mutation.label" default="Mutation" /></th>
								<th style="width: 12em"><g:message code="mutation.status.label" default="Status" /></th>
								<th style="width: 20em"><g:message code="mutation.genomicevidence.label" default="Sequencing" /></th>
								<th style="width: 20em"><g:message code="mutation.loe.label" default="Level of Evidence" /></th>
							</thead>
							<tbody>
								<g:each in="${subjectInstance.decisions.sort { a,b -> a.date.compareTo(b.date) }}" var="decision" status="i">
								<g:if test="${decision.decisionType != Decision.TYPE_WITHDRAWN}">
								<g:render template="/decision/tableSummary" bean="${decision}" var="decision" />
								</g:if>
								</g:each>
							</tbody>
						</table>
						</g:if>
						<g:else>
						<h4>No previous decisions</h4>
						</g:else>
						<g:form name="myForm" controller="decision" action="create" method="get">
					  		<g:hiddenField name="_subject" value="${subjectInstance.id}"/>
			    			<g:actionSubmit value="Create New Decision" action="create" />
			    		</g:form>
					</div>
				</div>
            </div>
        </div>
        <content tag="postJQuery">
			<%-- This is included after we have loaded jQuery --%>
        	<g:javascript>
jQuery(document).ready(function() {
  jQuery("#tabs").tabs({ cache: false });
  jQuery("#tabs").bind('tabsselect', 
    function(event, ui) {
      var theTabId = "#" + ui.panel.id + " table";
      jQuery(theTabId).trigger("reloadGrid", [{current:true}]);
      window.location.hash = ui.tab.hash;
    });
  jQuery.address.change(function(event){
    jQuery("#tabs").tabs("select", window.location.hash)
  });
});
        	
        	</g:javascript>
        </content>
    </body>
</html>
