<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'knowledge.label', default: 'Knowledge Base')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="knowledge.label" default="Knowledge Base" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            
            <div id="searchform">
            	<form>
					<input id="searchterm" type="text" name="query" autocomplete="off" value="${params.query ?: ''}"/>
					<input type="submit" value="Search" />
				</form>
            </div>
            
            <div>
            	<h3>Genes</h3>
            	<div id="generesults"></div>
            </div>
            <hr>
            <div>
            	<h3>Mutations</h3>
            	<div id="mutationresults"></div>
            </div>
        </div>
        </div>
        <content tag="postJQuery">
			<%-- This is included after we have loaded jQuery --%>
            <g:javascript src='libs/jquery-ui-onDelayedKeyup.js'/>
        	<g:javascript>
function updateSearch() {
  jQuery.ajax({
    url: "${createLinkTo(dir:'knowledge',file:'queryGeneHtml')}",
    data: {"term": jQuery("#searchterm").val()},
    success: function(data, textStatus, jqXHR) {
      jQuery("#generesults").empty().append(data);
    },
    error: function(jqXHR, textStatus, errorThrown) {
	  jQuery('#generesults').empty().append(errorThrown);
    }		
  });
  jQuery.ajax({
    url: "${createLinkTo(dir:'knowledge',file:'queryMutationHtml')}",
    data: {"term": jQuery("#searchterm").val()},
    success: function(data, textStatus, jqXHR) {
      jQuery("#mutationresults").empty().append(data);
    },
    error: function(jqXHR, textStatus, errorThrown) {
	  jQuery('#mutationresults').empty().append(errorThrown);
    }		
  });
}
        	
jQuery(document).ready(function() {
  
  jQuery("#searchterm").onDelayedKeyup({
    handler: function() {
      updateSearch();
    }
  });
  
  updateSearch();
});
        	</g:javascript>
		</content>
    </body>
</html>