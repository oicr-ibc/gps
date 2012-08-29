package org.obiba.heliotrope.util

import java.net.URLEncoder

/**
 * We need to encode elements in URLs, but we can't directly use java.net.URLEncoder,
 * as it handles spaces for form submissions rather than for URLs. Fortunately, the
 * difference is simply in the way spaces are handled, so this wraps up 
 * java.net.URLEncoder into a method that can be called to encode URL fragments. 
 */

object URIUtils {
  
  /**
   * Takes a string, and returns a string suitable for URL composition, with all
   * escaping done. 
   */
  def encodeUriString(value: String): String = {
    URLEncoder.encode(value, "UTF-8").replace("+", "%20")
  }
}
