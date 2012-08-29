<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Mutation report for ${mutation.knownGene.name} ${mutation.mutation}</title>
<g:unless test="${mutation.isConfirmed() }">
<meta name="draft"></meta>
</g:unless>
</head>
<body>
	<p>
    Status: 
    <g:if test="${! mutation.isComplete()}">
    <span class="statusincomplete">incomplete</span>
    </g:if>
    <g:elseif test="${! mutation.isConfirmed()}">
    <span class="statusnotconfirmed">not confirmed</span>
    </g:elseif>
    <g:else>
    <span class="statuscomplete">complete</span>
    </g:else>
	&#x2014; 
	<g:if test="${mutation.lastEditedBy}">
	Last saved by: ${mutation.lastEditedBy} on <g:formatDate format="MM/dd/yyyy hh:mm a" date="${mutation.lastUpdated}" />
	</g:if>
	<g:else>
	Not yet saved
	</g:else>
	</p>
	  
	<hr />
	<h3>Frequency of ${mutation.mutation} mutation in ${mutation.knownGene.name} in the top tumour types</h3>
	
	<table>
	<thead>
		<tr>
			<th align="left" width="30"></th>
			<th align="left" width="220">Tumour</th>
			<th align="left" width="80">Frequency</th>
			<th align="left" width="100">Samples</th>
		</tr>
	</thead>
	<tbody>
		<g:set var="count" value="${0}"/>
		
		<g:each in="${mutation.frequencies.asList().take(10)}" status="i" var="frequency">
			<tr>
				<td>${i+1}.</td>
				<td>${fieldValue(bean: frequency.tumourType, field: "name")}</td>
				<td><g:formatNumber number="${frequency.frequency * 100.0}" format="0.000" />%</td>
				<td>(${frequency.affected}/${frequency.samples} samples)</td>
				<g:set var="count" value="${i+1}"/>
			</tr>
		</g:each>
	</tbody>
	</table>
	
	<hr />
	<h3>${mutation.knownGene.name} characteristics</h3>
	
	<h4>Full name: ${mutation.knownGene.characteristics.fullName}</h4>
	
	<p>${mutation.knownGene.characteristics.description}</p>
	               
	<hr />
	<h3>${mutation.knownGene.name} ${mutation.mutation} characteristics</h3>
	  	
	<p>The functional consequence of this mutation is: <b>${mutation?.characteristics?.actionCode ?: 'unknown'}</b>.
	   Reference (PMID): <g:pubmed>${mutation?.characteristics?.actionReference ?: ''}</g:pubmed></p>
	
	<g:if test="${mutation?.characteristics?.actionCode == 'other' && mutation?.characteristics?.actionComment}">
	<g:each in="${(mutation?.characteristics?.actionComment ?: '').split('\n+')}" var="paragraph">
		<g:if test="${paragraph.trim().size() != 0}">
		<p><g:pubmed>${paragraph.trim()}</g:pubmed></p>
		</g:if>
	</g:each>
	</g:if>
					
	<hr />
	<h3>Clinical and Preclinical Studies</h3>
	
					<g:set var="significances" value="${mutation.orderedSignificances}"/>
					<g:each in="${significances}" status="i" var="significance">
					  <g:set var="frequency" value="${mutation.getSignificanceFrequency(significance)}"/>
					  <h4>${i+1}. ${fieldValue(bean: significance.tumourType, field: "name")}
            <g:if test="${frequency?.samples}">
	- <g:formatNumber number="${frequency.frequency * 100.0}" format="0.000" />%
	</g:if></h4>
	
			<p>In this tumour type, the clinical significance of this mutation <g:message code="significance.${significance.significanceCode}"/>.</p>
			<g:unless test="${significance.significance == significance.SIGNIFICANCE_UNKNOWN}">
				<p>${significance.significanceComment}</p>
				<p>Reference: <g:pubmed>${significance.significanceReference}</g:pubmed> - evidence ${significance.significanceEvidence}</p>
			</g:unless>
		</g:each>
		
	<hr />
	<h3>Availability of Investigational Agents</h3>
	  	
	<g:each in="${mutation.effectiveness}" var="effectiveness">
		<p>
		The available investigational agents <i>${effectiveness.agents}</i> have documented efficacy: 
		<b><g:message code="${effectiveness.effectivenessCode}"/></b>
		</p>
	</g:each>
	
	<hr />
	<h3>Sensitivity and Resistance Conferred by Mutation</h3>
	
	<g:each in="${mutation.sensitivity}" var="sensitivity">
						<p><g:message code="sensitivity.${sensitivity?.sensitivityCode ?: 'unknown'}" args="${[sensitivity?.agentName]}"/></p>
	</g:each>
	
	<hr />
	<h3>Report History</h3>
	  	
	<g:if test="${mutation.confirmations.size() > 0}">
		<table>
			<thead>
				<tr>
					<th align="left" width="120">Date</th>
					<th align="left" width="100">Confirmed by</th>
					<th align="left" width="220">Comment</th>
				</tr>
			</thead>
			<tbody>
	  			<g:each in="${mutation.confirmations}" var="confirmation">
	  				<tr>
	 					<td><g:formatDate format="MM/dd/yyyy hh:mm a" date="${confirmation.date}" /></td>
	  					<td>${confirmation.name ?: confirmation.userName}</td>
	  					<td>${confirmation.comment}</td>
	 				</tr>
	  			</g:each>
	 		</tbody>
		</table>
	</g:if>
	<g:else>
		<p>No history</p>
	</g:else>
</body>
</html>
