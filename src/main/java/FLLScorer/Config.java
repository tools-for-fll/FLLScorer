// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles storing the configuration values for the application.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Config
{
  /**
   * The database key used to store the accent color.
   */
  private static final String m_accentKey = new String("accent-color");

  /**
   * The database key used to store the division enable state.
   */
  private static final String m_divisionEnableKey =
    new String("division-enable");

  /**
   * The database key prefix used to store the division colors.
   */
  private static final String m_divisionColorKeyPrefix =
    new String("division-color");

  /**
   * The database key used to store the division count.
   */
  private static final String m_divisionCountKey =
    new String("division-count");

  /**
   * The database key prefix used to store the division names.
   */
  private static final String m_divisionNameKeyPrefix =
    new String("division-name");

  /**
   * The database key used to store the error color.
   */
  private static final String m_errorKey = new String("error-color");

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
   * The database key used to store the timer enable state.
   */
  private static final String m_timerEnableKey = new String("timer-enable");

  /**
   * The database key used to store the timer location.
   */
  private static final String m_timerLocationKey =
    new String("timer-location");

  /**
   * The database key used to store the WiFi password.
   */
  private static final String m_wifiPassword = "wifiPassword";

  /**
   * The database key used to store the WiFi SSID.
   */
  private static final String m_wifiSSID = "wifiSSID";

  /**
   * The object for the Config singleton.
   */
  private static Config m_instance = null;

  /**
   * The object for accessing the database.
   */
  private Database m_database = null;

  /**
   * The object for accessing the web server.
   */
  private WebServer m_webserver = null;

  /**
   * The object for accessing the timer.
   */
  private Timer m_timer = null;

  /**
   * The number of divisions.
   */
  private int m_divisionCount = 1;

  /**
   * The division enable state.
   */
  private boolean m_divisionEnable = false;

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
   * Gets the currently selected accent color.
   *
   * @return The currently selected accent color.
   */
  public String
  accentColorGet()
  {
    String accent_color;

    // Get the accent color from the database.
    accent_color = m_database.configValueGet(m_accentKey);

    // Set the default accent color if there is not one specified in the
    // database.
    if(accent_color == null)
    {
      accent_color = "--color-cyan";
    }

    // Return the accent color.
    return(accent_color);
  }

  /**
   * Sets the accent color.
   *
   * @param accent_color The accent color.
   */
  public void
  accentColorSet(String accent_color)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_accentKey, accent_color);

    // Update the SSI for the accent color.
    m_webserver.registerSSI("accent-color", accent_color);
    m_webserver.registerSSI("icon-color", accent_color.substring(8));
  }

  /**
   * Gets the color for a division.
   *
   * @param division The division to query.
   *
   * @return The name of the color for this division.
   */
  public String
  divisionColorGet(int division)
  {
    // Get the color for this division from the database.
    String color =
      m_database.configValueGet(m_divisionColorKeyPrefix + division);

    // If the division color does not exist in the database, replace it with
    // the default division color.
    if(color == null)
    {
      if(division == 1)
      {
        color = "--color-red";
      }
      else if(division == 2)
      {
        color = "--color-blue";
      }
      else if(division == 3)
      {
        color = "--color-yellow";
      }
      else
      {
        color = "--color-green";
      }
    }

    // Return the division color.
    return(color);
  }

  /**
   * Sets the color for a division.
   *
   * @param division The division to set.
   *
   * @param color The name of the color for this division.
   */
  public void
  divisionColorSet(int division, String color)
  {
    // Save the color for this division to the database.
    m_database.configValueSet(m_divisionColorKeyPrefix + division, color);
  }

  /**
   * Gets the number of divisions selected.
   *
   * @return The currently selected number of divisions.
   */
  public int
  divisionCountGet()
  {
    // Return the division count.
    return(m_divisionCount);
  }

  /**
   * Initializes the division count state.
   */
  void
  divisionCountInit()
  {
    String count;

    // Get the division enable state from the database.
    count = m_database.configValueGet(m_divisionCountKey);

    // Set the default division count if there is not one specified in the
    // database.
    if(count == null)
    {
      count = "2";
    }

    // Set the division count.
    m_divisionCount = Integer.parseInt(count);
  }

  /**
   * Sets the division count.
   *
   * @param count The division count.
   */
  public void
  divisionCountSet(int count)
  {
    // Save the division count.
    m_divisionCount = count;

    // Set or add the configuration value.
    m_database.configValueSet(m_divisionCountKey, Integer.toString(count));
  }

  /**
   * Gets the currently selected division enable state.
   *
   * @return The currently selected division enable state.
   */
  public boolean
  divisionEnableGet()
  {
    // Return the division enable.
    return(m_divisionEnable);
  }

  /**
   * Initializes the division enable state.
   */
  void
  divisionEnableInit()
  {
    String enable;

    // Get the division enable state from the database.
    enable = m_database.configValueGet(m_divisionEnableKey);

    // Set the default division enable state if there is not one specified in
    // the database.
    if(enable == null)
    {
      enable = "0";
    }

    // Set the division enable state.
    m_divisionEnable = enable.equals("0") ? false : true;

    // Update the SSI for division support.
    m_webserver.registerSSI("support_divisions",
                            m_divisionEnable ? " have_divisions" : "");
  }

  /**
   * Sets the division enable state.
   *
   * @param enable The division enable state.
   */
  public void
  divisionEnableSet(boolean enable)
  {
    // Save the division enable state.
    m_divisionEnable = enable;

    // Set or add the configuration value.
    m_database.configValueSet(m_divisionEnableKey, enable ? "1" : "0");

    // Update the SSI for division support.
    m_webserver.registerSSI("support_divisions",
                            m_divisionEnable ? " have_divisions" : "");
  }

  /**
   * Gets the name of a division.
   *
   * @param division The division to get.
   *
   * @return The name of the division.
   */
  public String
  divisionNameGet(int division)
  {
    // Get the division name from the database.
    var name = m_database.configValueGet(m_divisionNameKeyPrefix + division);

    // If the division name is not present in the database, use the default
    // name.
    if(name == null)
    {
      name = m_webserver.getSSI("str_config_division_name" + division +
                                "_default");
    }

    // Return the division name.
    return(name);
  }

  /**
   * Sets the name of a division.
   *
   * @param division The division to set.
   *
   * @param name The name for the division.
   */
  public void
  divisionNameSet(int division, String name)
  {
    // Save the division name in the database.
    m_database.configValueSet(m_divisionNameKeyPrefix + division, name);

    // Update the SSI for the division name.
    m_webserver.registerSSI("division" + division + "_name", name);
  }

  /**
   * Gets the currently selected error color.
   *
   * @return The currently selected error color.
   */
  public String
  errorColorGet()
  {
    String error_color;

    // Get the error color from the database.
    error_color = m_database.configValueGet(m_errorKey);

    // Set the default error color if there is not one specified in the
    // database.
    if(error_color == null)
    {
      error_color = "--color-red";
    }

    // Return the error color.
    return(error_color);
  }

  /**
   * Sets the error color.
   *
   * @param error_color The error color.
   */
  public void
  errorColorSet(String error_color)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_errorKey, error_color);

    // Update the SSI for the error color.
    m_webserver.registerSSI("error-color", error_color);
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
   * Gets the currently selected timer enable state.
   *
   * @return The currently selected timer enable state.
   */
  public boolean
  timerEnableGet()
  {
    String enable;

    // Get the timer enable state from the database.
    enable = m_database.configValueGet(m_timerEnableKey);

    // Set the default timer enable state if there is not one specified in the
    // database.
    if(enable == null)
    {
      enable = "0";
    }

    // Return the timer enable state.
    return(enable.equals("0") ? false : true);
  }

  /**
   * Sets the timer enable state.
   *
   * @param enable The timer enable state.
   */
  public void
  timerEnableSet(boolean enable)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_timerEnableKey, enable ? "1" : "0");

    // Update the SSI for the timer enable.
    m_webserver.registerSSI("timer-enable", enable ? "1" : "0");

    // Get an instance pointer to the Timer object if it does not yet exist.
    if(m_timer == null)
    {
      m_timer = Timer.getInstance();
    }

    // If the Timer object exists, inform it of the new timer display enable
    // state.
    if(m_timer != null)
    {
      m_timer.displayEnable(enable);
    }
  }

  /**
   * Gets the currently selected timer location.
   *
   * @return The currently selected timer location.
   */
  public String
  timerLocationGet()
  {
    String location;

    // Get the timer location from the database.
    location = m_database.configValueGet(m_timerLocationKey);

    // Set the default timer location if there is not one specified in the
    // database.
    if(location == null)
    {
      location = "top";
    }

    // Return the timer location.
    return(location);
  }

  /**
   * Sets the timer location.
   *
   * @param location The timer location.
   */
  public void
  timerLocationSet(String location)
  {
    // Set or add the configuration value.
    m_database.configValueSet(m_timerLocationKey, location);

    // Update the SSI for the timer location.
    m_webserver.registerSSI("timer-location", location);

    // Get an instance pointer to the Timer object if it does not yet exist.
    if(m_timer == null)
    {
      m_timer = Timer.getInstance();
    }

    // If the Timer object exists, inform it of the new timer display enable
    // state.
    if(m_timer != null)
    {
      m_timer.displayLocation(location);
    }
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
   * Handles requests for /admin/admin/config.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  public byte[]
  serveConfig(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();
    String action;

    // Extract the action from the hash map.
    action = paramMap.containsKey("action") ? paramMap.get("action") : null;

    // See if the action was provided.
    if(action == null)
    {
      // Set the result to indicate that the action was missing.
      result.set("result", "missing action");
    }

    // Otherwise, see if the action is to get the configuration values.
    else if(action.equals("get"))
    {
      // Return the configuration values.
      result.set("accent", accentColorGet());
      result.set("division_count", divisionCountGet());
      result.set("division_enable", divisionEnableGet());
      result.set("division1_color", divisionColorGet(1));
      result.set("division1_name", divisionNameGet(1));
      result.set("division2_color", divisionColorGet(2));
      result.set("division2_name", divisionNameGet(2));
      result.set("division3_color", divisionColorGet(3));
      result.set("division3_name", divisionNameGet(3));
      result.set("division4_color", divisionColorGet(4));
      result.set("division4_name", divisionNameGet(4));
      result.set("error", errorColorGet());
      result.set("timer_enable", timerEnableGet());
      result.set("timer_location", timerLocationGet());
      result.set("wifi_password", wifiPasswordGet());
      result.set("wifi_ssid", wifiSSIDGet());

      // This request was successful.
      result.set("result", "ok");
    }

    // Otherwise, see if the action is to set configuration values.
    else if(action.equals("set"))
    {
      // If no valid values are set, ensure that an error is returned.
      result.set("result", "unknown values");

      // Save the accent color if it was provided.
      if(paramMap.containsKey("accent"))
      {
        accentColorSet(paramMap.get("accent"));
        result.set("result", "ok");
      }

      // Save the division count if it was provided.
      if(paramMap.containsKey("division_count"))
      {
        divisionCountSet(Integer.parseInt(paramMap.get("division_count")));
        result.set("result", "ok");
      }

      // Save the division enable if it was provided.
      if(paramMap.containsKey("division_enable"))
      {
        divisionEnableSet(paramMap.get("division_enable").equals("1"));
        result.set("result", "ok");
      }

      // Save the division 1 color if it was provided.
      if(paramMap.containsKey("division1_color"))
      {
        divisionColorSet(1, paramMap.get("division1_color"));
        result.set("result", "ok");
      }

      // Save the division 1 name if it was provided.
      if(paramMap.containsKey("division1_name"))
      {
        try
        {
          divisionNameSet(1, URLDecoder.decode(paramMap.get("division1_name"),
                                               "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }

      // Save the division 2 color if it was provided.
      if(paramMap.containsKey("division2_color"))
      {
        divisionColorSet(2, paramMap.get("division2_color"));
        result.set("result", "ok");
      }

      // Save the division 2 name if it was provided.
      if(paramMap.containsKey("division2_name"))
      {
        try
        {
          divisionNameSet(2, URLDecoder.decode(paramMap.get("division2_name"),
                                               "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }

      // Save the division 3 color if it was provided.
      if(paramMap.containsKey("division3_color"))
      {
        divisionColorSet(3, paramMap.get("division3_color"));
        result.set("result", "ok");
      }

      // Save the division 3 name if it was provided.
      if(paramMap.containsKey("division3_name"))
      {
        try
        {
          divisionNameSet(3, URLDecoder.decode(paramMap.get("division3_name"),
                                               "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }

      // Save the division 4 color if it was provided.
      if(paramMap.containsKey("division4_color"))
      {
        divisionColorSet(4, paramMap.get("division4_color"));
        result.set("result", "ok");
      }

      // Save the division 4 name if it was provided.
      if(paramMap.containsKey("division4_name"))
      {
        try
        {
          divisionNameSet(4, URLDecoder.decode(paramMap.get("division4_name"),
                                               "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }

      // Save the error color if it was provided.
      if(paramMap.containsKey("error"))
      {
        errorColorSet(paramMap.get("error"));
        result.set("result", "ok");
      }

      // Save the timer enable state if it was provided.
      if(paramMap.containsKey("timer_enable"))
      {
        timerEnableSet(paramMap.get("timer_enable").equals("1"));
        result.set("result", "ok");
      }

      // Save the timer location if it was provided.
      if(paramMap.containsKey("timer_location"))
      {
        timerLocationSet(paramMap.get("timer_location"));
        result.set("result", "ok");
      }

      // Save the WiFi password if it was provided.
      if(paramMap.containsKey("wifi_password"))
      {
        try
        {
          wifiPasswordSet(URLDecoder.decode(paramMap.get("wifi_password"),
                                            "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }

      // Save the WiFi SSID if it was provided.
      if(paramMap.containsKey("wifi_ssid"))
      {
        try
        {
          wifiSSIDSet(URLDecoder.decode(paramMap.get("wifi_ssid"), "UTF-8"));
          result.set("result", "ok");
        }
        catch (Exception e)
        {
          result.set("result", "error");
        }
      }
    }
    else
    {
      // Set the result to indicate that the action is unknown.
      result.set("result", "unknown action");
    }

    // Convert the response into a byte array and return it.
    try
    {
      String json = JSONParser.serialize(result);
      return(json.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
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

  /**
   * Finishes the setup for the configuration settings.  This should be called
   * after the WebServer is setup, so that it can access the web server.
   */
  public void
  finishSetup()
  {
    // Get a reference to the web server.
    m_webserver = WebServer.getInstance();

    // Get the stored value, if any, of the division count and enable.
    divisionCountInit();
    divisionEnableInit();

    // Set the accent and error colors as a Server Side Includes.
    m_webserver.registerSSI("accent-color", accentColorGet());
    m_webserver.registerSSI("division1_name", divisionNameGet(1));
    m_webserver.registerSSI("division2_name", divisionNameGet(2));
    m_webserver.registerSSI("division3_name", divisionNameGet(3));
    m_webserver.registerSSI("division4_name", divisionNameGet(4));
    m_webserver.registerSSI("error-color", errorColorGet());
    m_webserver.registerSSI("icon-color", accentColorGet().substring(8));
    m_webserver.registerSSI("timer-enable", timerEnableGet() ? "1" : "0");
    m_webserver.registerSSI("timer-location", timerLocationGet());

    // Register the dynamic handler for the config.json file.
    m_webserver.registerDynamicFile("/admin/config/config.json",
                                    this::serveConfig);
  }
}