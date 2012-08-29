package org.obiba.heliotrope.spray
import cc.spray.directives.PathEnd
import cc.spray.Directives

trait MoreDirectives extends Directives {
  
  /**
   * Matches the end of the path (e.g.: equivalent to {@code path("")})
   */
  lazy val pathEnd = path(PathEnd)

}