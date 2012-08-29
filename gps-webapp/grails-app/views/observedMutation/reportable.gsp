
<%@ page import="ca.on.oicr.gps.model.data.ObservedMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'observedMutation.label', default: 'Reportable Mutation')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        	<th><g:message code="observedMutation.patientId.label" default="Patient ID" /></th>
                        
                        	<th><g:message code="sample.barcode.label" default="Sample Barcode" /></th>
                        
                        	<th><g:message code="observedMutation.mutation.label" default="Mutation" /></th>
                                                
                        	<th><g:message code="panel.technology.label" default="Technology" /></th>

                        	<th><g:message code="observedMutation.frequency.label" default="Frequency" /></th>

                        	<th><g:message code="observedMutation.processDate.label" default="Processed" /></th>

                        	<th><g:message code="observedMutation.reported.label" default="Reported" /></th>

                			<th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${observedMutationInstanceList}" status="i" var="observedMutationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        	<g:set var="runSample" value="${observedMutationInstance.runSample}" />
                        	<g:set var="process" value="${runSample.process}" />
                        	<g:set var="subject" value="${runSample.sample.subject}"/>
                        
                        	<td>
                        		<g:link controller="summary" action="show" id="${subject.id}">
                        		${subject.patientId}
                        		</g:link>
                        	</td>
                        
                        	<td>
                        		<g:link controller="sample" action="show" id="${runSample.sample.id}">
                        		${runSample.sample.barcode}
                        		</g:link>
                        	</td>

                            <td>
                            	<g:link action="show" params="${ [ mutation: observedMutationInstance.knownMutation.toLabel() ]}">
                            	${observedMutationInstance.knownMutation.toLabel()}
                            	</g:link>
                            </td>

                        	<td>${process.panel.technology}</td>

							<td><g:formatNumber number="${observedMutationInstance.frequency * 100.0}" format="0.0" />%</td>

                            <td><g:formatDate format='dd MMM, yyyy' date="${process.date}" /></td>
                            
                            <td>
                            	<g:if test="${observedMutationInstance.reported}">
                            	<g:formatDate format="dd MMM, yyyy" date="${observedMutationInstance.reported}" />
                            	</g:if>
                            </td>
                            
                            <td>
                            <g:link 
                                action="reported" 
                                id="${fieldValue(bean: observedMutationInstance, field: 'id')}">${message(code: 'default.button.reported.label', default: 'Mark Reported')}</g:link>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${observedMutationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
