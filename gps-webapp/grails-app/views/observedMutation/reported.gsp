<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="default.reported.label" default="Mark Reported" /></title>
    </head>
    <body>
        
        <div class="body">
            <h1><g:message code="default.reported.label" default="Mark Reported" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${observedMutationInstance}">
            <div class="errors">
                <g:renderErrors bean="${observedMutationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="reported"/></g:set>
            <g:form method="get" data-url="${url}">
                <g:hiddenField name="id" value="${observedMutationInstance?.id}" />
                <g:hiddenField name="version" value="${observedMutationInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="reported"><g:message code="observedMutation.reported.label" default="Date Reported" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: observedMutationInstance, field: 'reported', 'errors')}">
                                    <g:jqDatePicker name="reported" value="${new Date()}"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="updateReported" value="${message(code: 'default.button.markReported.label', default: 'Mark Reported')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
