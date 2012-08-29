
<%@ page import="ca.on.oicr.gps.model.laboratory.Panel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'panel.label', default: 'Panel')}" />
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
                            <td valign="top" class="name"><g:message code="panel.name.label" default="Name" /></td>
                            <td valign="top" class="value">${fieldValue(bean: panelInstance, field: "name")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="panel.technology.label" default="Technology" /></td>
                            <td valign="top" class="value">${fieldValue(bean: panelInstance, field: "technology")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="panel.versionString.label" default="Version" /></td>
                            <td valign="top" class="value">${fieldValue(bean: panelInstance, field: "versionString")}</td>
                        </tr>
                    </tbody>
                </table>
                <h1>Panel Targets</h1>
                <table>
                    <thead>
                       <tr>         
                          <th>Gene</th>
                          <th>Chromosome</th>
                          <th>Start</th>
                          <th>Stop</th>
                          <th>Ref. Allele</th>
                          <th>Var. Allele</th>
						           </tr>
                    </thead>
                	<tbody>
		                <g:each in="${panelInstance.targets.sort { a,b -> a.start.compareTo(b.start) }}" var="targetInstance">
		                	<tr>
		                		<td>${fieldValue(bean: targetInstance, field: "gene")}</td>
		                		<td>${fieldValue(bean: targetInstance, field: "chromosome")}</td>
		                		<td>${fieldValue(bean: targetInstance, field: "start")}</td>
		                		<td>${fieldValue(bean: targetInstance, field: "stop")}</td>
                        <td>${fieldValue(bean: targetInstance, field: "refAllele")}</td>
                        <td>${fieldValue(bean: targetInstance, field: "varAllele")}</td>
		                	</tr>
		                </g:each>                	
                	</tbody>
                </table>
            </div>
        </div>
    </body>
</html>
