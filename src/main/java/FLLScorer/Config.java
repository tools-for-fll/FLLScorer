// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.util.Locale;

/**
 * Handles storing the configuration values for the application.
 */
public class Config
{
  /**
   * The database key used to store the current event.
   */
  private static final String m_eventKey = new String("event");

  /**
   * The database key used to store the current locale.
   */
  private static final String m_localeKey = new String("locale");

  /**
   * The database key used to store the current season.
   */
  private static final String m_seasonKey = new String("season");

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
   *               system setting if this is <b>null</b >.
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
   * @param event The ID of the esason; the selection will be removed if this
   *              is <b>null</b>.
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