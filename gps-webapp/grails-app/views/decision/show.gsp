<%@ page import="ca.on.oicr.gps.model.data.Decision" %>
<%@ page import="ca.on.oicr.gps.model.data.ReportableMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'decision.label', default: 'Decision')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" />: <g:link controller="summary" action="show" id="${decisionInstance.subject.id}" fragment="decisions">${decisionInstance.subject.patientId}</g:link></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${decisionInstance}">
            <div class="errors">
                <g:renderErrors bean="${decisionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <div>
				<h4>All decisions</h4>
				<ul>
					<g:each in="${decisionInstance.subject.decisions.sort { a,b -> a.date.compareTo(b.date) }}" var="decision" status="i">
					<li><p><g:link controller="decision" action="show" id="${decision.id}">
					<g:render template="/decision/summary" bean="${decision}" var="decision" />
					</g:link></p></li>
					</g:each>
				</ul>
				<hr/>
				<form>
					<g:hiddenField name="_subject" value="${decisionInstance.subject.id}"/>
					<g:hiddenField name="_decision" value="${decisionInstance.id}"/>
					<h4>${decisionInstance.summary}</h4>
					<g:if test="${decisionInstance.decisionType != Decision.TYPE_WITHDRAWN}">
	    			<g:actionSubmit value="Withdraw This Decision" action="withdraw" />
	    			</g:if>
					
					<p class="nomargin">
						<span style="width: 20em; display: inline-block">Sample source:</span>
						<g:select disabled="${true}" name="source" 
							valueMessagePrefix="sample.source"
							from="${Decision.constraints.source.inList}" 
							value="${decisionInstance?.source}"
							noSelection="${['null':'Select One...']}"/>
					</p>
					<p class="nomargin">
						<g:checkBox disabled="${true}" style="margin-right: 1em" id="noTumour" name="noTumour" value="${decisionInstance.noTumour}"/>
						<label for="noTumour">No tumour</label>
					</p>
					<p class="nomargin">
						<g:checkBox disabled="${true}" style="margin-right: 1em" id="insufficientMaterial" name="insufficientMaterial" value="${decisionInstance.insufficientMaterial}"/>
						<label for="insufficientMaterial">Insufficient material for analysis</label>
					</p>
					<p class="nomargin">
						<g:checkBox disabled="${true}" style="margin-right: 1em" id="noMutationsFound" name="noMutationsFound" value="${decisionInstance.noMutationsFound}"/>
						<label for="noMutationsFound">No mutations found</label>
					</p>
					<p class="nomargin">
						<g:checkBox disabled="${true}" style="margin-right: 1em" id="unanimous" name="unanimous" value="${decisionInstance.unanimous}"/>
						<label for="unanimous">Decision is unanimous</label>
					</p>
					<p class="nomargin">
						<span style="width: 20em; display: inline-block">Report status:</span>
						<g:select disabled="${true}" name="decisionType" 
							valueMessagePrefix="report.decisionType"
							from="${Decision.constraints.decisionType.inList}" 
							value="${decisionInstance?.decisionType}"
							noSelection="${['null':'Select One...']}"/>
					</p>
					<p class="nomargin">
						<span style="width: 20em; display: inline-block"><label for="decision">Overall comment to clinician: </label></span>
						<g:textField disabled="${true}" name="decision" id="decision" value="${decisionInstance?.decision}"/>
					</p>
					
					<g:each in="${decisionInstance.reportableMutations}" var="rep" status="i">
						<hr/>
						<g:set var="observedMutations" value="${rep.observedMutations}"/>
						<g:set var="knownMutation" value="${observedMutations.iterator().next().knownMutation}"/>
						<g:set var="panels" value="${rep.getPanels()}"/>
						<h4>Mutation: ${knownMutation.toLabel()}</h4>
						<g:hiddenField name="_mutationIds" value="${observedMutations.join(',')}"/>
						<p class="nomargin">
							<g:checkBox disabled="${true}" style="margin-right: 1em" name="_identifiedBySequenom" id="_identifiedBySequenom"
								value="${panels.find { it.technology == 'Sequenom' } != null}"/>
							<label for="_identifiedBySequenom">Identified by Sequenom OncoCarta</label>
						</p>
						<p class="nomargin">
							<g:checkBox disabled="${true}" style="margin-right: 1em" name="_identifiedByPacbio" id="_identifiedByPacbio"
								value="${panels.find { it.technology == 'PacBio' } != null}"/>
							<label for=_identifiedByPacbio>Identified by PacBio</label>
						</p>
						<p class="nomargin">
							<g:checkBox disabled="${true}" style="margin-right: 1em" name="_validatedByABI" id="_validatedByABI"
								value="${panels.find { it.technology == 'ABI' } != null}"/>
							<label for="_validatedByABI">Validated by ABI Sanger</label>
						</p>
						<p class="nomargin">
							<g:checkBox disabled="${true}" style="margin-right: 1em" id="reportable" name="reportable" value="${true}" checked="${rep.reportable}"/>
							<label for="reportable">Reportable</label>
						</p>
						<p class="nomargin">
							<g:checkBox disabled="${true}" style="margin-right: 1em" id="actionable" name="actionable" value="${true}" checked="${rep.actionable}"/>
							<label for="actionable">Actionable</label>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block">Level of evidence (mutation):</span>
							<g:select disabled="${true}" 
								name="levelOfEvidence" 
								from="${ReportableMutation.constraints.levelOfEvidence.inList}"  
								noSelection="${['':'Not Specified...']}"
								value="${rep.levelOfEvidence}"/>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block">Level of evidence (gene):</span>
							<g:select disabled="${true}" 
								name="levelOfEvidenceGene" 
								from="${ReportableMutation.constraints.levelOfEvidenceGene.inList}" 
								noSelection="${['':'Not Specified...']}"
								value="${rep.levelOfEvidenceGene}"/>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block"><label for="comment">Comment to clinician:</label></span>
							<g:textField disabled="${true}" id="comment" name="comment" value="${rep.comment}"/>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block"><label for="justification">Justification for decision:</label></span>
							<g:textField disabled="${true}" id="justification" name="justification" value="${rep.justification}"/>
						</p>
					</g:each>
					<hr/>
	    			<g:actionSubmit value="Create New Decision" action="create" />
				</form>
            </div>
        </div>
    </body>
</html>
