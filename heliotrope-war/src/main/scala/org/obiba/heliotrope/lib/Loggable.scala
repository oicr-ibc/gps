package org.obiba.heliotrope.lib

import org.slf4j.{LoggerFactory}
 
trait Loggable {
 
  /**
   * The Logger instance. Created when we need it.
   */
  private lazy val logger = LoggerFactory.getLogger(getClass)
 
  /**
   * Log a debug level message.
   *
   * @param msg Message to log, only evaluated if debug logging is
   * enabled.
   * @param t Throwable, optional.
   */
  protected def debug(msg: => AnyRef, t: => Throwable = null): Unit = {
    if (logger.isDebugEnabled) {
      if (t != null) {
        logger.debug(msg.toString, t);
      } else {
        logger.debug(msg.toString)
      }
    }
  }
 
  /**
   * Log a info level message.
   *
   * @param msg Message to log, only evaluated if info logging is
   * enabled.
   * @param t Throwable, optional.
   */
  protected def info(msg: => AnyRef, t: => Throwable = null): Unit = {
    if (logger.isInfoEnabled) {
      if (t != null) {
        logger.info(msg.toString, t);
      } else {
        logger.info(msg.toString)
      }
    }
  }
 
  /**
   * Log a warn level message.
   *
   * @param msg Message to log, only evaluated if warn logging is
   * enabled.
   * @param t Throwable, optional.
   */
  protected def warn(msg: => AnyRef, t: => Throwable = null): Unit = {
    if (logger.isWarnEnabled) {
      if (t != null) {
        logger.warn(msg.toString, t);
      } else {
        logger.warn(msg.toString)
      }
    }
  }
 
  /**
   * Log a error level message.
   *
   * @param msg Message to log, only evaluated if error logging is
   * enabled.
   * @param t Throwable, optional.
   */
  protected def error(msg: => AnyRef, t: => Throwable = null): Unit = {
    if (logger.isErrorEnabled) {
      if (t != null) {
        logger.error(msg.toString, t);
      } else {
        logger.error(msg.toString)
      }
    }
  }
 
}