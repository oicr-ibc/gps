package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

trait IdentifiableComponent {
  val _id: Option[ObjectId]
  val identifier: String
}

