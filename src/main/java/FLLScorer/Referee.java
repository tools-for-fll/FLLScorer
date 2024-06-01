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
 * Handles the referee page.
 */
public class Referee
{
  /**
   * The object for the Referee singleton.
   */
  private static Referee m_instance = null;

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
   * Gets the Referee singleton object, creating it if necessary.
   *
   * @return Returns the Referee singleton.
   */
  public static Referee
  getInstance()
  {
    // Create the Scoreboard object if required.
    if(m_instance == null)
    {
      m_instance = new Referee();
    }

    // Return the Referee object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Referee()
  {
  }

  /**
   * Gets a score sheet.
   *
   * @param result The JSON object into which the score sheet is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   */
  private void
  get(JSONObject result, String id, String match)
  {
  }

  /**
   * Lists the scores.
   *
   * @param result The JSON object into which the scores are placed.
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
    ArrayList<Integer> match1 = new ArrayList<Integer>();
    ArrayList<Integer> match2 = new ArrayList<Integer>();
    ArrayList<Integer> match3 = new ArrayList<Integer>();
    ArrayList<Integer> match4 = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id,
                             (l_id, l_season_id, l_number, l_name) ->
      {
        // Ignore this team if it is not at this event.
        if(!m_database.teamAtEventGet(event_id, l_id))
        {
          return;
        }

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
        match1.add(i, 0);
        match2.add(i, 0);
        match3.add(i, 0);
        match4.add(i, 0);
      });

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id,
                              (l_id, l_season_id, l_event_id, team_id,
                               l_match1, match1_cv, match1_sheet, l_match2,
                               match2_cv, match2_sheet, l_match3, match3_cv,
                               match3_sheet, l_match4, match4_cv,
                               match4_sheet) ->
      {
        // Ignore this score if it is not for a team at this event (should not
        // happen).
        int idx = ids.indexOf(team_id);
        if(idx == -1)
        {
          return;
        }

        // Indicate if there is a match 1 score.
        if(l_match1 != null)
        {
          match1.set(idx, 2);
        }
        else if(match1_sheet != null)
        {
          match1.set(idx, 1);
        }

        // Indicate if there is a match 2 score.
        if(l_match2 != null)
        {
          match2.set(idx, 2);
        }
        else if(match2_sheet != null)
        {
          match2.set(idx, 1);
        }

        // Indicate if there is a match 3 score.
        if(l_match3 != null)
        {
          match3.set(idx, 2);
        }
        else if(match3_sheet != null)
        {
          match3.set(idx, 1);
        }

        // Indicate if there is a match 4 score.
        if(l_match4 != null)
        {
          match4.set(idx, 2);
        }
        else if(match4_sheet != null)
        {
          match4.set(idx, 1);
        }
      });

    // Loop through the teams.
    JSONArray scores = new SimpleJSONArray();
    for(int i = 0; i < numbers.size(); i++)
    {
      // Add this team's scores to the score array.
      JSONObject score = new SimpleJSONObject();
      score.set("id", ids.get(i));
      score.set("number", numbers.get(i));
      score.set("name", names.get(i));
      score.set("match1", match1.get(i));
      score.set("match2", match2.get(i));
      score.set("match3", match3.get(i));
      score.set("match4", match4.get(i));
      scores.addEntry(score);
    }

    // Add the scores array to the JSON response.
    result.set("scores", scores);

    // Add the number of matches to the JSON response.
    if(event_id != -1)
    {
      result.set("matches", m_database.eventGetMatches(event_id));
    }
    else
    {
      result.set("matches", 3);
    }
  }

  /**
   * Saves the score sheet, without scoring it.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   *
   * @param json The JSON representation of the score sheet selections.
   */
  private void
  save(JSONObject result, String id, String match, String json)
  {
  }

  /**
   * Scores a score sheet.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param json The JSON representation of the score sheet selections.
   */
  private void
  score(JSONObject result, String json)
  {
  }

  /**
   * Submits a score sheet, scoring and then saving it.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   *
   * @param json The JSON representation of the score sheet selections.
   */
  private void
  submit(JSONObject result, String id, String match, String json)
  {
  }

  /**
   * Serves the JSON data for the current state of the referees.
   *
   * @param path The path that was requested.
   *
   * @param parameters The parameters associated with the request.
   *
   * @return A byte array containing the JSON data for the referees.
   */
  private byte[]
  serveRefereeJson(String path, String parameters)
  {
    String action = null, id = null, match = null, json = null;
    JSONObject result = new SimpleJSONObject();
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

        // See if this is the "match" key.
        if(items[0].equals("match"))
        {
          // Save the match for later use.
          match = items[1];
        }

        // See if this is the "json" key.
        if(items[0].equals("json"))
        {
          // Convert and save the json for later use.
          json = URLDecoder.decode(items[1], StandardCharsets.UTF_8);
        }
      }
    }

    // See if the action was specified and is "get", for getting a score sheet.
    if("get".equals(action) && (id != null) && (match != null))
    {
      // Get the score sheet.
      get(result, id, match);
    }

    // See if the action was specified and is "save", for saving but not
    // scoring a score sheet.
    else if("save".equals(action) && (id != null) && (match != null) &&
            (json != null))
    {
      // Save the score sheet.
      save(result, id, match, json);
    }

    // See if the action was specified and is "score", for getting the score
    // for a score sheet.
    else if("score".equals(action) && (json != null))
    {
      // Score the score sheet.
      score(result, json);
    }

    // See if the action was specified and is "submit", for scoring and
    // saving a score sheet.
    else if("submit".equals(action) && (id != null) && (match != null) &&
            (json != null))
    {
      // Submit the score sheet.
      submit(result, id, match, json);
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
   * Performs initial setup for the referee page.
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

    // Register the dynamic handler for the referee.json file.
    m_webserver.registerDynamicFile("/referee/referee.json",
                                    this::serveRefereeJson);
  }
}