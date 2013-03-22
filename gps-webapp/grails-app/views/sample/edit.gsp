
<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /> ${fieldValue(bean: sampleInstance, field: "barcode")}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${sampleInstance}">
            <div class="errors">
                <g:renderErrors bean="${sampleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="update"/></g:set>
            <g:form method="get" data-url="${url}">
                <g:hiddenField name="id" value="${sampleInstance?.id}" />
                <g:hiddenField name="version" value="${sampleInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="barcode"><g:message code="sample.barcode.label" default="Barcode" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'barcode', 'errors')}">
                                    <g:textField name="barcode" value="${sampleInstance?.barcode}" />
                                </td>
                            </tr>

							<%-- For now, at least, don't let anyone edit the patient id --%>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="sample.patientId.label" default="Patient Id" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: sampleInstance.subject, field: "patientId")}</td>
	                        </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="type"><g:message code="sample.requiresCollection.label" default="Requires Collection" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'requiresCollection', 'errors')}">
                                	<g:checkBox id="requiresCollection" name="requiresCollection" value="${sampleInstance.requiresCollection != null ? sampleInstance.requiresCollection : true}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="type"><g:message code="sample.type.label" default="Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'type', 'errors')}">
                                    <g:select name="type" from="${sampleInstance.constraints.type.inList}" value="${sampleInstance?.type}" valueMessagePrefix="sample.type"  />
                                </td>
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="type"><g:message code="sample.source.label" default="Source" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'source', 'errors')}">
                                    <g:select 
                                        name="source" 
                                        from="${sampleInstance.constraints.source.inList}" 
                                        value="${sampleInstance?.source}" 
                                        valueMessagePrefix="sample.source"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="type"><g:message code="sample.site.label" default="Site" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'site', 'errors')}">
                                    <g:select 
                                        name="site" 
                                        noSelection="${ ['': '-'] }"
                                        from="${sampleInstance.constraints.site.inList}" 
                                        value="${sampleInstance?.site}" 
                                        valueMessagePrefix="sample.site"
                                        />
                                </td>
                            </tr>
                        
                             <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCollected"><g:message code="sample.dateCollected.label" default="Date Collected" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'dateCollected', 'errors')}">
                                    <g:jqDatePicker name="dateCollected" value="${sampleInstance?.dateCollected}" />
                                </td>
                            </tr>
 
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dnaConcentration"><g:message code="sample.dnaConcentration.label" default="Dna Concentration(ng/&#956;L)" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'dnaConcentration', 'errors')}">
                                    <g:textField name="dnaConcentration" value="${fieldValue(bean: sampleInstance, field: 'dnaConcentration')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dnaQuality"><g:message code="sample.dnaQuality.label" default="Dna Quality" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'dnaQuality', 'errors')}">
                                    <g:textField name="dnaQuality" value="${sampleInstance?.dnaQuality}" />
                                </td>
                            </tr>
                        
                            <g:if test="${sampleInstance?.dateReceived}">
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateReceived"><g:message code="sample.dateReceived.label" default="Date Received" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'dateReceived', 'errors')}">
                                    <g:jqDatePicker name="dateReceived" value="${sampleInstance?.dateReceived}"/>
                                </td>
                            </tr>
                            </g:if>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <%-- <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>  --%>
                </div>
            </g:form>
        </div>
    </body>
</html>
