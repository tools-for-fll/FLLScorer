// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

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
   * @return Returns the Score singleton.
   */
  public static Scores
  getInstance()
  {
    // Create the Score object if required.
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
   * Handles requests for /teams/teams.json.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveScores(String path, String parameters)
  {
    String action = null, id = null, match = null, id2 = null, match2 = null;
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

        // See if this is the "match" key.
        if(items[0].equals("match"))
        {
          // Convert and save the match for later use.
          match = items[1];
        }

        // See if this is the "id2" key.
        if(items[0].equals("id2"))
        {
          // Save the ID for later use.
          id2 = items[1];
        }

        // See if this is the "match2" key.
        if(items[0].equals("match2"))
        {
          // Convert and save the match for later use.
          match2 = items[1];
        }
      }
    }

    // See if the action was specified and is "delete", for deleting a team's
    // score.
    if("delete".equals(action) && (id != null) && (match != null))
    {
      // Ensure that the match number is valid.
      if((Integer.parseInt(match) >= 1) && (Integer.parseInt(match) <= 4))
      {
        // Delete the score from the database.
        if(m_database.scoreMatchRemove(season_id, event_id,
                                       Integer.parseInt(id),
                                       Integer.parseInt(match)) == true)
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

    // See if the action was specified and is "exchange", for exchanging a
    // team's score.
    else if("exchange".equals(action) && (id != null) && (match != null) &&
            (id2 != null) && (match2 != null))
    {
      ArrayList<Integer> a_score = new ArrayList<Integer>();
      ArrayList<Integer> a_cv = new ArrayList<Integer>();
      ArrayList<String> a_sheet = new ArrayList<String>();

      // Get the first match score.
      if(m_database.scoreMatchGet(season_id, event_id, Integer.parseInt(id),
                                  Integer.parseInt(match),
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
      else if(m_database.scoreMatchGet(season_id, event_id,
                                       Integer.parseInt(id2),
                                       Integer.parseInt(match2),
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
      else if(m_database.scoreMatchAdd(season_id, event_id,
                                       Integer.parseInt(id2),
                                       Integer.parseInt(match2),
                                       a_score.get(0), a_cv.get(0),
                                       a_sheet.get(0)) == -1)
      {
        // Return an error.
        result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
      }

      // Then set the first match score to the second match score.
      else if(m_database.scoreMatchAdd(season_id, event_id,
                                       Integer.parseInt(id),
                                       Integer.parseInt(match),
                                       a_score.get(1), a_cv.get(1),
                                       a_sheet.get(1)) == -1)
      {
        // The first match score could not be updated, so attempt to restore
        // the second match score.  If this fails...there is data loss.
        m_database.scoreMatchAdd(season_id, event_id, Integer.parseInt(id2),
                                 Integer.parseInt(match2), a_score.get(1),
                                 a_cv.get(1), a_sheet.get(1));

        // Return an error.
        result.set("result", m_webserver.getSSI("str_scores_exchange_error"));
      }
      else
      {
        // Return success since the scores were exchanged.
        result.set("result", "ok");
      }
    }

    // Otherwise, return a list of the teams.
    else
    {
      // A list of information about teams, maintained in team number order.
      ArrayList<Integer> a_ids = new ArrayList<Integer>();
      ArrayList<Integer> a_numbers = new ArrayList<Integer>();
      ArrayList<String> a_names = new ArrayList<String>();
      ArrayList<Integer> a_match1 = new ArrayList<Integer>();
      ArrayList<Integer> a_match2 = new ArrayList<Integer>();
      ArrayList<Integer> a_match3 = new ArrayList<Integer>();
      ArrayList<Integer> a_match4 = new ArrayList<Integer>();

      // Enumerate the teams from the database for this season.
      m_database.
        teamEnumerate(season_id,
                      (l_id, l_season_id, l_number, l_name) ->
                      {
                        // Ignore this team if it is not at this event.
                        if(!m_database.teamAtEventGet(event_id, l_id))
                        {
                          return;
                        }

                        // Find the place in the list to insert this team in
                        // number order.
                        int i;
                        for(i = 0; i < a_numbers.size(); i++)
                        {
                          if(l_number < a_numbers.get(i))
                          {
                            break;
                          }
                        }

                        // Add this team to the lists.
                        a_ids.add(i, l_id);
                        a_numbers.add(i, l_number);
                        a_names.add(i, l_name);
                        a_match1.add(i, -1);
                        a_match2.add(i, -1);
                        a_match3.add(i, -1);
                        a_match4.add(i, -1);
                      });

      // Enumerate the scores for this event.
      m_database.
        scoreEnumerate(season_id, event_id,
                       (l_id, l_season_id, l_event_id, team_id, l_match1,
                        match1_cv, match1_sheet, l_match2, match2_cv,
                        match2_sheet, l_match3, match3_cv, match3_sheet,
                        l_match4, match4_cv, match4_sheet) ->
                       {
                         // Ignore this score if it is not for a team at this
                         // event (should not happen).
                         int idx = a_ids.indexOf(team_id);
                         if(idx == -1)
                         {
                           return;
                         }

                         // Save the match 1 score, if it exists.
                         if(l_match1 != null)
                         {
                           a_match1.set(idx, l_match1);
                         }

                         // Save the match 2 score, if it exists.
                         if(l_match2 != null)
                         {
                           a_match2.set(idx, l_match2);
                         }

                         // Save the match 3 score, if it exists.
                         if(l_match3 != null)
                         {
                           a_match3.set(idx, l_match3);
                         }

                         // Save the match 4 score, if it exists.
                         if(l_match4 != null)
                         {
                           a_match4.set(idx, l_match4);
                         }
                       });

      // Loop through the teams.
      JSONArray scores = new SimpleJSONArray();
      for(int i = 0; i < a_numbers.size(); i++)
      {
        // Add this team's scores to the score array.
        JSONObject score = new SimpleJSONObject();
        score.set("id", a_ids.get(i));
        score.set("number", a_numbers.get(i));
        score.set("name", a_names.get(i));
        score.set("match1", (a_match1.get(i) == -1) ? "" : a_match1.get(i));
        score.set("match2", (a_match2.get(i) == -1) ? "" : a_match2.get(i));
        score.set("match3", (a_match3.get(i) == -1) ? "" : a_match3.get(i));
        score.set("match4", (a_match4.get(i) == -1) ? "" : a_match4.get(i));
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
    m_webserver.registerDynamicFile("/scores/scores.json", this::serveScores);
  }
}