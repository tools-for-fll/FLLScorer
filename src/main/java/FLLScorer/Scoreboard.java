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
   * The Config object.
   */
  private Config m_config = null;

  /**
   * The WebServer object.
   */
  private WebServer m_webserver = null;

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
   * @param ad The division of the first team.
   *
   * @param a1 The match 1 score for the first team.
   *
   * @param a2 The match 2 scores for the first team.
   *
   * @param a3 The match 3 score for the first team.
   *
   * @param a4 The match 4 score for the first team.
   *
   * @param bd The division of the second team.
   *
   * @param b1 The match 1 score for the second team.
   *
   * @param b2 The match 2 score for the second team.
   *
   * @param b3 The match 3 score for the second team.
   *
   * @param b4 The match 4 score for the second team.
   *
   * The divisions are used as a first order sort; lower number divisions
   * always "place" higher than higher number divisions (grouping the teams in
   * a division together).
   *
   * @return 1 if the first team places higher than the second team, -1 if the
   *         second team places higher than the first team, and 0 if the teams
   *         are tied.
   */
  private int
  scoresCompare(int ad, float a1, float a2, float a3, float a4, int bd,
                float b1, float b2, float b3, float b4)
  {
    // See if the second team "places" higher than the first team, based on
    // their divisions.
    if(ad > bd)
    {
      return(-1);
    }

    // See if the first team "places" higher than the second team, based on
    // their diviison.
    if(ad < bd)
    {
      return(1);
    }

    // Convert the scores into integer arrays.
    Float a[] = { a1, a2, a3, a4 };
    Float b[] = { b1, b2, b3, b4 };

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

      // See if the first team places higher than the seocnd team, based on
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
    ArrayList<Integer> a_ids = new ArrayList<Integer>();
    ArrayList<Integer> a_place = new ArrayList<Integer>();
    ArrayList<Integer> a_numbers = new ArrayList<Integer>();
    ArrayList<String> a_names = new ArrayList<String>();
    ArrayList<Integer> a_divisions = new ArrayList<Integer>();
    ArrayList<Float> a_high = new ArrayList<Float>();
    ArrayList<Float> a_match0 = new ArrayList<Float>();
    ArrayList<Float> a_match1 = new ArrayList<Float>();
    ArrayList<Float> a_match2 = new ArrayList<Float>();
    ArrayList<Float> a_match3 = new ArrayList<Float>();
    ArrayList<Float> a_match4 = new ArrayList<Float>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, event_id, a_ids, a_numbers, a_names,
                             a_divisions);

    // If divisions are not enabled, force all the teams to the same division
    // (in case they have different divisions in the database).
    if(!m_config.divisionEnableGet())
    {
      for(int idx = 0; idx < a_divisions.size(); idx++)
      {
        a_divisions.set(idx, 0);
      }
    }

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < a_numbers.size(); idx++)
    {
      a_match0.add(idx, (float)-100);
      a_match1.add(idx, (float)-100);
      a_match2.add(idx, (float)-100);
      a_match3.add(idx, (float)-100);
      a_match4.add(idx, (float)-100);
      a_high.add(idx, (float)-100);
    }

    // A list of information about the scores.
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Float> score0 = new ArrayList<Float>();
    ArrayList<Float> score1 = new ArrayList<Float>();
    ArrayList<Float> score2 = new ArrayList<Float>();
    ArrayList<Float> score3 = new ArrayList<Float>();
    ArrayList<Float> score4 = new ArrayList<Float>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, teams, score0, null,
                              null, score1, null, null, score2, null, null,
                              score3, null, null, score4, null, null);

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

      // Save the match 0 score, if it exists.
      if(score0.get(idx) != null)
      {
        a_match0.set(team_idx, score0.get(idx));
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

    // Determine which scores to consider.
    boolean bScore3 = (matches == 3) || (matches == 103) || (matches == 4);
    boolean bScore4 = (matches == 4);

    // Loop through all the teams to find their high score.
    for(int idx = 0; idx < a_ids.size(); idx++)
    {
      // Get the high score.
      float high = a_match1.get(idx);
      high = (a_match2.get(idx) > high) ? a_match2.get(idx) : high;
      if(bScore3)
      {
        high = (a_match3.get(idx) > high) ? a_match3.get(idx) : high;
      }
      if(bScore4)
      {
        high = (a_match4.get(idx) > high) ? a_match4.get(idx) : high;
      }
      a_high.set(idx, high);
    }

    // Loop through all the teams, sorting them into placement order (with
    // equally-placed teams and teams without any scores remaining in team
    // number order).
    for(int i = 0; i < a_ids.size(); i++)
    {
      // Loop through all the preceeding teams.
      for(int j = 0; j < i; j++)
      {
        // See if this preceeding team has a score, and it has a lower score
        // than the current team.
        if((a_high.get(j) == -1) ||
           (scoresCompare(a_divisions.get(i), a_match1.get(i), a_match2.get(i),
                          bScore3 ? a_match3.get(i) : 0,
                          bScore4 ? a_match4.get(i) : 0, a_divisions.get(j),
                          a_match1.get(j), a_match2.get(j),
                          bScore3 ? a_match3.get(j) : 0,
                          bScore4 ? a_match4.get(j) : 0) > 0))
        {
          // Remove the current team from it's location in the arrays and then
          // insert it back into the position where the preceeding team is
          // located.
          a_ids.add(j, a_ids.remove(i));
          a_numbers.add(j, a_numbers.remove(i));
          a_names.add(j, a_names.remove(i));
          a_divisions.add(j, a_divisions.remove(i));
          a_high.add(j, a_high.remove(i));
          a_match0.add(j, a_match0.remove(i));
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
    int place = 1;
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
              (scoresCompare(a_divisions.get(i), a_match1.get(i),
                             a_match2.get(i), bScore3 ? a_match3.get(i) : 0,
                             bScore4 ? a_match4.get(i) : 0,
                             a_divisions.get(i - 1), a_match1.get(i - 1),
                             a_match2.get(i - 1),
                             bScore3 ? a_match3.get(i - 1) : 0,
                             bScore4 ? a_match4.get(i - 1) : 0) != 0))
      {
        // See if there is a change in division.
        if((i != 0) && (a_divisions.get(i) != a_divisions.get(i - 1)))
        {
          // Reset the place back to one with the change in division.
          place = 1;
        }

        // Assign the next place to this team.
        a_place.add(i, place);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        a_place.add(i, a_place.get(i - 1));
      }

      // Increment the place.
      place++;
    }

    // Loop through the teams.
    JSONArray scores = new SimpleJSONArray();
    JSONArray divScores =
      m_config.divisionEnableGet() ? new SimpleJSONArray() : null;
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
      float points = a_high.get(i);
      if(points != -100)
      {
        score.set("high", (points < 0) ? 0 : points);
      }
      points = a_match0.get(i);
      if(points != -100)
      {
        score.set("m0", (points < 0) ? 0 : points);
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
      if(divScores != null)
      {
        if((i != 0) && (a_divisions.get(i) != a_divisions.get(i - 1)))
        {
          scores.addEntry(divScores);
          divScores = new SimpleJSONArray();
        }

        divScores.addEntry(score);
      }
      else
      {
        scores.addEntry(score);
      }
    }

    // Add the scores array to the JSON response.
    if(divScores != null)
    {
      scores.addEntry(divScores);
    }
    result.set("scores", scores);

    // See if divisions are enabled.
    if(m_config.divisionEnableGet())
    {
      JSONArray divisions = new SimpleJSONArray();
      JSONArray colors = new SimpleJSONArray();

      // Loop through the divisions.
      for(int idx = 1; idx <= m_config.divisionCountGet(); idx++)
      {
        // Get the name and color for this division.
        divisions.addEntry(m_config.divisionNameGet(idx));
        colors.addEntry(m_config.divisionColorGet(idx));
      }

      // Add the division information to the JSON response.
      result.set("divisions", divisions);
      result.set("colors", colors);
    }
    else
    {
      // Add the accent color to the JSON response.
      result.set("color", m_webserver.getSSI("accent-color"));
    }

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
    m_config = Config.getInstance();
    m_webserver = WebServer.getInstance();
    m_season = Seasons.getInstance();
    m_event = Events.getInstance();

    // Register the dynamic handler for the scoreboard.json file.
    WebServer.getInstance().registerDynamicFile("/scoreboard/scoreboard.json",
                                                this::serveScoreJson);
  }
}