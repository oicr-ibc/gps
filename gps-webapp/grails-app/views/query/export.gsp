<%@ page import="ca.on.oicr.gps.model.reporting.Query" %><%@ page import="org.apache.commons.lang.time.DateFormatUtils" %><?xml version="1.0" encoding="UTF-8"?>
<g:set var="rsmd" value="${resultSet.getMetaData()}"/>
<g:set var="numberOfColumns" value="${rsmd.getColumnCount()}"/>
<ss:Workbook xmlns:ss='urn:schemas-microsoft-com:office:spreadsheet'>
	<ss:Styles>
		<ss:Style ss:ID='date1'>
			<ss:NumberFormat ss:Format='Short Date'/>
		</ss:Style>
	</ss:Styles>
	
	<ss:Worksheet ss:Name='Data'>
		<ss:Table>
			<g:set var="dummy" value="${resultSet.beforeFirst()}"/>
    		<ss:Row>
				<g:each var="i" in="${(1..numberOfColumns)}">
    				<ss:Cell>
						<ss:Data ss:Type='String'>${rsmd.getColumnLabel(i)}</ss:Data>
    				</ss:Cell>
    			</g:each>
    		</ss:Row>
   			<g:while test="${resultSet.next()}">
   				<ss:Row>
   				<g:each var="i" in="${(1..numberOfColumns) }">
  					<g:set var="x" value="${resultSet.getObject(i)}"/>
  					<g:if test="${x instanceof Number}">
					<ss:Cell><ss:Data ss:Type='Number'>${x}</ss:Data></ss:Cell>
  					</g:if>
  					<g:elseif test="${x instanceof java.sql.Timestamp}">
					<ss:Cell ss:StyleID='date1'><ss:Data ss:Type='DateTime'>${DateFormatUtils.ISO_DATETIME_FORMAT.format(x)}</ss:Data></ss:Cell>
  					</g:elseif>
  					<g:else>
  					<ss:Cell><ss:Data ss:Type='String'>${x}</ss:Data></ss:Cell>
  					</g:else>
   				</g:each>
   				</ss:Row>
   			</g:while>
    	</ss:Table>
    </ss:Worksheet>
</ss:Workbook>