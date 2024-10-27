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
import org.bspfsystems.simplejson.SimpleJSONArray;
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

    // Load the current season's rubric if necessary.
    loadRubric();

    // Add the current locale to the JSON response.
    result.set("locale", m_config.localeGet());

    // Add the season's rubric to the JSON response.
    result.set("rubric", m_rubric);

    // Add information about the team to the JSON response.
    result.set("id", id);
    result.set("number", m_database.teamNumberGet(season_id, id));
    result.set("name", m_database.teamNameGet(season_id, id));

    // Get the selections for this team.
    JSONObject teamRubric = null;
    try
    {
      String rubricData = m_database.judgingRubricGet(season_id, event_id, id);
      if(rubricData != null)
      {
        teamRubric = JSONParser.deserializeObject(rubricData);
      }
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }

    // Add the team's rubric choices to the JSON response.
    result.set("choices", teamRubric);

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

    // Create a JSON array for the team list.
    JSONArray teams = new SimpleJSONArray();

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

      // Add this team to the JSON array.
      JSONObject team = new SimpleJSONObject();
      team.set("id", ids.get(i));
      team.set("number", numbers.get(i));
      team.set("name", names.get(i));
      team.set("color", color);
      teams.addEntry(team);
    }

    // Add the team list JSON array to the JSON response.
    result.set("teams", teams);

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
    boolean complete;

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

    // Assume the rubric is complete until a missing entry is found.
    complete = true;

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
            else
            {
              // The rubric is not complete.
              complete = false;
            }
          }
        }
      }
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }

    // Do not provide scores for this rubric unless it is complete.
    if(!complete)
    {
      project = -1;
      robot_design = -1;
      core_values = -1;
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
      String ret = JSONParser.serialize(result);
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