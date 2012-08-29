
<%@ page import="ca.on.oicr.gps.model.data.Subject" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'subject.label', default: 'Patient')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <div class="buttons">
                <form method="post" action="list">
                    <g:textField name="patientId" value="${params.patientId}" ></g:textField>
                    <span class="button"><input type="submit" class="search" value="Search Patient Identifier" name="_action_list"></span>
                </form>
            </div>
            <div class="list">
                <table>
                    <thead>
                        <tr>         
                            <g:sortableColumn property="patientId" 
                                              title="${message(code: 'sample.patientId.label', default: 'Patient Id')}" 
                                              params="${params}" />
                            <th>${message(code: 'status.consentDate.label', default: 'Consent Date')}</th>
                            <th>${message(code: 'status.biopsyDate.label', default: 'Biopsy Date')}</th>
                            <th>${message(code: 'status.elapsedWorkingDays.label', default: 'Elapsed Working Days')}</th>
                            <th>${message(code: 'status.medidataUploadDate.label', default: 'MediData Upload Date')}</th>
                            <th>${message(code: 'status.sequenomDate.label', default: 'Sequenom Run Date')}</th>
                            <th>${message(code: 'status.pacBioDate.label', default: 'PacBio Run Date')}</th>
                            <th>${message(code: 'status.sangerDate.label', default: 'Sanger Run Date')}</th>
                			<th>Actions</th>
						</tr>
                    </thead>
                    <tbody>
                    <g:each in="${subjectReportableInstanceList}" status="i" var="subjectReportableInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link controller="summary" action="show" id="${fieldValue(bean: subjectReportableInstance.subject, field: 'id')}">${subjectReportableInstance.subject.patientId}</g:link></td>
                            <td>${dateFormat(date: subjectReportableInstance.consentDate)}</td>
                            <td>${dateFormat(date: subjectReportableInstance.biopsyDate)}</td>
                            <td>${subjectReportableInstance.subject.summary.elapsedWorkingDays}</td>
                            <td>${dateFormat(date: subjectReportableInstance.medidataUploadDate)}</td>
                            <td>${dateFormat(date: subjectReportableInstance.sequenomDate)}</td>
                            <td>${dateFormat(date: subjectReportableInstance.pacBioDate)}</td>
                            <td>${dateFormat(date: subjectReportableInstance.sangerDate)}</td>
                            <td>
                                <g:if test="${subjectReportableInstance.subject.reports}">
                                	<g:link controller="report" action="subject" id="${fieldValue(bean: subjectReportableInstance.subject, field: 'id')}">${message(code: 'default.button.generateMediData.label', default: 'View MediData')}</g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${subjectReportableInstanceTotal}" />
            </div>
        </div>
        </div>
    </body>
</html>
