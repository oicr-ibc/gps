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
        	<g:form action="saveMutation" method="post">
        		<g:hiddenField name="submit_action" value="" id="submit_action" />
	        	<g:include action="setMutation" model="${ [mutation: mutation, cosmicMutation: cosmicMutation] }"/>
	        </g:form>
        </div>
        </div>
    </body>
</html>