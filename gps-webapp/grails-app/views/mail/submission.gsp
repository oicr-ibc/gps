<%@ page contentType="text/plain" %><g:set var='url'><g:createLink controller="attachment" action="download" id="${attachmentId}" absolute="true"/></g:set>
A mutation analysis data file has been submitted to GPS:

User name:         ${fieldValue(bean: submissionInstance, field: "userName")}
File name:         ${fieldValue(bean: submissionInstance, field: "fileName")}
Submission type:   ${fieldValue(bean: submissionInstance, field: "dataType")}
Received:          <g:formatDate format='MM/dd/yyyy @ hh:mm a' date="${submissionInstance?.dateSubmitted}" />

This data file contains ${submissionInstance.getPrimaryPanel()} mutation data for the following ${fieldValue(bean: submissionInstance, field: "patientCount")} patients:
<g:each in="${submissionInstance.getPatients()}" var="patient"> * ${patient} 
</g:each>

The file can be retrieved from: ${url}

GPS - https://gps.oicr.on.ca
Please do not respond to this email.
