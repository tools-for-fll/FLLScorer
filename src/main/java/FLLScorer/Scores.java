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
 * Handles the scores tab.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Scores
{
  /**
   * The object for the Scores singleton.
   */
  private static Scores m_instance = null;

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
   * Gets the Scores singleton object, creating it if necessary.
   *
   * @return Returns the Scores singleton.
   */
  public static Scores
  getInstance()
  {
    // Create the Scores object if required.
    if(m_instance == null)
    {
        m_instance = new Scores();
    }

    // Return the Scores object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Scores()
  {
  }

  /**
   * Deletes a teams score.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the team.
   *
   * @param match The match number.
   */
  private void
  delete(JSONObject result, int id, int match)
  {
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Ensure that the match number is valid.
    if((match >= 1) && (match <= 4))
    {
      // Delete the score from the database.
      if(m_database.scoreMatchRemove(season_id, event_id, id, match) == true)
      {
        // Return successs since the match score was deleted.
        result.set("result", "ok");
      }
      else
      {
        // Return an error since the match score could not be delete.
        result.set("result", m_webserver.getSSI("str_scores_delete_error"));
      }
    }
    else
    {
      // Return an error since the match number is not valid.
      result.set("result", m_webserver.getSSI("str_scores_unknown_match"));
    }
  }

  /**
   * Exchanges scores between two teams/matches.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the first team.
   *
   * @param match The match of the first team.
   *
   * @param id2 The ID of the second team.
   *
   * @param match2 The match of the second team.
   */
  private void
  exchange(JSONObject result, int id, int match, int id2, int match2)
  {
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    ArrayList<Integer> a_score = new ArrayList<Integer>();
    ArrayList<Integer> a_cv = new ArrayList<Integer>();
    ArrayList<String> a_sheet = new ArrayList<String>();

    // Get the first match score.
    if(m_database.scoreMatchGet(season_id, event_id, id, match,
                                (score, cv, sheet) ->
                                {
                                  a_score.add(score);
                                  a_cv.add(cv);
                                  a_sheet.add(sheet);
                                }) == false)
    {
      // Return an error.
      result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
    }

    // Then get the second match score.
    else if(m_database.scoreMatchGet(season_id, event_id, id2, match2,
                                     (score, cv, sheet) ->
                                     {
                                       a_score.add(score);
                                       a_cv.add(cv);
                                       a_sheet.add(sheet);
                                     }) == false)
    {
      // Return an error.
      result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
    }

    // Then set the second match score to the first match score.
    else if(m_database.scoreMatchAdd(season_id, event_id, id2, match2,
                                     a_score.get(0), a_cv.get(0),
                                     a_sheet.get(0)) == -1)
    {
      // Return an error.
      result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
    }

    // Then set the first match score to the second match score.
    else if(m_database.scoreMatchAdd(season_id, event_id, id, match,
                                     a_score.get(1), a_cv.get(1),
                                     a_sheet.get(1)) == -1)
    {
      // The first match score could not be updated, so attempt to restore
      // the second match score.  If this fails...there is data loss.
      m_database.scoreMatchAdd(season_id, event_id, id2, match2,
                               a_score.get(1), a_cv.get(1), a_sheet.get(1));

      // Return an error.
      result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
    }
    else
    {
      // Return success since the scores were exchanged.
      result.set("result", "ok");
    }
  }

  /**
   * Lists the scores of the teams.
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
    ArrayList<Integer> match1 = new ArrayList<Integer>();
    ArrayList<Integer> match2 = new ArrayList<Integer>();
    ArrayList<Integer> match3 = new ArrayList<Integer>();
    ArrayList<Integer> match4 = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, event_id, ids, numbers, names);

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < numbers.size(); idx++)
    {
      match1.add(idx, -1);
      match2.add(idx, -1);
      match3.add(idx, -1);
      match4.add(idx, -1);
    };

    // A list of information about the scores.
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Integer> score1 = new ArrayList<Integer>();
    ArrayList<Integer> score2 = new ArrayList<Integer>();
    ArrayList<Integer> score3 = new ArrayList<Integer>();
    ArrayList<Integer> score4 = new ArrayList<Integer>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, teams, score1, null,
                              null, score2, null, null, score3, null, null,
                              score4, null, null);

    // Loop through all the scores
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Ignore this score if it is not for a team at this event (should not
      // happen).
      int team_idx = ids.indexOf(teams.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // Save the match 1 score, if it exists.
      if(score1.get(idx) != null)
      {
        match1.set(team_idx, score1.get(idx));
      }

      // Save the match 2 score, if it exists.
      if(score2.get(idx) != null)
      {
        match2.set(team_idx, score2.get(idx));
      }

      // Save the match 3 score, if it exists.
      if(score3.get(idx) != null)
      {
        match3.set(team_idx, score3.get(idx));
      }

      // Save the match 4 score, if it exists.
      if(score4.get(idx) != null)
      {
        match4.set(team_idx, score4.get(idx));
      }
    }

    // Loop through the teams.
    JSONArray scores = new SimpleJSONArray();
    for(int i = 0; i < numbers.size(); i++)
    {
      // Add this team's scores to the score array.
      JSONObject score = new SimpleJSONObject();
      score.set("id", ids.get(i));
      score.set("number", numbers.get(i));
      score.set("name", names.get(i));
      score.set("match1", (match1.get(i) == -1) ? "" : match1.get(i));
      score.set("match2", (match2.get(i) == -1) ? "" : match2.get(i));
      score.set("match3", (match3.get(i) == -1) ? "" : match3.get(i));
      score.set("match4", (match4.get(i) == -1) ? "" : match4.get(i));
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
   * Handles requests for /admin/scores/scores.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveScoresJSON(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "delete", for deleting a team's score.
      if(paramMap.get("action").equals("delete") &&
         paramMap.containsKey("id") && paramMap.containsKey("match"))
      {
        // Delete this score.
        delete(result, Integer.parseInt(paramMap.get("id")),
               Integer.parseInt(paramMap.get("match")));
      }

      // See if the action is "exchange", for exchanging a team's score.
      else if(paramMap.get("action").equals("exchange") &&
              paramMap.containsKey("id") && paramMap.containsKey("match") &&
              paramMap.containsKey("id2") && paramMap.containsKey("match2"))
      {
        // Exchange the scores.
        exchange(result, Integer.parseInt(paramMap.get("id")),
                 Integer.parseInt(paramMap.get("match")),
                 Integer.parseInt(paramMap.get("id2")),
                 Integer.parseInt(paramMap.get("match2")));
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
      String json = JSONParser.serialize(result);
      return(json.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Handles requests for /admin/scores/scores.csv.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveScoresCSV(String path, HashMap<String, String> paramMap)
  {
    String csv = new String();
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();
    int matches;

    // Get the number of matches at this event.
    if(event_id != -1)
    {
      matches = m_database.eventGetMatches(event_id);
    }
    else
    {
      matches = 3;
    }

    // A list of information about teams, maintained in team number order.
    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> match1 = new ArrayList<Integer>();
    ArrayList<Integer> cv1 = new ArrayList<Integer>();
    ArrayList<Integer> match2 = new ArrayList<Integer>();
    ArrayList<Integer> cv2 = new ArrayList<Integer>();
    ArrayList<Integer> match3 = new ArrayList<Integer>();
    ArrayList<Integer> cv3 = new ArrayList<Integer>();
    ArrayList<Integer> match4 = new ArrayList<Integer>();
    ArrayList<Integer> cv4 = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, event_id, ids, numbers, names);

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < numbers.size(); idx++)
    {
      match1.add(idx, -1);
      cv1.add(idx, -1);
      match2.add(idx, -1);
      cv2.add(idx, -1);
      match3.add(idx, -1);
      cv3.add(idx, -1);
      match4.add(idx, -1);
      cv4.add(idx, -1);
    };

    // A list of information about the scores.
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Integer> score1 = new ArrayList<Integer>();
    ArrayList<Integer> core1 = new ArrayList<Integer>();
    ArrayList<Integer> score2 = new ArrayList<Integer>();
    ArrayList<Integer> core2 = new ArrayList<Integer>();
    ArrayList<Integer> score3 = new ArrayList<Integer>();
    ArrayList<Integer> core3 = new ArrayList<Integer>();
    ArrayList<Integer> score4 = new ArrayList<Integer>();
    ArrayList<Integer> core4 = new ArrayList<Integer>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, teams, score1, core1,
                              null, score2, core2, null, score3, core3, null,
                              score4, core4, null);

    // Loop through all the scores
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Ignore this score if it is not for a team at this event (should not
      // happen).
      int team_idx = ids.indexOf(teams.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // Save the match 1 score, if it exists.
      if(score1.get(idx) != null)
      {
        match1.set(team_idx, score1.get(idx));
      }
      if(core1.get(idx) != null)
      {
        cv1.set(team_idx, core1.get(idx));
      }

      // Save the match 2 score, if it exists.
      if(score2.get(idx) != null)
      {
        match2.set(team_idx, score2.get(idx));
      }
      if(core2.get(idx) != null)
      {
        cv2.set(team_idx, core2.get(idx));
      }

      // Save the match 3 score, if it exists.
      if(score3.get(idx) != null)
      {
        match3.set(team_idx, score3.get(idx));
      }
      if(core3.get(idx) != null)
      {
        cv3.set(team_idx, core3.get(idx));
      }

      // Save the match 4 score, if it exists.
      if(score4.get(idx) != null)
      {
        match4.set(team_idx, score4.get(idx));
      }
      if(core4.get(idx) != null)
      {
        cv4.set(team_idx, core4.get(idx));
      }
    }

    // Add the header to the CSV string.
    if(matches == 3)
    {
      csv += "number,name,match1,match2,match3,cv1,cv2,cv3\n";
    }
    else
    {
      csv += "number,name,match1,match2,match3,match4,cv1,cv2,cv3,cv4\n";
    }

    // Loop through the teams.
    for(int i = 0; i < numbers.size(); i++)
    {
      // Add this team's scores to the CSV string.
      csv += numbers.get(i);
      csv += "," + names.get(i);
      csv += "," + ((match1.get(i) == -1) ? "" : match1.get(i));
      csv += "," + ((match2.get(i) == -1) ? "" : match2.get(i));
      csv += "," + ((match3.get(i) == -1) ? "" : match3.get(i));
      if(matches == 4)
      {
        csv += "," + ((match4.get(i) == -1) ? "" : match4.get(i));
      }
      csv += "," + ((cv1.get(i) == -1) ? "" : cv1.get(i));
      csv += "," + ((cv2.get(i) == -1) ? "" : cv2.get(i));
      csv += "," + ((cv3.get(i) == -1) ? "" : cv3.get(i));
      if(matches == 4)
      {
        csv += "," + ((cv4.get(i) == -1) ? "" : cv4.get(i));
      }
      csv += "\n";
    }

    // Convert the response into a byte array and return it.
    return(csv.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Performs initial setup for the scores handler.
   */
  public void
  setup()
  {
    // Get references to the web server, database, seasons, and events objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

    // Register the dynamic handler for the scores.json file.
    m_webserver.registerDynamicFile("/admin/scores/scores.json",
                                    this::serveScoresJSON);
    m_webserver.registerDynamicFile("/admin/scores/scores.csv",
                                    this::serveScoresCSV);
  }
}