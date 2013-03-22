
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="default.home.label" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="default.home.label" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            
            <g:render template="/blocks/summary" />
            
            <g:render template="/blocks/links" />
            
            <g:render template="/blocks/reports" />

           	<sec:ifAnyGranted roles="ROLE_GPS-ADMINS">
            <g:render template="/blocks/admin" />
			</sec:ifAnyGranted>
        </div>
        </div>
    </body>
</html>