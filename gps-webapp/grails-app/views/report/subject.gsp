
<%@ page import="ca.on.oicr.gps.model.data.Subject" %>
<%@ page import="groovy.util.Node" %>
<%@ page import="groovy.util.NodeList" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'subject.label', default: 'Patient')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="report.heading" args="${[subject.patientId, reportData.attribute('date')]}" /></h1>
            <div class="buttons">
            </div>
            
            <g:each in="${reportData.sampleType}" var="sampleType">
            	
            </g:each>
            <g:each in="${reportData.sampleType}" var="sampleType">
            	<h3>Sample type: ${sampleType.attribute("type")}</h3>
            	
	            <g:set var="mutationCount" value="${0}"/>
            	<g:each in="${sampleType.sample}" var="sample">
            		<g:if test="${sample.mutation}">
            			<g:set var="mutationCount" value="${mutationCount + 1}"/>
            		</g:if>
            	</g:each>
            	
            	<g:if test="${mutationCount == 0}">
            		<p>No mutations detected</p>
            	</g:if>
            	
            	<g:else>
            		<table>
	                    <thead>
	                        <tr>         
	                            <th>Sample</th>
	                            <th>Mutation</th>
	                            <th>Mutation ID</th>
	                            <th>Identified by PacBio</th>
	                            <th>% Frequency (PacBio)</th>
	                            <th>Identified by Sequenom</th>
	                            <th>% Frequency (Sequenom)</th>
	                            <th>Confirmed by Sanger</th>
							</tr>
	                    </thead>
	                    <tbody>				
							<g:each in="${sampleType.sample}" var="sample">
								<g:each in="${sample.mutation}" var="mutation">
									<tr>
										<td>
											<g:link controller="sample" action="show" id="${sample.attribute('id')}">
											${sample.attribute("barcode")}
											</g:link>
										</td>
										<td>
											<g:link controller="observedMutation" action="show" params="${ [ mutation: mutation.attribute('gene') + ' ' + mutation.attribute('mutation') ]}">
												${mutation.attribute("gene")} ${mutation.attribute("mutation")}
											</g:link>
										</td>
										<td>${mutation.attribute("publicId")}</td>
										<g:if test="${mutation.attribute('panelTechnology').equals('PacBio')}">
											<td>Yes</td>
											<td><g:formatNumber number="${Float.parseFloat(mutation.attribute('frequency'))}" format="##0.00%"/></td>
											<td></td>
											<td></td>
											<td></td>
											<td></td>
										</g:if>
										<g:if test="${mutation.attribute('panelTechnology').equals('Sequenom')}">
											<td></td>
											<td></td>
											<td>Yes</td>
											<td><g:formatNumber number="${Float.parseFloat(mutation.attribute('frequency'))}" format="##0.00%"/></td>
											<td></td>
											<td></td>
										</g:if>
									</tr>
								</g:each>
							</g:each>
						</tbody>
					</table>
            	</g:else>
            </g:each>
		</div>
	</div>
</body>
</html>