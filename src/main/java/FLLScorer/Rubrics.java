// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the rubrics tab.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Rubrics
{
  /**
   * The object for the Rubrics singleton.
   */
  private static Rubrics m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The Database object.
   */
  private Database m_database = null;

  /**
   * The Seasons object.
   */
  private Seasons m_season = null;

  /**
   * The Events object.
   */
  private Events m_event = null;

  /**
   * Gets the Rubrics singleton object, creating it if necessary.
   *
   * @return Returns the Rubrics singleton.
   */
  public static Rubrics
  getInstance()
  {
    // Create the Rubrics object if required.
    if(m_instance == null)
    {
        m_instance = new Rubrics();
    }

    // Return the Rubrics object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Rubrics()
  {
  }

  /**
   * Deletes a team's rubrics.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   */
  private void
  delete(JSONObject result, int id)
  {
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Delete the rubric from the database.
    if(m_database.judgingRemove(season_id, event_id, id) == true)
    {
      // Return successs since the match score was deleted.
      result.set("result", "ok");
    }
    else
    {
      // Return an error since the match score could not be delete.
      result.set("result", m_webserver.getSSI("str_rubrics_delete_error"));
    }
  }

  /**
   * Exchanges rubrics between two teams.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the first team.
   *
   * @param id2 The ID of the second team.
   */
  private void
  exchange(JSONObject result, int id, int id2)
  {
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Arrays to contains the judging results to swap.
    ArrayList<Integer> project = new ArrayList<Integer>();
    ArrayList<Integer> robot = new ArrayList<Integer>();
    ArrayList<Integer> core = new ArrayList<Integer>();
    ArrayList<String> rubric = new ArrayList<String>();

    // Get the first judging result.
    if(m_database.judgingGet(season_id, event_id, id, project, robot, core,
                             rubric) == false)
    {
      // Return an error.
      result.set("result", m_webserver.getSSI("str_rubrics_exchange_error"));
      return;
    }

    // Get the second judging result.
    m_database.judgingGet(season_id, event_id, id2, project, robot, core,
                          rubric);

    // Change the first team's judging result.
    if(rubric.size() == 1)
    {
      if(m_database.judgingRemove(season_id, event_id, id) == false)
      {
        // Return an error.
        result.set("result", m_webserver.getSSI("str_rubrics_exchange_error"));
        return;
      }
    }
    else
    {
      if(m_database.judgingAdd(season_id, event_id, id, project.get(1),
                               robot.get(1), core.get(1), rubric.get(1)) == -1)
      {
        // Return an error.
        result.set("result", m_webserver.getSSI("str_rubrics_exchange_error"));
        return;
      }
    }

    // Change the second team's judging result.
    if(m_database.judgingAdd(season_id, event_id, id2, project.get(0),
                             robot.get(0), core.get(0), rubric.get(0)) == -1)
    {
      // Restore the first team's judging result.
      m_database.judgingAdd(season_id, event_id, id, project.get(0),
                            robot.get(0), core.get(0), rubric.get(0));

        // Return an error.
        result.set("result", m_webserver.getSSI("str_rubrics_exchange_error"));
        return;
    }

    // Success.
    result.set("result", "ok");
  }

  /**
   * Lists the rubrics of the teams.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   */
  private void
  list(JSONObject result)
  {
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // A list of information about teams, maintained in team number order.
    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> project = new ArrayList<Integer>();
    ArrayList<Integer> robot = new ArrayList<Integer>();
    ArrayList<Integer> core = new ArrayList<Integer>();
    ArrayList<Boolean> haveRubric = new ArrayList<Boolean>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, event_id, ids, numbers, names);

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < numbers.size(); idx++)
    {
      project.add(idx, -1);
      robot.add(idx, -1);
      core.add(idx, -1);
      haveRubric.add(idx, false);
    };

    // A list of information about the rubrics.
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Integer> ip = new ArrayList<Integer>();
    ArrayList<Integer> rd = new ArrayList<Integer>();
    ArrayList<Integer> cv = new ArrayList<Integer>();
    ArrayList<String> rb = new ArrayList<String>();

    // Enumerate the rubrics for this event.
    m_database.judgingEnumerate(season_id, event_id, null, null, null, teams,
                                ip, rd, cv, rb);

    // Loop through all the rubrics.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Ignore this rubric if it is not for a team at this event (should not
      // happen).
      int team_idx = ids.indexOf(teams.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // Save the Innovation Project score, if it exists.
      if(ip.get(idx) != null)
      {
        project.set(team_idx, ip.get(idx));
      }

      // Save the Robot Design score, if it exists.
      if(rd.get(idx) != null)
      {
        robot.set(team_idx, rd.get(idx));
      }

      // Save the Core Values score, if it exists.
      if(cv.get(idx) != null)
      {
        core.set(team_idx, cv.get(idx));
      }

      // Determine if there is a rubric.
      if(rb.get(idx) != null)
      {
        haveRubric.set(team_idx, true);
      }
    }

    // Loop through the teams.
    JSONArray rubrics = new SimpleJSONArray();
    for(int i = 0; i < numbers.size(); i++)
    {
      // Add this team's judging scores to the rubrics array.
      JSONObject rubric = new SimpleJSONObject();
      rubric.set("id", ids.get(i));
      rubric.set("number", numbers.get(i));
      rubric.set("name", names.get(i));
      rubric.set("project", (project.get(i) != -1) ? project.get(i) :
                            (haveRubric.get(i) ? "***" : "-"));
      rubric.set("robot", (robot.get(i) != -1) ? robot.get(i) :
                          (haveRubric.get(i) ? "***" : "-"));
      rubric.set("core", (core.get(i) != -1) ? core.get(i) :
                         (haveRubric.get(i) ? "***" : "-"));
      rubrics.addEntry(rubric);
    }

    // Add the rubrics array to the JSON response.
    result.set("rubrics", rubrics);
  }

  /**
   * Handles requests for /admin/rubrics/rubrics.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveScores(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "delete", for deleting a team's rubric.
      if(paramMap.get("action").equals("delete") && paramMap.containsKey("id"))
      {
        // Delete this rubric.
        delete(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "exchange", for exchanging a team's score.
      else if(paramMap.get("action").equals("exchange") &&
              paramMap.containsKey("id") && paramMap.containsKey("id2"))
      {
        // Exchange the scores.
        exchange(result, Integer.parseInt(paramMap.get("id")),
                 Integer.parseInt(paramMap.get("id2")));
      }

      // See if the action is "list", for listing the scores.
      else if(paramMap.get("action").equals("list"))
      {
        // List the scores of the teams.
        list(result);
      }

      // Otherwise, return an error.
      else
      {
        result.set("result", "error");
      }
    }

    // Otherwise, return a list of the teams.
    else
    {
      // List the scores of the teams.
      list(result);
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
   * Performs initial setup for the rubrics handler.
   */
  public void
  setup()
  {
    // Get references to the web server, database, seasons, and events objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

    // Register the dynamic handler for the rubrics.json file.
    m_webserver.registerDynamicFile("/admin/rubrics/rubrics.json",
                                    this::serveScores);
  }
}