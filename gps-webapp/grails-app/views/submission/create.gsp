
<%@ page import="ca.on.oicr.gps.model.data.Submission" %>
<%@ page import="ca.on.oicr.gps.pipeline.model.PipelineError" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'submission.label', default: 'Submission')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${submissionInstance}">
            <div class="errors">
                <g:renderErrors bean="${submissionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="save"/></g:set>
            <g:uploadForm action="save" method="post" data-url="${url}">
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dataType"><g:message code="submission.dataType.label" default="Data Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dataType', 'errors')}">
                                     <g:select name="dataType" from="${pipelines.collect { it.getTypeKey() } }" value="${submissionInstance?.dataType}" valueMessagePrefix="sample.dataType"  />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="userName"><g:message code="submission.userName.label" default="User Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'userName', 'errors')}">
                                    <g:set var="username"><sec:username/></g:set>
                                    <g:hiddenField name="userName" value="${username}" />
                                    <sec:username/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateSubmitted"><g:message code="submission.dateSubmitted.label" default="Date Submitted" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dateSubmitted', 'errors')}">
                                    <g:jqDatePicker disabled="disabled" name="dateSubmitted" precision="day" value="${new Date()}"  />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dataFile"><g:message code="submission.dataFile.label" default="Data File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: submissionInstance, field: 'dataFile', 'errors')}">
                                    <input type="file" id="dataFile" name="dataFile" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
		         <g:if test="${flash.pipelineErrors}">
			         <h1>Errors</h1>
			         <ul class="errors">
				         <g:each in="${flash.pipelineErrors}">
				         	<li>
					         	<g:if test="${it instanceof PipelineError}">
					         	<g:message code="${it.key}" args="${it.args}"/>
					         	</g:if>
					         	<g:elseif test="${it instanceof Exception}">
					         	${it.getLocalizedMessage()}
					         	</g:elseif>
					         	<g:else>
					         	${it}
					         	</g:else>
					         </li>
				         </g:each>
			         </ul>
				</g:if>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                    <span class="button"><g:link class="delete" action="list">Cancel</g:link></span>
                </div>
            </g:uploadForm>
        </div>
    </body>
</html>
