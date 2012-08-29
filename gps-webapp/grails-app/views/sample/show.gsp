<%@ page import="ca.on.oicr.gps.model.data.Sample" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'sample.label', default: 'Sample')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /> ${fieldValue(bean: sampleInstance, field: "barcode")}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.barcode.label" default="Barcode" /></td>
                            <td valign="top" class="value">${fieldValue(bean: sampleInstance, field: "barcode")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.patientId.label" default="Patient Id" /></td>
                            <td valign="top" class="value">${fieldValue(bean: sampleInstance.subject, field: "patientId")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.type.label" default="Type" /></td>
                            <td valign="top" class="value">${fieldValue(bean: sampleInstance, field: "type")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.source.label" default="Source" /></td>
                            <td valign="top" class="value">${message(code: 'sample.source.' + sampleInstance.source, default: '-')}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.site.label" default="Site" /></td>
                            <td valign="top" class="value">${message(code: 'sample.site.' + sampleInstance.site, default: '-')}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.dateCollected.label" default="Date Collected" /></td>
                            <td valign="top" class="value"><g:formatDate format='dd MMM, yyyy' date="${sampleInstance?.dateCollected}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.dnaConcentration.label" default="Dna Concentration(ng/&#956;L)" /></td>
                            <td valign="top" class="value">${fieldValue(bean: sampleInstance, field: "dnaConcentration")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.dnaQuality.label" default="Dna Quality" /></td>
                            <td valign="top" class="value">${fieldValue(bean: sampleInstance, field: "dnaQuality")}</td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.dateCreated.label" default="Date Created" /></td>
                            <td valign="top" class="value"><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${sampleInstance?.dateCreated}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.dateReceived.label" default="Date Received" /></td>
                            <td valign="top" class="value"><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${sampleInstance?.dateReceived}" /></td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="sample.lastUpdated.label" default="Last Updated" /></td>
                            <td valign="top" class="value"><g:formatDate format='dd MMM, yyyy @ hh:mm a' date="${sampleInstance?.lastUpdated}" /></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${sampleInstance?.id}" />
                    <sec:ifAnyGranted roles="ROLE_GPS-CONTRIBUTORS">
                    <span class="button">
                        <g:link class="edit dialog-trigger" action="edit" id="${fieldValue(bean: sampleInstance, field: 'id')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
                    </span>
                    </sec:ifAnyGranted>
                </g:form>
            </div>
        </div>
    </body>
</html>
