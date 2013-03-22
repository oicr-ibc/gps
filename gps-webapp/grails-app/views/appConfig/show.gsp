
<%@ page import="ca.on.oicr.gps.model.system.AppConfig" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'appConfig.label', default: 'Setting')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="appConfig.configKey.label" default="Setting Key" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: appConfigInstance, field: "configKey")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="wrapname" colspan="2"><g:message code="appConfig.configValue.label" default="Setting Value" /></td>
                        </tr>
                        
                        <tr class="prop">
                        	<td valign="top" class="wrapvalue" colspan="2">${fieldValue(bean: appConfigInstance, field: "configValue")}</td>                        
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${appConfigInstance?.id}" />
                    
                    <span class="button">
                    	<g:link class="edit dialog-trigger" action="edit" id="${fieldValue(bean: appConfigInstance, field: 'id')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
                    </span>
                </g:form>
            </div>
        </div>
    </body>
</html>
