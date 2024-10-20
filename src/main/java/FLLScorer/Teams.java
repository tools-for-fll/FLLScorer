// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

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
 * Handles the teams tab.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Teams
{
  /**
   * The object for the Teams singleton.
   */
  private static Teams m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The database object.
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
   * Gets the Teams singleton object, creating it if necessary.
   *
   * @return Returns the Teams singleton.
   */
  public static Teams
  getInstance()
  {
    // Create the Teams object if required.
    if(m_instance == null)
    {
      m_instance = new Teams();
    }

    // Return the Teams object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Teams()
  {
  }

  /**
   * Adds a team.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param number The team number.
   *
   * @param name The team name.
   */
  private void
  add(JSONObject result, int number, String name)
  {
    // Get the season ID.
    int season_id = m_season.seasonIdGet();

    // See if there is already a team with the given number.
    if(m_database.teamGet(season_id, number) >= 0)
    {
      // Return an error since the team already exists.
      result.set("result", m_webserver.getSSI("str_teams_already_exists"));
    }

    // Add the team to the database.
    else if(m_database.teamAdd(season_id, number, name) >= 0)
    {
      // Return success since the team was added.
      result.set("result", "ok");
    }

    // Otherwise, an error occurred when adding the team.
    else
    {
      // Return an error.
      result.set("result", m_webserver.getSSI("str_teams_add_failed"));
    }
  }

  /**
   * Adds a team to the event.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   */
  private void
  addToEvent(JSONObject result, int id)
  {
    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Add this team to the event.
    if(m_database.teamAtEventSet(season_id, event_id, id) == true)
    {
      // Return success since the team was added.
      result.set("result", "ok");
    }

    // Otherwise, an error occurred when adding the team.
    else
    {
      // Return an error since the team could not be added.
      result.set("result", m_webserver.getSSI("str_teams_event_add_failed"));
    }
  }

  /**
   * Counts the number of teams at the event.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   */
  private void
  count(JSONObject result)
  {
    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // A list of the team IDs for the teams at this event.
    ArrayList<Integer> teams = new ArrayList<Integer>();

    // Enumerate the teams at this event.
    m_database.teamAtEventEnumerate(season_id, event_id, -1, null, null,
                                    teams);

    // Set the result to the number of teams at this event.
    result.set("count", teams.size());
    result.set("result", "ok");
  }

  /**
   * Deletes a team.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   */
  private void
  delete(JSONObject result, int id)
  {
    // Get the season ID.
    int season_id = m_season.seasonIdGet();

    // Delete the team from the database.
    m_database.teamRemove(season_id, id);

    // Return success since the team was deleted.
    result.set("result", "ok");
  }

  /**
   * Edits a team.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   *
   * @param number The team number.
   *
   * @param name The team name.
   */
  private void
  edit(JSONObject result, int id, int number, String name)
  {
    // Edit the team in the database.
    if(m_database.teamEdit(id, number, name) == true)
    {
      // Return success since the team was edited.
      result.set("result", "ok");
    }

    // Otherwise, an error occurred when editing the team.
    else
    {
      // Return an error since the team could not be edited.
      result.set("result", m_webserver.getSSI("str_teams_edit_failed"));
    }
  }

  /**
   * Lists the teams.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
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
    ArrayList<Boolean> inEvent = new ArrayList<Boolean>();
    ArrayList<Boolean> seasonScores = new ArrayList<Boolean>();
    ArrayList<Boolean> eventScores = new ArrayList<Boolean>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, -1, ids, numbers, names);

    // Add the other information about the teams.
    for(int idx = 0; idx < numbers.size(); idx++)
    {
      int id = ids.get(idx);
      inEvent.add(idx, m_database.teamAtEventGet(event_id, id));
      seasonScores.add(idx, m_database.teamHasScores(season_id, id));
      eventScores.add(idx, m_database.teamHasEventScores(event_id, id));
    };

    // Loop through the teams.
    JSONArray teams = new SimpleJSONArray();
    for(int i = 0; i < numbers.size(); i++)
    {
      // Add this team to the team array.
      JSONObject team = new SimpleJSONObject();
      team.set("id", ids.get(i));
      team.set("number", numbers.get(i));
      team.set("name", names.get(i));
      team.set("inEvent", inEvent.get(i));
      team.set("seasonScores", seasonScores.get(i));
      team.set("eventScores", eventScores.get(i));
      teams.addEntry(team);
    }

    // Add the team array to the JSON response.
    result.set("teams", teams);
  }

  /**
   * Removes a team from an event.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   */
  private void
  removeFromEvent(JSONObject result, int id)
  {
    // Get the event ID.
    int event_id = m_event.eventIdGet();

    // Remove this team from the event.
    if(m_database.teamAtEventRemove(event_id, id) == true)
    {
      // Return success since the team was removed.
      result.set("result", "ok");
    }

    // Otherwise, an error occurred when removing the team.
    else
    {
      // Return an error since the team coudl not be removed.
      result.set("result",
                 m_webserver.getSSI("str_teams_event_remove_failed"));
    }
  }

  /**
   * Handles requests for /admin/teams/teams.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveTeams(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "add", for adding a team.
      if(paramMap.get("action").equals("add") &&
         paramMap.containsKey("number") && paramMap.containsKey("name"))
      {
        // Add the team.
        add(result, Integer.parseInt(paramMap.get("number")),
            URLDecoder.decode(paramMap.get("name"), StandardCharsets.UTF_8));
      }

      // See if the action is "delete", for deleting a team.
      else if(paramMap.get("action").equals("delete") &&
              paramMap.containsKey("id"))
      {
        // Delete the team.
        delete(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "edit", for editing a team.
      else if(paramMap.get("action").equals("edit") &&
              paramMap.containsKey("id") && paramMap.containsKey("number") &&
              paramMap.containsKey("name"))
      {
        // Edit the team.
        edit(result, Integer.parseInt(paramMap.get("id")),
             Integer.parseInt(paramMap.get("number")),
             URLDecoder.decode(paramMap.get("name"), StandardCharsets.UTF_8));
      }

      // See if the action is "in_event", for adding a team to the current
      // event.
      else if(paramMap.get("action").equals("in_event") &&
              paramMap.containsKey("id"))
      {
        // Add the team to the event.
        addToEvent(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "not_in_event", for removing a team from the
      // current event.
      else if(paramMap.get("action").equals("not_in_event") &&
              paramMap.containsKey("id"))
      {
        // Remove the team from the event.
        removeFromEvent(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "count", for returning the count of teams at the
      // current event.
      else if(paramMap.get("action").equals("count"))
      {
        // Count the teams at the event.
        count(result);
      }

      // See if the action is "list", for listing the teams.
      else if(paramMap.get("action").equals("list"))
      {
        // List the teams.
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
      // List the teams.
      list(result);
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
   * Performs initial setup for the teams handler.
   */
  public void
  setup()
  {
    // Get references to the web server, database, season, and the event
    // objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

   // Register the dynamic handler for the teams.json file.
   m_webserver.registerDynamicFile("/admin/teams/teams.json",
                                   this::serveTeams);
  }
}