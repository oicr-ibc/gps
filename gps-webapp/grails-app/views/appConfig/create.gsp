

<%@ page import="ca.on.oicr.gps.model.system.AppConfig" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'appConfig.label', default: 'Setting')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${appConfigInstance}">
            <div class="errors">
                <g:renderErrors bean="${appConfigInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="save"/></g:set>
            <g:form action="save" method="get" data-url="${url}">
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="configKey"><g:message code="appConfig.configKey.label" default="Setting Key" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: appConfigInstance, field: 'configKey', 'errors')}">
                                    <g:textField name="configKey" value="${appConfigInstance?.configKey}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="wrapname" colspan="2">
                                  <label for="configValue"><g:message code="appConfig.configValue.label" default="Setting Value" /></label>
                                </td>
                            </td>
                            
                            <tr class="prop">
                                <td valign="top" class="wrapvalue ${hasErrors(bean: appConfigInstance, field: 'configValue', 'errors')}" colspan="2">
                                    <g:textArea name="configValue" value="${appConfigInstance?.configValue}" rows="5"/>
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
