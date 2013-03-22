<%@ page contentType="text/plain" %>
The following samples still need to be received:

<g:each in="${sampleList}" status="i" var="sampleInstance">
${sampleInstance.barcode}	Registered: <g:formatDate format='MM/dd/yyyy @ hh:mm a' date="${sampleInstance?.dateCreated}" />
</g:each>

GPS - https://gps.oicr.on.ca
Please do not respond to this email.
