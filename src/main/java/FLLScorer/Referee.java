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
import java.util.concurrent.TimeUnit;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;

/**
 * Handles the referee page.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
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
   * The JSON scoresheet for the current season.
   */
  private JSONObject m_scoresheet = null;

  /**
   * The season for the currently loaded JSON scoresheet.
   */
  private String m_scoresheetSeason = null;

  /**
   * The time of the last score update; used to determine when to push changes
   * to the active referees.
   */
  private long m_lastUpdate = 0;

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
   * Loads the scoresheet for the current season.
   */
  private void
  loadScoresheet()
  {
    String fragment;
    InputStream in;

    // Get the season.
    String season = m_config.seasonGet();

    // See if the scoresheet for this season is already loaded.
    if(season.equals(m_scoresheetSeason))
    {
      return;
    }

    // Extract the year and scoresheet number from the season.
    String year = season.substring(0, 4);
    String number = season.substring(5);
    if(number.equals("0"))
    {
      number = "";
    }

    // See if there is an information file for this season.
    in = ResourceStream.getResourceStream("seasons/" + year + "/scoresheet" +
                                          number + ".json");
    if(in == null)
    {
      return;
    }

    // Read and parse the contents of the information JSON.
    try
    {
      fragment = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      m_scoresheet = JSONParser.deserializeObject(fragment);
      m_scoresheetSeason = season;
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
    }
  }

  /**
   * Gets a scoresheet.
   *
   * @param result The JSON object into which the scoresheet is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   */
  private void
  get(JSONObject result, int id, int match)
  {
    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Load the current season's scoresheet if necessary.
    loadScoresheet();

    // Add the current locale to the JSON response.
    result.set("locale", m_config.localeGet());

    // Add the season's scoresheet to the JSON response.
    result.set("scoresheet", m_scoresheet);

    // Add information about the team to the JSON response.
    result.set("id", id);
    result.set("number", m_database.teamNumberGet(season_id, id));
    result.set("name", m_database.teamNameGet(season_id, id));
    result.set("match", match);

    // See if there is a score or scoresheet for this team/match.
    m_database.scoreMatchGet(season_id, event_id, id, match,
                             (score, cv, sheet) ->
      {
        // Add the score to the JSON response, if it exists.
        if(score != null)
        {
          if(score.intValue() < 0)
          {
            result.set("score", 0);
          }
          else
          {
            result.set("score", score.intValue());
          }
        }

        // Add the scoresheet to the JSON response, if it exists.
        if(sheet != null)
        {
         result.set("sheet", sheet);
        }
      });

    // Success.
    result.set("result", "ok");
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
    m_database.teamEnumerate(season_id, event_id, ids, numbers, names);

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < numbers.size(); idx++)
    {
      match1.add(idx, 0);
      match2.add(idx, 0);
      match3.add(idx, 0);
      match4.add(idx, 0);
    }

    // A list of information about scores.
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Integer> score1 = new ArrayList<Integer>();
    ArrayList<String> match1_sheet = new ArrayList<String>();
    ArrayList<Integer> score2 = new ArrayList<Integer>();
    ArrayList<String> match2_sheet = new ArrayList<String>();
    ArrayList<Integer> score3 = new ArrayList<Integer>();
    ArrayList<String> match3_sheet = new ArrayList<String>();
    ArrayList<Integer> score4 = new ArrayList<Integer>();
    ArrayList<String> match4_sheet = new ArrayList<String>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, teams, score1, null,
                              match1_sheet, score2, null, match2_sheet, score3,
                              null, match3_sheet, score4, null, match4_sheet);

    // Loop through all the scores.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Get the index the team for these scores.
      int team_idx = ids.indexOf(teams.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // Determine the correct indicator for the match1 score.
      if(score1.get(idx) != null)
      {
        match1.set(team_idx, 2);
      }
      else if(match1_sheet.get(idx) != null)
      {
        match1.set(team_idx, 1);
      }

      // Determine the correct indicator for the match2 score.
      if(score2.get(idx) != null)
      {
        match2.set(team_idx, 2);
      }
      else if(match2_sheet.get(idx) != null)
      {
        match2.set(team_idx, 1);
      }

      // Determine the correct indicator for the match3 score.
      if(score3.get(idx) != null)
      {
        match3.set(team_idx, 2);
      }
      else if(match3_sheet.get(idx) != null)
      {
        match3.set(team_idx, 1);
      }

      // Determine the correct indicator for the match4 score.
      if(score4.get(idx) != null)
      {
        match4.set(team_idx, 2);
      }
      else if(match4_sheet.get(idx) != null)
      {
        match4.set(team_idx, 1);
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

    // Success.
    result.set("result", "ok");
  }

  /**
   * Saves the scoresheet, without scoring it.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   *
   * @param json The JSON representation of the scoresheet selections.
   */
  private void
  save(JSONObject result, int id, int match, String json)
  {
    // Convert an empty JSON object into a null for storage into the database.
    if(json.equals("{}"))
    {
      json = null;
    }

    // Save the scoresheet for this team/match.
    if(m_database.scoreMatchAdd(m_season.seasonIdGet(), m_event.eventIdGet(),
                               id, match, null, null, json) == -1)
    {
      // Return an error since the scoresheet couldn't be saved.
      result.set("result", m_webserver.getSSI("str_referee_save_fail"));
    }
    else
    {
      // Success.
      result.set("result", "ok");

      // There are changes that need to be sent to the active referees.
      m_lastUpdate = java.lang.System.currentTimeMillis();
    }
  }

  /**
   * Scores a scoresheet.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param json The JSON representation of the scoresheet selections.
   */
  private void
  score(JSONObject result, String json)
  {
    // An expression evaluator for any scoring rules.
    DoubleEvaluator eval = new DoubleEvaluator();

    // The set of variables used by the scoring rules.
    StaticVariableSet<Double> vars = new StaticVariableSet<Double>();

    // The set of game pieces.
    HashMap<String, String> pieces_desc = new HashMap<String, String>();
    HashMap<String, Integer> pieces_qty = new HashMap<String, Integer>();
    HashMap<String, Integer> pieces_used = new HashMap<String, Integer>();
    HashMap<String, String> pieces_mission = new HashMap<String, String>();

    // Catch any exceptions and return an error.
    try
    {
      // Convert the scoresheet selections into a JSON object.
      JSONObject sheet = JSONParser.deserializeObject(json);

      // Load the current season's scoresheet if necessary.
      loadScoresheet();

      // Get the game pieces from the scoresheet.
      JSONArray pieces = m_scoresheet.getArray("pieces");

      // Loop through the game pieces.
      for(int i = 0; (pieces != null) && (i < pieces.size()); i++)
      {
        // Get the JSON object for this game piece.
        JSONObject piece = pieces.getObject(i);

        // Get the name of this game piece.
        String name = piece.getString("name");

        // Get the description of this game piece.  If it is not available in
        // the current locale, default back to en_US.
        String desc =
          piece.getObject("description").getString(m_config.localeGet());
        if(desc == null)
        {
          desc = piece.getObject("description").getString("en_US");
        }

        // Get the quantity of this game piece.
        int quantity = piece.getInteger("quantity");

        // Get the mission for which an over use of this game pieces is marked
        // as being in error.
        String mission = piece.getString("mission");

        // Add this piece to the map of game pieces.
        pieces_desc.put(name, desc);
        pieces_qty.put(name, quantity);
        pieces_used.put(name, 0);
        pieces_mission.put(name, mission);
      }

      // Get the missions from the scoresheet.
      JSONArray missions = m_scoresheet.getArray("missions");

      // Start with zero points.
      int points = 0;

      // Loop through the missions.
      for(int i = 0; i < missions.size(); i++)
      {
        // For a multiple item mission, start with an accumlated selection of
        // zero.
        int sel = 0;

        // The mission-specific score starts at zero.
        int mission_points = 0;

        // Get the JSON object for this mission, and the mission ID from that.
        JSONObject mission = missions.getObject(i);
        String mission_id = mission.getString("mission");

        // Get the mission items and loop through them.
        JSONArray items = mission.getArray("items");
        for(int j = items.size(); j > 0; j--)
        {
          // Get the JSON object for this mission item, and the ID for it.
          JSONObject item = items.getObject(j - 1);
          Integer item_id = item.getInteger("id");

          // Get the selection for this item.
          int selection = sheet.getInteger(mission_id + "_" + item_id);

          // Save the value of this selection to the rules variable set.
          vars.set(mission_id + "_" + item_id, (double)selection);

          // Get the score list for this item.
          JSONArray scores = item.getArray("score");
          if(scores == null)
          {
            // There is not a score list for this item, so the entire mission
            // is scored based on the accumulation of items. Get the type of
            // this item.
            String choice = item.getString("type");
            if(choice.equals("yesno"))
            {
              // This item is a yes/no choice, so double the previous
              // accumulation (since this selection has two choices) and add
              // the value of this selection.
              sel = (sel * 2) + selection;
            }
            else if(choice.equals("enum"))
            {
              // This item is an enumeration, so multiply by the number of
              // choices and add the value of this selection.
              sel = ((sel *
                      item.getObject("choices").getArray("en_US").size()) +
                     selection);
            }
          }
          else
          {
            // There are scores for this item, so add to the score based on
            // this selection.
            points += scores.getInteger(selection);
            mission_points += scores.getInteger(selection);
          }

          // Get the pieces for this item.
          pieces = item.getArray("pieces");

          // Loop through the pieces for this item.
          for(int k = 0; (pieces != null) && (k < pieces.size()); k++)
          {
            // Get the name and quantity from this game piece.
            String name = pieces.getObject(k).getString("name");
            int quantity =
              pieces.getObject(k).getArray("quantity").getInteger(selection);

            // Update the quantity of this piece in use.
            pieces_used.put(name, pieces_used.get(name) + quantity);
          }
        }

        // See if there is a mission-based score list.
        JSONArray scores = mission.getArray("score");
        if(scores != null)
        {
          // Add to the score based on the accumulation of selections.
          points += scores.getInteger(sel);
          mission_points += scores.getInteger(sel);
        }

        // Save the score of this mission to the rules variable set.
        vars.set(mission_id, (double)mission_points);
      }

      // Loop through the missions.
      for(int i = 0; i < missions.size(); i++)
      {
        // Get the JSON object for this mission, and the mission ID from that.
        JSONObject mission = missions.getObject(i);
        String mission_id = mission.getString("mission");

        // See if there are any constraints on this mission.
        JSONArray constraints = mission.getArray("constraints");
        if(constraints != null)
        {
          // Loop through the constraints.
          for(int j = 0; j < constraints.size(); j++)
          {
            // Get this constraint.
            JSONObject constraint = constraints.getObject(j);

            // Get the rule for this constraint.
            String rule = constraint.getString("rule");

            // Evaluate this rule.
            if(eval.evaluate(rule, vars) < 0)
            {
              // The rule failed, so get the description of the failure.
              JSONObject desc = constraint.getObject("description");
              String error = desc.getString(m_config.localeGet());
              if(error == null)
              {
                error = desc.getString("en_US");
              }

              // Add this failure description to the result.
              String res = result.getString("result");
              if(res == null)
              {
                res = "";
              }
              result.set("result", res + mission_id + ":" + error + "\n");
            }
          }
        }

        // See if there is a scoring rule for this mission.
        String rule = mission.getString("score_rule");
        if(rule != null)
        {
          // Evaluate this rule and add the resulting points to the score.
          points += Math.round(eval.evaluate(rule, vars));
        }
      }

      // Interate through the game pieces.
      pieces_used.forEach((name, qty) ->
        {
          // See if more pieces were used than are available.
          if(pieces_used.get(name) > pieces_qty.get(name))
          {
            // Too many pieces were used, so add this failure description to
            // the result.
            String res = result.getString("result");
            if(res == null)
            {
              res = "";
            }

            // Get the name of this game piece.
            String piece = pieces_desc.get(name);

            // Get the too many pieces error message.
            String message = m_webserver.getSSI("str_referee_too_many_pieces");

            // Substitue in the name of this game piece.
            message = message.replace("${piece}", piece);

            // Append this error message to the existing result.
            result.set("result",
                       res + pieces_mission.get(name) + ":" + message + "\n");
          }
        });

      // Add the score to the JSON response.
      result.set("score", points);

      // Add the core values score to the JSON response.
      if(!sheet.isNull("CV_1"))
      {
        result.set("cv", sheet.getInteger("CV_1") + 1);
      }
      if(!sheet.isNull("GP_1"))
      {
        result.set("cv", sheet.getInteger("GP_1") + 1);
      }

      // Success.
      if(result.getString("result") == null)
      {
        result.set("result", "ok");
      }
    }
    catch(Exception e)
    {
      System.out.println("JSON error: " + e);
      result.set("result", m_webserver.getSSI("str_referee_parse_fail"));
    }
  }

  /**
   * Publishes a scoresheet, scoring and then saving it.
   *
   * @param result The JSON object into which the result is placed.
   *
   * @param id The ID of the team.
   *
   * @param match The match.
   *
   * @param json The JSON representation of the scoresheet selections.
   */
  private void
  publish(JSONObject result, int id, int match, String json)
  {
    // Score the scoresheet.
    score(result, json);
    if(!result.getString("result").equals("ok"))
    {
      return;
    }

    // Get the score and remove it from the JSON response.
    int score = result.getInteger("score");
    result.unset("score");

    // Get the core values score and remove it from the JSON response.
    Integer cv = result.getInteger("cv");
    if(!result.isSet("cv"))
    {
      cv = null;
    }

    // Save the scoresheet for this team/match.
    if(m_database.scoreMatchAdd(m_season.seasonIdGet(), m_event.eventIdGet(),
                                id, match, score, cv, json) == -1)
    {
      // Return an error since the scoresheet couldn't be saved.
      result.set("result", m_webserver.getSSI("str_referee_save_fail"));
    }
    else
    {
      // Success.
      result.set("result", "ok");

      // There are changes that need to be sent to the active referees.
      m_lastUpdate = java.lang.System.currentTimeMillis();
    }
  }

  /**
   * Serves the JSON data for the current state of the referees.
   *
   * @param path The path that was requested.
   *
   * @param paramMap The parameters associated with the request.
   *
   * @return A byte array containing the JSON data for the referees.
   */
  private byte[]
  serveRefereeJson(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "get", for getting a scoresheet.
      if(paramMap.get("action").equals("get") && paramMap.containsKey("id") &&
         paramMap.containsKey("match"))
      {
        // Get the scoresheet.
        get(result, Integer.parseInt(paramMap.get("id")),
            Integer.parseInt(paramMap.get("match")));
      }

      // See if the action is "save", for saving but not scoring a scoresheet.
      else if(paramMap.get("action").equals("save") &&
              paramMap.containsKey("id") && paramMap.containsKey("match") &&
              paramMap.containsKey("json"))
      {
        // Save the scoresheet.
        save(result, Integer.parseInt(paramMap.get("id")),
             Integer.parseInt(paramMap.get("match")),
             URLDecoder.decode(paramMap.get("json"), StandardCharsets.UTF_8));
      }

      // See if the action is "score", for getting the score for a scoresheet.
      else if(paramMap.get("action").equals("score") &&
              paramMap.containsKey("json"))
      {
        // Score the scoresheet.
        score(result,
              URLDecoder.decode(paramMap.get("json"), StandardCharsets.UTF_8));
        if(result.isSet("score") && (result.getInteger("score") < 0))
        {
          result.set("score", 0);
        }
      }

      // See if the action is "publish", for scoring and saving a scoresheet.
      else if(paramMap.get("action").equals("publish") &&
              paramMap.containsKey("id") && paramMap.containsKey("match") &&
              paramMap.containsKey("json"))
      {
        // Publish the scoresheet.
        publish(result, Integer.parseInt(paramMap.get("id")),
                Integer.parseInt(paramMap.get("match")),
                URLDecoder.decode(paramMap.get("json"),
                                  StandardCharsets.UTF_8));
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
   * The WebSocket for the referee.
   */
  @WebSocket
  public static class RefereeSocket implements Runnable
  {
    /**
     * The object for the Referee singleton.
     */
    private Referee m_instance = Referee.getInstance();

    /**
     * The session for this WebSocket.
     */
    private Session m_session;

    /**
     * The time, in milliseconds, at which the update was sent to the
     * WebSocket.
     */
    private long m_lastSend = 0;

    /**
     * Called when the WebSocket is first opened.
     *
     * @param session The session for this WebSocket.
     */
    @OnWebSocketOpen
    public void
    onOpen(Session session)
    {
      // Save the session.
      m_session = session;

      // Start the thread that handles sending updates via the WebSocket.
      new Thread(this).start();
    }

    /**
     * Called when the WebSocket is closed.
     *
     * @param closeCode The code for why the WebSocket was closed.
     *
     * @param closeReasonPhrase The reason the WebSocket was closed.
     */
    @OnWebSocketClose
    public void
    onClose(int closeCode, String closeReasonPhrase)
    {
      // Clear the stored session, which causes the background thread to exit.
      m_session = null;
    }

    /**
     * The code that runs in the referee thread.
     */
    @Override
    public void
    run()
    {
      // Loop while the session is still active.
      while (m_session != null)
      {
        // Get the current time.
        long now = java.lang.System.currentTimeMillis();

        // See if there are score updates to be sent.
        if(m_instance.m_lastUpdate > m_lastSend)
        {
          // Capture the time of the update that is being performed.
          now = m_instance.m_lastUpdate;

          // Arrays to hold the score data from the database.
          ArrayList<Integer> teamNumber = new ArrayList<Integer>();
          ArrayList<String> match1Sheet = new ArrayList<String>();
          ArrayList<Integer> match1Score = new ArrayList<Integer>();
          ArrayList<String> match2Sheet = new ArrayList<String>();
          ArrayList<Integer> match2Score = new ArrayList<Integer>();
          ArrayList<String> match3Sheet = new ArrayList<String>();
          ArrayList<Integer> match3Score = new ArrayList<Integer>();
          ArrayList<String> match4Sheet = new ArrayList<String>();
          ArrayList<Integer> match4Score = new ArrayList<Integer>();

          // Get the scores for this event.
          m_instance.m_database.
            scoreEnumerate(m_instance.m_season.seasonIdGet(),
                           m_instance.m_event.eventIdGet(), null, teamNumber,
                           match1Score, null, match1Sheet, match2Score, null,
                           match2Sheet, match3Score, null, match3Sheet,
                           match4Score, null, match4Sheet);

          // Loop through the tests that have scores (or scoresheets).
          for(int idx = 0; idx < teamNumber.size(); idx++)
          {
            Integer state, score;

            // Determine the state of this team's first round.
            score = match1Score.get(idx);
            if(match1Sheet.get(idx) == null)
            {
              state = 0;
            }
            else if(score == null)
            {
              state = 1;
            }
            else
            {
              state = 2;
            }

            // Send this team's first round state to the client.
            m_session.sendText("m1:" + teamNumber.get(idx) + ":" + state +
                               ":" + ((score == null) ? "" : score),
                               Callback.NOOP);

            // Determine the state of this team's second round.
            score = match2Score.get(idx);
            if(match2Sheet.get(idx) == null)
            {
              state = 0;
            }
            else if(score == null)
            {
              state = 1;
            }
            else
            {
              state = 2;
            }

            // Send this team's second round state to the client.
            m_session.sendText("m2:" + teamNumber.get(idx) + ":" + state +
                               ":" + ((score == null) ? "" : score),
                               Callback.NOOP);

            // Determine the state of this team's third round.
            score = match3Score.get(idx);
            if(match3Sheet.get(idx) == null)
            {
              state = 0;
            }
            else if(score == null)
            {
              state = 1;
            }
            else
            {
              state = 2;
            }

            // Send this team's third round state to the client.
            m_session.sendText("m3:" + teamNumber.get(idx) + ":" + state +
                               ":" + ((score == null) ? "" : score),
                               Callback.NOOP);

            // Determine the state of this team's fourth round.
            score = match4Score.get(idx);
            if(match4Sheet.get(idx) == null)
            {
              state = 0;
            }
            else if(score == null)
            {
              state = 1;
            }
            else
            {
              state = 2;
            }

            // Send this team's fourth round state to the client.
            m_session.sendText("m4:" + teamNumber.get(idx) + ":" + state +
                               ":" + ((score == null) ? "" : score),
                               Callback.NOOP);
          }

          // Set the last send time to the update time.  It is possible that
          // another update came in during the time it took to send this
          // update, but it will get sent out the next time through the loop.
          m_lastSend = now;
        }
        else if((now - m_lastSend) > 1000)
        {
          // Send a NOP via the WebSocket (to keep it from timing out and
          // closing).
          m_session.sendText("nop", Callback.NOOP);

          // Increment the last send time by a second.  This effectively
          // precludes the possibility of a missed update.
          m_lastSend += 1000;
        }

        // Delay for half a second.
        try
        {
          TimeUnit.MILLISECONDS.sleep(500);
        }
        catch(InterruptedException e)
        {
        }
      }
    }
  }

  /**
   * A creator for referee WebSockets.
   */
  private static class RefereeSocketCreator implements JettyWebSocketCreator
  {
    // Creates a WebSocket for the incoming request.
    @Override
    public Object
    createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest,
                    JettyServerUpgradeResponse jettyServerUpgradeResponse)
    {
      // Create a new referee WebSocket for this request.
      return(new RefereeSocket());
    }
  }

  /**
   * Performs initial setup for the referee page.
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

    // Register the dynamic handler for the referee.json file.
    m_webserver.registerDynamicFile("/referee/referee.json",
                                    this::serveRefereeJson);

    // Register the WebSocket that supports the referee page.
    m_webserver.addWebSocket("/referee/referee.ws", new RefereeSocketCreator(),
                             5000);
  }
}