<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="geneName" value="${gene.name}" />
        <title>${geneName}</title>
    </head>
    <body>
        <div id="page">
        <div class="body">
        	<g:form action="saveGene" method="post">
        		<g:hiddenField name="submit_action" value="" id="submit_action" />
	        	<g:include action="setGene" model="${ [gene: gene] }"/>
	        </g:form>
        </div>
        </div>
    </body>
</html>