package org.collectd.java;

import org.collectd.api.Collectd;

public class GenericJMXLogger extends Collectd {

  public static void logError(String message)
  {
    Collectd.logError(message);
  }

  public static void logWarning(String message)
  {
    Collectd.logWarning(message);
  }

  public static void logNotice(String message)
  {
    Collectd.logNotice(message);
  }

  public static void logInfo(String message)
  {
    Collectd.logInfo(message);
  }

  public static void logDebug(String message)
  {
    Collectd.logDebug(message);
  }
}
