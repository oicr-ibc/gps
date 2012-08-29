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
            
           	<g:form name="myForm" controller="knowledge" action="confirm" method="post">
	    		<g:hiddenField name="mutation" value="${mutation.toLabel()}"/>
	    		<g:actionSubmit value="Cancel" action="mutation" /><br/>
	    		<g:actionSubmit value="Save and Confirm" action="confirm" />
	    		with comment:
	    		<g:textField name="confirmation_comment" value="" size="40" maxlength="255" />
	    	</g:form>
            
            <div style="width:70em">
				<g:render template="mutationReport" model="${[mutation: mutation, cosmicMutation: cosmicMutation]}"/>
			</div>
        </div>
        </div>
    </body>
</html>