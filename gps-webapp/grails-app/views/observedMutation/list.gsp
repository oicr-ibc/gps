
<%@ page import="ca.on.oicr.gps.model.data.ObservedMutation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'observedMutation.label', default: 'Observed Mutation')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="mutation" title="${message(code: 'observedMutation.mutation.label', default: 'Mutation')}" />
                        
                            <g:sortableColumn property="primaryTumorSite" title="${message(code: 'observedMutation.primaryTumorSite.label', default: 'Primary Tumor Site')}" />
                        
                            <th><g:message code="observedMutation.subjectCount.label" default="# Subjects" /></th>
                        
                            <th><g:message code="observedMutation.runSample.label" default="OncoCarta?" /></th>
                        
                            <g:sortableColumn property="date" title="${message(code: 'observedMutation.processDate.label', default: 'Processed')}" />

                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${observedMutationInstanceList}" status="i" var="observedMutationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>
                            	<g:link action="show" params="${ [ mutation: observedMutationInstance.mutation ]}">
                            	${observedMutationInstance.mutation}
                            	</g:link>
                            </td>

                            <td>${observedMutationInstance.primaryTumorSite}</td>

                            <td>${observedMutationInstance.subjectCount}</td>

                            <td>${observedMutationInstance.technology ? message(code: 'observedMutation.isOncoCartaYes.label', default: 'Yes') : ''}</td>
                            
                            <td><g:formatDate format='dd MMM, yyyy' date="${observedMutationInstance.date}" /></td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${observedMutationInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
