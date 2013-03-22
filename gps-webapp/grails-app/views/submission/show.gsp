
<%@ page import="ca.on.oicr.gps.model.data.Submission" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'submission.label', default: 'Submission')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

       <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="submission.id.label" default="Id" /></td>
                            <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "id")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="submission.dataType.label" default="Data Type" /></td>
                            <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "dataType")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="submission.userName.label" default="User Name" /></td>
                            <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "userName")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="submission.dateSubmitted.label" default="Date Submitted" /></td>
                            <td valign="top" class="value"><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${submissionInstance?.dateSubmitted}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="submission.fileName.label" default="Data File" /></td>
                            <td valign="top" class="value">${submissionInstance?.fileName}</td>
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${submissionInstance?.id}" />
                    <sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">	
                    <span class="button"><g:link class="dialog-trigger edit" action="edit" id="${submissionInstance?.id}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link></span>
                	</sec:ifAnyGranted>
                </g:form>
            </div>
        </div>
    </body>
</html>
