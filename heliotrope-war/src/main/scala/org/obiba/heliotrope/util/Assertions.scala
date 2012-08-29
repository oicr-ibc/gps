package org.obiba.heliotrope.util

trait Assertions {
  def assert(assertion: Boolean) { 
    if (!assertion) 
      throw new java.lang.AssertionError("assertion failed") 
  } 

 def assume(assumption: Boolean) { 
    if (!assumption) 
      throw new java.lang.AssertionError("assumption failed") 
  } 

  def require(requirement: Boolean) { 
    if (!requirement) 
      throw new IllegalArgumentException("requirement failed") 
  } 
}
