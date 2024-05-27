// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the seasons tab.
 */
public class Seasons
{
  /**
   * The object for the Seasons singleton.
   */
  private static Seasons m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The Database object.
   */
  private Database m_database = null;

  /**
   * The Config object.
   */
  private Config m_config = null;

  /**
   * The Events object.
   */
  private Events m_events = null;

  /**
   * A list of the season identifiers, corresponding to directory names in the
   * resources/seasons directory of the jar file.
   */
  private ArrayList<Integer> m_index = null;

  /**
   * A list of the years for the seasons (for example, "2024-2025").
   */
  private ArrayList<String> m_years = null;

  /**
   * A list of the names for the season (for example, "Submerged").
   */
  private ArrayList<String> m_names = null;

  /**
   * A list of boolean that are <b>true<b> if the season is enabled (meaning
   * that there is a scoresheet available for that season).
   */
  private ArrayList<Boolean> m_enabled = null;

  /**
   * A list of database IDs for the seasons.
   */
  private ArrayList<Integer> m_databaseId = null;

  /**
   * Gets the Seasons singleton object, creating it if necessary.
   *
   * @return Returns the Seasons singleton.
   */
  public static Seasons
  getInstance()
  {
    // Create the Seasons object if required.
    if(m_instance == null)
    {
      m_instance = new Seasons();
    }

    // Return the Seasons object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Seasons()
  {
  }

  /**
   * Gets the season_id for the currently selected season.
   *
   * @return Returns the season_id for the currently selected season.
   */
  public int
  seasonIdGet()
  {
    // Return the season_id.
    int year = Integer.parseInt(m_config.seasonGet());
    return(m_databaseId.get(m_index.indexOf(year)));
  }

  /**
   * Handles requests for /seasons/seasons.json.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveSeasons(String path, String parameters)
  {
    JSONObject result = new SimpleJSONObject();
    String[] params;

    // If there are no parameters, return the currently selected season.
    if(parameters == null)
    {
      int idx = m_index.indexOf(Integer.parseInt(m_config.seasonGet()));
      result.set("id", m_config.seasonGet());
      result.set("year", m_years.get(idx));
      result.set("name", m_names.get(idx));
      result.set("result", "ok");
    }

    // Otherwise, split the parameters, and loop through all of them.
    else
    {
      params = parameters.split("&");
      for(int i = 0; i < params.length; i++)
      {
        // Split this parameter into its key and value.
        String[] items = params[i].split("=");

        // See if the key for this parameter is the year.
        if(items[0].equals("year"))
        {
          // Get the requested year.
          int year = Integer.parseInt(items[1]);

          // Ignore this request if the given season is not a known season.
          if(!m_index.contains(year))
          {
            break;
          }

          // Ignore this request if the given seasin is not enabled.
          if(!m_enabled.get(m_index.indexOf(year)))
          {
            break;
          }

          // Save the new value for the season.
          m_config.seasonSet(items[1]);

          // Update the SSI for the selected season.
          m_webserver.registerSSI("season_selected", m_config.seasonGet());

          // Update the selected event based on the new season.
          m_events.selectDefault();

          // Set the result to success.
          result.set("result", "ok");
        }
      }
    }

    // If there is not result, set an error.
    if(result.getString("result") == null)
    {
      result.set("result", "error");
    }

    // Convert the response into a byte array and return it.
    try
    {
      String json = JSONParser.format(JSONParser.serialize(result));
      return(json.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Performs initial setup for the seasons handler.
   */
  public void
  setup()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String locale = Config.getInstance().localeGet();
    String name, year, latest, fragment;
    Boolean enabled;
    JSONObject json;
    InputStream in;

    // Get references to the web server, database, and the configuration
    // manager.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_config = Config.getInstance();
    m_events = Events.getInstance();

    // Create the arrays to keep track of the known seasons.
    m_index = new ArrayList<Integer>();
    m_years = new ArrayList<String>();
    m_names = new ArrayList<String>();
    m_enabled = new ArrayList<Boolean>();
    m_databaseId = new ArrayList<Integer>();

    // Loop through the possible seasons.
    latest = null;
    for(int i = 1999; ; i++)
    {
      // See if there is an information file for this season.
      in = classLoader.getResourceAsStream("seasons/" + i + "/info.json");
      if(in == null)
      {
        break;
      }

      // Read and parse the contents of the information JSON.
      try
      {
        fragment = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        json = JSONParser.deserializeObject(fragment);
      }
      catch(Exception e)
      {
        System.out.println("JSON error: " + e);
        break;
      }

      // Get the name of the season, in the currently selected locale.
      name = json.getObject("name").getString(locale);
      if(name == null)
      {
        // The name of the season doesn't exist for the currently selected
        // locale, so get the default en_US name.
        name = json.getObject("name").getString("en_US");
      }

      // Get the year of this season.
      year = json.getString("year");

      // Determine if the season is enabled in the application.
      enabled = (json.getString("disabled") == null) ? true : false;

      // Add the details of this season to the lists.
      m_index.add(i);
      m_years.add(year);
      m_names.add(name);
      m_enabled.add(enabled);

      // If this season is enabled, it is the latest season found so far.
      // Therefore, save it as the latest season.
      if(enabled)
      {
        latest = String.valueOf(i);
      }

      // Add this season to the database.
      m_databaseId.add(m_database.seasonAdd(year, name));

      // Register a path mapping with the web server for the logo that goes
      // with this season.
      m_webserver.registerPathMapping("/logos/" + i + ".png",
                                      "seasons/" + i + "/logo.png");
    }

    // If there is not a season selected, select the latest season.
    if(m_config.seasonGet() == null)
    {
      m_config.seasonSet(latest);
    }

    // Set the selected season SSI fragment in the web server.
    m_webserver.registerSSI("season_selected", m_config.seasonGet());

    // Generate and set the HTML fragment for displaying all the known seasons.
    fragment = "";
    for(int i = m_index.size(); i > 0; i--)
    {
      fragment += "        <div id=\"seasons_" + m_index.get(i - 1) +
                  "\" class=\"seasons_tile\">\n";
      fragment += "          <img src=\"logos/" + m_index.get(i - 1) +
                  ".png\">\n";
      fragment += "          <p>" + m_years.get(i - 1) + " " +
                  m_names.get(i - 1) + "</p>\n";
      fragment += "        </div>\n";
    }
    fragment = fragment.substring(0, fragment.length() - 1);
    m_webserver.registerSSI("seasons_html", fragment);

    // Generate and set the JS fragment for enabling the click handler, or
    // disabling the tile, for all the known seasons.
    fragment = "";
    for(int i = 0; i < m_index.size(); i++)
    {
      if(m_enabled.get(i))
      {
        fragment += "  $(\"#seasons_" + m_index.get(i) +
                    "\").click(() => { seasonsSelect(\"" + m_index.get(i) +
                    "\"); });\n";
        fragment += "  $(\"#seasons_" + m_index.get(i) +
                    "\").attr(\"tabindex\", \"0\");";
        fragment += "  $(\"#seasons_" + m_index.get(i) + "\").on(\"keyup\", " +
                    "(event) => { if(event.key == \"Enter\") { $(\"#seasons_" +
                    m_index.get(i) +
                    "\").click(); event.preventDefault(); }});";
      }
      else
      {
        fragment += "  $(\"#seasons_" + m_index.get(i) +
                    "\").addClass(\"disabled\");\n";
      }
    }
    fragment = fragment.substring(0, fragment.length() - 1);
    m_webserver.registerSSI("seasons_js", fragment);

    // Register the dynamic handler for the season.json file.
    m_webserver.registerDynamicFile("/seasons/seasons.json",
                                    this::serveSeasons);
  }
}