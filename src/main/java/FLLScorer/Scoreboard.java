// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the scoreboard.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Scoreboard
{
  /**
   * The object for the Scoreboard singleton.
   */
  private static Scoreboard m_instance = null;

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
   * Gets the Scoreboard singleton object, creating it if necessary.
   *
   * @return Returns the Scoreboard singleton.
   */
  public static Scoreboard
  getInstance()
  {
    // Create the Scoreboard object if required.
    if(m_instance == null)
    {
      m_instance = new Scoreboard();
    }

    // Return the Scoreboard object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Scoreboard()
  {
  }

  /**
   * Compares two sets of team scores to determine the placement order.
   *
   * @param a1 The match 1 score for the first team.
   *
   * @param a2 The match 2 scores for the first team.
   *
   * @param a3 The match 3 score for the first team.
   *
   * @param a4 The match 4 score for the first team.
   *
   * @param b1 The match 1 score for the second team.
   *
   * @param b2 The match 2 score for the second team.
   *
   * @param b3 The match 3 score for the second team.
   *
   * @param b4 The match 4 score for the second team.
   *
   * @return 1 if the first team places higher than the second team, -1 if the
   *         second team places higher than the first team, and 0 if the teams
   *         are tied.
   */
  private int
  scoresCompare(int a1, int a2, int a3, int a4, int b1, int b2, int b3, int b4)
  {
    // Convert the scores into integer arrays.
    Integer a[] = { a1, a2, a3, a4 };
    Integer b[] = { b1, b2, b3, b4 };

    // Sort the array in descrending order.
    Arrays.sort(a, Collections.reverseOrder());
    Arrays.sort(b, Collections.reverseOrder());

    // Loop through the four scores.
    for(int idx = 0; idx < 4; idx++)
    {
      // See if the second team places higher than the first team, based on
      // this score.
      if(a[idx] < b[idx])
      {
        return(-1);
      }

      // See if hte first team places higher than the seocnd team, based on
      // this score.
      if(a[idx] > b[idx])
      {
        return(1);
      }
    }

    // All of the scores match, so the teams are tied.
    return(0);
  }

  /**
   * Serves the JSON data for the current state of the scoreboard.
   *
   * @param path The path that was requested.
   *
   * @param paramMap The parameters associated with the request.
   *
   * @return A byte array containing the JSON data for the scoreboard.
   */
  private byte[]
  serveScoreJson(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // A list of information about teams, maintained in team number order.
    ArrayList<Integer> a_ids = new ArrayList<Integer>();
    ArrayList<Integer> a_place = new ArrayList<Integer>();
    ArrayList<Integer> a_numbers = new ArrayList<Integer>();
    ArrayList<String> a_names = new ArrayList<String>();
    ArrayList<Integer> a_high = new ArrayList<Integer>();
    ArrayList<Integer> a_match1 = new ArrayList<Integer>();
    ArrayList<Integer> a_match2 = new ArrayList<Integer>();
    ArrayList<Integer> a_match3 = new ArrayList<Integer>();
    ArrayList<Integer> a_match4 = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, event_id, a_ids, a_numbers, a_names);

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < a_numbers.size(); idx++)
    {
      a_match1.add(idx, -100);
      a_match2.add(idx, -100);
      a_match3.add(idx, -100);
      a_match4.add(idx, -100);
      a_high.add(idx, -100);
    }

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
      int team_idx = a_ids.indexOf(teams.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // Save the match 1 score, if it exists.
      if(score1.get(idx) != null)
      {
        a_match1.set(team_idx, score1.get(idx));
      }

      // Save the match 2 score, if it exists.
      if(score2.get(idx) != null)
      {
        a_match2.set(team_idx, score2.get(idx));
      }

      // Save the match 3 score, if it exists.
      if(score3.get(idx) != null)
      {
        a_match3.set(team_idx, score3.get(idx));
      }

      // Save the match 4 score, if it exists.
      if(score4.get(idx) != null)
      {
        a_match4.set(team_idx, score4.get(idx));
      }
    }

    // Loop through all the teams to find their high score.
    for(int idx = 0; idx < a_ids.size(); idx++)
    {
      // Get the high score.
      int high = a_match1.get(idx);
      high = (a_match2.get(idx) > high) ? a_match2.get(idx) : high;
      high = (a_match3.get(idx) > high) ? a_match3.get(idx) : high;
      high = (a_match4.get(idx) > high) ? a_match4.get(idx) : high;
      a_high.set(idx, high);
    }

    // Loop through all the teams, sorting them into placement order (with
    // equally-placed teams and teams without any scores remaining in team
    // number order).
    for(int i = 0; i < a_ids.size(); i++)
    {
      // This team does not need to get moved if it does not have any scores.
      if(a_high.get(i) == -100)
      {
        continue;
      }

      // Loop through all the preceeding teams.
      for(int j = 0; j < i; j++)
      {
        // See if this preceeding team has a score, and it has a lower score
        // than the current team.
        if((a_high.get(j) == -1) ||
           (scoresCompare(a_match1.get(i), a_match2.get(i), a_match3.get(i),
                          a_match4.get(i), a_match1.get(j), a_match2.get(j),
                          a_match3.get(j), a_match4.get(j)) > 0))
        {
          // Remove the current team from it's location in the arrays and then
          // insert it back into the position where the preceeding team is
          // located.
          a_ids.add(j, a_ids.remove(i));
          a_numbers.add(j, a_numbers.remove(i));
          a_names.add(j, a_names.remove(i));
          a_high.add(j, a_high.remove(i));
          a_match1.add(j, a_match1.remove(i));
          a_match2.add(j, a_match2.remove(i));
          a_match3.add(j, a_match3.remove(i));
          a_match4.add(j, a_match4.remove(i));

          // This team has been placed in the right place, so no further
          // preceeding teams need to be examined.
          break;
        }
      }
    }

    // Loop through all the teams, assigning places (with equally-placed teams
    // sharing the highest place).
    for(int i = 0; i < a_ids.size(); i++)
    {
      // Do not assign a place if this team has no scores.
      if(a_high.get(i) == -100)
      {
        a_place.add(i, -1);
      }

      // Otherwise, if this is the first team or this team does not have the
      // same placement as the preceding team, give it a placement based on the
      // loop index.
      else if((i == 0) ||
              (scoresCompare(a_match1.get(i), a_match2.get(i), a_match3.get(i),
                             a_match4.get(i), a_match1.get(i - 1),
                             a_match2.get(i - 1), a_match3.get(i - 1),
                             a_match4.get(i - 1)) != 0))
      {
        a_place.add(i, i + 1);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        a_place.add(i, a_place.get(i - 1));
      }
    }

    // Loop through the teams.
    JSONArray scores = new SimpleJSONArray();
    for(int i = 0; i < a_ids.size(); i++)
    {
      // Add this team's scores to the score array.
      JSONObject score = new SimpleJSONObject();
      if(a_place.get(i) != -1)
      {
        score.set("place", a_place.get(i));
      }
      score.set("num", a_numbers.get(i));
      score.set("name", a_names.get(i));
      int points = a_high.get(i);
      if(points != -100)
      {
        score.set("high", (points < 0) ? 0 : points);
      }
      points = a_match1.get(i);
      if(points != -100)
      {
        score.set("m1", (points < 0) ? 0 : points);
      }
      points = a_match2.get(i);
      if(points != -100)
      {
        score.set("m2", (points < 0) ? 0 : points);
      }
      points = a_match3.get(i);
      if(points != -100)
      {
        score.set("m3", (points < 0) ? 0 : points);
      }
      points = a_match4.get(i);
      if(points != -100)
      {
        score.set("m4", (points < 0) ? 0 : points);
      }
      scores.addEntry(score);
    }

    // Add the scores array to the JSON response.
    result.set("scores", scores);

    // Add the number of matches to the JSON response.
    if(event_id != -1)
    {
      result.set("event", m_database.eventGetName(event_id));
      result.set("matches", m_database.eventGetMatches(event_id));
    }
    else
    {
      result.set("event", "");
      result.set("matches", 3);
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
   * Performs initial setup for the scoreboard.
   */
  public void
  setup()
  {
    // Get references to the web server, database, season, and the event
    // objects.
    m_database = Database.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

    // Register the dynamic handler for the scoreboard.json file.
    WebServer.getInstance().registerDynamicFile("/scoreboard/scoreboard.json",
                                                this::serveScoreJson);
  }
}