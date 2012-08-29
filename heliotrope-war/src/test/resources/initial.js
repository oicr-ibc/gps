db.study.drop();
db.subject.drop();
db.protocol.drop();
db.user.drop();

db.createCollection("study");
db.createCollection("subject");
db.createCollection("protocol");
db.createCollection("user");

// ================================================================================================
// U S E R

db.user.insert({
 "userName": "test",
});
db.user.insert({
 "userName": "guest",
});

var userCursor = db.user.find({"userName": "test"});
var userObjectId = userCursor.next()._id;

// ================================================================================================
// S T U D Y

db.study.insert({
    "title": "GPS",
    "description": "The Genome Potato Sandwich"
});
db.study.insert({
    "title": "PNA",
    "description": "Prefer Not to Answer"
});

var studyCursor = db.study.find({"title": "GPS"});
var studyObjectId = studyCursor.next()._id;

var otherStudyCursor = db.study.find({"title": "PNA"});
var otherStudyObjectId = otherStudyCursor.next()._id;

// ================================================================================================
// P R O T O C O L

db.protocol.insert({
 "name": "enrolment",
 "label": {
     "default": "Enrolment",
     "fr": "Enrôlement"
 },
 "values": [
     {
         "name": "enrolmentDate",
         "type": "Date",
         "label": {
             "default": "Enrolment Date",
             "fr": "Date de Enrôlement"
         },
         "roles": {
             "user": "read_write",
             "other": "read"
         },
         "audit": "full"
     },
     {
         "name": "gender",
         "type": "String",
         "range": ["F", "M"],
         "label": {
             "default": "Gender",
             "fr": "Genre"
         },
         "roles": {
             "user": "read_write",
             "other": "read"
         },
         "audit": "full"
     },
     {
         "name": "primaryTissue",
         "type": "String",
         "label": {
             "default": "Primary Tissue",
             "fr": "Tissu primaire"
         },
         "roles": {
             "user": "read_write",
             "other": "read"
         },
         "audit": "full"
     }
 ]
});

var enrolmentProtocolCursor = db.protocol.find({"label.default": "Enrolment"});
var enrolmentProtocolObjectId = enrolmentProtocolCursor.next()._id;

// ================================================================================================
// S U B J E C T

db.subject.insert({
    "subjectId": "GEN-001",
    "study": [studyObjectId], 
    "protocolData": { "gender": "F", "enrolmentDate": new Date(2011, 5, 1), "primaryTissue": "colon" },
    "processData": [
    	{
    		"protocol": enrolmentProtocolObjectId,
    		"completed": new Date(),
    		"completedBy": userObjectId,
    		"data": {
    			"gender": "F",
    			"enrolmentDate": new Date(2011, 5, 1),
    			"primaryTissue": "colon"
    		}
    	}
    ]
});

db.subject.insert({
    "subjectId": "GEN-002",
    "study": [studyObjectId],
    "protocolData": { "gender": "F", "enrolmentDate": new Date(2011, 5, 4), "primaryTissue": "breast" },
    "processData": [
    	{
    		"protocol": enrolmentProtocolObjectId,
    		"completed": new Date(),
    		"completedBy": userObjectId,
    		"data": {
    			"gender": "F",
    			"enrolmentDate": new Date(2011, 5, 4),
    			"primaryTissue": "breast"
    		}
    	}
    ]
});

db.subject.insert({
    "subjectId": "GEN-003",
    "study": [studyObjectId],
    "protocolData": { "gender": "M", "enrolmentDate": new Date(2011, 5, 11), "primaryTissue": "prostate" },
    "processData": []
});

db.subject.insert({
    "subjectId": "PNA-001",
    "study": [otherStudyObjectId],
    "protocolData": { "gender": "F", "enrolmentDate": new Date(2011, 5, 15), "primaryTissue": "other" },
    "processData": [
    	{
    		"protocol": enrolmentProtocolObjectId,
    		"completed": new Date(),
    		"completedBy": userObjectId,
    		"data": {
    			"gender": "F",
    			"enrolmentDate": new Date(2011, 5, 15),
    			"primaryTissue": "other"
    		}
    	}
    ]
});
