
<%@ page import="ca.on.oicr.gps.model.reporting.Query" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="name" title="${message(code: 'query.name.label', default: 'Name')}" />
                        
                            <th>
                            <g:message code="default.query.actions.label" default="Actions" />
                            </th>

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${queryInstanceList}" status="i" var="queryInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>
                            	<g:link action="show" id="${queryInstance.id}">
                            	${fieldValue(bean: queryInstance, field: "name")}
                            	</g:link>
                            </td>
                        
                            <td>
                            	<g:link action="run" id="${queryInstance.id}">
                            	<g:message code="default.query.run.label" default="Run" />
                            	</g:link>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${queryInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
