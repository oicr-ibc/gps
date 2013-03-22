
<%@ page import="ca.on.oicr.gps.model.data.Subject" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'summary.mutations.label', default: 'Mutation Summary Report')}" />
        <title><g:message code="summary.mutations.label" default="Mutation Summary Report" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="summary.mutations.label" default="Mutation Summary Report" /></h1>
            <div class="list">
                <table>
                    <thead>
                        <tr>         
                            <g:sortableColumn property="patientId" 
                                              title="${message(code: 'sample.patientId.label', default: 'Patient Id')}" 
                                              params="${params}" />
                            <th>${message(code: 'status.consentDate.label', default: 'Consent Date')}</th>
                            <th>${message(code: 'status.biopsyDate.label', default: 'Biopsy Date')}</th>
                            <th>${message(code: 'sample.barcode.label', default: 'Sample')}</th>
                            <th>${message(code: 'sample.source.label', default: 'Sample Source')}</th>
                            <th>${message(code: 'status.elapsedWorkingDays.label', default: 'Elapsed Working Days')}</th>
                            <th>${message(code: 'panel.technology.label', default: 'Technology')}</th>
                            <th>${message(code: 'mutations.label', default: 'Observed Mutations')}</th>
						</tr>
                    </thead>
                    <tbody>
                    <g:each in="${runSampleList}" status="i" var="runSample">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link controller="summary" action="show" id="${fieldValue(bean: runSample.sample.subject, field: 'id')}">${runSample.sample.subject.patientId}</g:link></td>
                            <td>${dateFormat(date: runSample.sample.subject.summary.consentDate)}</td>
                            <td>${dateFormat(date: runSample.sample.subject.summary.biopsyDate)}</td>
                            <td>${runSample.sample.barcode}</td>
                            <td><g:message code="${ 'sample.source.' + (runSample.sample?.source ?: 'none') }" default="${runSample.sample?.source}" /></td>
                            <td>${runSample.sample.subject.summary.elapsedWorkingDays}</td>
                            <td>${runSample?.process?.panel?.technology}</td>
                            <td><g:join in="${ runSample.mutations.collectAll { b -> (link(action: 'show', controller: 'observedMutation', params: [mutation: b.knownMutation.toLabel()], b.knownMutation.toLabel()) + ' (' + String.format('%.2f', b.frequency) + ')') }.join(', ') }" delimiter=", " /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
        </div>
    </body>
</html>
