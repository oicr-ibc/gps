<%@ page import="ca.on.oicr.gps.model.data.Decision" %>
<%@ page import="ca.on.oicr.gps.model.data.ReportableMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'decision.label', default: 'Decision')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" />: <g:link controller="summary" action="show" id="${decisionInstance.subject.id}" fragment="decisions">${decisionInstance.subject.patientId}</g:link></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${decisionInstance}">
            <div class="errors">
                <g:renderErrors bean="${decisionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <div>
				<h4>Other decisions</h4>
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
					<h4>Overall decision report</h4>
					
					<p class="nomargin">
						<span style="width: 20em; display: inline-block">Sample source:</span>
						<g:select name="source" 
							valueMessagePrefix="sample.source"
							from="${Decision.constraints.source.inList}" 
							value="${decisionInstance?.source}"
							noSelection="${['':'Select One...']}"/>
					</p>
					<p class="nomargin">
						<g:checkBox style="margin-right: 1em" id="noTumour" name="noTumour" value="${decisionInstance.noTumour}"/>
						<label for="noTumour">No tumour</label>
					</p>
					<p class="nomargin">
						<g:checkBox style="margin-right: 1em" id="insufficientMaterial" name="insufficientMaterial" value="${decisionInstance.insufficientMaterial}"/>
						<label for="insufficientMaterial">Insufficient material for analysis</label>
					</p>
					<p class="nomargin">
						<g:checkBox style="margin-right: 1em" id="noMutationsFound" name="noMutationsFound" value="${decisionInstance.noMutationsFound}"/>
						<label for="noMutationsFound">No mutations found</label>
					</p>
					<p class="nomargin">
						<g:checkBox style="margin-right: 1em" id="unanimous" name="unanimous" value="${decisionInstance.unanimous}"/>
						<label for="unanimous">Decision is unanimous</label>
					</p>
					<p class="nomargin">
						<span style="width: 20em; display: inline-block">Report status:</span>
						<g:select name="decisionType" 
							valueMessagePrefix="report.decisionType"
							from="${Decision.constraints.decisionType.inList}" 
							value="${decisionInstance?.decisionType}"
							noSelection="${['':'Select One...']}"/>
					</p>
					<p class="nomargin">
						<span style="width: 20em; display: inline-block"><label for="decision">Overall comment to clinician: </label></span>
					</p>
          <p class="nomargin">
            <g:textArea name="decision" id="decision" value="${decisionInstance?.decision}" cols="${60}" />
          </p>
          					
					<g:each in="${decisionInstance.reportableMutations}" var="rep" status="i">
						<hr/>
						<g:set var="observedMutations" value="${rep.observedMutations}"/>
						<g:set var="knownMutation" value="${observedMutations.iterator().next().knownMutation}"/>
						<g:set var="panels" value="${rep.getPanels()}"/>
						<h4>Mutation: ${knownMutation.toLabel()}</h4>
						<g:hiddenField name="_mutationIds" value="${observedMutations.collect { it.id }.join(',')}"/>
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
							<g:checkBox style="margin-right: 1em" id="reportable" name="reportable" value="${i}" checked="${rep.reportable}"/>
							<label for="reportable">Reportable</label>
						</p>
						<p class="nomargin">
							<g:checkBox style="margin-right: 1em" id="actionable" name="actionable" value="${i}" checked="${rep.actionable}"/>
							<label for="actionable">Actionable</label>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block">Level of evidence (mutation):</span>
							<g:select 
								valueMessagePrefix="report.levelOfEvidence"
								name="levelOfEvidence" 
								from="${ReportableMutation.constraints.levelOfEvidence.inList}"  
								noSelection="${['':'Not Specified...']}"
								value="${rep.levelOfEvidence}"/>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block">Level of evidence (gene):</span>
							<g:select 
								valueMessagePrefix="report.levelOfEvidence"
								name="levelOfEvidenceGene" 
								from="${ReportableMutation.constraints.levelOfEvidenceGene.inList}" 
								noSelection="${['':'Not Specified...']}"
								value="${rep.levelOfEvidenceGene}"/>
						</p>
						<p class="nomargin">
							<span style="width: 20em; display: inline-block"><label for="comment">Comment to clinician:</label></span>
						</p>
						<p class="nomargin">
              <g:textArea id="comment" name="comment" value="${rep.comment}" cols="${60}"/>
            </p>
            
						<p class="nomargin">
							<span style="width: 20em; display: inline-block"><label for="justification">Justification for decision:</label></span>
						</p>
            <p class="nomargin">
              <g:textArea id="justification" name="justification" value="${rep.justification}" cols="${60}"/>
            </p>						
						
					</g:each>
	    			<g:actionSubmit value="Save Decision" action="save" />
            <g:actionSubmit value="Cancel" action="cancel" />
				</form>
            </div>
        </div>
    </body>
</html>
