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
 * Handles the standings.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Standings
{
  /**
   * The object for the Standings singleton.
   */
  private static Standings m_instance = null;

  /**
   * The database object.
   */
  private Database m_database = null;

  /**
   * The Seasons object.
   */
  private Seasons m_season = null;

  /**
   * Gets the Standings singleton object, creating it if necessary.
   *
   * @return Returns the Standings singleton.
   */
  public static Standings
  getInstance()
  {
    // Create the Standings object if required.
    if(m_instance == null)
    {
      m_instance = new Standings();
    }

    // Return the Standings object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Standings()
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
   * Gets the robot game rankings for the teams at an event.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param teams The set of teams for the season.
   *
   * @param ranks The array into which the robot game rankings of the teams at
   *              this event are stored.
   *
   * @param cv The array into which the robot game Core Values scores for the
   *           teams at this event are stored.
   */
  private void
  getRobotRankings(int season_id, int event_id, ArrayList<Integer> teams,
                   ArrayList<Integer> ranks, ArrayList<Integer> cv)
  {
    // A list of information about teams, maintained in team number order.
    ArrayList<Integer> match1 = new ArrayList<Integer>();
    ArrayList<Integer> match2 = new ArrayList<Integer>();
    ArrayList<Integer> match3 = new ArrayList<Integer>();
    ArrayList<Integer> match4 = new ArrayList<Integer>();
    ArrayList<Integer> high = new ArrayList<Integer>();

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      match1.add(idx, -100);
      match2.add(idx, -100);
      match3.add(idx, -100);
      match4.add(idx, -100);
      high.add(idx, -100);
    }

    // A list of information about the scores.
    ArrayList<Integer> ids2 = new ArrayList<Integer>();
    ArrayList<Integer> score1 = new ArrayList<Integer>();
    ArrayList<Integer> core1 = new ArrayList<Integer>();
    ArrayList<Integer> score2 = new ArrayList<Integer>();
    ArrayList<Integer> core2 = new ArrayList<Integer>();
    ArrayList<Integer> score3 = new ArrayList<Integer>();
    ArrayList<Integer> core3 = new ArrayList<Integer>();
    ArrayList<Integer> score4 = new ArrayList<Integer>();
    ArrayList<Integer> core4 = new ArrayList<Integer>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, ids2, score1, core1,
                              null, score2, core2, null, score3, core3, null,
                              score4, core4, null);

    // Loop through all the scores.
    for(int idx = 0; idx < ids2.size(); idx++)
    {
      // Ignore this score if it is not for a team at this event (should not
      // happen).
      int team_idx = teams.indexOf(ids2.get(idx));
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

      // Sum up and save the core values scores, if they exist.
      Integer s1 = core1.get(idx);
      Integer s2 = core2.get(idx);
      Integer s3 = core3.get(idx);
      Integer s4 = core4.get(idx);
      System.out.println((s1 == null) ? 0 : s1);
      System.out.println((s2 == null) ? 0 : s2);
      System.out.println((s3 == null) ? 0 : s3);
      System.out.println((s4 == null) ? 0 : s4);
      cv.add(team_idx, (((s1 == null) ? 0 : s1) + ((s2 == null) ? 0 : s2) +
                        ((s3 == null) ? 0 : s3) + ((s4 == null) ? 0 : s4)));
      System.out.println(cv.get(team_idx));
    }

    // Loop through all the teams to find their high score.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Get the high score.
      int max = match1.get(idx);
      max = (match2.get(idx) > max) ? match2.get(idx) : max;
      max = (match3.get(idx) > max) ? match3.get(idx) : max;
      max = (match4.get(idx) > max) ? match4.get(idx) : max;
      high.set(idx, max);
    }

    // An array that maps between the teams in their given order and a sorted
    // order.  Start with that order being a 1-to-1 mapping.
    ArrayList<Integer> sort = new ArrayList<Integer>();
    for(int idx = 0; idx < teams.size(); idx++)
    {
      sort.add(idx, idx);
    }

    // Loop through all the teams, sorting them into placement order (with
    // equally-placed teams and teams without any scores remaining in team
    // number order).
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // This team does not need to get moved if it does not have any scores.
      if(high.get(sort.get(idx)) == -100)
      {
        continue;
      }

      // Loop through all the preceeding teams.
      for(int idx2 = 0; idx2 < idx; idx2++)
      {
        // See if this preceeding team has a score, and it has a lower score
        // than the current team.
        if((high.get(sort.get(idx2)) == -1) ||
           (scoresCompare(match1.get(sort.get(idx)), match2.get(sort.get(idx)),
                          match3.get(sort.get(idx)), match4.get(sort.get(idx)),
                          match1.get(sort.get(idx2)),
                          match2.get(sort.get(idx2)),
                          match3.get(sort.get(idx2)),
                          match4.get(sort.get(idx2))) > 0))
        {
          // Remove the current team from it's location in the sort array and
          // then insert it back into the position where the preceeding team is
          // located.
          sort.add(idx2, sort.remove(idx));

          // This team has been placed in the right place, so no further
          // preceeding teams need to be examined.
          break;
        }
      }
    }

    // Loop through all the teams, assigning places (with equally-placed teams
    // sharing the highest place).
    for(int idx = 0; idx < teams.size(); idx++)
    {
      ranks.add(idx, -1);
    }
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Do not assign a place if this team has no scores.
      if(high.get(sort.get(idx)) == -100)
      {
        continue;
      }

      // Otherwise, if this is the first team or this team does not have the
      // same placement as the preceding team, give it a placement based on the
      // loop index.
      if((idx == 0) ||
         (scoresCompare(match1.get(sort.get(idx)), match2.get(sort.get(idx)),
                        match3.get(sort.get(idx)), match4.get(sort.get(idx)),
                        match1.get(sort.get(idx - 1)),
                        match2.get(sort.get(idx - 1)),
                        match3.get(sort.get(idx - 1)),
                        match4.get(sort.get(idx - 1))) != 0))
      {
        ranks.set(sort.get(idx), idx + 1);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        ranks.set(sort.get(idx), ranks.get(sort.get(idx - 1)));
      }
    }
  }

  /**
   * Gets the rankings for a particular judging area for the teams at an event.
   *
   * @param teams The list of team IDs in the order that matches the other team
   *              information.
   *
   * @param ids The list of team IDs in the order that matches the scores.
   *
   * @param scores The array containing the scores for a judging area, which is
   *               replaced with the rankings for the teams in this judging
   *               area.
   */
  private void
  rankJudgingArea(ArrayList<Integer> teams, ArrayList<Integer> ids,
                  ArrayList<Integer> scores)
  {
    // An array that maps between the teams in their given order and a sorted
    // order.  Start with that order being a 1-to-1 mapping.
    ArrayList<Integer> sort = new ArrayList<Integer>();
    for(int idx = 0; idx < teams.size(); idx++)
    {
      sort.add(idx, idx);
    }

    // Sort the teams by their judging score.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Ignore this score if it is not for a team at this event (should not
      // happen).
      int team_idx = teams.indexOf(ids.get(idx));
      if(team_idx == -1)
      {
        continue;
      }

      // This team does not need to be moved if it does not have a judging
      // score.
      if(scores.get(sort.get(idx)) == -1)
      {
         continue;
      }

      // Loop through the preceeding teams.
      for(int idx2 = 0; idx2 < idx; idx2++)
      {
        // See if the preceeding team has a judging score, and it has a lower
        // score than the current team.
        if((scores.get(sort.get(idx2)) != -1) &&
           (scores.get(sort.get(idx2)) < scores.get(sort.get(idx))))
         {
          // Remove the current team from it's location in the sort array and
          // then insert it back into the position where the preceeding team is
          // located.
          sort.add(idx2, sort.remove(idx));

          // This team has been placed in the right place, so no further
          // preceeding teams need to be examined.
          break;
         }
      }
    }

    // A temporary array to store the team's rankings.
    ArrayList<Integer> ranks = new ArrayList<Integer>();
    for(int idx = 0; idx < teams.size(); idx++)
    {
      ranks.add(idx, -1);
    }

    // Loop through all the teams, assigning places (with equally-placed teams
    // sharing the highest place).
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Do not assign a place if this team has no judging score.
      if(scores.get(sort.get(idx)) == -1)
      {
        continue;
      }

      // Otherwise, if this is the first team or this team does not have the
      // same placement as the preceding team, give it a placement based on the
      // loop index.
      if((idx == 0) ||
         (scores.get(sort.get(idx)) != scores.get(sort.get(idx - 1))))
      {
        ranks.set(sort.get(idx), idx + 1);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        ranks.set(sort.get(idx), ranks.get(sort.get(idx - 1)));
      }
    }

    // Save these rankings as the judging rankings.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      scores.set(idx, ranks.get(idx));
    }
  }

  /**
   * Gets the judging rankings for the teams at an event.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param teams The set of teams for the season.
   *
   * @param robotCore The Core Values scores from the robot game.
   *
   * @param project The array into which the Innovation Project rankings for
   *                this event are stored.
   *
   * @param robot The array into which the Robot Design rankings for this event
   *              are stored.
   *
   * @param core The array into which the Core Values rankings for this event
   *             are stored.
   */
  private void
  getJudgingRankings(int season_id, int event_id,
                     ArrayList<Integer> teams,
                     ArrayList<Integer> robotCore,
                     ArrayList<Integer> project,
                     ArrayList<Integer> robot,
                     ArrayList<Integer> core)
  {
    // A list of information about the judging scores.
    ArrayList<Integer> ids = new ArrayList<Integer>();

    // Enumerate the judging scores for this event.
    m_database.judgingEnumerate(season_id, event_id, null, null, null, ids,
                                project, robot, core, null);

    // Append empty scores/teams to the judging list to match up the number of
    // teams at the event.
    while(ids.size() < teams.size())
    {
      ids.add(-1);
      project.add(-1);
      robot.add(-1);
      core.add(-1);
    }

    // Sort the judging scores to match the team list order.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Get the index of this team in the team list.  If it does not exist
      // (which should not happen) or it matches the current index of the team,
      // there is nothing to do with this team.
      int idx2 = ids.indexOf(teams.get(idx));
      if((idx2 == -1) || (idx == idx2))
      {
        continue;
      }

      // Remove this team and its scores from its current position and insert
      // it into the correct position, so that these arrays match the order of
      // the teams array.
      ids.add(idx, ids.remove(idx2));
      project.add(idx, project.remove(idx2));
      robot.add(idx, robot.remove(idx2));
      core.add(idx, core.remove(idx2));
    }

    // Add the robot game Core Values scores to the judging Core Values scores.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      Integer cv1 = core.get(idx);
      if(cv1 == null)
      {
        cv1 = 0;
      }
      Integer cv2 = robotCore.get(idx);
      if(cv2 == null)
      {
        cv2 = 0;
      }
      core.set(idx, cv1 + cv2);
    }

    // Rank the teams in the various judging areas.
    rankJudgingArea(teams, ids, project);
    rankJudgingArea(teams, ids, robot);
    rankJudgingArea(teams, ids, core);
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
  serveStandingsJson(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();
    int season_id = m_season.seasonIdGet();

    // Arrays to store information about the events.
    ArrayList<Integer> events = new ArrayList<Integer>();
    ArrayList<String> dates = new ArrayList<String>();

    // Get the list of events for this season.
    m_database.eventEnumerate(season_id, events, null, dates, null, null);

    // Arrays to store information about the teams.
    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> scores = new ArrayList<Integer>();
    ArrayList<Integer> event1 = new ArrayList<Integer>();
    ArrayList<Integer> event2 = new ArrayList<Integer>();
    ArrayList<Integer> place = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, -1, ids, numbers, names);

    // Give each team a "no event score" indicator for the two events, which
    // will be replace by an event score when one is found.
    for(int idx = 0; idx < ids.size(); idx++)
    {
      scores.add(idx, -1);
      event1.add(idx, -1);
      event2.add(idx, -1);
    }

    // Loop through the events for this season.
    for(int idx = 0; idx < events.size(); idx++)
    {
      // Enumerate the teams from the database for this event.
      ArrayList<Integer> eventTeams = new ArrayList<Integer>();
      ArrayList<Integer> eventNumbers = new ArrayList<Integer>();
      m_database.teamEnumerate(season_id, events.get(idx), eventTeams,
                               eventNumbers, null);

      // Get the robot ranking for this event.
      ArrayList<Integer> robotRanks = new ArrayList<Integer>();
      ArrayList<Integer> robotCore = new ArrayList<Integer>();
      getRobotRankings(season_id, events.get(idx), eventTeams, robotRanks,
                       robotCore);

      // Get the judging rankings for this event.
      ArrayList<Integer> judgingProject = new ArrayList<Integer>();
      ArrayList<Integer> judgingRobot = new ArrayList<Integer>();
      ArrayList<Integer> judgingCore = new ArrayList<Integer>();
      getJudgingRankings(season_id, events.get(idx), eventTeams, robotCore,
                         judgingProject, judgingRobot, judgingCore);

      // Loop through the teams.
      for(int idx2 = 0; idx2 < ids.size(); idx2++)
      {
        // Get the index of this team at the event.  If it does not exist
        // (meaning the team did not attend this event), skip this team.
        int teamIdx = eventTeams.indexOf(ids.get(idx2));
        if(teamIdx == -1)
        {
          continue;
        }

        // Start to calculate the event score for this team.
        int score = 0;

        // See if this team has a robot game ranking.
        if(robotRanks.get(teamIdx) != -1)
        {
          // This team has a robot game ranking, so use it to determine their
          // robot game score.
          if(eventTeams.size() == 1)
          {
            // There is only a single team at the event, so assign the maximum
            // points.
            score += 200;
          }
          else
          {
            // Compute the number of robot game points that this team earned.
            score += (((100 * (eventTeams.size() - robotRanks.get(teamIdx))) /
                       (eventTeams.size() - 1)) + 100);
          }
        }

        // See if this team has an Innovation Project ranking.
        if(judgingProject.get(teamIdx) != -1)
        {
          // This team has an Innovation Project ranking, so use it to
          // determine the Innovation Project score.
          if(eventTeams.size() == 1)
          {
            // There is only a single team at the event, so assign the maximum
            // points.
            score += 200;
          }
          else
          {
            // Compute the number of Innovation Project points that this team
            // earned.
            score += (((100 * (eventTeams.size() -
                               judgingProject.get(teamIdx))) /
                       (eventTeams.size() - 1)) + 100);
          }
        }

        // See if this team has a Robot Design ranking.
        if(judgingRobot.get(teamIdx) != -1)
        {
          // This team has a Robot Design ranking, so use it to determine the
          // Robot Design score.
          if(eventTeams.size() == 1)
          {
            // There is only a single team at the event, so assign the maximum
            // points.
            score += 200;
          }
          else
          {
            // Compute the number of Robot Design points that this team earned.
            score += (((100 * (eventTeams.size() -
                               judgingRobot.get(teamIdx))) /
                       (eventTeams.size() - 1)) + 100);
          }
        }

        // See if this team has a Core Values ranking.
        if(judgingCore.get(teamIdx) != -1)
        {
          // This team has a Core Values ranking, so use it to determine the
          // Core Values score.
          if(eventTeams.size() == 1)
          {
            // There is only a single team at the event, so assign the maximum
            // points.
            score += 200;
          }
          else
          {
            // Compute the number of Core Values points that this team earned.
            score += (((100 * (eventTeams.size() - judgingCore.get(teamIdx))) /
                       (eventTeams.size() - 1)) + 100);
          }
        }

        // Save this score in the appropriate place.  The score is ignored if
        // it is zero, otherwise it is saved as the first or second event score
        // for this team.  If the team already has a first and second event
        // score, this score is discarded (only the scores from the first two
        // events count).
        if(score == 0)
        {
        }
        else if(event1.get(idx2) == -1)
        {
          event1.set(idx2, score);
        }
        else if(event2.get(idx2) == -1)
        {
          event2.set(idx2, score);
        }
      }
    }

    // Loop through the teams, determining their league scores from their two
    // event scores.
    for(int idx = 0; idx < ids.size(); idx++)
    {
      // Get this team's two event scores.
      int score1 = event1.get(idx);
      int score2 = event2.get(idx);

      // Set the team's league score based on the event scores.
      if((score1 != -1) && (score2 != -1))
      {
        scores.set(idx, score1 + score2);
      }
      else if(score1 != -1)
      {
        scores.set(idx, score1);
      }
    }

    // Loop through the teams, sorting them by league score.
    for(int idx1 = 0; idx1 < ids.size(); idx1++)
    {
      // Ignore this team if it does not have a league score.
      if(scores.get(idx1) == -1)
      {
        continue;
      }

      // Loop through the preceeding teams.
      for(int idx2 = 0; idx2 < idx1; idx2++)
      {
        // See if this score for the preceeding team is lower than the score
        // for this team.
        if(scores.get(idx1) > scores.get(idx2))
        {
          // Remove this team from it's current position and insert it before
          // the preceeding team, since this team has a higher score.
          numbers.add(idx2, numbers.remove(idx1));
          names.add(idx2, names.remove(idx1));
          scores.add(idx2, scores.remove(idx1));
          event1.add(idx2, event1.remove(idx1));
          event2.add(idx2, event2.remove(idx1));

          // This team is placed in the correct order, so no further checking
          // is required.
          break;
        }
      }
    }

    // Loop through all the teams, assigning places (with equally-placed teams
    // sharing the highest place).
    for(int idx = 0; idx < ids.size(); idx++)
    {
      // Do not assign a place if this team has no scores.
      if(scores.get(idx) == -1)
      {
        place.add(idx, -1);
      }

      // Otherwise, if this is the first team or this team does not have the
      // same placement as the preceding team, give it a placement based on the
      // loop index.
      else if((idx == 0) || (scores.get(idx) < scores.get(idx - 1)))
      {
        place.add(idx, idx + 1);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        place.add(idx, place.get(idx - 1));
      }
    }

    // Loop through the teams.
    JSONArray standings = new SimpleJSONArray();
    for(int idx = 0; idx < ids.size(); idx++)
    {
      // Add this team's scores to the score array.
      JSONObject team = new SimpleJSONObject();
      if(place.get(idx) != -1)
      {
        team.set("place", place.get(idx));
      }
      team.set("num", numbers.get(idx));
      team.set("name", names.get(idx));
      int score = scores.get(idx);
      if(score != -1)
      {
        team.set("score", score);
      }
      score = event1.get(idx);
      if(score != -1)
      {
        team.set("event1", score);
      }
      score = event2.get(idx);
      if(score != -1)
      {
        team.set("event2", score);
      }
      standings.addEntry(team);
    }

    // Add the standings array to the JSON response.
    result.set("standings", standings);

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
    // Get references to the database and season objects.
    m_database = Database.getInstance();
    m_season = Seasons.getInstance();

    // Register the dynamic handler for the standings.json file.
    WebServer.getInstance().registerDynamicFile("/standings/standings.json",
                                                this::serveStandingsJson);
  }
}