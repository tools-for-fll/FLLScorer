// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the seasons tab.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
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
   * The Timekeeper object.
   */
  private TimeKeeper m_timekeeper = null;

  /**
   * A list of the season identifiers, corresponding to directory names in the
   * resources/seasons directory of the jar file.
   */
  private ArrayList<String> m_index = null;

  /**
   * A list of the years for the seasons (for example, "2024-2025").
   */
  private ArrayList<String> m_years = null;

  /**
   * A list of the names for the season (for example, "Submerged").
   */
  private ArrayList<String> m_names = null;

  /**
   * A list of the match lengths for the season.
   */
  private ArrayList<Integer> m_matchLens = null;

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
    String year = m_config.seasonGet();
    return(m_databaseId.get(m_index.indexOf(year)));
  }

  /**
   * Handles requests for /admin/seasons/seasons.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveSeasons(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();
    int idx;

    // See if the year was provided.
    if(paramMap.containsKey("year"))
    {
      // Get the requested year.
      String year = paramMap.get("year");

      // See if this is a known season, and it is enabled.
      idx = m_index.indexOf(year);
      if((idx != -1) && m_enabled.get(idx))
      {
        // Save the new value for the season.
        m_config.seasonSet(year);
      }
      else
      {
        idx = m_index.indexOf(m_config.seasonGet());
      }

      // Update the SSI for the selected season.
      m_webserver.registerSSI("season_selected", m_config.seasonGet());
      m_webserver.registerSSI("year_selected",
                              m_config.seasonGet().substring(0, 4));

      // Update the match length in the time keeper.
      m_timekeeper.matchLength(m_matchLens.get(idx));

      // Update the selected event based on the new season.
      m_events.selectDefault();

      // Set the result to success.
      result.set("result", "ok");
    }
    else
    {
      // Get the currently selected season.
      String year = m_config.seasonGet();
      idx = m_index.indexOf(year);

      // Return the details of the selected season.
      result.set("id", year);
      result.set("year", m_years.get(idx));
      result.set("name", m_names.get(idx));
      result.set("result", "ok");
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
    String name, year, year2, prev, latest, fragment;
    Boolean enabled;
    JSONObject json;
    InputStream in;
    int matchLen;

    // Get references to the web server, database, configuration manager,
    // events, and timekeeper objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_config = Config.getInstance();
    m_events = Events.getInstance();
    m_timekeeper = TimeKeeper.getInstance();

    // Create the arrays to keep track of the known seasons.
    m_index = new ArrayList<String>();
    m_years = new ArrayList<String>();
    m_names = new ArrayList<String>();
    m_matchLens = new ArrayList<Integer>();
    m_enabled = new ArrayList<Boolean>();
    m_databaseId = new ArrayList<Integer>();

    // Loop through the possible seasons.
    latest = null;
    for(int i = 2000; ; i++)
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

      // Get the year of this season.
      year = json.getString("year");

      // Get the match length, defaulting to 150 seconds if it is not supplied.
      matchLen = json.getInteger("match_len");
      if(matchLen == 0)
      {
        matchLen = 150;
      }

      // Determine if the season is enabled in the application.
      enabled = (json.getString("disabled") == null) ? true : false;

      // Get the name of the season, in the currently selected locale.
      name = json.getObject("name").getString(locale);
      if(name == null)
      {
        // The name of the season doesn't exist for the currently selected
        // locale, so get the default en_US name.
        name = json.getObject("name").getString("en_US");
      }

      // Add this season to the database.
      m_index.add(Integer.toString(i) + ".0");
      m_years.add(year);
      m_names.add(name);
      m_matchLens.add(matchLen);
      m_enabled.add(enabled);
      m_databaseId.add(m_database.seasonAdd(year + ".0", name));

      // Loop through the possible alternate season scoresheets.
      for(int j = 1; ; j++)
      {
        // Get the name of this possible alternate season, breaking out of the
        // loop if it does not exist.
        JSONObject alternate = json.getObject("name" + j);
        if(alternate == null)
        {
          break;
        }

        // Get the name of this alternative season, getting the locale-specific
        // name, or the en_US version as a backup.
        name = alternate.getString(locale);
        if(name == null)
        {
          name = alternate.getString("en_US");
        }

        // Get the match length, defaulting to 150 seconds if it is not
        // supplied.
        matchLen = json.getInteger("match_len" + j);
        if(matchLen == 0)
        {
          matchLen = 150;
        }

        // Add this alternative season to the database.
        m_index.add(Integer.toString(i) + "." + j);
        m_years.add(year);
        m_names.add(name);
        m_matchLens.add(matchLen);
        m_enabled.add(enabled);
        m_databaseId.add(m_database.seasonAdd(year + "." + j, name));
      }

      // If this season is enabled, it is the latest season found so far.
      // Therefore, save it as the latest season.
      if(enabled)
      {
        latest = Integer.toString(i) + ".0";
      }

      // Register a path mapping with the web server for the logo that goes
      // with this season.
      m_webserver.registerPathMapping("/logos/" + i + ".png",
                                      "seasons/" + i + "/logo.png");
      m_webserver.registerPathMapping("/logos/" + i + ".gif",
                                      "seasons/" + i + "/logo.gif");
    }

    // If there is not a season selected, select the latest season.
    if(m_config.seasonGet() == null)
    {
      m_config.seasonSet(latest);
    }

    // Set the selected season and year SSI fragment in the web server.
    m_webserver.registerSSI("season_selected", m_config.seasonGet());
    m_webserver.registerSSI("year_selected",
                            m_config.seasonGet().substring(0, 4));

    // Update the match length in the time keeper.
    int idx = m_index.indexOf(m_config.seasonGet());
    if(idx != -1)
    {
      m_timekeeper.matchLength(m_matchLens.get(idx));
    }

    // Generate and set the HTML fragment for displaying all the known seasons.
    fragment = "";
    for(int i = m_index.size(); i > 0; i--)
    {
      // Get the year of this season.
      year = m_index.get(i - 1);
      year = year.substring(0, 4);

      // Generate the HTML for this season's tile.
      fragment += "        <div id=\"seasons_" + year +
                  "\" class=\"seasons_tile\">\n";
      fragment += "          <img src=\"logos/" + year + ".png\">\n";
      if(m_index.get(i - 1).contains(".0"))
      {
        // There is not an alternate game for this season, so simply have the
        // name of the season.
        fragment += "          <p>" + m_years.get(i - 1) + " " +
                    m_names.get(i - 1) + "</p>\n";
      }
      else
      {
        // There is at least one alternate game for this season, so create a
        // select for choosing the alternate game.
        fragment += "          <select id=\"scoresheet_" + year + "\">\n";
        int j = 1;
        while(m_index.get(i - j - 1).substring(0, 4).equals(year))
        {
          j++;
        }
        fragment += "            <option value=\"0\">" +
                    m_years.get(i - j - 1) + " " + m_names.get(i - j) +
                    "</option>\n";
        for(int k = 1; k < j; k++)
        {
          fragment += "            <option value=\"" + k + "\">" +
                      m_names.get(i - j + k) + "</option>\n";
        }
        fragment += "          </select>\n";
        i -= j - 1;
      }

      // Close out the tile.
      fragment += "        </div>\n";
    }

    // Set this fragment as the HTML element for the seasons panel.
    fragment = fragment.substring(0, fragment.length() - 1);
    m_webserver.registerSSI("seasons_html", fragment);

    // Generate and set the JS fragment for enabling the click handler, or
    // disabling the tile, for all the known seasons.
    fragment = "";
    prev = "";
    for(int i = 0; i < m_index.size(); i++)
    {
      // Get the year of this season.
      year = m_index.get(i);
      year = year.substring(0, 4);

      // If this is the same year as the previous season (meaning it is an
      // alternate game), it has already been handled and can be skipped.
      if(year.equals(prev))
      {
        continue;
      }

      // Get the year of the next season.
      if((i + + 1) < m_index.size())
      {
        year2 = ((i + 1) < m_index.size()) ? m_index.get(i + 1) : "";
        year2 = year2.substring(0, 4);
      }
      else
      {
        year2 = "";
      }

      // See if this season is enabled.
      if(m_enabled.get(i))
      {
        // Generate the Javascript to enable this season in the UI and respond
        // to clicks.
        fragment += "  $(\"#seasons_" + year +
                    "\").click(() => { seasonsSelect(\"" + year + "\"); });\n";
        fragment += "  $(\"#seasons_" + year +
                    "\").attr(\"tabindex\", \"0\");\n";
        fragment += "  $(\"#seasons_" + year + "\").on(\"keyup\", " +
                    "(event) => { if(event.key == \"Enter\") { $(\"#seasons_" +
                    year + "\").click(); event.preventDefault(); }});\n";

        // Add the change event handler to the select if there are alternate
        // games for this season.
        if(year.equals(year2))
        {
          fragment += "  $(\"#scoresheet_" + year +
                      "\").on(\"change\", () => seasonsSelect(\"" + year +
                      "\"));\n";
        }
      }
      else
      {
        // Generate the Javascript to disable this season in the UI.
        fragment += "  $(\"#seasons_" + year + "\").addClass(\"disabled\");\n";
      }

      // This season is now the previous season.
      prev = year;
    }

    // Set this fragment as the Javascript element for the seasons panel.
    fragment = fragment.substring(0, fragment.length() - 1);
    m_webserver.registerSSI("seasons_js", fragment);

    // Register the dynamic handler for the season.json file.
    m_webserver.registerDynamicFile("/admin/seasons/seasons.json",
                                    this::serveSeasons);
  }
}