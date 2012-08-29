// Drop and recreate all the collections. This restores the database to a known initial state. 
// This is because we don't really have a good mocking system for MongoDB, so we just
// use it as it stands. 
//
// Note that the data may well be modified after testing, because we will typically 
// make changes during the test process. 

db.study.drop();
db.subject.drop();
db.sample.drop();
db.step_protocol.drop();
db.step_process.drop();
db.user.drop();

db.createCollection("study");
db.createCollection("subject");
db.createCollection("sample");
db.createCollection("step_protocol");
db.createCollection("step_process");
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

db.study.insert({
  "identifier" : "GPS",
  "description" : "Genome Potato Sandwich"
});
db.study.insert({
  "identifier" : "PNA",
  "description" : "Prefer Not to Answer"
});
db.study.ensureIndex({identifier:1},{unique:true});

var studyObjectId = db.study.find({
  "identifier" : "GPS"
}).next()._id;

var otherStudyObjectId = db.study.find({
  "identifier" : "PNA"
}).next()._id;

// ================================================================================================
// S U B J E C T

db.subject.insert({
  "identifier" : "GEN-001",
  "studyId" : studyObjectId
});

db.subject.insert({
  "identifier" : "GEN-002",
  "studyId" : studyObjectId
});

db.subject.insert({
  "identifier" : "GEN-003",
  "studyId" : studyObjectId
});

db.subject.insert({
  "identifier" : "PNA-001",
  "studyId" : otherStudyObjectId
});

db.subject.insert({
  "identifier" : "AMB-001",
  "studyId" : studyObjectId,
  "data" : {
    "studyName" : "GPS"
  }
});

db.subject.insert({
  "identifier" : "AMB-001",
  "studyId" : otherStudyObjectId,
  "data" : {
    "studyName" : "PNA"
  }
});
db.subject.ensureIndex({study:1,identifier:1},{unique:true});

// ================================================================================================
// S A M P L E

db.sample.insert({
  "identifier" : "SAMPLE-GEN-001-BIO-001",
  "studyId" : studyObjectId,
  "subjectId" : db.subject.find({
    "identifier" : "GEN-001"
  }).next()._id,
  "data" : {
    "source" : "study",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 12),
    "dnaConcentration" : 123.0,
    "dnaQuality" : "good"
  }
});

db.sample.insert({
  "identifier" : "SAMPLE-GEN-001-BLD-001",
  "studyId" : studyObjectId,
  "subjectId" : db.subject.find({
    "identifier" : "GEN-001"
  }).next()._id,
  "data" : {
    "source" : "blood",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 12),
    "dnaConcentration" : 175.0,
    "dnaQuality" : "good"
  }
});

db.sample.insert({
  "identifier" : "SAMPLE-GEN-001-ARC-001",
  "studyId" : studyObjectId,
  "subjectId" : db.subject.find({
    "identifier" : "GEN-001"
  }).next()._id,
  "data" : {
    "source" : "archival",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 13),
    "dnaConcentration" : 64.0,
    "dnaQuality" : "moderate"
  }
});

db.sample.insert({
  "identifier" : "SAMPLE-GEN-001-BIO-002",
  "studyId" : studyObjectId,
  "subjectId" : db.subject.find({
    "identifier" : "GEN-001"
  }).next()._id,
  "data" : {
    "source" : "study",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 14),
    "dnaConcentration" : 508.0,
    "dnaQuality" : "poor"
  }
});

db.sample.insert({
  "identifier" : "SAMPLE-GEN-002-BIO-001",
  "studyId" : studyObjectId,
  "subjectId" : db.subject.find({
    "identifier" : "GEN-002"
  }).next()._id,
  "data" : {
    "source" : "study",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 15),
    "dnaConcentration" : 143.0,
    "dnaQuality" : "poor"
  }
});
db.sample.ensureIndex({study:1,identifier:1},{unique:true});

// ================================================================================================
// S T E P   P R O T O C O L

// First, a protocol for sample registration. This is intended to provide the fields needed
// for sample data.

db.step_protocol.insert({
  "identifier" : "CreateSubject",
  "studyId" : studyObjectId,
  "ownerType" : "subject",
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
    "type" : "String",
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  } ]
});

db.step_protocol.insert({
  "identifier" : "CreateSample",
  "studyId" : studyObjectId,
  "ownerType" : "sample",
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
    "type" : "String",
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  } ]
});

db.step_protocol.insert({
  "identifier" : "sampleRegistration",
  "studyId" : studyObjectId,
  "ownerType" : "sample",
  "label" : {
    "default" : "Sample Registration"
  },
  "values" : [ {
    "name" : "source",
    "controlType" : "select",
    "label" : {
      "default" : "Sample Source"
    },
    "type" : "String",
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
    "type" : "String",
    "range" : [ "FFPE", "frozen", "blood" ],
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "collected",
    "controlType" : "date",
    "label" : {
      "default" : "Date Collected"
    },
    "type" : "Date",
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
    "type" : "Float",
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
    "type" : "String",
    "range" : [ "good", "poor" ],
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  } ]
});

