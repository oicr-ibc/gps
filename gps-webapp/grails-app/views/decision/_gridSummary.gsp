<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.DateTime" %>

<%-- Yes this is hideous. Localizable text is not easy to generate in complex
     blocks, especially without the nice whitespace formatting tricks that we
     get from TT 
--%>

<g:set var="fmt" value="${DateTimeFormat.forPattern('d MMMM yyyy h:mma')}"/>
<g:set var="dateValue" value="${new DateTime(decision.date)}"/>

<g:message code="report.decisionType.${decision.decisionType}" /> report:
<g:if test="${decision.noTumour}">
<g:message code="report.decision.noTumour" default="No tumour"/>.
</g:if>
<g:if test="${decision.insufficientMaterial}">
<g:message code="report.decision.insufficientMaterial" default="Insufficient material"/>.
</g:if>
<g:if test="${decision.noMutationsFound}">
<g:message code="report.decision.noMutationsFound" default="No mutations found"/>.
</g:if>
<g:if test="${decision.unanimous}">
<g:message code="report.decision.unanimous" default="Unanimous"/>.
</g:if>
<g:else>
<g:message code="report.decision.notUnanimous" default="Not unanimous"/>.
</g:else>
<g:each in="${decision.reportableMutations}" var="rep" status="i">
	<g:set var="observedMutations" value="${rep.observedMutations}"/>
	<g:set var="knownMutation" value="${observedMutations.iterator().next().knownMutation}"/>
	<g:set var="panels" value="${rep.getPanels()}"/>
	${knownMutation.toLabel()}:
	<g:each in="${panels.findAll { it.technology != 'ABI' }.sort { a,b -> a.technology.compareTo(b.technology) }}" var="panel" status="j">
		<g:message code="report.decision.identifiedBy" default="identified by {0}" args="${[panel.technology]}"/>,
	</g:each>
	<g:if test="${panels.find { it.technology == 'ABI' } != null}">
		<g:message code="report.decision.validatedBy" default="validated by {0}" args="${['ABI']}"/>,
	</g:if>
	<g:if test="${rep.reportable}">
		<g:message code="report.decision.reportable" default="reportable"/>,
	</g:if>
	<g:else>
		<g:message code="report.decision.notReportable" default="not reportable"/>,
	</g:else>
	<g:if test="${rep.actionable}">
		<g:message code="report.decision.actionable" default="actionable"/>,
	</g:if>
	<g:else>
		<g:message code="report.decision.notActionable" default="not actionable"/>,
	</g:else>
	<g:message code="report.decision.loe" default="LOE: {0}" args="${[rep.levelOfEvidence]}"/><%--
	--%><g:if test="${rep.levelOfEvidenceGene}">,
		<g:message code="report.decision.loe.gene" default="{0} LOE: {1}" args="${[knownMutation.gene, rep.levelOfEvidenceGene]}"/><%--
	--%></g:if><%--
	--%><g:if test="${rep.comment}">,
		${rep.comment}<%--
	--%></g:if><%--
	--%><g:if test="${rep.justification}">,
		Justification: ${rep.justification}<%--
	--%></g:if>.
	<g:if test="${decision.decision}">
		Decision: ${decision.decision}.
	</g:if>
</g:each>