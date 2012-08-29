
<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}" />
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
                    <g:textField name="barcode" value="${params.barcode}"></g:textField>
                    <span class="button"><input type="submit" class="search" value="Search Barcode" name="_action_list"></span>
                    <span style="float:right" class="button">
			            <sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
                    	<g:link class="dialog-trigger create" action="create">Register <g:message code="default.new.label" args="[entityName]" /></g:link>
                    	</sec:ifAnyGranted>
                    </span>
                </form>
            </div>
            <div class="list">
                <table>
                    <thead>
                        <tr>         
                            <g:sortableColumn property="barcode" title="${message(code: 'sample.barcode.label', default: 'Barcode')}" />
                            <g:sortableColumn property="patientId" title="${message(code: 'sample.patientId.label', default: 'Patient Id')}" />
                            <g:sortableColumn property="source" title="${message(code: 'sample.source.label', default: 'Source')}" />
                            <g:sortableColumn property="dateCreated" title="${message(code: 'sample.dateCreated.label', default: 'Registered')}" />
                            <g:sortableColumn property="dateReceived" title="${message(code: 'sample.dateReceived.label', default: 'Received')}" />
                			<th>Actions</th>
						</tr>
                    </thead>
                    <tbody>
                    <g:each in="${sampleInstanceList}" status="i" var="sampleInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${fieldValue(bean: sampleInstance, field: 'id')}">${fieldValue(bean: sampleInstance, field: "barcode")}</g:link></td>
                            <td>${fieldValue(bean: sampleInstance.subject, field: "patientId")}</td>
                            <td><g:message code="${ 'sample.source.' + (sampleInstance?.source ?: 'none') }" default="${sampleInstance?.source}" /></td>
                            <td><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${sampleInstance?.dateCreated}" /></td>
                            <td><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${sampleInstance?.dateReceived}" /></td>
                            <td>
                                <g:link class="dialog-trigger" action="show" id="${fieldValue(bean: sampleInstance, field: 'id')}">${message(code: 'default.button.show.label', default: 'View')}</g:link>
                                <sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
                                <g:link class="dialog-trigger" action="edit" id="${fieldValue(bean: sampleInstance, field: 'id')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
                            	</sec:ifAnyGranted>
                            	<sec:ifAnyGranted roles="ROLE_GPS-OICR">
                                <g:if test="${sampleInstance.requiresCollection && !sampleInstance.dateReceived}">
                           	    <g:link class="dialog-trigger receive-sample" action="receive" id="${fieldValue(bean: sampleInstance, field: 'id')}">${message(code: 'default.button.receive.label', default: 'Receive')}</g:link>
                           	    </g:if>
                           	    </sec:ifAnyGranted>
                           	</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${sampleInstanceTotal}" params="${ [barcode: params.barcode ] }"/>
            </div>
        </div>
        </div>
    </body>
</html>
