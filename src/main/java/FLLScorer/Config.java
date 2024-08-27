// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.util.Locale;

/**
 * Handles storing the configuration values for the application.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Config
{
  /**
   * The database key used to store the current event.
   */
  private static final String m_eventKey = new String("event");

  /**
   * The database key used to store the HTTP debug configuration.
   */
  private static final String m_httpDebugKey = new String("httpDebug");

  /**
   * The database key used to store the current locale.
   */
  private static final String m_localeKey = new String("locale");

  /**
   * The database key used to store the current season.
   */
  private static final String m_seasonKey = new String("season");

  /**
   * The database key used to store the security bypass configuration.
   */
  private static final String m_securityBypassKey =
    new String("securityBypass");

  /**
   * The database key used to store the WiFi SSID.
   */
  private static final String m_wifiSSID = "wifiSSID";

  /**
   * The database key used to store the WiFi password.
   */
  private static final String m_wifiPassword = "wifiPassword";

  /**
   * The object for the Config singleton.
   */
  private static Config m_instance = null;

  /**
   * The object for accessing the database.
   */
  private Database m_database = null;

  /**
   * The current event.
   */
  private String m_event = null;

  /**
   * The current locale.
   */
  private String m_locale = null;

  /**
   * The current season.
   */
  private String m_season = null;

  /**
   * Gets the Config singleton object, creating it if necessary.
   *
   * @return Returns the Config singleton.
   */
  public static Config
  getInstance()
  {
    // Create the Config object if required.
    if(m_instance == null)
    {
        m_instance = new Config();
    }

    // Return the Config object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Config()
  {
  }

  /**
   * Gets the currently selected event.
   *
   * @return The ID of the currently selected event.
   */
  public String
  eventGet()
  {
    // Return the ID of the currently selected event.
    return(m_event);
  }

  /**
   * Sets the selected event.
   *
   * @param event The ID of the event; the selection will be removed if this is
   *              <b>null</b>.
   */
  public void
  eventSet(String event)
  {
    // See if the specified event is null.
    if(event == null)
    {
      // Remove the event selection from the database.
      m_database.configValueRemove(m_eventKey);
    }

    // Otherwise, see if the event is different.
    else if(!event.equals(m_event))
    {
      // Update the event selection in the database.
      m_database.configValueSet(m_eventKey, event);
    }

    // Update the selected event.
    m_event = event;
  }

  /**
   * Gets the HTTP debug configuration value.
   *
   * @return <b>true</b> if HTTP accesses should be logged to the terminal for
   *         debugging purposes.
   */
  public boolean
  httpDebugGet()
  {
    // Get the value of the configuration value from the database.
    String debug = m_database.configValueGet(m_httpDebugKey);

    // If the configuration value does not exist in the database, or has a
    // value of zero, then HTTP debug logging should not occur.
    if((debug == null) || (Integer.parseInt(debug) == 0))
    {
      return(false);
    }

    // HTTP debug logging is enabled.
    return(true);
  }

  /**
   * Sets the HTTP debug configuration value.
   *
   * @param enable A boolean that is <b>true</b> if HTTP accesses should be
   *               logged to the terminal for debugging purposes.
   */
  public void
  httpDebugSet(boolean enable)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_httpDebugKey, enable ? "1" : "0");
  }

  /**
   * Gets the currently selected locale.
   *
   * @return The IETF language code for the current locale.
   */
  public String
  localeGet()
  {
    // Return the language code for the current locale.
    return(m_locale);
  }

  /**
   * Sets the locale.
   *
   * @param locale The IETF langugage code for the locale; will be set to the
   *               system setting if this is <b>null</b>.
   */
  public void
  localeSet(String locale)
  {
    // See if the specified event is null.
    if(locale == null)
    {
      // Remove the locale selection from the database.
      m_database.configValueRemove(m_localeKey);

      // Get the system locale.
      m_locale = Locale.getDefault().toString();
    }

    // Otherwise, see if the event is different.
    else if(!locale.equals(m_locale))
    {
      // Update the event selection in the database.
      m_database.configValueSet(m_localeKey, locale);

      // Update the selected locale.
      m_locale = locale;
    }
  }

  /**
   * Gets the currently selected season.
   *
   * @return The ID of the currently selected season.
   */
  public String
  seasonGet()
  {
    // Return the ID of the currently selected season.
    return(m_season);
  }

  /**
   * Sets the selected season.
   *
   * @param season The ID of the season; the selection will be removed if this
   *               is <b>null</b>.
   */
  public void
  seasonSet(String season)
  {
    // See if the specified season is null.
    if(season == null)
    {
      // Remove the season selection from the database.
      m_database.configValueRemove(m_seasonKey);
    }

    // Otherwise, see if the event is different.
    else if(!season.equals(m_season))
    {
      // Update the season selection in the database.
      m_database.configValueSet(m_seasonKey, season);
    }

    // Update the selected season.
    m_season = season;
  }

  /**
   * Gets the security bypass configuration value.
   *
   * @return <b>true</b> if security should be bypassed to make it easier to
   *         develop/modify/enhance the application.
   */
  public boolean
  securityBypassGet()
  {
    // Get the value of the configuration value from the database.
    String debug = m_database.configValueGet(m_securityBypassKey);

    // If the configuration value does not exist in the database, or has a
    // value of zero, then security should not be bypassed.
    if((debug == null) || (Integer.parseInt(debug) == 0))
    {
      return(false);
    }

    // Security should be bypassed.
    return(true);
  }
  /**
   * Sets the security bypass configuration value.
   *
   * @param enable A boolean that is <b>true</b> if security should be bypassed
   *               to make it easier to develop/modify/enhance the application.
   */
  public void
  securityBypassSet(boolean enable)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_securityBypassKey, enable ? "1" : "0");
  }

  /**
   * Gets the WiFi SSID value.
   *
  * @return The SSID of the WiFi network.
   */
  public String
  wifiSSIDGet()
  {
    // Return the SSID of the WiFi network.
    return(m_database.configValueGet(m_wifiSSID));
  }

  /**
   * Sets the WiFi SSID value.
   *
   * @param ssid The SSID of the WiFi network.
   */
  public void
  wifiSSIDSet(String ssid)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_wifiSSID, ssid);
  }

  /**
   * Gets the WiFi password value.
   *
  * @return The password of the WiFi network.
   */
  public String
  wifiPasswordGet()
  {
    // Return the password of the WiFi network.
    return(m_database.configValueGet(m_wifiPassword));
  }

  /**
   * Sets the WiFi password value.
   *
   * @param ssid The password of the WiFi network.
   */
  public void
  wifiPasswordSet(String password)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_wifiPassword, password);
  }

  /**
   * Performs initial setup for the configuration settings.
   */
  public void
  setup()
  {
    // Get a pointer to the database for storing the configuration settings
    // persistently.
    m_database = Database.getInstance();

    // Get the stored value, if any, of the event.
    m_event = m_database.configValueGet(m_eventKey);

    // Get the stored value of the locale.  If one does not exist, use the
    // default system locale.
    m_locale = m_database.configValueGet(m_localeKey);
    if(m_locale == null)
    {
      m_locale = Locale.getDefault().toString();
    }

    // Get the stored value, if any, of the season.
    m_season = m_database.configValueGet(m_seasonKey);
  }
}