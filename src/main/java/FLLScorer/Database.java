// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * The interface for accepting the enumerated contents of the season table.
 */
interface SeasonMethod
{
  void accept(int id, String year, String name);
}

/**
 * The interface for accepting the enumerated contents of the event table.
 */
interface EventMethod
{
  void accept(int id, int season_id, String date, int matches, String name);
}

/**
 * The interface for accepting the enumerated contents of the team table.
 */
interface TeamMethod
{
  void accept(int id, int season_id, int number, String name);
}

/**
 * The interface for accepting the enumerated contents of the teamAtEvent
 * table.
 */
interface TeamAtEventMethod
{
  void accept(int season_id, int event_id, int team_id);
}

/**
 * The interface for accepting the enumerated contents of the score table.
 */
interface ScoreMethod
{
  void accept(int id, int season_id, int event_id, int team_id,
              Integer match1, Integer match1_cv, String match1_sheet,
              Integer match2, Integer match2_cv, String match2_sheet,
              Integer match3, Integer match3_cv, String match3_sheet,
              Integer match4, Integer match4_cv, String match4_sheet);
}

/**
 * The interface for accepting a match score.
 */
interface MatchScoreMethod
{
  void accept(Integer match, Integer match_cv, String match_sheet);
}

/**
 * The interface for accepting the enumerated contents of the judging table.
 */
interface JudgingMethod
{
  void accept(int id, int season_id, int event_id, int team_id, int project,
              int robot_design, int core_values, String rubric);
}

