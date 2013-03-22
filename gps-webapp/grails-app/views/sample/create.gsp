

<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${sampleInstance}">
            <div class="errors">
                <g:renderErrors bean="${sampleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="save"/></g:set>
            <g:form action="save" method="get" data-url="${url}">
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
                                                    
                            <tr class="prop">
                            	<td valign="top" class="name">
                                    <label for="patientIdPicker"><g:message code="sample.patientId.label" default="Patient Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance.subject, field: 'patientId', 'errors')}">
                                    <input id="patientIdPicker" name="patientId">
                                </td>
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
                                <td valign="top" class="value ${hasErrors(bean: sampleInstance, field: 'souce', 'errors')}">
                                    <g:select name="source" from="${sampleInstance.constraints.source.inList}" value="${sampleInstance?.source}" valueMessagePrefix="sample.source"  />
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
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
