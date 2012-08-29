
<%@ page import="ca.on.oicr.gps.model.data.ObservedMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'observedMutation.label', default: 'Observed Mutations')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="mutation.mutation.label" default="Mutation" /></td>
                            
                            <td valign="top" class="value">
                            	<g:link controller="knowledge" action="mutation" params="${[mutation: mutation.toLabel()]}">
	                            ${fieldValue(bean: mutation.knownGene, field: "name")}
	                            ${fieldValue(bean: mutation, field: "mutation")}
	                            <g:message code="observedMutation.openKnowledgeBase.label" default="(Open in Knowledge Base)" />
	                            </g:link>
                            </td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="mutation.numberOfPatients.label" default="Number of Patients" /></td>
                            
                            <td valign="top" class="value">${numberOfPatients}</td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="mutation.numberOfSamples.label" default="Number of Samples" /></td>
                            
                            <td valign="top" class="value">${numberOfSamples}</td>
                            
                        </tr>
                    </tbody>
                </table>
                
		        <div id="tabs">
				    <h3>Observations</h3>
				    <div id="observations">
				    	<table id="observationgrid" class="summarygrid">
				    		<thead>
				    			<tr>
				    				<th style="width: 15em"><g:message code="subject.patientId.label" default="Patient ID"/></th>
				    				<th style="width: 15em"><g:message code="sample.barcode.label" default="Sample barcode" /></th>
				    				<th style="width: 10em"><g:message code="sample.type.label" default="Sample type" /></th>
				    				<th style="width: 10em"><g:message code="sample.source.label" default="Sample source" /></th>
				    				<th style="width: 10em"><g:message code="process.technology.label" default="Technology" /></th>
				    				<th style="width: 8em"><g:message code="observedMutation.frequency.label" default="Frequency" /></th>
				    				<th style="width: 10em"><g:message code="process.date.label" default="Processed" /></th>
				    			</tr>
				    		</thead>
					    	<tbody>
					    		<g:each in="${observations}" var="observed">
					    			<g:set var="runSample" value="${observed.runSample}"/>
					    			<g:set var="process" value="${runSample.process}"/>
					    			<g:set var="panel" value="${process.panel}"/>
					    			<tr>
					    				<td>
					    					<g:link controller="summary" action="show" id="${runSample.sample.subject.id}">
					    					${fieldValue(bean: runSample.sample.subject, field: "patientId")}
					    					</g:link>
					    				</td>
					    				
					    				<td>
					    					<g:link controller="sample" action="show" id="${runSample.sample.id}">
					    					${fieldValue(bean: runSample.sample, field: "barcode")}
					    					</g:link>
					    				</td>
					    				
					    				<td>${fieldValue(bean: runSample.sample, field: "type")}</td>
					    				
					    				<td>
					    					<g:message code="${ 'sample.source.' + (runSample?.sample?.source ?: 'none') }" default="${runSample?.sample?.source}" />
					    				</td>
					    				
					    				<td>${panel.technology}</td>
					    				<td>${fieldValue(bean: observed, field: "frequency")}</td>
					    				<td><g:formatDate format="M/dd/yy" date="${process.date}" /></td>
					    			</tr>
					    		</g:each>
					    	</tbody>
				    	</table>
				    </div>
				</div>

            </div>
        </div>
    </body>
</html>