/**
 * Interfaces to the persistent database storage to add, remove, and update its
 * contents.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Database
{
  /**
   * The instance of the Database singleton.
   */
  private static Database m_instance = null;

  /**
   * The connection to the SQlite database.
   */
  private Connection m_connection = null;

  /**
   * When set to <b>true</b>, every SQL statement is printed to the terminal
   * for debugging purposes.
   */
  private static boolean m_debug = false;

  /**
   * Gets the singleton for accessing the database.
   *
   * @return The object to use for accessing the database.
   */
  public static Database
  getInstance()
  {
    // See if the singleton has been created yet.
    if(m_instance == null)
    {
      // Create the singleton.
      m_instance = new Database();
    }

    // Return the singleton instance.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Database()
  {
  }

  /**
   * Creates the tables in the database.
   *
   * @return <b>true</b> if the tables are created successfully.
   */
  private boolean
  createTables()
  {
    // The SQL commands to create the tables.
    String config = "create table if not exists config " +
                    "( " +
                    "  key char, " +
                    "  value char " +
                    ")";
    String season = "create table if not exists season " +
                    "( " +
                    "  id integer primary key, " +
                    "  year char, " +
                    "  name char " +
                    ")";
    String event = "create table if not exists event " +
                    "( " +
                    "  id integer primary key, " +
                    "  season_id integer, " +
                    "  date char, " +
                    "  matches integer, " +
                    "  name char " +
                    ")";
    String team = "create table if not exists team " +
                  "( " +
                  "  id integer primary key, " +
                  "  season_id integer, " +
                  "  number integer, " +
                  "  name char " +
                  ")";
    String teamAtEvent = "create table if not exists teamAtEvent " +
                         "( " +
                         "  season_id integer, " +
                         "  event_id integer, " +
                         "  team_id integer " +
                         ")";
    String score = "create table if not exists score " +
                   "( " +
                   "  id integer primary key, " +
                   "  season_id integer, " +
                   "  event_id integer, " +
                   "  team_id integer, " +
                   "  match1 integer, " +
                   "  match1_cv integer, " +
                   "  match1_sheet char, " +
                   "  match2 integer, " +
                   "  match2_cv integer, " +
                   "  match2_sheet char, " +
                   "  match3 integer, " +
                   "  match3_cv integer, " +
                   "  match3_sheet char, " +
                   "  match4 integer, " +
                   "  match4_cv integer, " +
                   "  match4_sheet char " +
                   ")";
    String judging = "create table if not exists judging " +
                     "( " +
                     "  id integer primary key, " +
                     "  season_id integer, " +
                     "  event_id integer, " +
                     "  team_id integer, " +
                     "  project integer, " +
                     "  robot_design integer, " +
                     "  core_values integer " +
                     "  rubric char " +
                     ")";

    // Attempt to create the tables, catching (and ignoring) any errors that
    // may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // Execute the SQL statement to create the config table.
      if(m_debug)
      {
        System.out.println(config);
      }
      stmt.executeUpdate(config);

      // Execute the SQL statement to create the season table.
      if(m_debug)
      {
        System.out.println(season);
      }
      stmt.executeUpdate(season);

      // Execute the SQL statement to create the event table.
      if(m_debug)
      {
        System.out.println(event);
      }
      stmt.executeUpdate(event);

      // Execute the SQL statement to create the team table.
      if(m_debug)
      {
        System.out.println(team);
      }
      stmt.executeUpdate(team);

      // Execute the SQL statement to create the teamAtEvent table.
      if(m_debug)
      {
        System.out.println(teamAtEvent);
      }
      stmt.executeUpdate(teamAtEvent);

      // Execute the SQL statement to create the score table.
      if(m_debug)
      {
        System.out.println(score);
      }
      stmt.executeUpdate(score);

      // Execute the SQL statement to create the judging table.
      if(m_debug)
      {
        System.out.println(judging);
      }
      stmt.executeUpdate(judging);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Gets the value of a configuration item.
   *
   * @param key The name of the configuration item.
   *
   * @return The value of the configuration item.
   */
  public String
  configValueGet(String key)
  {
    String value = null;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to get the configuration value.
      String sql = "select value from config where key = '" + key + "'";

      // Read the value from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the value from the result.
        value = result.getString("value");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the value.
    return(value);
  }

  /**
   * Sets the value of a configuration item.
   *
   * @param key The name of the configuration item.
   *
   * @param value The value of the configuration item.
   *
   * @return <b>true</b> if the configuration item is set successfully.
   */
  public boolean
  configValueSet(String key, String value)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for, set, and update the configuration
      // value.
      String sql1 = "select value from config where key = " +
                    stmt.enquoteLiteral(key);
      String sql2 = "insert into config (key, value) values (" +
                    stmt.enquoteLiteral(key) + ", " +
                    stmt.enquoteLiteral(value) + ")";
      String sql3 = "update config set value = " + stmt.enquoteLiteral(value) +
                    " where key = " + stmt.enquoteLiteral(key);

      // See if the key already exists.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(result.next() == false)
      {
        // The key does not exist, so add it to the database.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);
      }
      else
      {
        // The key exists, so see if the requested value is different than the
        // current value.
        if(!result.getString("value").equals(value))
        {
          // Update the value.
          if(m_debug)
          {
            System.out.println(sql3);
          }
          stmt.executeUpdate(sql3);
        }
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Removes a configuration item.
   *
   * @param key The name of the configuration item.
   *
   * @return <b>true</b> if the configuration item is removed successfully.
   */
  public boolean
  configValueRemove(String key)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to delete the configuration value.
      String sql = "delete from config where key = " +
                   stmt.enquoteLiteral(key);

      // Write the value to the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Adds a season to the database.
   *
   * @param year The year for this season; typically something like
   *             "2024-2025".
   *
   * @param name The name of the challenge.
   *
   * @return The season ID for the season.
   */
  public int
  seasonAdd(String year, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for and create the season.
      String sql1 = "select id from season where year = " +
                    stmt.enquoteLiteral(year) + " and name = " +
                    stmt.enquoteLiteral(name);
      String sql2 = "insert into season (year, name) values (" +
                    stmt.enquoteLiteral(year) + ", " +
                    stmt.enquoteLiteral(name) + ")";

      // See if the season already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The season does not exist, so create it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);

        // Read the ID of the newly created season.
        if(m_debug)
        {
          System.out.println(sql1);
        }
        result = stmt.executeQuery(sql1);
        result.next();
      }

      // Extract the ID from the results.
      int id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();

      // Return the ID of the season.
      return(id);
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // The season could not be added.
    return(-1);
  }

  /**
   * Enumerates the seasons in the database.
   *
   * @param callback The function to call for each season in the database.
   *
   * @return <b>true</b> if the seasons are enumerated successfully.
   */
  public boolean
  seasonEnumerate(SeasonMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the seasons.
      String sql = "select * from season";

      // Query the season table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Provide the values from this row to the callback.
        callback.accept(result.getInt("id"),
                        result.getString("year"),
                        result.getString("name"));
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Removes a season from the database.
   *
   * @param id The ID of the season.
   *
   * @return <b>true</b> if the season is removed successfully.
   */
  public boolean
  seasonRemove(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to remove the season.
      String sql = "delete from season where id = " + id;

      // Delete this season from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Adds an event to the database.
   *
   * @param season_id The ID of the season.
   *
   * @param date The date of the event.
   *
   @ @param matches The number of robot game matches at the event.
   *
   * @param name The name of the event.
   *
   * @return The event ID for the event.
   */
  public int
  eventAdd(int season_id, String date, int matches, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for and insert an event.
      String sql1 = "select id from event where season_id = " + season_id +
                    " and date = " + stmt.enquoteLiteral(date) +
                    " and matches = " + matches + " and name = " +
                    stmt.enquoteLiteral(name);
      String sql2 = "insert into event (season_id, date, matches, name) " +
                    "values (" + season_id + ", " + stmt.enquoteLiteral(date) +
                    ", " + matches + ", " + stmt.enquoteLiteral(name) + ")";

      // See if the event already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The event does not exist, so create it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);

        // Read the ID of the newly created event.
        if(m_debug)
        {
          System.out.println(sql1);
        }
        result = stmt.executeQuery(sql1);
        result.next();
      }

      // Extract the ID from the results.
      int id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();

      // Return the ID of the event.
      return(id);
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // The event could not be added.
    return(-1);
  }

  /**
   * Edits an event in the database.
   *
   * @param event_id The ID of the event.
   *
   * @param date The event's date.
   *
   * @param matches The number of robot game matches at the event.
   *
   * @param name The event's name.
   *
   * @return <b>true</b> if the event is edited successfully.
   */
  public boolean
  eventEdit(int event_id, String date, int matches, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to edit the event.
      String sql = "update event set date = " + stmt.enquoteLiteral(date) +
                   ", matches = " + matches + ", name = " +
                   stmt.enquoteLiteral(name) + " where id = " + event_id;

      // Edit the event.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Enumerates the events in the database.
   *
   * @param season_id The ID of the season.
   *
   * @param callback The function to call for each event in the database.
   *
   * @return <b>true</b> if the events are enumerated successfully.
   */
  public boolean
  eventEnumerate(int season_id, EventMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the events.
      String sql = "select * from event where season_id = " + season_id;

      // Query the event table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Provide the values from this row to the callback.
        callback.accept(result.getInt("id"),
                        result.getInt("season_id"),
                        result.getString("date"),
                        result.getInt("matches"),
                        result.getString("name"));
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Gets the date of an event from the database.
   *
   * @param event_id The ID of the event.
   *
   * @return The date of the event.
   */
  public String
  eventGetDate(int event_id)
  {
    String date = null;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to get the date of the event.
      String sql = "select date from event where id = " + event_id;

      // Get the date of the event from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the date from the results.
        date = result.getString("date");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the date of the event.
    return(date);
  }

  /**
   * Gets the ID of an event from the database.
   *
   * @param season_id The ID of the season.
   *
   * @param date The date of the event.
   *
   * @param matches The number of robot game matches at the event.
   *
   * @param name The name of the event.
   *
   * @return The event ID for the event.
   */
  public int
  eventGetId(int season_id, String date, int matches, String name)
  {
    int id = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to look up the event.
      String sql = "select id from event where season_id = " + season_id +
                   " and date = " + stmt.enquoteLiteral(date) +
                   " and matches = " + matches + " and name = " +
                   stmt.enquoteLiteral(name);

      // Get the ID of the event from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the ID from the results.
        id = result.getInt("id");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the ID of the event.
    return(id);
  }

  /**
   * Gets the number of matches at an event from the database.
   *
   * @param event_id The ID of the event.
   *
   * @return The number of matches at the event.
   */
  public int
  eventGetMatches(int event_id)
  {
    int matches = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to get the number of matches at the event.
      String sql = "select matches from event where id = " + event_id;

      // Get the number of matches at the event from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the number of matches from the results.
        matches = result.getInt("matches");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the number of matches at the event.
    return(matches);
  }

  /**
   * Gets the name of an event from the database.
   *
   * @param event_id The ID of the event.
   *
   * @return The name of the event.
   */
  public String
  eventGetName(int event_id)
  {
    String name = null;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to get the name of the event.
      String sql = "select name from event where id = " + event_id;

      // Get the name of the event from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the date from the results.
        name = result.getString("name");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the name of the event.
    return(name);
  }

  /**
   * Determines if an event has any scores.
   *
   * @param event_id The ID of the event.
   *
   * @return <b>true</b> if the event has scores.
   */
  public boolean
  eventHasScores(int event_id)
  {
    boolean scores = false;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for event scores.
      String sql = "select id from score where event_id = " + event_id;

      // See if the event has scores in the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // There is a score.
        scores = true;
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the result.
    return(scores);
  }

  /**
   * Removes an event from the database.
   *
   * @param id The ID of the event.
   *
   * @return <b>true</b> if the event is removed successfully.
   */
  public boolean
  eventRemove(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to delete an event.
      String sql1 = "delete from score where event_id = " + id;
      String sql2 = "delete from judging where event_id = " + id;
      String sql3 = "delete from teamAtEvent where event_id = " + id;
      String sql4 = "delete from event where id = " + id;

      // Delete this event from the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      stmt.executeUpdate(sql1);
      if(m_debug)
      {
        System.out.println(sql2);
      }
      stmt.executeUpdate(sql2);
      if(m_debug)
      {
        System.out.println(sql3);
      }
      stmt.executeUpdate(sql3);
      if(m_debug)
      {
        System.out.println(sql4);
      }
      stmt.executeUpdate(sql4);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Adds a team to the database.
   *
   * @param season_id The ID of the season.
   *
   * @param number The team's number (as assigned by FIRST).
   *
   * @param name The team's name.
   *
   * @return The team ID for the team.
   */
  public int
  teamAdd(int season_id, int number, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for and insert the team.
      String sql1 = "select id from team where season_id = " + season_id +
                    " and number = " + number + " and name = " +
                    stmt.enquoteLiteral(name);
      String sql2 = "insert into team (season_id, number, name) values (" +
                    season_id + ", " + number + ", " +
                    stmt.enquoteLiteral(name) + ")";

      // See if the team already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The team does not exist, so create it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);

        // Read the ID of the newly created team.
        if(m_debug)
        {
          System.out.println(sql1);
        }
        result = stmt.executeQuery(sql1);
        result.next();
      }

      // Extract the ID from the results.
      int id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();

      // Return the ID of the event.
      return(id);
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // The team could not be added.
    return(-1);
  }

  /**
   * Edits a team in the database.
   *
   * @param team_id The ID of the team.
   *
   * @param number The team's number.
   *
   * @param name The team's name.
   *
   * @return <b>true</b> if the team is edited successfully.
   */
  public boolean
  teamEdit(int team_id, String number, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to edit the team.
      String sql = "update team set number = " + stmt.enquoteLiteral(number) +
                   ", name = " + stmt.enquoteLiteral(name) + " where id = " +
                   team_id;

      // Edit the team.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Enumerates the teams in the database.
   *
   * @param season_id The ID of the season.
   *
   * @param callback The function to call for each team in the database.
   *
   * @return <b>true</b> if the teams are enumerated successfully.
   */
  public boolean
  teamEnumerate(int season_id, TeamMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the teams.
      String sql = "select * from team where season_id = " + season_id;

      // Query the team table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Provide the values from this row to the callback.
        callback.accept(result.getInt("id"),
                        result.getInt("season_id"),
                        result.getInt("number"),
                        result.getString("name"));
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Gets the ID of a team from the database.
   *
   * @param season_id The ID of the season.
   *
   * @param number The team's number (as assigned by FIRST).
   *
   * @return The team ID for the team.
   */
  public int
  teamGet(int season_id, int number)
  {
    int id = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to look up the team.
      String sql = "select id from team where season_id = " + season_id +
                   " and number = " + number;

      // Get the ID of the team from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // Extract the ID from the results.
        id = result.getInt("id");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the ID of the team.
    return(id);
  }

  /**
   * Determines if a team has any scores for a particular event.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @return <b>true</b> if the team has scores.
   */
  public boolean
  teamHasEventScores(int event_id, int team_id)
  {
    boolean scores = false;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for team scores at this event.
      String sql = "select id from score where event_id = " + event_id +
                   " and team_id = " + team_id;

      // See if the team has event scores in the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // There is a score.
        scores = true;
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the result.
    return(scores);
  }

  /**
   * Determines if a team has any scores for a particular season.
   *
   * @param season_id The ID of the season.
   *
   * @param team_id The ID of the team.
   *
   * @return <b>true</b> if the team has scores.
   */
  public boolean
  teamHasScores(int season_id, int team_id)
  {
    boolean scores = false;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for team scores.
      String sql = "select id from score where season_id = " + season_id +
                   " and team_id = " + team_id;

      // See if the team has scores in the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // There is a score.
        scores = true;
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the result.
    return(scores);
  }

  /**
   * Gets the name of a team.
   *
   * @param season_id The ID of the season.
   *
   * @param team_id The ID of the team.
   *
   * @return The name of the team.
   */
  public String
  teamNameGet(int season_id, int team_id)
  {
    String name = null;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to get the name of the team.
      String sql = "select name from team where season_id = " + season_id +
                   " and id = " + team_id;

      // Get the name of this team from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        name = result.getString("name");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the team name.
    return(name);
  }

  /**
   * Gets the number of a team.
   *
   * @param season_id The ID of the season.
   *
   * @param team_id The ID of the team.
   *
   * @return The name of the team.
   */
  public int
  teamNumberGet(int season_id, int team_id)
  {
    int number = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to get the name of the team.
      String sql = "select number from team where season_id = " + season_id +
                   " and id = " + team_id;

      // Get the name of this team from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        number = result.getInt("number");
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the team number.
    return(number);
  }

  /**
   * Removes a team from the database.
   *
   * @param season_id The ID of the season.
   *
   * @param team_id The ID of the team.
   */
  public void
  teamRemove(int season_id, int team_id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // XYZZY teamAtEvent...

      // The SQL command to remove the team.
      String sql1 = "delete from score where season_id = " + season_id +
                    " and team_id = " + team_id;
      String sql2 = "delete from judging where season_id = " + season_id +
                    " and team_id = " + team_id;
      String sql3 = "delete from teamAtEvent where season_id = " + season_id +
                    " and team_id = " + team_id;
      String sql4 = "delete from team where id = " + team_id;

      // Delete this team from the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      stmt.executeUpdate(sql1);
      if(m_debug)
      {
        System.out.println(sql2);
      }
      stmt.executeUpdate(sql2);
      if(m_debug)
      {
        System.out.println(sql3);
      }
      stmt.executeUpdate(sql3);
      if(m_debug)
      {
        System.out.println(sql4);
      }
      stmt.executeUpdate(sql4);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }
  }

  /**
   * Enumerates the teams at an event.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @param callback The function to call for each team to event association in
   *                 the database.
   *
   * @return <b>true</b> if the scores are enumerated successfully.
   */
  public boolean
  teamAtEventEnumerate(int season_id, int event_id, int team_id,
                       TeamAtEventMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to enumerate the teams at events.
      String sql;
      if((season_id == -1) && (event_id == -1) && (team_id == -1))
      {
        sql = "select * from teamAtEvent";
      }
      else
      {
        sql = "select * from teamAtEvent where";
        if(season_id != -1)
        {
          sql += " season_id = " + season_id + " and";
        }
        if(event_id != -1)
        {
          sql += " event_id = " + event_id + " and";
        }
        if(team_id != -1)
        {
          sql += " team_id = " + team_id + " and";
        }
        sql = sql.substring(0, sql.length() - 4);
      }

      // Query the teamAtEvent table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Provide the values from this row to the callback.
        callback.accept(result.getInt("season_id"),
                        result.getInt("event_id"),
                        result.getInt("team_id"));
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Determines if a team is associated with an event.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @return <b>true</b> if the team is part of the event.
   */
  public boolean
  teamAtEventGet(int event_id, int team_id)
  {
    boolean atEvent = false;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for a team at an event.
      String sql = "select * from teamAtEvent where event_id = " + event_id +
                   " and team_id = " + team_id;

      // See if the team is at this event.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);
      if(result.next())
      {
        // The team is at this event.
        atEvent = true;
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the result.
    return(atEvent);
  }

  /**
   * Removes a team from an event.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   */
  public boolean
  teamAtEventRemove(int event_id, int team_id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to remove the team from the event.
      String sql1 = "delete from score where event_id = " + event_id +
                    " and team_id = " + team_id;
      String sql2 = "delete from judging where event_id = " + event_id +
                    " and team_id = " + team_id;
      String sql3 = "delete from teamAtEvent where event_id = " + event_id +
                    " and team_id = " + team_id;

      // Delete this team from this event in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      stmt.executeUpdate(sql1);
      if(m_debug)
      {
        System.out.println(sql2);
      }
      stmt.executeUpdate(sql2);
      if(m_debug)
      {
        System.out.println(sql3);
      }
      stmt.executeUpdate(sql3);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Adds a team to an event.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @return <b>true<b> if the team is added to the event successfully.
   */
  public boolean
  teamAtEventSet(int season_id, int event_id, int team_id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for a team at an event.
      String sql1 = "select * from teamAtEvent where event_id = " + event_id +
                    " and team_id = " + team_id;
      String sql2 = "insert into teamAtEvent (season_id, event_id, team_id) " +
                    "values (" + season_id + ", " + event_id + ", " + team_id +
                    ")";

      // See if the team is at the event.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The team is not at the event, so add it to the event.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Enumerates the scores in the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param callback The function to call for each score in the database.
   *
   * @return <b>true</b> if the scores are enumerated successfully.
   */
  public boolean
  scoreEnumerate(int season_id, int event_id, ScoreMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the scores.
      String sql = "select * from score where season_id = " + season_id +
                   " and event_id = " + event_id;

      // Query the score table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Get the scores, handling the possible NULLs appropriately.
        Integer match1 = result.getInt("match1");
        match1 = result.wasNull() ? null : match1;
        Integer match1_cv = result.getInt("match1_cv");
        match1_cv = result.wasNull() ? null : match1_cv;
        String match1_sheet = result.getString("match1_sheet");
        match1_sheet = result.wasNull() ? null : match1_sheet;
        Integer match2 = result.getInt("match2");
        match2 = result.wasNull() ? null : match2;
        Integer match2_cv = result.getInt("match2_cv");
        match2_cv = result.wasNull() ? null : match2_cv;
        String match2_sheet = result.getString("match2_sheet");
        match2_sheet = result.wasNull() ? null : match2_sheet;
        Integer match3 = result.getInt("match3");
        match3 = result.wasNull() ? null : match3;
        Integer match3_cv = result.getInt("match3_cv");
        match3_cv = result.wasNull() ? null : match3_cv;
        String match3_sheet = result.getString("match3_sheet");
        match3_sheet = result.wasNull() ? null : match3_sheet;
        Integer match4 = result.getInt("match4");
        match4 = result.wasNull() ? null : match4;
        Integer match4_cv = result.getInt("match4_cv");
        match4_cv = result.wasNull() ? null : match4_cv;
        String match4_sheet = result.getString("match4_sheet");
        match4_sheet = result.wasNull() ? null : match4_sheet;

        // Provide the values from this row to the callback.
        callback.accept(result.getInt("id"),
                        result.getInt("season_id"),
                        result.getInt("event_id"),
                        result.getInt("team_id"),
                        match1, match1_cv, match1_sheet, match2, match2_cv,
                        match2_sheet, match3, match3_cv, match3_sheet, match4,
                        match4_cv, match4_sheet);
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Adds a match score to the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @param match The number of the match.
   *
   * @param score The robot game score for this match.
   *
   * @param cv The core values score for this match.
   *
   * @param sheet The robot game score sheet (JSON) for this match.
   *
   * @return The score ID for the score.
   */
  public int
  scoreMatchAdd(int season_id, int event_id, int team_id, int match,
                Integer score, Integer cv, String sheet)
  {
    int id = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for, insert, and update the match 1 score.
      String sql1 = "select id from score where season_id = " + season_id +
                    " and event_id = " + event_id + " and team_id = " +
                    team_id;
      String sql2 = "insert into score (season_id, event_id, team_id, " +
                    "match" + match + ", match" + match + "_cv, match" +
                    match + "_sheet) values (" + season_id + ", " + event_id +
                    ", " + team_id + ", " + score + ", " + cv + ", " +
                    ((sheet == null) ? null : stmt.enquoteLiteral(sheet)) +
                    ")";
      String sql3 = "update score set match" + match + " = " + score +
                    ", match" + match + "_cv = " + cv + ", match" + match +
                    "_sheet = " +
                    ((sheet == null) ? null : stmt.enquoteLiteral(sheet)) +
                    " where season_id = " + season_id + " and event_id = " +
                    event_id + " and team_id = " + team_id;

      // See if the score already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The score does not exist, so create it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);
      }
      else
      {
        // The score does exist, so update it now.
        if(m_debug)
        {
          System.out.println(sql3);
        }
        stmt.executeUpdate(sql3);
      }

      // Read the ID of the score.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      result = stmt.executeQuery(sql1);
      result.next();

      // Extract the ID from the results.
      id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the ID of the event.
    return(id);
  }

  /**
   * Gets a match score from the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @param match The number of the match.
   *
   * @param callback The function to call to provide the score.
   *
   * @return <b>true</b> if the match score is returned successfully.
   */
  public boolean
  scoreMatchGet(int season_id, int event_id, int team_id, int match,
                MatchScoreMethod callback)
  {
    Integer score, cv;
    String sheet;
    boolean ret = false;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL commands to check for, insert, and update the match 1 score.
      String sql = "select match" + match + ", match" + match + "_cv, match" +
                   match + "_sheet from score " + "where season_id = " +
                   season_id + " and event_id = " + event_id +
                   " and team_id = " + team_id;

      // Get the score from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // See if the query was successful.
      if(result.next())
      {
        // Extract the values from the result.
        score = result.getInt("match" + match);
        score = result.wasNull() ? null : score;
        cv = result.getInt("match" + match + "_cv");
        cv = result.wasNull() ? null : cv;
        sheet = result.getString("match" + match + "_sheet");
      }
      else
      {
        // The score doesn't exist, so return no score.
        score = null;
        cv = null;
        sheet = null;
      }

      // Send the result to the callback.
      callback.accept(score, cv, sheet);

      // Close the SQL statement.
      stmt.close();

      // Success.
      ret = true;
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the result.
    return(ret);
  }

  /**
   * Removes a match score from the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @param match The number of the match.
   *
   * @return <b>true</b> if the match score is removed successfully.
   */
  public boolean
  scoreMatchRemove(int season_id, int event_id, int team_id, int match)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to check for and remove the match 1 score.
      String sql1 = "select id from score where season_id = " + season_id +
                    " and event_id = " + event_id + " and team_id = " +
                    team_id;
      String sql2 = "update score set match" + match + " = null, match" +
                    match + "_cv = null, " + "match" + match +
                    "_sheet = null where season_id = " + season_id +
                    " and event_id = " + event_id + " and team_id = " +
                    team_id;

      // See if the score already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(result.next())
      {
        // The score does exist, so update it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Removes a score from the database.
   *
   * @param id The ID of the score.
   *
   * @return <b>true</b> if the score is remove successfully.
   */
  public boolean
  scoreRemove(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to remove a score.
      String sql = "delete from score where id = " + id;

      // Delete this score from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success
    return(true);
  }

  /**
   * Adds a judging result to the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param team_id The ID of the team.
   *
   * @param project The innovation project score.
   *
   * @param robot_design The robot design score.
   *
   * @param core_values The core values score.
   *
   * @param rubric The rubric choices in JSON format.
   *
   * @return The judging ID for the judging result.
   */
  public int
  judgingAdd(int season_id, int event_id, int team_id, int project,
             int robot_design, int core_values, String rubric)
  {
    int id = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to check for, insert, and update the judging result.
      String sql1 = "select id from judging where season_id = " + season_id +
                    " and event_id = " + event_id + " and team_id = " +
                    team_id;
      String sql2 = "insert into judging (season_id, event_id, team_id, " +
                    "project, robot_design, core_values, rubric) values (" +
                    season_id + ", " + event_id + ", " + team_id + ", " +
                    project + ", " + robot_design + "," + core_values + "," +
                    rubric + ")";
      String sql3 = "update judging set project = " + project +
                    ", robot_design = " + robot_design + ", core_values = " +
                    core_values + ", rubric = " + rubric +
                    " where season_id = " + season_id + " and event_id = " +
                    event_id + " and team_id = " + team_id;

      // See if the judging result already exists in the database.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      ResultSet result = stmt.executeQuery(sql1);
      if(!result.next())
      {
        // The judging result does not exist, so create it now.
        if(m_debug)
        {
          System.out.println(sql2);
        }
        stmt.executeUpdate(sql2);
      }
      else
      {
        // The judging result does exist, so update it now.
        if(m_debug)
        {
          System.out.println(sql3);
        }
        stmt.executeUpdate(sql3);
      }

      // Read the ID of the judging result.
      if(m_debug)
      {
        System.out.println(sql1);
      }
      result = stmt.executeQuery(sql1);
      result.next();

      // Extract the ID from the results.
      id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the ID of the judging result.
    return(id);
  }

  /**
   * Enumerates the judging results in the database.
   *
   * @param season_id The ID of the season.
   *
   * @param event_id The ID of the event.
   *
   * @param callback The function to call for each judging result in the
   *                 database.
   *
   * @return <b>true</b> if the judging results are enumerated successfully.
   */
  public boolean
  judgingEnumerate(int season_id, int event_id, JudgingMethod callback)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to enumreate the judging results.
      String sql = "select * from judging where season_id = " + season_id +
                   " and event_id = " + event_id;

      // Query the judging table.
      if(m_debug)
      {
        System.out.println(sql);
      }
      ResultSet result = stmt.executeQuery(sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Provide the values from this row to the callback.
        callback.accept(result.getInt("id"),
                        result.getInt("season_id"),
                        result.getInt("event_id"),
                        result.getInt("team_id"),
                        result.getInt("project"),
                        result.getInt("robot_design"),
                        result.getInt("core_values"),
                        result.getString("rubric"));
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Removes a judging result from the database.
   *
   * @param id The ID of the judging result.
   *
   * @return <b>true</b> if the judging result is removed successfully.
   */
  public boolean
  judgingRemove(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to remove the judging result.
      String sql = "delete from judging where id = " + id;

      // Delete this judging from the database.
      if(m_debug)
      {
        System.out.println(sql);
      }
      stmt.executeUpdate(sql);

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(false);
    }

    // Success.
    return(true);
  }

  /**
   * Performs initial setup for the database.
   */
  public void
  setup()
  {
    // Connect to the file that contains the database.
    try
    {
      m_instance.m_connection =
        DriverManager.getConnection("jdbc:sqlite:scores.db");
    }
    catch (Exception e)
    {
      m_instance.m_connection = null;
    }

    // Create the required tables in the database.
    if(m_instance.m_connection != null)
    {
      m_instance.createTables();
    }
  }
}