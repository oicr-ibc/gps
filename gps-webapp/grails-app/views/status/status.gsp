<%@ page import="ca.on.oicr.gps.model.reporting.Query" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'query.label', default: 'Query')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
      <h1><g:message code="default.query.results.label" default="Cumulative status" /></h1>
      <div class="additionalBlock">
      </div>
      <g:set var="dummy" value="${patientCountResultSet.accumulateByColumn(2)}"/>
      <g:set var="dummy" value="${patientCountResultSet.accumulateByColumn(3)}"/>
      <g:set var="dummy" value="${patientCountResultSet.accumulateByColumn(4)}"/>
      <g:set var="patientNumberOfColumns" value="${patientCountResultSet.getColumnCount()}"/>
      <table style="width: auto">
        <thead>
          <g:each var="i" in="${(1..patientNumberOfColumns) }">
            <th>
              ${patientCountResultSet.getColumnLabel(i)}
            </th>
          </g:each>
        </thead>
        <tbody>
          <g:set var="dummy" value="${patientCountResultSet.beforeFirst()}"/>
          <g:while test="${patientCountResultSet.next()}">
            <tr>
            <g:each var="i" in="${(1..patientNumberOfColumns) }">
              <th>
                ${patientCountResultSet.getObject(i)}
              </th>
            </g:each>
            </tr>
          </g:while>
        </tbody>
      </table>
    </body>
</html>