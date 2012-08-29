//package org.obiba.heliotrope.tracker
//
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//
///**
// * A base class for serializable objects within Heliotrope. This provides most of the core
// * data functionality, although not quite everything to do with serialization is yet 
// * handled. 
// */
//
//abstract class SerializableMongoRecord[T <: SerializableMongoRecord[T]] extends MongoRecord[T] with ObjectIdPk[T] {
//  self: T =>
//}
