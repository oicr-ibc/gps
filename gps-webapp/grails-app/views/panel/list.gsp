<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'panel.label', default: 'Panel')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="buttons">
                <form method="post" action="list">
                	<sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
                    <span style="float:right" class="button">
                    	<g:link class="create" action="create">Register <g:message code="default.new.label" args="[entityName]" /></g:link>
                    </span>
                	</sec:ifAnyGranted>
                </form>
            </div>
            <div class="list">
                <table>
                    <thead>
                        <tr>         
                            <g:sortableColumn property="name" title="${message(code: 'panel.name.label', default: 'Name')}" />
                            <g:sortableColumn property="technology" title="${message(code: 'panel.technology.label', default: 'Technology')}" />
                            <th>Version</th>
						</tr>
                    </thead>
                    <tbody>
                    <g:each in="${panelInstanceList}" status="i" var="panelInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${panelInstance.id}">${fieldValue(bean: panelInstance, field: "name")}</g:link></td>
                            <td>${fieldValue(bean: panelInstance, field: "technology")}</td>
                            <td>${fieldValue(bean: panelInstance, field: "versionString")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${panelInstanceTotal}"/>
            </div>
        </div>
        </div>
    </body>
</html>
