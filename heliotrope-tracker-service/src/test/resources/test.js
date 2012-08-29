// Drop and recreate all the collections. This restores the database to a known initial state. 
// This is because we don't really have a good mocking system for MongoDB, so we just
// use it as it stands. 
//
// Note that the data may well be modified after testing, because we will typically 
// make changes during the test process. 

db.study.drop();
db.subject.drop();
db.sample.drop();
db.protocol.drop();
db.step.drop();
db.user.drop();

db.createCollection("study");
db.createCollection("subject");
db.createCollection("sample");
db.createCollection("protocol");
db.createCollection("step");
db.createCollection("user");

var userObjectId = "admin";

// ================================================================================================
// U S E R

// This password string is the SHA1 digest for "admin" encoded using Base64 encoding. It is 
// a reasonable first estimate for what to store in the database for testing, and also for
// bootstrapping. 

db.user.insert({
  "identifier" : userObjectId,
  "passwordDigest" : "0DPiKuNIrrVmD8IUCuw1hQxNqZc="
});
db.user.ensureIndex({identifier:1},{unique:true});

// ================================================================================================
// S T U D Y

function addStudy(identifier, description) {
  return db.study.insert({
    "identifier" : identifier,
    "description" : description,
    "data" : {}
  });
};
function getStudyId(identifier) {
  return db.study.find({
    "identifier" : identifier
  }).next()._id;
};
function addSubject(studyIdentifier, subjectIdentifier) {
  return db.subject.insert({
    "identifier" : subjectIdentifier,
    "studyIdentifier" : studyIdentifier
  });
};
function getSubjectId(studyIdentifier, subjectIdentifier) {
  return db.subject.find({
    "identifier" : subjectIdentifier,
    "studyIdentifier" : studyIdentifier
  }).next()._id;
};
function getSampleId(studyIdentifier, sampleIdentifier) {
  return db.sample.find({
    "identifier" : sampleIdentifier,
    "studyIdentifier" : studyIdentifier
  }).next()._id;
};
function addSample(studyIdentifier, subjectIdentifier, identifier) {
  return db.sample.insert({
    "identifier" : identifier,
    "studyIdentifier" : studyIdentifier,
    "subjectIdentifier" :subjectIdentifier,
    "data" : {}
  });
};
function addStepProtocol(studyIdentifier, identifier, descriptor) {
  descriptor.identifier = identifier;
  descriptor.studyIdentifier = studyIdentifier;
  db.protocol.insert(descriptor);
}
function getStepProtocolId(studyIdentifier, identifier) {
  return db.protocol.find({
    "identifier" : identifier,
    "studyIdentifier" : studyIdentifier
  }).next()._id;
};
function getSampleSubjectIdentifier(studyIdentifier, identifier) {
  return db.sample.find({
    "identifier" : identifier,
    "studyIdentifier" : studyIdentifier
  }).next().subjectIdentifier;
};
function addSubjectStepData(studyIdentifier, subjectIdentifier, identifier, descriptor) {
  descriptor.studyIdentifier = studyIdentifier;
  descriptor.protocolIdentifier = identifier;
  descriptor.ownerType = "subject";
  descriptor.subjectIdentifier = subjectIdentifier;
  db.step.insert(descriptor);
}
function addSampleStepData(studyIdentifier, sampleIdentifier, identifier, descriptor) {
  descriptor.studyIdentifier = studyIdentifier;
  descriptor.protocolIdentifier = identifier;
  descriptor.ownerType = "sample";
  descriptor.sampleIdentifier = sampleIdentifier;
  descriptor.subjectIdentifier = getSampleSubjectIdentifier(studyIdentifier, sampleIdentifier)
  db.step.insert(descriptor);
}
function addSubjectEmptyStep(studyId, subjectId, identifier, completedDate) {
  addSubjectStepData(studyId, subjectId, identifier, {
    "data" : {
      "completed" : completedDate,
      "lastUpdated" : completedDate,
      "lastUpdatedBy" : userObjectId      
    }
  });
};
function addSampleEmptyStep(studyId, sampleId, identifier, completedDate) {
  addSampleStepData(studyId, sampleId, identifier, {
    "data" : {
      "completed" : completedDate,
      "lastUpdated" : completedDate,
      "lastUpdatedBy" : userObjectId
    }
  });
};
function addEnrolment(studyId, subjectId, completedDate, gender, primaryTissue, primaryPhysician, institution) {
  addSubjectStepData(studyId, subjectId, "enrolment", {
    "data" : {
      "completed" : completedDate,
      "lastUpdated" : completedDate,
      "lastUpdatedBy" : userObjectId,
      "gender" : gender,
      "primaryTissue" : primaryTissue,
      "primaryPhysician" : primaryPhysician,
      "institution" : institution
    }
  });
};
function addConsent(studyId, subjectId, completedDate, consentGiven) {
  addSubjectStepData(studyId, subjectId, "consent", {
    "data" : { 
      "completed" : completedDate,
      "lastUpdated" : completedDate,
      "lastUpdatedBy" : userObjectId,
      "consentGiven" : consentGiven
    }
  });
};
function addSampleRegistration(studyId, sampleId, completedDate, source, type, dnaConcentration, dnaQuality) {
  addSampleStepData(studyId, sampleId, "sampleRegistration", {
    "data" : {
      "completed" : completedDate,
      "lastUpdated" : completedDate,
      "lastUpdatedBy" : userObjectId,
      "source" : source,
      "type" : type,
      "dnaConcentration" : dnaConcentration,
      "dnaQuality" : dnaQuality
    }
  });
};

