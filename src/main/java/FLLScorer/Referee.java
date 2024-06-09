// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;

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
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String fragment;
    InputStream in;

    // Get the season.
    String season = m_config.seasonGet();

    // See if the scoresheet for this season is already loaded.
    if(season.equals(m_scoresheetSeason))
    {
      return;
    }

    // See if there is an information file for this season.
    in = classLoader.getResourceAsStream("seasons/" + season +
                                         "/scoresheet.json");
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
  get(JSONObject result, String id, String match)
  {
    // Convert the ID and match to integers.
    int team_id = Integer.parseInt(id);
    int match_num = Integer.parseInt(match);

    // Get the season and event IDs.
    int season_id = m_season.seasonIdGet();
    int event_id = m_event.eventIdGet();

    // Get the current locale.
    String locale = m_config.localeGet();

    // Load the current season's scoresheet if necessary.
    loadScoresheet();

    // Construct the HTML for the scoresheet starting with an empty string.
    String html = "";

    // Get the missions from the scoresheet.
    JSONArray missions = m_scoresheet.getArray("missions");

    // Loop through the missions.
    for(int i = 0; i < missions.size(); i++)
    {
      // Get the JSON object for this mission, and the mission ID from that.
      JSONObject mission = missions.getObject(i);
      String mission_id = mission.getString("mission");

      // Get the name of the mission.  If it is not available in the current
      // locale, default back to en_US.
      String name = mission.getObject("name").getString(locale);
      if(name == null)
      {
        name = mission.getObject("name").getString("en_US");
      }

      // Add the mission, ID, and name to the HTML.
      html += "<div id=\"" + mission_id + "\" class=\"mission\">";
      html += "  <div class=\"mission_id\">";
      html += "    <span>";
      html += "      " + mission_id;
      html += "    </span>";
      html += "  </div>";
      html += "  <div class=\"mission_name\">";
      html += "    <span>";
      html += "      " + name;
      html += "    </span>";
      html += "  </div>";

      // Get the mission items and loop through them.
      JSONArray items = mission.getArray("items");
      for(int j = 0; j < items.size(); j++)
      {
        // Get the JSON object for this mission item, and the ID for it.
        JSONObject item = items.getObject(j);
        Integer item_id = item.getInteger("id");

        // Get the description for this mission item.  If the description is
        // not available in the current locale, default back to en_US.
        String description = item.getObject("description").getString(locale);
        if(description == null)
        {
          description = item.getObject("description").getString("en_US");
        }

        // Add the description of this item to the HTML.
        html += "  <hr class=\"mission_item\">";
        html += "  <div class=\"mission_desc\">";
        html += "    <span>";
        html += "      " + description;
        html += "    </span>";
        html += "  </div>";
        html += "  <div id=\"" + mission_id + "_" + item_id +
                "\" class=\"mission_sel\">";

        // See if this item has a yes/no selection.
        if(item.getString("type").equals("yesno"))
        {
          // Add a yes and no button to the selection HTML.
          html += "    <button onclick=\"itemToggle('#" + mission_id + "_" +
                  item_id.toString() + "', this);\">" +
                  m_webserver.getSSI("str_no") + "</button>";
          html += "    <button onclick=\"itemToggle('#" + mission_id + "_" +
                  item_id.toString() + "', this);\">" +
                  m_webserver.getSSI("str_yes") + "</button>";
        }

        // Otherwise, see if this item has an enumeration selection.
        else if(item.getString("type").equals("enum"))
        {
          // Get the array of choices.  If they are not available in the current
          // locale, default back to en_US.
          JSONArray choices = item.getObject("choices").getArray(locale);
          if(choices == null)
          {
            choices = item.getObject("choices").getArray("en_US");
          }

          // Loop through the item choices.
          for(int k = 0; k < choices.size(); k++)
          {
            // Add a button for this choice to the HTML.
            html += "    <button onclick=\"itemToggle('#" + mission_id + "_" +
                    item_id.toString() + "', this);\">" +
                    choices.getString(k) + "</button>";
          }
        }

        // Otherwise, the selection type is unknown.
        else
        {
          html += "    <button>ERROR!</button>";
        }

        // End this item.
        html += "  </div>";
      }

      // Add the mission error message contaianer.
      html += "  <div class=\"error\">";
      html += "    <hr class=\"mission_item\">";
      html += "    <div class=\"mission_error\">";
      html += "    </div>";
      html += "  </div>";

      // End this mission.
      html += "</div>";
    }

    // Add the scoresheet HTML to the JSON response.
    result.set("scoresheet", html);

    // Add information about the team to the JSON response.
    result.set("id", team_id);
    result.set("number", m_database.teamNumberGet(season_id, team_id));
    result.set("name", m_database.teamNameGet(season_id, team_id));
    result.set("match", match_num);

    // See if there is a score or scoresheet for this team/match.
    m_database.scoreMatchGet(season_id, event_id, team_id, match_num,
                             (score, cv, sheet) ->
      {
        // Add the score to the JSON response, if it exists.
        if(score != null)
        {
          result.set("score", score.intValue());
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
  save(JSONObject result, String id, String match, String json)
  {
    // Convert an empty JSON object into a null for storage into the database.
    if(json.equals("{}"))
    {
      json = null;
    }

    // Save the scoresheet for this team/match.
    if(m_database.scoreMatchAdd(m_season.seasonIdGet(), m_event.eventIdGet(),
                               Integer.parseInt(id), Integer.parseInt(match),
                               null, null, json) == -1)
    {
      // Return an error since the scoresheet couldn't be saved.
      result.set("result", m_webserver.getSSI("str_referee_save_fail"));
    }
    else
    {
      // Success.
      result.set("result", "ok");
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

    // Catch any exceptions and return an error.
    try
    {
      // Convert the scoresheet selections into a JSON object.
      JSONObject sheet = JSONParser.deserializeObject(json);

      // Load the current season's scoresheet if necessary.
      loadScoresheet();

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
          }
        }

        // See if there is a mission-based score list.
        JSONArray scores = mission.getArray("score");
        if(scores != null)
        {
          // Add to the score based on the accumulation of selections.
          points += scores.getInteger(sel);
        }
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

            // Evalute this rule.
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
      }

      // Add the score to the JSON response.
      result.set("score", points);

      // Add the core values score to the JSON response.
      if(!sheet.isNull("CV_1"))
      {
        result.set("cv", sheet.getInteger("CV_1") + 1);
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
  publish(JSONObject result, String id, String match, String json)
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
                               Integer.parseInt(id), Integer.parseInt(match),
                               score, cv, json) == -1)
    {
      // Return an error since the scoresheet couldn't be saved.
      result.set("result", m_webserver.getSSI("str_referee_save_fail"));
    }
    else
    {
      // Success.
      result.set("result", "ok");
    }
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

    // See if the action was specified and is "get", for getting a scoresheet.
    if("get".equals(action) && (id != null) && (match != null))
    {
      // Get the scoresheet.
      get(result, id, match);
    }

    // See if the action was specified and is "save", for saving but not
    // scoring a scoresheet.
    else if("save".equals(action) && (id != null) && (match != null) &&
            (json != null))
    {
      // Save the scoresheet.
      save(result, id, match, json);
    }

    // See if the action was specified and is "score", for getting the score
    // for a scoresheet.
    else if("score".equals(action) && (json != null))
    {
      // Score the scoresheet.
      score(result, json);
    }

    // See if the action was specified and is "publish", for scoring and
    // saving a scoresheet.
    else if("publish".equals(action) && (id != null) && (match != null) &&
            (json != null))
    {
      // Publish the scoresheet.
      publish(result, id, match, json);
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
  }
}