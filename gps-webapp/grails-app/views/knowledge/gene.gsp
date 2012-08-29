<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'knowledge.label', default: 'Knowledge Base')}" />
        <g:set var="geneName" value="${gene.name}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="gene.label" default="Gene"/>: ${geneName}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div id="tabs" class="knowledge_tabs">
			    <ul>
			        <li><a href="#summarycontainer"><span>Summary</span></a></li>
			        <li><a href="#genomiccontainer"><span>Genomic information</span></a></li>
<%--			        <li><a href="#reportcontainer"><span>Report</span></a></li>--%>
			        <li><a href="#searchcontainer"><span>Search sources</span></a></li>
			    </ul>
			    <div id="summarycontainer">
			    	<g:form name="myForm" controller="knowledge" action="editGene" method="get">
			    		<g:hiddenField name="gene" value="${geneName}"/>
			    		<g:actionSubmit value="Edit Gene Information" action="editGene" />
			    	</g:form>
			    	
		    		<table>
			    		<tbody>
			    			<tr>
			    				<th style="width: 14em" class="nobackground">Gene:</th>
			    				<td>${geneName}</td>
			    			</tr>
			    			<tr>
			    				<th class="nobackground">Full name:</th>
			    				<td>${gene.characteristics.fullName}</td>
			    			</tr>
			    			<tr>
			    				<th class="nobackground">Somatic tumour types:</th>
			    				<td>${gene.characteristics.somaticTumorTypes}</td>
			    			</tr>
			    			<tr>
			    				<th class="nobackground">Germline tumour types:</th>
			    				<td>${gene.characteristics.germlineTumorTypes}</td>
			    			</tr>
			    			<tr>
			    				<th class="nobackground">Cancer syndrome:</th>
			    				<td>${gene.characteristics.cancerSyndrome}</td>
			    			</tr>
			    			<tr>
			    				<th colspan="2" class="nobackground">Description:</th>
			    			</tr>
			    			<tr>
			    				<td colspan="2">${gene.characteristics.description}</td>
			    			</tr>
			    			<tr>
			    				<th colspan="2" class="nobackground">Known mutations:</th>
			    			</tr>
			    			<tr>
			    				<td colspan="2">
			    				<g:join in="${gene.visibleMutations.collect { it.toLabel() }.collect { link([action: 'mutation', params: [mutation: it]], it).toString() }.sort { a,b -> a.compareTo(b) } }" />
			    				</td>
			    			</tr>
			    		</tbody>
			    	</table>
			    </div>
			    <div id="genomiccontainer">
	                <table>
	                    <tbody>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="gene.chromosome.label" default="Chromosome" />:</td>
	                            <td valign="top" class="value">${gene.chromosome}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="gene.start.label" default="Start" />:</td>
	                            <td valign="top" class="value">${gene.start}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="gene.stop.label" default="Stop" />:</td>
	                            <td valign="top" class="value">${gene.stop}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="gene.geneSize.label" default="Size" />:</td>
	                            <td valign="top" class="value">${gene.geneSize}</td>
	                        </tr>
						</tbody>
					</table>					
				</div>
			    <div id="reportcontainer" style="display: none">
			    	<g:form name="myForm" controller="knowledge" action="editGeneReport" params="${ [gene: gene.name] }">
			    	<g:actionSubmit value="Edit Report" action="edit" />
			    	</g:form>
			    </div>
			    <div id="searchcontainer">
			    	<table style="width: 44em">
			    		<tbody>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.google.com/search">
										<input type="text"   name="q" size="25" maxlength="255" value="${gene.name}" />
										<input type="submit" value="Google Web Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://scholar.google.com/scholar">
								      <input type="hidden" name="hl" value="en">
								      <input type="text" name="q" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="submit" name="btnG" value="Google Scholar Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.ncbi.nlm.nih.gov/pubmed/">
								      <input type="text" name="term" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="submit" name="submit" value="PubMed Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.ncbi.nlm.nih.gov/gene">
								      <input type="text" name="term" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="submit" name="submit" value="NCBI Gene Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.sanger.ac.uk/perl/genetics/CGP/cosmic">
								      <input type="text" name="ln" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="hidden" name="action" value="gene"/>
								      <input type="submit" name="submit" value="Sanger COSMIC Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.uniprot.org/uniprot/">
								      <input type="text" name="query" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="hidden" name="sort" value="score"/>
								      <input type="submit" name="submit" value="UniProt Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.genecards.org/cgi-bin/carddisp.pl">
								      <input type="text" name="gene" size="25" maxlength="255" value="${gene.name}"/>
								      <input type="submit" name="submit" value="GeneCards Search" />
									</form>
			    				</th>
			    			</tr>
			    			<tr>
			    				<th class="nobackground" colspan="2">
			    					<form method="get" target="_blank" action="http://www.google.com/search">
								      <input type="text" name="q" size="25" maxlength="255" value="${gene.name}"/>
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