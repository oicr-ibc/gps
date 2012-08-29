<%@ page import="ca.on.oicr.gps.model.data.Submission" %>

<div class="body">
     <h1>Pipeline run</h1>
     <g:if test="${flash.message}">
     <div class="message">${flash.message}</div>
     </g:if>
     <div class="dialog">
         <table>
             <tbody>
                 <tr class="prop">
                     <td valign="top" class="name"><g:message code="submission.id.label" default="Id" /></td>
                     <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "id")}</td>
                 </tr>
                 <tr class="prop">
                     <td valign="top" class="name"><g:message code="submission.dataType.label" default="Data Type" /></td>
                     <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "dataType")}</td>
                 </tr>
                 <tr class="prop">
                     <td valign="top" class="name"><g:message code="submission.userName.label" default="User Name" /></td>
                     <td valign="top" class="value">${fieldValue(bean: submissionInstance, field: "userName")}</td>
                 </tr>
                 <tr class="prop">
                     <td valign="top" class="name"><g:message code="submission.dateSubmitted.label" default="Date Submitted" /></td>
                     <td valign="top" class="value"><g:formatDate format='yyyy/MM/dd' date="${submissionInstance?.dateSubmitted}" /></td>
                 </tr>
                 <tr class="prop">
                     <td valign="top" class="name"><g:message code="submission.fileName.label" default="Data File" /></td>
                     <td valign="top" class="value">${submissionInstance?.fileName}</td>
                 </tr>
             </tbody>
         </table>
         <g:if test="${pipelineErrors}">
	         <h1>Errors</h1>
	         <ul class="errors">
		         <g:each in="${pipelineErrors}">
		         	<li><g:message code="${it.key}" args="${it.args}"/></li>
		         </g:each>
	         </ul>
		</g:if>
		<g:if test="${! pipelineErrors}">
	         <h1>Success!</h1>
		</g:if>
     </div>
</div>
