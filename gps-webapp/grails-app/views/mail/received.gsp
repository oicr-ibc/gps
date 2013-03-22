<%@ page contentType="text/plain" %>
The following sample has been marked as received by: ${userName}:

Id:                ${fieldValue(bean: sampleInstance, field: "id")}
Barcode:           ${fieldValue(bean: sampleInstance, field: "barcode")}
Type:              ${fieldValue(bean: sampleInstance, field: "type")}
Dna Concentration: ${fieldValue(bean: sampleInstance, field: "dnaConcentration")} ng/uL
Dna Quality:       ${fieldValue(bean: sampleInstance, field: "dnaQuality")}
Patient Id:        ${fieldValue(bean: sampleInstance.subject, field: "patientId")}
Gender:            ${fieldValue(bean: sampleInstance.subject, field: "gender")}
Registered:        <g:formatDate format='MM/dd/yyyy @ hh:mm a' date="${sampleInstance?.dateCreated}" />
Received:          <g:formatDate format='MM/dd/yyyy @ hh:mm a' date="${sampleInstance?.dateReceived}" />

GPS - https://gps.oicr.on.ca
Please do not respond to this email.
