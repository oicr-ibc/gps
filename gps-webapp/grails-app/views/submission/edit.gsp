<%@ page import="ca.on.oicr.gps.model.data.Submission" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'submission.label', default: 'Submission')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${submissionInstance}">
            <div class="errors">
                <g:renderErrors bean="${submissionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="update"/></g:set>
            <g:uploadForm method="get" data-url="${url}">
                <g:hiddenField name="id" value="${submissionInstance?.id}" />
                <g:hiddenField name="version" value="${submissionInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                   <label for="dataType"><g:message code="submission.dataType.label" default="Data Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dataType', 'errors')}">
                                     <g:select name="dataType" from="${submissionInstance.constraints.dataType.inList}" value="${submissionInstance?.dataType}" valueMessagePrefix="sample.dataType"  />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                   <label for="dateSubmitted"><g:message code="submission.dateSubmitted.label" default="Date Submitted" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dateSubmitted', 'errors')}">
                                   <g:jqDatePicker name="dateSubmitted" value="${submissionInstance?.dateSubmitted}" />
                                </td>
                            </tr>
                        <!--
                            <tr class="prop">
                                <td valign="top" class="name">
                                   <label for="dataFile"><g:message code="submission.dataFile.label" default="Data File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dataFile', 'errors')}">
                                   <input type="file" id="dataFile" name="dataFile" />
                                </td>
                            </tr>
                        -->
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <!--<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>-->
                </div>
            </g:uploadForm>
        </div>
    </body>
</html>
