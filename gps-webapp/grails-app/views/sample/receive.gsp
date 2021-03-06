<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /> ${fieldValue(bean: sampleInstance, field: "barcode")}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${sampleInstance}">
            <div class="errors">
                <g:renderErrors bean="${sampleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="received"/></g:set>
            <g:form method="get" data-url="${url}">
                <g:hiddenField name="id" value="${sampleInstance?.id}" />
                <g:hiddenField name="version" value="${sampleInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="barcode"><g:message code="sample.barcode.label" default="Barcode" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'barcode', 'errors')}">
                                    ${sampleInstance?.barcode}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateReceived"><g:message code="sample.dateReceived.label" default="Date Received" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'dateReceived', 'errors')}">
                                    <g:jqDatePicker name="dateReceived" value="${new Date()}"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <!--<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>-->
                </div>
            </g:form>
        </div>
    </body>
</html>
i