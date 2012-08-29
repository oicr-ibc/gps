
<%@ page import="ca.on.oicr.gps.model.system.AppConfig" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'appConfig.label', default: 'Setting')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
		<div id="page">
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
		    <div class="buttons">
                <form method="post" action="list">
                    <span style="float:right" class="button"><g:link class="dialog-trigger create" action="create">Register <g:message code="default.new.label" args="[entityName]" /></g:link></span>
                </form>
            </div>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="configKey" title="${message(code: 'appConfig.configKey.label', default: 'Setting Name')}" />
                        
                            <g:sortableColumn property="configValue" title="${message(code: 'appConfig.configValue.label', default: 'Setting Value')}" />
							<th>Actions</th>                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${appConfigInstanceList}" status="i" var="appConfigInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean: appConfigInstance, field: "configKey")}</td>
                        
                            <td>${fieldValue(bean: appConfigInstance, field: "configValue")}</td>
                        	<td>
                                <g:link class="dialog-trigger" action="show" id="${fieldValue(bean: appConfigInstance, field: 'id')}">${message(code: 'default.button.show.label', default: 'View')}</g:link>
                                <g:link class="dialog-trigger" action="edit" id="${fieldValue(bean: appConfigInstance, field: 'id')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
                           	</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${appConfigInstanceTotal}" />
            </div>
        </div>
        </div>
    </body>
</html>
