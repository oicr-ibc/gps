
<%@ page import="ca.on.oicr.gps.model.data.Submission" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'submission.label', default: 'Submission')}" />
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
                	<sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
                    <span style="float:right" class="button"><g:link class="create" action="create">Register <g:message code="default.new.label" args="[entityName]" /></g:link></span>
                	</sec:ifAnyGranted>
                </form>
            </div>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="id" title="${message(code: 'submission.id.label', default: 'Id')}" />
                        	<g:sortableColumn property="dateSubmitted" title="${message(code: 'submission.dateSubmitted.label', default: 'Submitted')}" />
                            <g:sortableColumn property="userName" title="${message(code: 'submission.userName.label', default: 'User Name')}" />
                            <th>Data File</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${submissionInstanceList}" status="i" var="submissionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'} ${submissionInstance.processes.size() > 0 ? 'active' : 'passive'}">
                            <td><g:link action="show" id="${submissionInstance.id}">${fieldValue(bean: submissionInstance, field: "id")}</g:link></td>
                            <td><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${submissionInstance.dateSubmitted}"/></td>
                            <td>${fieldValue(bean: submissionInstance, field: "userName")}</td>
                            <td>${fieldValue(bean: submissionInstance, field: "fileName")}</td>
                            <td>
                                <g:link class="dialog-trigger" action="show" id="${fieldValue(bean: submissionInstance, field: 'id')}">${message(code: 'default.button.show.label', default: 'View')}</g:link><sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">,
                                <g:link class="dialog-trigger" action="edit" id="${fieldValue(bean: submissionInstance, field: 'id')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>,
                                <g:link class="download" action="download" id="${submissionInstance.id}">Download</g:link><g:if test="${submissionInstance.processes.size() == 0}">,
                                <g:link class="dialog-trigger" controller="pipeline" action="run" id="${submissionInstance.id}">Run Pipeline</g:link>
                                </g:if>
                               	</sec:ifAnyGranted>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${submissionInstanceTotal}" />
            </div>
        </div>
        </div>
    </body>
</html>
