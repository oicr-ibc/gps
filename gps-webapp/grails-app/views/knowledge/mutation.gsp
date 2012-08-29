
<%@ page import="ca.on.oicr.gps.model.data.ObservedMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="geneName" value="${mutation.knownGene.name}" />
        <g:set var="mutationName" value="${mutation.mutation}" />
        <title>${geneName} ${mutationName}</title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1>${geneName} ${mutationName}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
	        <div id="tabs" class="knowledge_tabs">
			    <ul>
			        <li><a href="#summarycontainer"><span>Mutation information</span></a></li>
			        <li><a href="#genomiccontainer"><span>Genomic information</span></a></li>
			        <li><a href="#reportcontainer"><span>Reports</span></a></li>
			        <li><a href="#searchcontainer"><span>Search sources</span></a></li>
			    </ul>
			    <div id="summarycontainer">

			    	<g:form name="myForm" controller="knowledge" action="edit" method="get">
			    		<g:hiddenField name="mutation" value="${mutation.toLabel()}"/>
			    		<g:actionSubmit value="Edit Mutation Information" action="edit" />
			    	</g:form>
			    	
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
		    		<h3>Observations of ${mutation.knownGene.name} ${mutation.mutation}</h3>
  					<g:set var="observedCount" value="${ObservedMutation.getObservationCount(mutation)}"/>
  					<g:set var="patientCount" value="${ObservedMutation.getPatientCount(mutation)}"/>
  					
  					<g:if test="${observedCount > 0}">
  						<g:link controller="observedMutation" action="show" params="${ [mutation: mutation.toLabel() ]}">
  						<p>Observed ${observedCount} times in ${patientCount} patients - see details
  						</g:link>
  					</g:if>
  					<g:else>
  						<p>Not observed</p>
  					</g:else>
  
					<hr />
					<h3>Frequency of ${mutation.mutation} mutation in ${mutation.knownGene.name} in the top tumour types</h3>
					
					<table style="width: 52em">
					<thead>
						<tr>
							<th style="width: 1.0em"></th>
							<th style="width: 27.0em">Tumour</th>
							<th style="width: 9.0em">Frequency</th>
							<th style="width: 15.0em">Samples</th>
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
					<h3>
						${mutation.knownGene.name} characteristics
						<g:link action="gene" params="${ [gene: mutation.knownGene.name]}">
						(Show gene information)
						</g:link>
					</h3>
					
					<h4>Full name: ${mutation.knownGene.characteristics.fullName}</h4>
					
					<p>${mutation.knownGene.characteristics.description}</p>
					               
					<hr />
					<h3>${mutation.knownGene.name} ${mutation.mutation} characteristics</h3>
					  	
					<p>The functional consequence of this mutation is: <b><g:message code="action.${mutation?.characteristics?.actionCode ?: 'unknown'}" /></b>.
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
			    </div>
			    <div id="genomiccontainer">
	                <table>
	                    <tbody>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.chromosome.label" default="Chromosome" /></td>
	                            <td valign="top" class="value">${mutation.chromosome}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.gene.label" default="Gene" /></td>
	                            <td valign="top" class="value">${mutation.knownGene.name}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.mutation.label" default="Mutation" /></td>
	                            <td valign="top" class="value">${mutation.mutation}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.start.label" default="Start position" /></td>
	                            <td valign="top" class="value">${mutation.start}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.stop.label" default="End position" /></td>
	                            <td valign="top" class="value">${mutation.stop}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.refAllele.label" default="Reference allele" /></td>
	                            <td valign="top" class="value">${mutation.refAllele}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="mutation.allele.label" default="Allele" /></td>
	                            <td valign="top" class="value">${mutation.varAllele}</td>
	                        </tr>
						</tbody>
					</table>					
				</div>
			    <div id="reportcontainer">
			    	
			    	<!-- Embed the report from its own template -->
			    	
			    		<h3>Clinician Reports</h3>
			    		
			    		<ul>
			    			<li><g:link action="preview" params="${ [mutation: mutation.knownGene.name + ' ' + mutation.mutation] }">Preview current Clinician Report</g:link></li>
			    		</ul>
	  	
						<g:if test="${mutation.confirmations.size() > 0}">
							<table style="width: 62em">
								<thead>
									<tr>
										<th style="width: 20em">Date</th>
										<th style="width: 12em">Confirmed by</th>
										<th style="width: 20em">Comment</th>
										<th style="width: 10em"></th>
									</tr>
								</thead>
								<tbody>
						  			<g:each in="${mutation.confirmations}" var="confirmation">
						  				<tr>
						 					<td><g:formatDate format="MM/dd/yyyy hh:mm a" date="${confirmation.date}" /></td>
						  					<td>${confirmation.name ?: confirmation.userName}</td>
						  					<td>${confirmation.comment}</td>
						  					<td><g:link action="download" id="${confirmation.id}">Download PDF</g:link></td>
						 				</tr>
						  			</g:each>
						 		</tbody>
							</table>
						</g:if>
						<g:else>
							<p>No previous reports</p>
						</g:else>
			    	
<%--			    	<g:render template="mutationReport" model="${[mutation: mutation, cosmicMutation: cosmicMutation]}"/>--%>
			    </div>
			    <div id="searchcontainer">
			    	<table style="width: 44em">
			    		<tbody>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.google.com/search">
										<input type="text"   name="q" size="25" maxlength="255" value="${mutation.knownGene.name} ${mutation.mutation}" />
										<input type="submit" value="Google Web Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://scholar.google.com/scholar">
								      <input type="hidden" name="hl" value="en">
								      <input type="text" name="q" size="25" maxlength="255" value="${mutation.knownGene.name} ${mutation.mutation}"/>
								      <input type="submit" name="btnG" value="Google Scholar Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.ncbi.nlm.nih.gov/pubmed/">
								      <input type="text" name="term" size="25" maxlength="255" value="${mutation.knownGene.name} ${mutation.mutation}"/>
								      <input type="submit" name="submit" value="PubMed Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    				  <form action="http://cancer.sanger.ac.uk/cosmic-search/s/" method="post">
			    				    <input type="text" name="q" size="25" maxlength="255" value="${mutation.knownGene.name} ${mutation.mutation}"/>
                      <input type="submit" name="submit" value="Sanger COSMIC Search" />
                  </form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.google.com/search">
								      <input type="text" name="q" size="25" maxlength="255" value="${mutation.knownGene.name} ${mutation.mutation}"/>
								      <input type="hidden" name="sitesearch" value="mycancergenome.org"/>
								      <input type="submit" name="submit" value="My Cancer Genome Search" />
									</form>
			    				</th>
			    			</tr>
			    		</tbody>
			    	</table>
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