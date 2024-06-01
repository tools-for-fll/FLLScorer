// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the teams tab.
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
   * Handles requests for /admin/teams/teams.json.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveTeams(String path, String parameters)
  {
    String action = null, id = null, number = null, name = null;
    JSONObject result = new SimpleJSONObject();
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();
    String[] params, items;

    // See if there are parameters to be parsed.
    if(parameters != null)
    {
      // Split the parameter string into its individual parameters.
      params = parameters.split("&");

      // Loop through the parameters.
      for(var i = 0; i < params.length; i++)
      {
        // Split this parameter into its key/value.
        items = params[i].split("=");

        // See if this is the "action" key.
        if(items[0].equals("action"))
        {
          // Save the action for later use.
          action = items[1];
        }

        // See if this is the "id" key.
        if(items[0].equals("id"))
        {
          // Save the ID for later use.
          id = items[1];
        }

        // See if this is the "number" key.
        if(items[0].equals("number"))
        {
          // Convert and save the number for later use.
          number = URLDecoder.decode(items[1], StandardCharsets.UTF_8);
        }

        // See if this is the "name" key.
        if(items[0].equals("name"))
        {
          // Convert and save the name for later use.
          name = URLDecoder.decode(items[1], StandardCharsets.UTF_8);
        }
      }
    }

    // See if the action was specified and is "add", for adding a team.
    if("add".equals(action) && (number != null) && (name != null))
    {
      // See if there is already a team with the given number.
      if(m_database.teamGet(season_id, Integer.parseInt(number)) >= 0)
      {
        // Return an error since the team already exists.
        result.set("result", m_webserver.getSSI("str_teams_already_exists"));
      }

      // Add the team to the database.
      else if(m_database.teamAdd(season_id, Integer.parseInt(number),
                                 name) >= 0)
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

    // See if the action was specified and is "delete", for deleting a team.
    else if("delete".equals(action) && (id != null))
    {
      // Delete the team from the database.
      m_database.teamRemove(season_id, Integer.parseInt(id));

      // Return success since the team was deleted.
      result.set("result", "ok");
    }

    // See if the action was specified and is "edit", for editing a team.
    else if("edit".equals(action) && (id != null) && (number != null) &&
            (name != null))
    {
      // Edit the team in the database.
      if(m_database.teamEdit(Integer.parseInt(id), number, name) == true)
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

    // See if the action was specified and is "in_event", for adding a team to
    // the current event.
    else if("in_event".equals(action) && (id != null))
    {
      // Add this team to the event.
      if(m_database.teamAtEventSet(season_id, event_id,
                                   Integer.parseInt(id)) == true)
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

    // See if the action was specified and is "not_in_event", for removing a
    // team from the current event.
    else if("not_in_event".equals(action) && (id != null))
    {
      // Remove this team from the event.
      if(m_database.teamAtEventRemove(event_id, Integer.parseInt(id)) == true)
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

    // See if the action was specified and is "count", for returning the count
    // of teams at the current event.
    else if("count".equals(action))
    {
      // A list of the team IDs for the teams at this event.
      ArrayList<Integer> count = new ArrayList<Integer>();

      // Enumerate the teams at this event.
      m_database.teamAtEventEnumerate(season_id, event_id, -1,
                                      (l_season_id, l_event_id, l_team_id) ->
        {
          // Add this team ID to the list.
          count.add(l_team_id);
        });

      // Set the result to the number of teams at this event.
      result.set("count", count.size());
      result.set("result", "ok");
    }

    // Otherwise, return a list of the teams.
    else
    {
      // A list of information about teams, maintained in team number order.
      ArrayList<Integer> ids = new ArrayList<Integer>();
      ArrayList<Integer> numbers = new ArrayList<Integer>();
      ArrayList<String> names = new ArrayList<String>();
      ArrayList<Boolean> inEvent = new ArrayList<Boolean>();
      ArrayList<Boolean> seasonScores = new ArrayList<Boolean>();
      ArrayList<Boolean> eventScores = new ArrayList<Boolean>();

      // Enumerate the teams from the database for this season.
      m_database.teamEnumerate(season_id,
                               (l_id, l_season_id, l_number, l_name) ->
        {
          // Find the place in the list to insert this team in number order.
          int i;
          for(i = 0; i < numbers.size(); i++)
          {
            if(l_number < numbers.get(i))
            {
              break;
            }
          }

          // Add this team to the lists.
          ids.add(i, l_id);
          numbers.add(i, l_number);
          names.add(i, l_name);
          inEvent.add(i, m_database.teamAtEventGet(event_id, l_id));
          seasonScores.add(i, m_database.teamHasScores(season_id, l_id));
          eventScores.add(i, m_database.teamHasEventScores(event_id, l_id));
        });

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