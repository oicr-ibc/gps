
<%@ page import="ca.on.oicr.gps.pipeline.model.PipelineError" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'panel.label', default: 'Panel')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${panelInstance}">
            <div class="errors">
                <g:renderErrors bean="${panelInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:set var='url'><g:createLink action="save"/></g:set>
            <g:uploadForm action="save" method="post" data-url="${url}">
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="panel.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: panelInstance, field: 'name', 'errors')}">
                                     <g:textField name="name" value="${panelInstance?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="technology"><g:message code="panel.technology.label" default="Technology" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: panelInstance, field: 'technology', 'errors')}">
                                     <g:textField name="technology" value="${panelInstance?.technology}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="versionString"><g:message code="panel.versionString.label" default="Version" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: panelInstance, field: 'versionString', 'errors')}">
                                     <g:textField name="versionString" value="${panelInstance?.versionString}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dataFile"><g:message code="panel.dataFile.label" default="Data File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: panelInstance, field: 'dataFile', 'errors')}">
                                    <input type="file" id="dataFile" name="dataFile" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                    <span class="button"><g:link class="delete" action="list">Cancel</g:link></span>
                </div>
            </g:uploadForm>
        </div>
    </body>
</html>
