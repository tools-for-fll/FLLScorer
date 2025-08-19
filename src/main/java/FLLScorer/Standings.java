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
  scoresCompare(float a1, float a2, float a3, float a4, float b1, float b2,
                float b3, float b4)
  {
    // Convert the scores into arrays.
    Float a[] = { a1, a2, a3, a4 };
    Float b[] = { b1, b2, b3, b4 };

    // Sort the array in descending order.
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
    ArrayList<Float> match1 = new ArrayList<Float>();
    ArrayList<Float> match2 = new ArrayList<Float>();
    ArrayList<Float> match3 = new ArrayList<Float>();
    ArrayList<Float> match4 = new ArrayList<Float>();
    ArrayList<Float> high = new ArrayList<Float>();

    // Set the score indicator for each team to no score available.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      match1.add(idx, (float)-100);
      match2.add(idx, (float)-100);
      match3.add(idx, (float)-100);
      match4.add(idx, (float)-100);
      high.add(idx, (float)-100);
      cv.add(idx, null);
    }

    // A list of information about the scores.
    ArrayList<Integer> ids2 = new ArrayList<Integer>();
    ArrayList<Float> score1 = new ArrayList<Float>();
    ArrayList<Integer> core1 = new ArrayList<Integer>();
    ArrayList<Float> score2 = new ArrayList<Float>();
    ArrayList<Integer> core2 = new ArrayList<Integer>();
    ArrayList<Float> score3 = new ArrayList<Float>();
    ArrayList<Integer> core3 = new ArrayList<Integer>();
    ArrayList<Float> score4 = new ArrayList<Float>();
    ArrayList<Integer> core4 = new ArrayList<Integer>();

    // Enumerate the scores for this event.
    m_database.scoreEnumerate(season_id, event_id, null, ids2, null, null,
                              null, score1, core1, null, score2, core2, null,
                              score3, core3, null, score4, core4, null);

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
      cv.set(team_idx, (((s1 == null) ? 0 : s1) + ((s2 == null) ? 0 : s2) +
                        ((s3 == null) ? 0 : s3) + ((s4 == null) ? 0 : s4)));
    }

    // Loop through all the teams to find their high score.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Get the high score.
      float max = match1.get(idx);
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
        if((scores.get(sort.get(idx2)) == -1) ||
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

    // Insert empty scores to the judging list for teams that do not have any
    // judging scores at the event.
    for(int idx = 0; idx < teams.size(); idx++)
    {
      int idx2 = ids.indexOf(teams.get(idx));
      if(idx2 == -1)
      {
        ids.add(idx, -1);
        project.add(idx, -1);
        robot.add(idx, -1);
        core.add(idx, -1);
      }
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

      // Swap these two entries, so that these arrays match the order of the
      // teams array.
      Collections.swap(ids, idx, idx2);
      Collections.swap(project, idx, idx2);
      Collections.swap(robot, idx, idx2);
      Collections.swap(core, idx, idx2);
    }

    // Add the robot game Core Values scores to the judging Core Values scores.
    for(int idx = 0; (idx < teams.size()) && (idx < robotCore.size()); idx++)
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
      if((cv1 + cv2) == 0)
      {
        core.set(idx, -1);
      }
      else
      {
        core.set(idx, cv1 + cv2);
      }
    }

    // Rank the teams in the various judging areas.
    rankJudgingArea(teams, ids, project);
    rankJudgingArea(teams, ids, robot);
    rankJudgingArea(teams, ids, core);
  }

  /**
   * Computes the scores for an event.
   *
   * @param eventId The ID of the event.
   *
   * @param teams The list of the teams in the league.
   *
   * @param eventTeams The list of the teams at this event.
   *
   * @param event1 The list of first event scores for the teams in the league.
   *
   * @param event2 The list of second event scores for the teams in the league.
   */
  private void
  computeScores(int eventId, ArrayList<Integer> teams,
                ArrayList<Integer> eventTeams, ArrayList<Integer> event1,
                ArrayList<Integer> event2)
  {
    int season_id = m_season.seasonIdGet();

    // Get the robot ranking for this event.
    ArrayList<Integer> robotRanks = new ArrayList<Integer>();
    ArrayList<Integer> robotCore = new ArrayList<Integer>();
    getRobotRankings(season_id, eventId, eventTeams, robotRanks, robotCore);

    // Get the judging rankings for this event.
    ArrayList<Integer> judgingProject = new ArrayList<Integer>();
    ArrayList<Integer> judgingRobot = new ArrayList<Integer>();
    ArrayList<Integer> judgingCore = new ArrayList<Integer>();
    getJudgingRankings(season_id, eventId, eventTeams, robotCore,
                       judgingProject, judgingRobot, judgingCore);

    // The number of teams in each ranking area.
    int teamsRobotGame = 0;
    int teamsProject = 0;
    int teamsRobotDesign = 0;
    int teamsCoreValues = 0;

    // Loop through all the teams.
    for(int team = 0; team < eventTeams.size(); team++)
    {
      // If this team exists and has a robot game rank, increment the count
      // of robot game teams.
      if((team < robotRanks.size()) && (robotRanks.get(team) != -1))
      {
        teamsRobotGame++;
      }

      // If this team exists and has a project rank, increment the count of
      // project teams.
      if((team < judgingProject.size()) && (judgingProject.get(team) != -1))
      {
        teamsProject++;
      }

      // If this team exists and has a robot design rank, increment the count
      // of robot design teams.
      if((team < judgingRobot.size()) && (judgingRobot.get(team) != -1))
      {
        teamsRobotDesign++;
      }

      // If the team exists and has a core values rank, increment the count
      // of core values teams.
      if((team < judgingCore.size()) && (judgingCore.get(team) != -1))
      {
        teamsCoreValues++;
      }
    }

    // Loop through the teams.
    for(int team = 0; team < eventTeams.size(); team++)
    {
      // Start to calculate the event score for this team.
      int score = 0;

      // See if this team has a robot game ranking.
      if(robotRanks.get(team) != -1)
      {
        // This team has a robot game ranking, so use it to determine their
        // robot game score.
        if(teamsRobotGame == 1)
        {
          // There is only a single team at the event, so assign the maximum
          // points.
          score += 200;
        }
        else
        {
          // Compute the number of robot game points that this team earned.
          score += (((100 * (teamsRobotGame - robotRanks.get(team))) /
                    (teamsRobotGame - 1)) + 100);
        }
      }

      // See if this team has an Innovation Project ranking.
      if(judgingProject.get(team) != -1)
      {
        // This team has an Innovation Project ranking, so use it to
        // determine the Innovation Project score.
        if(teamsProject == 1)
        {
          // There is only a single team at the event, so assign the maximum
          // points.
          score += 200;
        }
        else
        {
          // Compute the number of Innovation Project points that this team
          // earned.
          score += (((100 * (teamsProject - judgingProject.get(team))) /
                    (teamsProject - 1)) + 100);
        }
      }

      // See if this team has a Robot Design ranking.
      if(judgingRobot.get(team) != -1)
      {
        // This team has a Robot Design ranking, so use it to determine the
        // Robot Design score.
        if(teamsRobotDesign == 1)
        {
          // There is only a single team at the event, so assign the maximum
          // points.
          score += 200;
        }
        else
        {
          // Compute the number of Robot Design points that this team earned.
          score += (((100 * (teamsRobotDesign - judgingRobot.get(team))) /
                    (teamsRobotDesign - 1)) + 100);
        }
      }

      // See if this team has a Core Values ranking.
      if(judgingCore.get(team) != -1)
      {
        // This team has a Core Values ranking, so use it to determine the
        // Core Values score.
        if(teamsCoreValues == 1)
        {
          // There is only a single team at the event, so assign the maximum
          // points.
          score += 200;
        }
        else
        {
          // Compute the number of Core Values points that this team earned.
          score += (((100 * (teamsCoreValues - judgingCore.get(team))) /
                    (teamsCoreValues - 1)) + 100);
        }
      }

      // Save this score in the appropriate place.  The score is ignored if
      // it is zero, otherwise it is saved as the first or second event score
      // for this team.  If the team already has a first and second event
      // score, this score is discarded (only the scores from the first two
      // events count).
      int teamIdx = teams.indexOf(eventTeams.get(team));
      if(score == 0)
      {
      }
      else if(event1.get(teamIdx) == -1)
      {
        event1.set(teamIdx, score);
      }
      else if(event2.get(teamIdx) == -1)
      {
        event2.set(teamIdx, score);
      }
    }
  }

  /**
   * Compares two sets of team scores to determine the placement order.
   *
   * @param ad The division for the first team.
   *
   * @param a The overall score for the first team.
   *
   * @param a1 The event 1 score for the first team.
   *
   * @param a2 The event 2 score for the first team.
   *
   * @param bd The division for the second team.
   *
   * @param b The overall score for the second team.
   *
   * @param b1 The event 1 score for the second team.
   *
   * @param b2 The event 2 score for the second team.
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
  eventScoresCompare(int ad, float a, float a1, float a2, int bd, float b,
                     float b1, float b2)
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

    // See if the second team places higher than the first team, based on their
    // overall score.
    if(a > b)
    {
      return(1);
    }

    // See if the first team places higher than the second team, based on their
    // overall score.
    if(a < b)
    {
      return(-1);
    }

    // Convert the event scores into arrays.
    Float ea[] = { a1, a2 };
    Float eb[] = { b1, b2 };

    // Sort the array in descending order.
    Arrays.sort(ea, Collections.reverseOrder());
    Arrays.sort(eb, Collections.reverseOrder());

    // Loop through the two event scores.
    for(int idx = 0; idx < 2; idx++)
    {
      // See if the second team places higher than the first team, based on
      // this event score.
      if(ea[idx] < eb[idx])
      {
        return(-1);
      }

      // See if the first team places higher than the seocnd team, based on
      // this event score.
      if(ea[idx] > eb[idx])
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
    ArrayList<Integer> teams = new ArrayList<Integer>();
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<Integer> divisions = new ArrayList<Integer>();
    ArrayList<Integer> scores = new ArrayList<Integer>();
    ArrayList<Integer> event1 = new ArrayList<Integer>();
    ArrayList<Integer> event2 = new ArrayList<Integer>();
    ArrayList<Integer> place = new ArrayList<Integer>();

    // Enumerate the teams from the database for this season.
    m_database.teamEnumerate(season_id, -1, teams, numbers, names, divisions);

    // Force all the divisions to be the same if division support is not
    // enabled (in case they are not the same in the database).
    if(!m_config.divisionEnableGet())
    {
      for(int idx = 0; idx < divisions.size(); idx++)
      {
        divisions.set(idx, 0);
      }
    }

    // Give each team a "no event score" indicator for the two events, which
    // will be replace by an event score when one is found.
    for(int idx = 0; idx < teams.size(); idx++)
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
                               eventNumbers, null, null);

      // See if division support is enabled.
      if(m_config.divisionEnableGet())
      {
        ArrayList<Integer> divTeams = new ArrayList<Integer>();

        // Loop through the divisions.
        for(int div = 1; div <= m_config.divisionCountGet(); div++)
        {
          // Remove any teams from the division list for this event.
          divTeams.clear();

          // Loop through the teams in this event.
          for(int team = 0; team < eventTeams.size(); team++)
          {
            // Add this team to the list of teams in this division at the event
            // if they are in the correct division.
            if(divisions.get(teams.indexOf(eventTeams.get(team))) == div)
            {
              divTeams.add(eventTeams.get(team));
            }
          }

          // Compute the scores for the teams in this division.
          computeScores(events.get(idx), teams, divTeams, event1, event2);
        }
      }
      else
      {
        // Compute the scores for all the teams at the event (since division
        // support is disabled).
        computeScores(events.get(idx), teams, eventTeams, event1, event2);
      }
    }

    // Loop through the teams, determining their league scores from their two
    // event scores.
    for(int idx = 0; idx < teams.size(); idx++)
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

    // Loop through the teams, sorting them by league score. (with
    // equally-placed teams and teams without any scores remaining in team
    // number order).
    for(int i = 0; i < teams.size(); i++)
    {
      // Loop through the preceeding teams.
      for(int j = 0; j < i; j++)
      {
        // See if this preceeding team has a score, and it has a lower score
        // than the current team.
        if((scores.get(j) == -1) ||
           (eventScoresCompare(divisions.get(i), scores.get(i), event1.get(i),
                               event2.get(i), divisions.get(j), scores.get(j),
                               event1.get(j), event2.get(j)) > 0))
        {
          // Remove this team from it's current position and insert it before
          // the preceeding team, since this team has a higher score.
          numbers.add(j, numbers.remove(i));
          names.add(j, names.remove(i));
          scores.add(j, scores.remove(i));
          event1.add(j, event1.remove(i));
          event2.add(j, event2.remove(i));

          // This team is placed in the correct order, so no further checking
          // is required.
          break;
        }
      }
    }

    // Loop through all the teams, assigning places (with equally-placed teams
    // sharing the highest place).
    int placeNum = 1;
    for(int idx = 0; idx < teams.size(); idx++)
    {
      // Do not assign a place if this team has no scores.
      if(scores.get(idx) == -1)
      {
        place.add(idx, -1);
      }

      // Otherwise, if this is the first team or this team does not have the
      // same placement as the preceding team, give it a placement based on the
      // loop index.
      else if((idx == 0) ||
              (eventScoresCompare(divisions.get(idx), scores.get(idx),
                                  event1.get(idx), event2.get(idx),
                                  divisions.get(idx - 1), scores.get(idx - 1),
                                  event1.get(idx - 1),
                                  event2.get(idx - 1)) != 0))
      {
        // See if there is a change in division.
        if((idx != 0) && (divisions.get(idx) != divisions.get(idx - 1)))
        {
          // Reset the place back to one with the change in division.
          placeNum = 1;
        }

        // Assign the next place to this team.
        place.add(idx, placeNum);
      }

      // Otherwise, give this team the same placement as the preceding team.
      else
      {
        place.add(idx, place.get(idx - 1));
      }

      // Increment the place.
      placeNum++;
    }

    // Loop through the teams.
    JSONArray standings = new SimpleJSONArray();
    JSONArray divStandings =
      m_config.divisionEnableGet() ? new SimpleJSONArray() : null;
    for(int idx = 0; idx < teams.size(); idx++)
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
      if(divStandings != null)
      {
        if((idx != 0) && (divisions.get(idx) != divisions.get(idx - 1)))
        {
          standings.addEntry(divStandings);
          divStandings = new SimpleJSONArray();
        }

        divStandings.addEntry(team);
      }
      else
      {
        standings.addEntry(team);
      }
    }

    // Add the standings array to the JSON response.
    if(divStandings != null)
    {
      standings.addEntry(divStandings);
    }
    result.set("standings", standings);

    // See if divisions are enabled.
    if(m_config.divisionEnableGet())
    {
      JSONArray divisionNames = new SimpleJSONArray();
      JSONArray colors = new SimpleJSONArray();

      // Loop through the divisions.
      for(int idx = 1; idx <= m_config.divisionCountGet(); idx++)
      {
        // Get the name and color for this division.
        divisionNames.addEntry(m_config.divisionNameGet(idx));
        colors.addEntry(m_config.divisionColorGet(idx));
      }

      // Add the division information to the JSON response.
      result.set("divisions", divisionNames);
      result.set("colors", colors);
    }
    else
    {
      // Add the accent color to the JSON response.
      result.set("color", m_webserver.getSSI("accent-color"));
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
    // Get references to the database, config, web server, and season objects.
    m_database = Database.getInstance();
    m_config = Config.getInstance();
    m_webserver = WebServer.getInstance();
    m_season = Seasons.getInstance();

    // Register the dynamic handler for the standings.json file.
    WebServer.getInstance().registerDynamicFile("/standings/standings.json",
                                                this::serveStandingsJson);
  }
}