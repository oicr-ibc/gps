
<%@ page import="ca.on.oicr.gps.model.reporting.Query" %>
<g:set var="numberOfColumns" value="${resultSet.getColumnCount()}"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="list" action="edit" id="${queryInstance.id}"><g:message code="default.edit.label" args="[entityName]" /></g:link></span>
        </div>
    	<h1><g:message code="default.query.results.label" default="Query results" /></h1>
    	<div class="additionalBlock">
    	<g:link action="export" id="${queryInstance.id}">
    	<g:message code="default.export.excel" default="Export data to Excel"/>
    	</g:link>
    	</div>
    	<table style="width: auto">
    		<thead>
    			<g:each var="i" in="${(1..numberOfColumns) }">
    				<th>
    					${resultSet.getColumnLabel(i)}
    				</th>
    			</g:each>
    		</thead>
    		<tbody>
    			<g:set var="dummy" value="${resultSet.beforeFirst()}"/>
    			<g:while test="${resultSet.next()}">
    				<tr>
    				<g:each var="i" in="${(1..numberOfColumns) }">
    					<th>
    						${resultSet.getObject(i)}
    					</th>
    				</g:each>
    				</tr>
    			</g:while>
    		</tbody>
    	</table>
    </body>
</html>