db.study.ensureIndex({identifier:1},{unique:true});
db.subject.ensureIndex({studyIdentifier:1,identifier:1},{unique:true});
db.sample.ensureIndex({studyIdentifier:1,identifier:1},{unique:true});
db.protocol.ensureIndex({studyIdentifier:1,identifier:1},{unique:true});
db.protocol.ensureIndex({owner:1,stepProtocol:1},{unique:false});

addStudy("GPS", "Genome Potato Sandwich");
addStudy("PNA", "Prefer Not to Answer");

//================================================================================================
//S T E P   P R O T O C O L

//First, a protocol for sample registration. This is intended to provide the fields needed
//for sample data.

addStepProtocol("GPS", "CreateSubject", {
  "ownerType" : "subject",
  "instantaneous" : true,
  "label" : {
   "default" : "Create Subject"
  },
  "values" : [ {
   "name" : "identifier",
   "controlType" : "text",
   "controlParams" : { "size" : 10, "maxlength" : 10 },
   "label" : {
     "default" : "Identifier"
   },
   "dataType" : "string",
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  } ]
});

addStepProtocol("GPS", "CreateSample", {
  "ownerType" : "sample",
  "instantaneous" : true,
  "label" : {
   "default" : "Create Sample"
  },
  "values" : [ {
   "name" : "identifier",
   "controlType" : "text",
   "controlParams" : { "size" : 20, "maxlength" : 20 },
   "label" : {
     "default" : "Identifier"
   },
   "dataType" : "string",
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  } ]
});

addStepProtocol("GPS", "pacbioArrival", {
  "ownerType" : "sample",
  "instantaneous" : true,
  "label" : {
   "default" : "Arrived in PacBio"
  },
  "values" : []
});

addStepProtocol("GPS", "sequenomArrival", {
  "ownerType" : "sample",
  "instantaneous" : true,
  "label" : {
   "default" : "Arrived in PacBio"
  },
  "values" : []
});

addStepProtocol("GPS", "sampleRegistration", {
  "ownerType" : "sample",
  "instantaneous" : true,
  "label" : {
   "default" : "Sample Registration"
  },
  "values" : [ {
   "name" : "source",
   "controlType" : "select",
   "label" : {
     "default" : "Sample Source"
   },
   "dataType" : "string",
   "range" : [ "study", "archival", "blood" ],
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  }, {
   "name" : "type",
   "controlType" : "select",
   "label" : {
     "default" : "Sample Type"
   },
   "range" : [ "FFPE", "frozen", "blood" ],
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  }, {
   "name" : "dnaConcentration",
   "controlType" : "text",
   "controlParams" : { "size" : 10, "maxlength" : 10 },
   "label" : {
     "default" : "DNA Concentration"
   },
   "dataType" : "float",
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  }, {
   "name" : "dnaQuality",
   "controlType" : "select",
   "label" : {
     "default" : "DNA Quality"
   },
   "dataType" : "string",
   "range" : [ "good", "poor" ],
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  } ]
});

