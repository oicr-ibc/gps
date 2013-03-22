

<%@ page import="ca.on.oicr.gps.model.reporting.Query" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${queryInstance}">
            <div class="errors">
                <g:renderErrors bean="${queryInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="query.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: queryInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${queryInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="bottom" colspan="2" class="name">
                                    <label for="body"><g:message code="query.body.label" default="Body" /></label>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" colspan="2" class="value ${hasErrors(bean: queryInstance, field: 'body', 'errors')}">
                                    <g:textArea rows="10" cols="60" name="body" value="${queryInstance?.body}" />
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
