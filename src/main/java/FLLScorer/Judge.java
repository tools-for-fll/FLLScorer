// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the jugde page.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Judge
{
  /**
   * The object for the Judge singleton.
   */
  private static Judge m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The database object.
   */
  private Database m_database = null;

  /**
   * The config object.
   */
  private Config m_config = null;

  /**
   * The Seasons object.
   */
  private Seasons m_season = null;

  /**
   * The Events object.
   */
  private Events m_event = null;

  /**
   * The JSON rubric for the current season.
   */
  private JSONObject m_rubric = null;

  /**
   * The season for the currently loaded JSON rubric.
   */
  private String m_rubricSeason = null;

  /**
   * Gets the Judge singleton object, creating it if necessary.
   *
   * @return Returns the Judge singleton.
   */
  public static Judge
  getInstance()
  {
    // Create the Judge object if required.
    if(m_instance == null)
    {
      m_instance = new Judge();
    }

    // Return the Judge object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Judge()
  {
  }

  /**
   * Loads the rubric for the current season.
   */
  private void
  loadRubric()
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String fragment;
    InputStream in;

    // Get the season.
    String season = m_config.seasonGet();

    // See if the rubric for this season is already loaded.
    if(season.equals(m_rubricSeason))
    {
      return;
    }

    // Extract the year from the season.
    String year = season.substring(0, 4);

    // See if there is an information file for this season.
    in = classLoader.getResourceAsStream("seasons/" + year + "/rubric.json");
    if(in == null)
    {
      return;
    }

    // Read and parse the contents of the information JSON.
    try
    {
      fragment = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      m_rubric = JSONParser.deserializeObject(fragment);
      m_rubricSeason = season;
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }
  }

  /**
   * Gets a rubric.
   *
   * @param result The JSON object into which the rubric is placed.
   *
   * @param id The ID of the team.
   */
  private void
  get(JSONObject result, int id)
  {
    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Get the current locale.
    String locale = m_config.localeGet();

    // Load the current season's rubric if necessary.
    loadRubric();

    // Get the selections for this team.
    JSONObject teamRubric = null;
    try
    {
      String rubricData = m_database.judgingGet(season_id, event_id, id);
      if(rubricData != null)
      {
        teamRubric = JSONParser.deserializeObject(rubricData);
      }
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }

    // Construct the HTML for the rubric starting with an empty string.
    String html = "";

    // Get the judging areas from the rubric.
    JSONArray areas = m_rubric.getArray("areas");

    // Generate the header, listing the judging areas.
    html += "<div class=\"area\">";

    // Loop through the areas.
    for(int i = 0; i < areas.size(); i++)
    {
      // Get the JSON object for this area.
      JSONObject area = areas.getObject(i);

      // Get the long name for this area.
      String area_name = area.getObject("name").getString(locale);
      if(area_name == null)
      {
        area_name = area.getObject("name").getString("en_US");
      }

      // Get the short name for this area.
      String area_name_short = area.getObject("short_name").getString(locale);
      if(area_name_short == null)
      {
        area_name_short = area.getObject("short_name").getString("en_US");
      }

      // Generate the header item for this judging area.
      html += "<div class=\"area_name\">";
      html += "<span class=\"name\">" + area_name + "</span>";
      html += "<span class=\"short_name\">" + area_name_short + "</span>";
      html += "</div>";
    }

    // Close the header div.
    html += "</div>";

    // Loop through the areas.
    for(int i = 0; i < areas.size(); i++)
    {
      // Create a div for this judging area.
      html += "<div class=\"items\" style=\"display: none;\">";

      // Get the JSON object for sections in this area.
      JSONArray sections = areas.getObject(i).getArray("sections");

      // Loop through the sections in this area.
      for(int j = 0; j < sections.size(); j++)
      {
        // Get the JSON object for this section.
        JSONObject section = sections.getObject(j);

        // Get the name for this section.
        String name = section.getObject("name").getString(locale);
        if(name == null)
        {
          name = section.getObject("name").getString("en_US");
        }

        // Get the description for this section.
        String desc = section.getObject("description").getString(locale);
        if(desc == null)
        {
          desc = section.getObject("description").getString("en_US");
        }

        // Create the header for this section.
        html += "<div class=\"section\">";
        html += "<span class=\"name\">" + name + "</span>";
        html += "<hr>";
        html += "<span class=\"description\">" + desc + "</span>";

        // Get the items for this section.
        JSONArray items = section.getArray("items");

        // Loop through the items.
        for(int k = 0; k < items.size(); k++)
        {
          // Get the JSON object for this item.
          JSONObject item = items.getObject(k);

          // If this is a Core Values item, set the Core Values class to add
          // to the buttons.
          String core = item.isSet("isCoreValues") ? " core" : "";

          // Construct the ID for this item.
          String item_id = "R" + i + "_" + j + "_" + k;

          // Determine if this item has a selection in this team's rubric.
          int selected = -1;
          if((teamRubric != null) && teamRubric.isSet(item_id))
          {
            selected = teamRubric.getInteger(item_id);
          }

          // Create a div for this item.
          html += "<hr>";
          html += "<div id=\"" + item_id + "\" class=\"select\">";

          // Add the first select for this item.
          desc = item.getObject("1").getString(locale);
          if(desc == null)
          {
            desc = item.getObject("1").getString("en_US");
          }
          html += "<button onclick=\"itemToggle('#" + item_id +
                  "', this);\" class=\"sel1" + core +
                  ((selected == 0) ? " selected" : "") +
                  "\"><span>1</span></button>";
          html += "<span class=\"desc1\">" + desc + "</span>";

          // Add the second select for this item.
          desc = item.getObject("2").getString(locale);
          if(desc == null)
          {
            desc = item.getObject("2").getString("en_US");
          }
          html += "<button onclick=\"itemToggle('#" + item_id +
                  "', this);\" class=\"sel2" + core +
                  ((selected == 1) ? " selected": "") +
                  "\"><span>2</span></button>";
          html += "<span class=\"desc2\">" + desc + "</span>";

          // Add the third select for this item.
          desc = item.getObject("3").getString(locale);
          if(desc == null)
          {
            desc = item.getObject("3").getString("en_US");
          }
          html += "<button onclick=\"itemToggle('#" + item_id +
                  "', this);\" class=\"sel3" + core +
                  ((selected == 2) ? " selected" : "") +
                  "\"><span>3</span></button>";
          html += "<span class=\"desc3\">" + desc + "</span>";

          // Add the fourth select for this item.
          desc = item.getObject("4").getString(locale);
          if(desc == null)
          {
            desc = item.getObject("4").getString("en_US");
          }
          html += "<button onclick=\"itemToggle('#" + item_id +
                  "', this);\" class=\"sel4" + core +
                  ((selected == 3) ? " selected" : "") +
                  "\"><span>4</span></button>";
          html += "<span class=\"desc4\">" + desc + "</span>";

          // Close the div for this select.
          html += "</div>";
        }

        // Close the div for this section.
        html += "</div>";
      }

      // Close the div for this juging area.
      html += "</div>";
    }

    // Add the number of judging areas to the JSON response.
    result.set("area_count", areas.size());

    // Add information about the team to the JSON response.
    result.set("id", id);
    result.set("number", m_database.teamNumberGet(season_id, id));
    result.set("name", m_database.teamNameGet(season_id, id));

    // Add the rubric HTML to the JSON response.
    result.set("rubric", html);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Lists the teams.
   *
   * @param result The JSON object into which the teams are placed.
   */
  private void
  list(JSONObject result)
  {
    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // A list of information about teams, maintained in team number order.
    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();

    // Enumerate the teams from the database for this season/event.
    m_database.teamEnumerate(season_id, event_id, ids, numbers, names);

    // A list of information about team rubrics.
    ArrayList<Integer> ids2 = new ArrayList<Integer>();
    ArrayList<Integer> project = new ArrayList<Integer>();
    ArrayList<Integer> robot = new ArrayList<Integer>();
    ArrayList<Integer> core = new ArrayList<Integer>();
    ArrayList<String> rubric = new ArrayList<String>();

    // Enumerate the team rubrics from the database for this season/event.
    m_database.judgingEnumerate(season_id, event_id, null, null, null, ids2,
                                project, robot, core, rubric);

    // Start with an empty HTML fragment for the team list.
    String html = "";

    // Loop through the teams.
    for(int i = 0; i < numbers.size(); i++)
    {
      // The default color for the edit button is yellow, meaning that the team
      // needs its rubrics filled in.
      String color = "yellow";

      // Get the judging index for this team.
      int j = ids2.indexOf(ids.get(i));

      // See if the judging index for this team is not -1, meaning that there
      // is an entry for this team in the judging database.
      if(j != -1)
      {
        // If all of judging areas have a score, then the rubric for this team
        // is completely filled out; in this case, make the button green to
        // indicate that it is done.
        if((project.get(j) != -1) && (robot.get(j) != -1) &&
           (core.get(j) != -1))
        {
          color = "green";
        }

        // Otherwise, if the rubric itself has a value, then it is partially
        // filled out; in this case, make the button red to indicate that it
        // is only partially completed.
        else if(rubric.get(j) != "")
        {
            color = "red";
        }
      }

      // Generate the HTML fragment for this team.
      html += "<div class=\"row\">";
      html += "<div class=\"name\">";
      html += "<span>";
      html += numbers.get(i) + " : " + names.get(i);
      html += "</span>";
      html += "</div>";
      html += "<div class=\"action\">";
      html += "<button class=\"" + color + "\" onclick=\"loadRubric(" +
              ids.get(i) + ");\">Edit</button>";
      html += "</div>";
      html += "</div>";
    }

    // Add the HTML fragment to the JSON response.
    result.set("html", html);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Saves the rubric.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param id The ID of the team.
   *
   * @param json The JSON representation of the rubric selections.
   */
  private void
  save(JSONObject result, int id, String json)
  {
    int project, robot_design, core_values;
    JSONObject scores;

    // See if this JSON string is empty.
    if(json.equals("{}"))
    {
      // Save the rubric for this team.
      if(m_database.judgingRemove(m_season.seasonIdGet(), m_event.eventIdGet(),
                                  id) == false)
      {
        result.set("result", m_webserver.getSSI("str_judge_save_fail"));
      }
      else
      {
        // Success.
        result.set("result", "ok");
      }

      // This request has been handled.
      return;
    }

    // The default score for the rubric areas is -1, meaning no score.
    project = -1;
    robot_design = -1;
    core_values = -1;

    // Catch and ignore any errors.
    try
    {
      // Convert the JSON string into an object.
      scores = JSONParser.deserializeObject(json);

      // Get the areas from the rubric.
      JSONArray areas = m_rubric.getArray("areas");

      // Loop through the areas.
      for(int i = 0; i < areas.size(); i++)
      {
        // Get the JSON object for this area.
        JSONObject area = areas.getObject(i);

        // Get the type of this area.
        String type = area.getString("type");

        // Get the sections in this area.
        JSONArray sections = area.getArray("sections");

        // Loop through the sections.
        for(int j = 0; j < sections.size(); j++)
        {
          // Get the JSON object for this section.
          JSONObject section = sections.getObject(j);

          // Get the items in this section.
          JSONArray items = section.getArray("items");

          // Loop through the items.
          for(int k = 0; k < items.size(); k++)
          {
            // Get the JSON object for this item.
            JSONObject item = items.getObject(k);

            // Determine if this is an individual item that also counts towards
            // Core Values.
            boolean isCoreValues = (item.isBoolean("isCoreValues") &&
                                    item.getBoolean("isCoreValues"));

            // See if this item has a value in the team's rubric.
            if(scores.isInteger("R" + i + "_" + j + "_" + k))
            {
              // Get the team's score for this item.
              int score = scores.getInteger("R" + i + "_" + j + "_" + k) + 1;

              // See if this area is Project.
              if(type.equals("project"))
              {
                // Add the team's score on this item to the Project score.
                project = (project == -1) ? score : (project + score);

                // If this item is a Core Values item, also add the team's
                // score on this item to the Core Values score.
                if(isCoreValues)
                {
                  core_values = ((core_values == -1) ? score :
                                 (core_values + score));
                }
              }

              // See if this area is Robot Design.
              if(type.equals("robot_design"))
              {
                // Add the team's score on this item to the Robot Design score.
                robot_design = ((robot_design == -1) ? score :
                                (robot_design + score));

                // If this item is a Core Values item, also add the team's
                // score on this item to the Core Values score.
                if(isCoreValues)
                {
                  core_values = ((core_values == -1) ? score :
                                 (core_values + score));
                }
              }

              // See if this area is Core Values.
              if(type.equals("core_values"))
              {
                // Add the team's score on this item to the Core Values score.
                core_values = ((core_values == -1) ? score :
                               (core_values + score));
              }
            }
          }
        }
      }
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }

    // Save the rubric for this team.
    if(m_database.judgingAdd(m_season.seasonIdGet(), m_event.eventIdGet(), id,
                             project, robot_design, core_values, json) == -1)
    {
      result.set("result", m_webserver.getSSI("str_judge_save_fail"));
    }
    else
    {
      // Success.
      result.set("result", "ok");
    }
  }

  /**
   * Serves the JSON data for the current state of the judges.
   *
   * @param path The path that was requested.
   *
   * @param paramMap The parameters associated with the request.
   *
   * @return A byte array containing the JSON data for the judges.
   */
  private byte[]
  serveJudgeJson(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "get", for getting a scoresheet.
      if(paramMap.get("action").equals("get") && paramMap.containsKey("id"))
      {
        // Get the scoresheet.
        get(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "save", for saving a rubric.
      else if(paramMap.get("action").equals("save") &&
              paramMap.containsKey("id") && paramMap.containsKey("json"))
      {
        // Save the scoresheet.
        save(result, Integer.parseInt(paramMap.get("id")),
             URLDecoder.decode(paramMap.get("json"), StandardCharsets.UTF_8));
      }

      // See if the action is "list", for listing the scores.
      else if(paramMap.get("action").equals("list"))
      {
        // List the scores.
        list(result);
      }

      // Otherwise, return an error.
      else
      {
        result.set("result", "error");
      }
    }

    // Otherwise, the list of teams and scores should be provided.
    else
    {
      // List the scores.
      list(result);
    }

    // Convert the response into a byte array and return it.
    try
    {
      String ret = JSONParser.format(JSONParser.serialize(result));
      return(ret.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Performs initial setup for the judge page.
   */
  public void
  setup()
  {
    // Get references to the web server, database, config, season, and event
    // objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_config = Config.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

    // Register the dynamic handler for the judge.json file.
    m_webserver.registerDynamicFile("/judge/judge.json", this::serveJudgeJson);
  }
}