db.step_protocol.insert({
  "identifier" : "enrolment",
  "studyId" : studyObjectId,
  "ownerType" : "subject",
  "label" : {
    "default" : "Enrolment",
    "fr" : "Enrôlement"
  },
  "values" : [ {
    "name" : "enrolmentDate",
    "controlType" : "date",
    "type" : "Date",
    "label" : {
      "default" : "Enrolment Date",
      "fr" : "Date de Enrôlement"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "gender",
    "controlType" : "select",
    "type" : "String",
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
    "name" : "primaryTissue",
    "controlParams" : { "size" : 20, "maxlength" : 20 },
    "type" : "String",
    "label" : {
      "default" : "Primary Tissue",
      "fr" : "Tissu primaire"
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  } ]
});

db.step_protocol.insert({
  "identifier" : "consent",
  "studyId" : studyObjectId,
  "ownerType" : "subject",
  "label" : {
    "default" : "Consent",
  },
  "values" : [ {
    "name" : "consentDate",
    "controlType" : "date",
    "type" : "Date",
    "label" : {
      "default" : "Consent Date",
    },
    "roles" : {
      "user" : "read_write",
      "other" : "read"
    },
    "audit" : "full"
  }, {
    "name" : "consentGiven",
    "controlType" : "checkbox",
    "type" : "Boolean",
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
db.step_protocol.ensureIndex({identifier:1},{unique:false});

var createSubjectStepProtocolObjectId = db.step_protocol.find({
  "identifier" : "CreateSubject",
  "studyId" : studyObjectId
}).next()._id;

var createSampleStepProtocolObjectId = db.step_protocol.find({
  "identifier" : "CreateSample",
  "studyId" : studyObjectId
}).next()._id;

var consentStepProtocolObjectId = db.step_protocol.find({
  "identifier" : "consent",
  "studyId" : studyObjectId
}).next()._id;

var enrolmentStepProtocolObjectId = db.step_protocol.find({
  "identifier" : "enrolment",
  "studyId" : studyObjectId
}).next()._id;

var sampleRegistrationStepProtocolObjectId = db.step_protocol.find({
  "identifier" : "sampleRegistration",
  "studyId" : studyObjectId
}).next()._id;

// ================================================================================================
// S T U D Y   P R O T O C O L

// Add the enrolment step to the GPS study protocol
// Note that this is an update, rather than create a complete study protocol
// object, at
// least for now we can embed it in the study.

db.study.update({
  "identifier" : "GPS"
}, {
  "$push" : {
    "steps" : createSubjectStepProtocolObjectId
  }
});

db.study.update({
  "identifier" : "GPS"
}, {
  "$push" : {
    "steps" : createSampleStepProtocolObjectId
  }
});

db.study.update({
  "identifier" : "GPS"
}, {
  "$push" : {
    "steps" : enrolmentStepProtocolObjectId
  }
});

db.study.update({
  "identifier" : "GPS"
}, {
  "$push" : {
    "steps" : consentStepProtocolObjectId
  }
});

db.study.update({
  "identifier" : "GPS"
}, {
  "$push" : {
    "steps" : sampleRegistrationStepProtocolObjectId
  }
});

// ================================================================================================
// S T E P P R O C E S S

db.step_process.insert({
  "stepProtocol" : enrolmentStepProtocolObjectId,
  "step" : "enrolment",
  "ownerType" : "subject",
  "owner" : db.subject.find({
    "identifier" : "GEN-001"
  }).next()._id,
  "completed" : new Date(),
  "completedBy" : userObjectId,
  "data" : {
    "gender" : "F",
    "enrolmentDate" : new Date(2011, 5, 1),
    "primaryTissue" : "colon"
  }
});

db.step_process.insert({
  "stepProtocol" : enrolmentStepProtocolObjectId,
  "step" : "enrolment",
  "ownerType" : "subject",
  "owner" : db.subject.find({
    "identifier" : "GEN-002"
  }).next()._id,
  "completed" : new Date(),
  "completedBy" : userObjectId,
  "data" : {
    "gender" : "F",
    "enrolmentDate" : new Date(2011, 5, 4),
    "primaryTissue" : "breast"
  }
});

db.step_process.insert({
  "stepProtocol" : enrolmentStepProtocolObjectId,
  "step" : "enrolment",
  "ownerType" : "subject",
  "owner" : db.subject.find({
    "identifier" : "PNA-001"
  }).next()._id,
  "completed" : new Date(),
  "completedBy" : userObjectId,
  "data" : {
    "gender" : "F",
    "enrolmentDate" : new Date(2011, 5, 15),
    "primaryTissue" : "other"
  }
});

db.step_process.insert({
  "stepProtocol" : sampleRegistrationStepProtocolObjectId,
  "step": "sampleRegistration",
  "ownerType" : "sample",
  "owner" : db.sample.find({
    "identifier" : "SAMPLE-GEN-001-BIO-001"
  }).next()._id,
  "completed" : new Date(),
  "completedBy" : userObjectId,
  "data" : {
    "source" : "study",
    "type" : "FFPE",
    "collected" : new Date(2011, 5, 12),
    "dnaConcentration" : 125.0,
    "dnaQuality" : "good"
  }
});
db.step_protocol.ensureIndex({owner:1,stepProtocol:1},{unique:false});

