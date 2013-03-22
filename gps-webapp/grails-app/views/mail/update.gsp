<%@ page contentType="text/plain" %><g:set var='url'><g:createLink controller="summary" action="index" absolute="true"/></g:set>
Since <g:if test="${modifiedSince}"><g:formatDate format='MM/dd/yyyy @ hh:mm a' date="${modifiedSince}" /></g:if><g:else>GPS was started</g:else>, the following changes have been recorded:

<g:if test="${changes.insert}">New patients: ${changes.insert}

</g:if><g:if test="${changes.update}">Updated patients: ${changes.update}

</g:if>For more information, see: ${url}

GPS - <g:createLink controller="summary" action="index" absolute="true"/>
Please do not respond to this email.
