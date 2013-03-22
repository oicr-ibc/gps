
<%@ page import="ca.on.oicr.gps.system.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div id="page">
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <%--
            <div class="buttons">
                <form method="post" action="list">
                    <g:textField name="barcode"></g:textField>
                    <span class="button"><input type="submit" class="search" value="Search Username" name="_action_list"></span>
                </form>
            </div>
            --%>
            <div class="list">
                <table>
                    <thead>
                        <tr>         
                            <g:sortableColumn property="userName" title="${message(code: 'user.userName.label', default: 'Username')}" />
                            <g:sortableColumn property="name" title="${message(code: 'user.name.label', default: 'Name')}" />
                            <g:sortableColumn property="email" title="${message(code: 'user.email.label', default: 'Email')}" />
                            <th>${message(code: 'user.role.label', default: 'Roles')}</th>
                			<th>Actions</th>
						</tr>
                    </thead>
                    <tbody>
                    <g:each in="${userList}" status="i" var="userInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link class="dialog-trigger" action="edit" id="${fieldValue(bean: userInstance, field: 'userName')}">${fieldValue(bean: userInstance, field: "userName")}</g:link></td>
                            <td>${fieldValue(bean: userInstance, field: "name")}</td>
                            <td><g:link url="${userInstance.emailURL}">${fieldValue(bean: userInstance, field: "email")}</g:link></td>
                            <td>${userInstance.authorities.join(", ")}</td>
                            <td>
                                <g:link class="dialog-trigger" action="edit" id="${fieldValue(bean: userInstance, field: 'userName')}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${userInstanceTotal}" />
            </div>
        </div>
        </div>
    </body>
</html>