addStepProtocol("GPS", "enrolment", {
  "ownerType" : "subject",
  "instantaneous" : false,
  "label" : {
    "default" : "Enrolment",
    "fr" : "Enrôlement"
  },
  "values" : [ {
    "name" : "gender",
    "controlType" : "select",
    "dataType" : "string",
    "range" : [ "F", "M" ],
    "label" : {
      "default" : "Gender",
      "fr" : "Genre"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "institution",
    "controlType" : "select",
    "dataType" : "string",
    "range" : [ "UHN/PMH", "Hamilton", "London", "Ottawa", "Thunder Bay" ],
    "label" : {
      "default" : "Institution",
      "fr" : "Institution"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "primaryTissue",
    "controlType" : "text",
    "controlParams" : { "size" : 20, "maxlength" : 20 },
    "dataType" : "string",
    "label" : {
      "default" : "Primary Tissue",
      "fr" : "Tissu primaire"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "primaryPhysician",
    "controlType" : "text",
    "controlParams" : { "size" : 20, "maxlength" : 20 },
    "dataType" : "string",
    "label" : {
      "default" : "Primary Physician",
      "fr" : "Médecin primaire"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  } ]
});

addStepProtocol("GPS", "consent", {
  "ownerType" : "subject",
  "instantaneous" : false,
  "label" : {
   "default" : "Consent",
  },
  "values" : [ {
   "name" : "consentGiven",
   "controlType" : "checkbox",
   "dataType" : "boolean",
   "label" : {
     "default" : "Consent Given",
   },
   "roles" : {
     "user" : "read_write",
     "other" : "read"
   },
   "audit" : "full"
  } ]
});

addSubject("GPS",  "TST-001");
addSubject("GPS",  "TST-002");
addSubject("GPS",  "TST-003");
addSubject("GPS",  "TST-004");
addSubject("GPS",  "TST-005");
addSubject("GPS",  "TST-006");
addSubject("GPS",  "TST-007");
addSubject("GPS",  "TST-008");
addSubject("GPS",  "TST-009");
addSubject("GPS",  "TST-010");
addSubject("GPS",  "TST-011");
addSubject("GPS",  "TST-012");
addSubject("GPS",  "TST-013");
addSubject("GPS",  "TST-014");
addSubject("GPS",  "TST-015");
addSubject("GPS",  "TST-016");
addSubject("GPS",  "TST-017");
addSubject("GPS",  "TST-018");
addSubject("GPS",  "TST-019");
addSubject("GPS",  "TST-020");

addSubject("PNA",  "PNA-001");
addSubject("PNA",  "TST-013");

addSample("GPS", "TST-001", "SAMPLE-TST-001-BIO-001");
addSample("GPS", "TST-001", "SAMPLE-TST-001-BIO-002");
addSample("GPS", "TST-002", "SAMPLE-TST-002-BIO-001");
addSample("GPS", "TST-002", "SAMPLE-TST-002-BIO-002");
addSample("GPS", "TST-003", "SAMPLE-TST-003-BIO-001");
addSample("GPS", "TST-003", "SAMPLE-TST-003-BIO-002");
addSample("GPS", "TST-004", "SAMPLE-TST-004-BIO-001");
addSample("GPS", "TST-004", "SAMPLE-TST-004-BIO-002");
addSample("GPS", "TST-005", "SAMPLE-TST-005-BIO-001");
addSample("GPS", "TST-005", "SAMPLE-TST-005-BIO-002");
addSample("GPS", "TST-006", "SAMPLE-TST-006-BIO-001");
addSample("GPS", "TST-006", "SAMPLE-TST-006-BIO-002");
addSample("GPS", "TST-007", "SAMPLE-TST-007-BIO-001");
addSample("GPS", "TST-007", "SAMPLE-TST-007-BIO-002");
addSample("GPS", "TST-008", "SAMPLE-TST-008-BIO-001");
addSample("GPS", "TST-008", "SAMPLE-TST-008-BIO-002");
addSample("GPS", "TST-009", "SAMPLE-TST-009-BIO-001");
addSample("GPS", "TST-009", "SAMPLE-TST-009-BIO-002");
addSample("GPS", "TST-010", "SAMPLE-TST-010-BIO-001");
addSample("GPS", "TST-010", "SAMPLE-TST-010-BIO-002");
addSample("GPS", "TST-011", "SAMPLE-TST-011-BIO-001");
addSample("GPS", "TST-011", "SAMPLE-TST-011-BIO-002");
addSample("GPS", "TST-012", "SAMPLE-TST-012-BIO-001");
addSample("GPS", "TST-012", "SAMPLE-TST-012-BIO-002");
addSample("GPS", "TST-013", "SAMPLE-TST-013-BIO-001");
addSample("GPS", "TST-013", "SAMPLE-TST-013-BIO-002");
addSample("GPS", "TST-014", "SAMPLE-TST-014-BIO-001");
addSample("GPS", "TST-014", "SAMPLE-TST-014-BIO-002");
addSample("GPS", "TST-015", "SAMPLE-TST-015-BIO-001");
addSample("GPS", "TST-015", "SAMPLE-TST-015-BIO-002");
addSample("GPS", "TST-016", "SAMPLE-TST-016-BIO-001");
addSample("GPS", "TST-016", "SAMPLE-TST-016-BIO-002");
addSample("GPS", "TST-017", "SAMPLE-TST-017-BIO-001");
addSample("GPS", "TST-017", "SAMPLE-TST-017-BIO-002");
addSample("GPS", "TST-018", "SAMPLE-TST-018-BIO-001");
addSample("GPS", "TST-018", "SAMPLE-TST-018-BIO-002");
addSample("GPS", "TST-019", "SAMPLE-TST-019-BIO-001");
addSample("GPS", "TST-019", "SAMPLE-TST-019-BIO-002");
addSample("GPS", "TST-020", "SAMPLE-TST-020-BIO-001");
addSample("GPS", "TST-020", "SAMPLE-TST-020-BIO-002");

addSample("PNA",  "PNA-001", "SAMPLE-PNA-001-BIO-001");
addSample("PNA",  "PNA-001", "SAMPLE-PNA-001-BIO-002");

addSample("PNA",  "TST-013", "SAMPLE-TST-013-BIO-001");
addSample("PNA",  "TST-013", "SAMPLE-TST-013-ARC-001");

addEnrolment("GPS", "TST-001", new Date(2011, 5, 5),   "F", "breast",     "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-002", new Date(2011, 5, 12),  "F", "breast",     "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-003", new Date(2011, 5, 17),  "M", "pancreas",   "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-004", new Date(2011, 5, 19),  "M", "colorectal", "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-005", new Date(2011, 5, 31),  "F", "ovarian",    "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-006", new Date(2011, 6, 15),  "F", "breast",     "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-007", new Date(2011, 6, 22),  "M", "lung",       "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-008", new Date(2011, 6, 28),  "F", "cervix",     "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-009", new Date(2011, 7,  4),  "M", "prostate",   "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-010", new Date(2011, 7,  6),  "F", "breast",     "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-011", new Date(2011, 8,  5),  "M", "breast",     "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-012", new Date(2011, 7, 15),  "F", "breast",     "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-013", new Date(2011, 7, 19),  "F", "pancreas",   "Waldroup", "London");
addEnrolment("GPS", "TST-014", new Date(2011, 7, 26),  "M", "colorectal", "Eilerman", "London");
addEnrolment("GPS", "TST-015", new Date(2011, 7, 27),  "F", "ovarian",    "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-016", new Date(2011, 8,  5),  "F", "breast",     "Haig", "Hamilton");
addEnrolment("GPS", "TST-017", new Date(2011, 8,  3),  "M", "lung",       "Borland", "PMH/UNH");
addEnrolment("GPS", "TST-018", new Date(2011, 8,  8),  "F", "cervix",     "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-019", new Date(2011, 8,  19), "F", "prostate",   "Santora", "PMH/UNH");
addEnrolment("GPS", "TST-020", new Date(2011, 8,  22), "F", "breast",     "Waldroup", "London");

addConsent("GPS", "TST-001", new Date(2011, 5,  5),  "Yes");
addConsent("GPS", "TST-002", new Date(2011, 5,  8),  "Yes");
addConsent("GPS", "TST-003", new Date(2011, 5, 17),  "Yes");
addConsent("GPS", "TST-004", new Date(2011, 5, 19),  "Yes");
addConsent("GPS", "TST-005", new Date(2011, 5, 31),  "Yes");
addConsent("GPS", "TST-006", new Date(2011, 6, 11),  "Yes");
addConsent("GPS", "TST-007", new Date(2011, 6, 22),  "Yes");
addConsent("GPS", "TST-008", new Date(2011, 6, 28),  "Yes");
addConsent("GPS", "TST-009", new Date(2011, 7,  4),  "Yes");
addConsent("GPS", "TST-010", new Date(2011, 7,  5),  "Yes");
addConsent("GPS", "TST-011", new Date(2011, 8,  5),  "Yes");
addConsent("GPS", "TST-012", new Date(2011, 7, 15),  "Yes");
addConsent("GPS", "TST-013", new Date(2011, 7, 11),  "Yes");
addConsent("GPS", "TST-014", new Date(2011, 7, 26),  "Yes");
addConsent("GPS", "TST-015", new Date(2011, 7, 27),  "Yes");
addConsent("GPS", "TST-016", new Date(2011, 8,  5),  "Yes");
addConsent("GPS", "TST-017", new Date(2011, 8,  2),  "Yes");
addConsent("GPS", "TST-018", new Date(2011, 8,  8),  "Yes");
addConsent("GPS", "TST-019", new Date(2011, 8,  19),  "Yes");
addConsent("GPS", "TST-020", new Date(2011, 8,  22),  "Yes");

addSubjectEmptyStep("GPS", "TST-001", "sequenomArrival", new Date(2011, 5,  6));
addSubjectEmptyStep("GPS", "TST-002", "sequenomArrival", new Date(2011, 5, 13));
addSubjectEmptyStep("GPS", "TST-003", "sequenomArrival", new Date(2011, 5, 18));
addSubjectEmptyStep("GPS", "TST-004", "sequenomArrival", new Date(2011, 5, 20));
addSubjectEmptyStep("GPS", "TST-005", "sequenomArrival", new Date(2011, 6,  2));
addSubjectEmptyStep("GPS", "TST-006", "sequenomArrival", new Date(2011, 6, 17));
addSubjectEmptyStep("GPS", "TST-007", "sequenomArrival", new Date(2011, 6, 24));
addSubjectEmptyStep("GPS", "TST-008", "sequenomArrival", new Date(2011, 6, 30));
addSubjectEmptyStep("GPS", "TST-009", "sequenomArrival", new Date(2011, 7,  7));
addSubjectEmptyStep("GPS", "TST-012", "sequenomArrival", new Date(2011, 7, 19));
addSubjectEmptyStep("GPS", "TST-013", "sequenomArrival", new Date(2011, 7, 21));
addSubjectEmptyStep("GPS", "TST-014", "sequenomArrival", new Date(2011, 7, 28));
addSubjectEmptyStep("GPS", "TST-015", "sequenomArrival", new Date(2011, 7, 29));
addSubjectEmptyStep("GPS", "TST-016", "sequenomArrival", new Date(2011, 8, 10));
addSubjectEmptyStep("GPS", "TST-017", "sequenomArrival", new Date(2011, 8,  5));
addSubjectEmptyStep("GPS", "TST-018", "sequenomArrival", new Date(2011, 8, 10));
addSubjectEmptyStep("GPS", "TST-019", "sequenomArrival", new Date(2011, 8, 23));
addSubjectEmptyStep("GPS", "TST-020", "sequenomArrival", new Date(2011, 8, 24));

addSubjectEmptyStep("GPS", "TST-001", "pacbioArrival", new Date(2011, 5, 11));
addSubjectEmptyStep("GPS", "TST-002", "pacbioArrival", new Date(2011, 5, 18));
addSubjectEmptyStep("GPS", "TST-003", "pacbioArrival", new Date(2011, 5, 23));
addSubjectEmptyStep("GPS", "TST-004", "pacbioArrival", new Date(2011, 5, 25));
addSubjectEmptyStep("GPS", "TST-005", "pacbioArrival", new Date(2011, 6,  6));
addSubjectEmptyStep("GPS", "TST-006", "pacbioArrival", new Date(2011, 6, 22));
addSubjectEmptyStep("GPS", "TST-007", "pacbioArrival", new Date(2011, 6, 28));
addSubjectEmptyStep("GPS", "TST-008", "pacbioArrival", new Date(2011, 7,  6));
addSubjectEmptyStep("GPS", "TST-012", "pacbioArrival", new Date(2011, 7, 28));
addSubjectEmptyStep("GPS", "TST-013", "pacbioArrival", new Date(2011, 7, 28));
addSubjectEmptyStep("GPS", "TST-014", "pacbioArrival", new Date(2011, 8,  2));
addSubjectEmptyStep("GPS", "TST-015", "pacbioArrival", new Date(2011, 8,  2));
addSubjectEmptyStep("GPS", "TST-016", "pacbioArrival", new Date(2011, 8, 15));
addSubjectEmptyStep("GPS", "TST-017", "pacbioArrival", new Date(2011, 8,  9));
addSubjectEmptyStep("GPS", "TST-018", "pacbioArrival", new Date(2011, 8, 14));
addSubjectEmptyStep("GPS", "TST-019", "pacbioArrival", new Date(2011, 8, 26));
addSubjectEmptyStep("GPS", "TST-020", "pacbioArrival", new Date(2011, 8, 30));

addSampleRegistration("GPS", "SAMPLE-TST-001-BIO-001", new Date(2011, 5,  5), "study", "FFPE", 9.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-001-BIO-002", new Date(2011, 5,  5), "study", "FFPE", 9.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-002-BIO-001", new Date(2011, 5, 12), "study", "FFPE", 6, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-002-BIO-002", new Date(2011, 5, 12), "study", "FFPE", 6, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-003-BIO-001", new Date(2011, 5, 17), "study", "FFPE", 7.1, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-003-BIO-002", new Date(2011, 5, 17), "study", "FFPE", 7.1, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-004-BIO-001", new Date(2011, 5, 19), "study", "FFPE", 16, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-004-BIO-002", new Date(2011, 5, 19), "study", "FFPE", 16, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-005-BIO-001", new Date(2011, 5, 31), "study", "FFPE", 9.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-005-BIO-002", new Date(2011, 5, 31), "study", "FFPE", 9.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-006-BIO-001", new Date(2011, 6, 15), "study", "FFPE", 20, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-006-BIO-002", new Date(2011, 6, 15), "study", "FFPE", 20, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-007-BIO-001", new Date(2011, 6, 22), "study", "FFPE", 8.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-007-BIO-002", new Date(2011, 6, 22), "study", "FFPE", 8.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-008-BIO-001", new Date(2011, 6, 28), "study", "FFPE", 12, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-008-BIO-002", new Date(2011, 6, 28), "study", "FFPE", 12, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-009-BIO-001", new Date(2011, 7,  4), "study", "FFPE", 13.5, "good");
addSampleRegistration("GPS", "SAMPLE-TST-009-BIO-002", new Date(2011, 7,  4), "study", "FFPE", 13.5, "good");
addSampleRegistration("GPS", "SAMPLE-TST-010-BIO-001", new Date(2011, 7,  6), "study", "FFPE", 34, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-010-BIO-002", new Date(2011, 7,  6), "study", "FFPE", 34, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-011-BIO-001", new Date(2011, 8, 24), "study", "FFPE", 25.7, "good");
addSampleRegistration("GPS", "SAMPLE-TST-011-BIO-002", new Date(2011, 8, 24), "study", "FFPE", 25.7, "good");
addSampleRegistration("GPS", "SAMPLE-TST-012-BIO-001", new Date(2011, 7, 15), "study", "FFPE", 21.5, "good");
addSampleRegistration("GPS", "SAMPLE-TST-012-BIO-002", new Date(2011, 7, 15), "study", "FFPE", 21.5, "good");
addSampleRegistration("GPS", "SAMPLE-TST-013-BIO-001", new Date(2011, 7, 20), "study", "FFPE", 82.2, "good");
addSampleRegistration("GPS", "SAMPLE-TST-013-BIO-002", new Date(2011, 7, 20), "study", "FFPE", 82.2, "good");
addSampleRegistration("GPS", "SAMPLE-TST-014-BIO-001", new Date(2011, 7, 26), "study", "FFPE", 22, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-014-BIO-002", new Date(2011, 7, 26), "study", "FFPE", 22, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-015-BIO-001", new Date(2011, 7, 27), "study", "FFPE", 13, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-015-BIO-002", new Date(2011, 7, 27), "study", "FFPE", 13, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-016-BIO-001", new Date(2011, 8,  8), "study", "FFPE", 28, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-016-BIO-002", new Date(2011, 8,  8), "study", "FFPE", 28, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-017-BIO-001", new Date(2011, 8,  3), "study", "FFPE", 12.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-017-BIO-002", new Date(2011, 8,  3), "study", "FFPE", 12.5, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-018-BIO-001", new Date(2011, 8, 19), "study", "FFPE", 32, "good");
addSampleRegistration("GPS", "SAMPLE-TST-018-BIO-002", new Date(2011, 8, 19), "study", "FFPE", 32, "good");
addSampleRegistration("GPS", "SAMPLE-TST-019-BIO-001", new Date(2011, 8, 22), "study", "FFPE", 32, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-019-BIO-002", new Date(2011, 8, 22), "study", "FFPE", 32, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-020-BIO-001", new Date(2011, 8, 25), "study", "FFPE", 59, "moderate");
addSampleRegistration("GPS", "SAMPLE-TST-020-BIO-002", new Date(2011, 8, 25), "study", "FFPE", 59, "moderate");
