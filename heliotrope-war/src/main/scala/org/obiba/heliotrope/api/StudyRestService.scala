package org.obiba.heliotrope.api

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.Req
import net.liftweb.http.GetRequest
import net.liftweb.json.JsonAST.JString
import org.obiba.heliotrope.domain.Study
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonAST.JArray

/**
 * Implementation of the REST service layer, based on the study parts of the URL
 * space, which probably means more or less everything. Here follows the URL
 * definitions:
 * 
 * GET /api/study                -> list of study objects
 * GET /api/study/xxx            -> details of the given study
 * GET /api/subject?studyid=xxx  -> list of subjects in the given study
 * GET /api/sample?studyid=xxx   -> list of samples in the given study
 * GET /api/subject/xxx
 * 
 */

object StudyRestService extends RestHelper {
  
  serve ( "api" / "study" prefix {
     case Nil JsonGet _ => JArray(Study.findAll.map { _.asJValue })
  })
  
}