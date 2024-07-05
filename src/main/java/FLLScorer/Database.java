// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.eclipse.jetty.util.security.Credential;

/**
 * The interface for accepting a match score.
 */
interface MatchScoreMethod
{
  void accept(Integer match, Integer match_cv, String match_sheet);
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
   * for debugging purposes.  This is configurable via the "dbDebug" value in
   * the configuration database table.
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
   * Executes the given SQL statement, which returns a single {@link ResultSet}
   * object.
   *
   * @param stmt The {@link Statement} to use.
   *
   * @param sql The SQL query.
   *
   * @return The {@link ResultSet} from the query.
   *
   * @throws SQLException if a database access error occurs, this method is
   *                      called on a closed {@link Statement}, the given SQL
   *                      statement produces anything other than a single
   *                      {@link ResultSet} object, the method is called on a
   *                      <code>PreparedStatement</code> or
   *                      <code>CallableStatement</code>.
   */
  private ResultSet
  executeQuery(Statement stmt, String sql) throws SQLException
  {
    // If debugging is enabled, print out the SQL statement.
    if(m_debug)
    {
      System.out.println(sql);
    }

    // Execute the SQL statement.
    return(stmt.executeQuery(sql));
  }

  /**
   * Executes the given SQL statement, which may be an INSERT, UPDATE, or
   * DELETE statement or an SQL statement that returns nothing, such as an SQL
   * DDL statement.
   *
   * @param stmt The {@link Statement} to use.
   *
   * @param sql The SQL query.
   *
   * @return The result of the query.
   *
   * @throws SQLException if a database access error occurs, this method is 
   *                      called on a closed {@link Statement}, the given SQL
   *                      statement produces a {@link ResultSet} object, the
   *                      method is called on a <code>PreparedStatement</code>
   *                      or <code>CallableStatement</code>.
   */
  private int
  executeUpdate(Statement stmt, String sql) throws SQLException
  {
    // If debugging is enabled, print out the SQL statement.
    if(m_debug)
    {
      System.out.println(sql);
    }

    // Execute the SQL statement.
    return(stmt.executeUpdate(sql));
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
    String user = "create table if not exists user " +
                  "( " +
                  "  id integer primary key, " +
                  "  name char, " +
                  "  password char, " +
                  "  admin integer, " +
                  "  host integer, " +
                  "  judge integer, " +
                  "  referee integer, " +
                  "  timekeeper integer " +
                  ")";
    String admin_check = "select " +
                         "  id " +
                         "from " +
                         "  user " +
                         "where " +
                         "  name = 'admin'";

    // Attempt to create the tables, catching (and ignoring) any errors that
    // may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // Execute the SQL statement to create the config table.
      executeUpdate(stmt, config);

      // Execute the SQL statement to create the season table.
      executeUpdate(stmt, season);

      // Execute the SQL statement to create the event table.
      executeUpdate(stmt, event);

      // Execute the SQL statement to create the team table.
      executeUpdate(stmt, team);

      // Execute the SQL statement to create the teamAtEvent table.
      executeUpdate(stmt, teamAtEvent);

      // Execute the SQL statement to create the score table.
      executeUpdate(stmt, score);

      // Execute the SQL statement to create the judging table.
      executeUpdate(stmt, judging);

      // Execute the SQL statement to create the user table.
      executeUpdate(stmt, user);

      // Execute the SQL statement to check for the admin user.
      ResultSet result = executeQuery(stmt, admin_check);
      if(result.next() == false)
      {
        // Create the admin user since it does not exist.
        userAdd("admin", Credential.Crypt.crypt("admin", "FLLRocks!"), 1, 0, 0,
                0, 0);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql1);
      if(result.next() == false)
      {
        // The key does not exist, so add it to the database.
        executeUpdate(stmt, sql2);
      }
      else
      {
        // The key exists, so see if the requested value is different than the
        // current value.
        if(!result.getString("value").equals(value))
        {
          // Update the value.
          executeUpdate(stmt, sql3);
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
      executeUpdate(stmt, sql);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The season does not exist, so create it now.
        executeUpdate(stmt, sql2);

        // Read the ID of the newly created season.
        result = executeQuery(stmt, sql1);
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
   * @param id An array for the IDs for the seasons.
   *
   * @param year An array for the years for the seasons.
   *
   * @param name An array for the names for the seasons.
   *
   * @return <b>true</b> if the seasons are enumerated successfully.
   */
  public boolean
  seasonEnumerate(ArrayList<Integer> id, ArrayList<String> year,
                  ArrayList<String> name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the seasons.
      String sql = "select * from season";

      // Query the season table.
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Add the values from this row to the arrays.
        id.add(result.getInt("id"));
        year.add(result.getString("year"));
        name.add(result.getString("name"));
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
      executeUpdate(stmt, sql);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The event does not exist, so create it now.
        executeUpdate(stmt, sql2);

        // Read the ID of the newly created event.
        result = executeQuery(stmt, sql1);
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
      executeUpdate(stmt, sql);

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
   * @param ids The array for the IDs of the events; can be <b>null</b> if the
   *            event IDs are not required.
   *
   * @param season_ids The array for the IDs of the seasons; can be <b>null</b>
   *                   if the season IDs are not required.
   *
   * @param dates The array for the dates of the events; must not be
   *              <b>null</b>.
   *
   * @param matches The array for the number of matches in the events; can be
   *                <b>null</b> if the number of matches are not required.
   *
   * @param name The array for the names of the events; can be <b>null</b> if
   *             the names are not required.
   *
   * @return <b>true</b> if the events are enumerated successfully.
   */
  public boolean
  eventEnumerate(int season_id, ArrayList<Integer> ids,
                 ArrayList<Integer> season_ids, ArrayList<String> dates,
                 ArrayList<Integer> matches, ArrayList<String> names)
  {
    int idx;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the events.
      String sql = "select * from event where season_id = " + season_id;

      // Query the event table.
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Get the date of this event.
        String ldate = result.getString("date");

        // Find where to place this event in date order.
        for(idx = 0; idx < dates.size(); idx++)
        {
          if(ldate.compareTo(dates.get(idx)) < 0)
          {
            break;
          }
        }

        // Insert the data for this event into the lists that have been
        // supplied.
        if(ids != null)
        {
          ids.add(idx, result.getInt("id"));
        }
        if(season_ids != null)
        {
          season_ids.add(idx, result.getInt("season_id"));
        }
        dates.add(idx, ldate);
        if(matches != null)
        {
          matches.add(idx, result.getInt("matches"));
        }
        if(names != null)
        {
          names.add(idx, result.getString("name"));
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      executeUpdate(stmt, sql1);
      executeUpdate(stmt, sql2);
      executeUpdate(stmt, sql3);
      executeUpdate(stmt, sql4);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The team does not exist, so create it now.
        executeUpdate(stmt, sql2);

        // Read the ID of the newly created team.
        result = executeQuery(stmt, sql1);
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
  teamEdit(int team_id, int number, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to edit the team.
      String sql = "update team set number = " + number + ", name = " +
                   stmt.enquoteLiteral(name) + " where id = " + team_id;

      // Edit the team.
      executeUpdate(stmt, sql);

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
   * @param event_id The ID of the event.
   *
   * @param ids The array for the IDs of the teams; can be <b>null</b> if the
   *            IDs are not required.
   *
   * @param numbers The array for the numbers of the teams; must not be
   *                <b>null</b>.
   *
   * @param names The array for the names of the teams; can be <b>null</b> if
   *              the names are not required.
   *
   * @return <b>true</b> if the teams are enumerated successfully.
   */
  public boolean
  teamEnumerate(int season_id, int event_id, ArrayList<Integer> ids,
                ArrayList<Integer> numbers, ArrayList<String> names)
  {
    int idx;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to enumerate the teams.
      String sql = "select * from team where season_id = " + season_id;

      // Query the team table.
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // If an event ID was specified and the team is not at this event, skip
        // it.
        if((event_id != -1) && !teamAtEventGet(event_id, result.getInt("id")))
        {
          continue;
        }

        // Get the number of this team.
        int lnumber = result.getInt("number");

        // Find where to place this event in date order.
        for(idx = 0; idx < numbers.size(); idx++)
        {
          if(lnumber < numbers.get(idx))
          {
            break;
          }
        }

        // Insert the data for this team into the lists that have been
        // supplied.
        if(ids != null)
        {
          ids.add(idx, result.getInt("id"));
        }
        numbers.add(idx, lnumber);
        if(names != null)
        {
          names.add(idx, result.getString("name"));
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      ResultSet result = executeQuery(stmt, sql);
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
      executeUpdate(stmt, sql1);
      executeUpdate(stmt, sql2);
      executeUpdate(stmt, sql3);
      executeUpdate(stmt, sql4);

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
   * @param seasons The array for the IDs of the seasons; can be <b>null</b> if
   *                the season IDs are not required.
   *
   * @param events The array for the IDs of the events; can be <b>null</b> if
   *               the event IDs are not required.
   *
   * @param teams The array for the IDs of the teams; can be <b>null</b> if the
   *              team IDs are not required.
   *
   * @return <b>true</b> if the scores are enumerated successfully.
   */
  public boolean
  teamAtEventEnumerate(int season_id, int event_id, int team_id,
                       ArrayList<Integer> seasons, ArrayList<Integer> events,
                       ArrayList<Integer> teams)
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
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Insert the data for this team into the lists that have been
        // supplied.
        if(seasons != null)
        {
          seasons.add(result.getInt("season_id"));
        }
        if(events != null)
        {
          events.add(result.getInt("event_id"));
        }
        if(teams != null)
        {
          teams.add(result.getInt("team_id"));
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
      ResultSet result = executeQuery(stmt, sql);
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
      executeUpdate(stmt, sql1);
      executeUpdate(stmt, sql2);
      executeUpdate(stmt, sql3);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The team is not at the event, so add it to the event.
        executeUpdate(stmt, sql2);
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
   * @param ids The array for the IDs of the scores; can be <b>null</b> if the
   *            score IDs are not required.
   *
   * @param teams The array for the IDs of the teams; can be <b>null</b> if the
   *              team IDs are not required.
   *
   * @param match1 The array for the match 1 scores; can be <b>null</b> if the
   *               match 1 scores are not required.
   *
   * @param match1_cv The array for the match 1 core values scores; can be
   *                  <b>null</b> if the match 1 core values scores are not
   *                  required.
   *
   * @param match1_sheet The array for the match 1 JSON scoresheets; can be
   *                     <b>null</b> if the match 1 JSON scoresheets are not
   *                     required.
   *
   * @param match2 The array for the match 2 scores; can be <b>null</b> if the
   *               match 2 scores are not required.
   *
   * @param match2_cv The array for the match 2 core values scores; can be
   *                  <b>null</b> if the match 2 core values scores are not
   *                  required.
   *
   * @param match2_sheet The array for the match 2 JSON scoresheets; can be
   *                     <b>null</b> if the match s JSON scoresheets are not
   *                     required.
   *
   * @param match3 The array for the match 3 scores; can be <b>null</b> if the
   *               match 3 scores are not required.
   *
   * @param match3_cv The array for the match 3 core values scores; can be
   *                  <b>null</b> if the match 3 core values scores are not
   *                  required.
   *
   * @param match3_sheet The array for the match 3 JSON scoresheets; can be
   *                     <b>null</b> if the match 3 JSON scoresheets are not
   *                     required.
   *
   * @param match4 The array for the match 4 scores; can be <b>null</b> if the
   *               match 4 scores are not required.
   *
   * @param match4_cv The array for the match 4 core values scores; can be
   *                  <b>null</b> if the match 4 core values scores are not
   *                  required.
   *
   * @param match4_sheet The array for the match 4 JSON scoresheets; can be
   *                     <b>null</b> if the match 4 JSON scoresheets are not
   *                     required.
   *
   * @return <b>true</b> if the scores are enumerated successfully.
   */
  public boolean
  scoreEnumerate(int season_id, int event_id, ArrayList<Integer> ids,
                 ArrayList<Integer> teams, ArrayList<Integer> match1,
                 ArrayList<Integer> match1_cv, ArrayList<String> match1_sheet,
                 ArrayList<Integer> match2, ArrayList<Integer> match2_cv,
                 ArrayList<String> match2_sheet, ArrayList<Integer> match3,
                 ArrayList<Integer> match3_cv, ArrayList<String> match3_sheet,
                 ArrayList<Integer> match4, ArrayList<Integer> match4_cv,
                 ArrayList<String> match4_sheet)
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
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Insert the data for this score into the lists that have been
        // supplied.
        if(ids != null)
        {
          ids.add(result.getInt("id"));
        }
        if(teams != null)
        {
          teams.add(result.getInt("team_id"));
        }
        if(match1 != null)
        {
          Integer score = result.getInt("match1");
          score = result.wasNull() ? null : score;
          match1.add(score);
        }
        if(match1_cv != null)
        {
          Integer score = result.getInt("match1_cv");
          score = result.wasNull() ? null : score;
          match1_cv.add(score);
        }
        if(match1_sheet != null)
        {
          String sheet = result.getString("match1_sheet");
          sheet = result.wasNull() ? null : sheet;
          match1_sheet.add(sheet);
        }
        if(match2 != null)
        {
          Integer score = result.getInt("match2");
          score = result.wasNull() ? null : score;
          match2.add(score);
        }
        if(match2_cv != null)
        {
          Integer score = result.getInt("match2_cv");
          score = result.wasNull() ? null : score;
          match2_cv.add(score);
        }
        if(match2_sheet != null)
        {
          String sheet = result.getString("match2_sheet");
          sheet = result.wasNull() ? null : sheet;
          match2_sheet.add(sheet);
        }
        if(match3 != null)
        {
          Integer score = result.getInt("match3");
          score = result.wasNull() ? null : score;
          match3.add(score);
        }
        if(match3_cv != null)
        {
          Integer score = result.getInt("match3_cv");
          score = result.wasNull() ? null : score;
          match3_cv.add(score);
        }
        if(match3_sheet != null)
        {
          String sheet = result.getString("match3_sheet");
          sheet = result.wasNull() ? null : sheet;
          match3_sheet.add(sheet);
        }
        if(match4 != null)
        {
          Integer score = result.getInt("match4");
          score = result.wasNull() ? null : score;
          match4.add(score);
        }
        if(match4_cv != null)
        {
          Integer score = result.getInt("match4_cv");
          score = result.wasNull() ? null : score;
          match4_cv.add(score);
        }
        if(match4_sheet != null)
        {
          String sheet = result.getString("match4_sheet");
          sheet = result.wasNull() ? null : sheet;
          match4_sheet.add(sheet);
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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The score does not exist, so create it now.
        executeUpdate(stmt, sql2);
      }
      else
      {
        // The score does exist, so update it now.
        executeUpdate(stmt, sql3);
      }

      // Read the ID of the score.
      result = executeQuery(stmt, sql1);
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
  // TODO do not use a callback
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
      ResultSet result = executeQuery(stmt, sql);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(result.next())
      {
        // The score does exist, so update it now.
        executeUpdate(stmt, sql2);
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
      executeUpdate(stmt, sql);

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
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The judging result does not exist, so create it now.
        executeUpdate(stmt, sql2);
      }
      else
      {
        // The judging result does exist, so update it now.
        executeUpdate(stmt, sql3);
      }

      // Read the ID of the judging result.
      result = executeQuery(stmt, sql1);
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
   * @param ids The array for the IDs of the judging item; can be <b>null</b>
   *            if the judging IDs are not required.
   *
   * @param seasons The array for the IDs of the seasons; can be <b>null</b> if
   *                the season IDs are not required.
   *
   * @param events The array for the IDs of the events; can be <b>null</b> if
   *               the event IDs are not required.
   *
   * @param teams The array for the IDs of the teams; can be <b>null</b> if the
   *              team IDs are not required.
   *
   * @param projects The array for the project scores; can be <b>null</b> if
   *                 the project scores are not required.
   *
   * @param robotDesigns The array for the robot design scores; can be
   *                     <b>null</b> if the robot design scores are not
   *                     required.
   *
   * @param coreValues The array for the core values scores; can be <b>null</b>
   *                   if the core values scores are not required.
   *
   * @param rubrics The array for the JSON rubric data; can be <b>null</b> if
   *                the JSON rubric data is not required.
   *
   * @return <b>true</b> if the judging results are enumerated successfully.
   */
  public boolean
  judgingEnumerate(int season_id, int event_id, ArrayList<Integer> ids,
                   ArrayList<Integer> seasons, ArrayList<Integer> events,
                   ArrayList<Integer> teams, ArrayList<Integer> projects,
                   ArrayList<Integer> robotDesigns,
                   ArrayList<Integer> coreValues, ArrayList<String> rubrics)
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
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Insert the data for this team into the lists that have been
        // supplied.
        if(ids != null)
        {
          ids.add(result.getInt("id"));
        }
        if(seasons != null)
        {
          seasons.add(result.getInt("season_id"));
        }
        if(events != null)
        {
          events.add(result.getInt("event_id"));
        }
        if(teams != null)
        {
          teams.add(result.getInt("team_id"));
        }
        if(projects != null)
        {
          projects.add(result.getInt("project"));
        }
        if(coreValues != null)
        {
          coreValues.add(result.getInt("core_values"));
        }
        if(rubrics != null)
        {
          rubrics.add(result.getString("rubrics"));
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

      // Delete this judging result from the database.
      executeUpdate(stmt, sql);

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
   * Adds a user to the database.
   *
   * @param name The user's name.
   *
   * @param password The user's password.
   *
   * @param admin One if the user has the <i>admin</i> role, zero otherwise.
   *
   * @param host One if the user has the <i>host</i> role, zero otherwise.
   *
   * @param judge One if the user has the <i>judge</i> role, zero otherwise.
   *
   * @param referee One if the user has the <i>referee</i> role, zero
   *                otherwise.
   *
   * @param timekeeper One if the user has the <i>timekeeper</i> role, zero
   *                   otherwise.
   *
   * @return The user ID for the added user, or -1 if the user already exists.
   */
  public int
  userAdd(String name, String password, int admin, int host, int judge,
          int referee, int timekeeper)
  {
    int id = -1;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to check for and insert the user.
      String sql1 = "select id from user where name = " +
                    stmt.enquoteLiteral(name);
      String sql2 = "insert into user (name, password, admin, host, judge, " +
                    "referee, timekeeper) values (" +
                    stmt.enquoteLiteral(name) + ", " +
                    stmt.enquoteLiteral(password) + ", " + admin + ", " +
                    host + ", " + judge + ", " + referee + "," + timekeeper +
                    ")";

      // See if the user result already exists in the database.
      ResultSet result = executeQuery(stmt, sql1);
      if(!result.next())
      {
        // The user does not exist, so create it now.
        executeUpdate(stmt, sql2);
      }
      else
      {
        // The user already exists, so return an error.
        stmt.close();
        return(-1);
      }

      // Read the ID of the user.
      result = executeQuery(stmt, sql1);
      result.next();

      // Extract the ID from the results.
      id = result.getInt("id");

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return(-1);
    }

    // Return the ID of the user.
    return(id);
  }

  /**
   * Enumerates the users in the database.
   *
   * @param id An array for the IDs of the users; can be <b>null</b> if the IDs
   *           are not required.
   *
   * @param name An array for the names of the users; must not be <b>null</b>.
   *
   * @param password An array for the passwords of the users; can be
   *                 <b>null</b> if the passwords are not required.
   *
   * @param admin An array for the admin role flags for the users; can be
   *              <b>null</b> if the admin role flags are not requried.
   *
   * @param host An array for the host role flags for the users; can be
   *             <b>null</b> if the host role flags are not required.
   *
   * @param judge An array for the judge role flags for the users; can be
   *              <b>null</b> if the judge role flags are not required.
   *
   * @param referee An array for the referee role flags for the users; can be
   *                <b>null</b> if the referee role flags are not required.
   *
   * @param timekeeper An array for the timekeeper role flags for the users;
   *                   can be <b>null</b> if the timekeeper role flags are not
   *                   required.
   *
   * @return <b>true</b> if the users are enumerated successfully.
   */
  public boolean
  userEnumerate(ArrayList<Integer> id, ArrayList<String> name,
                ArrayList<String> password, ArrayList<Integer> admin,
                ArrayList<Integer> host, ArrayList<Integer> judge,
                ArrayList<Integer> referee, ArrayList<Integer> timekeeper)
  {
    int idx;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to enumreate the users.
      String sql = "select * from user";

      // Query the user table.
      ResultSet result = executeQuery(stmt, sql);

      // Loop through all the rows in the table.
      while(result.next())
      {
        // Get the name of this user.
        String lname = result.getString("name");

        // Find where to place this user in alphabetical order.
        for(idx = 0; idx < name.size(); idx++)
        {
          if(lname.compareTo(name.get(idx)) < 0)
          {
            break;
          }
        }

        // Insert the data for this user into the lists that have been
        // supplied.
        if(id != null)
        {
          id.add(idx, result.getInt("id"));
        }
        name.add(idx, lname);
        if(password != null)
        {
          password.add(idx, result.getString("password"));
        }
        if(admin != null)
        {
          admin.add(idx, result.getInt("admin"));
        }
        if(host != null)
        {
          host.add(idx, result.getInt("host"));
        }
        if(judge != null)
        {
          judge.add(idx, result.getInt("judge"));
        }
        if(referee != null)
        {
          referee.add(idx, result.getInt("referee"));
        }
        if(timekeeper != null)
        {
          timekeeper.add(idx, result.getInt("timekeeper"));
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
   * Gets the name of a user.
   *
   * @param id The ID of the user.
   *
   * @return The name of the user.
   */
  public String
  userNameGet(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to set the user name.
      String sql = "select name from user where id = " + id;

      // Get the name of this user from the database.
      ResultSet result = executeQuery(stmt, sql);

      // Return an empty string if the user was not found.
      if(result.next() == false)
      {
        return("");
      }

      // Get the name of the user.
      String name = result.getString("name");

      // Close the SQL statement.
      stmt.close();

      // Return the name of the user.
      return(name);
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return("");
    }
  }

  /**
   * Sets the name of a user.
   *
   * @param id The ID of the user.
   *
   * @param name The name of the user.
   *
   * @return <b>true</b> if the user name is updated successfully.
   */
  public boolean
  userNameSet(int id, String name)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to set the user name.
      String sql = "update user set name = " + stmt.enquoteLiteral(name) +
                   " where id = " + id;

      // Update the name of this user in the database.
      executeUpdate(stmt, sql);

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
   * Gets the password of a user.
   *
   * @param id The ID of the user.
   *
   * @return The password of the user.
   */
  public String
  userPasswordGet(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to get the password.
      String sql = "select password from user where id = " + id;

      // Get the password for this user from the database.
      ResultSet result = executeQuery(stmt, sql);

      // Return an empty string if the user was not found.
      if(result.next() == false)
      {
        return("");
      }

      // Get the password.
      String password = result.getString("password");

      // Close the SQL statement.
      stmt.close();

      // Return the password.
      return(password);
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
      return("");
    }
  }

  /**
   * Sets the password of a user.
   *
   * @param id The ID of the user.
   *
   * @param password The password of the user.
   *
   * @return <b>true</b> if the password is updated successfully.
   */
  public boolean
  userPasswordSet(int id, String password)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to set the password.
      String sql = "update user set password = " +
                   stmt.enquoteLiteral(password) + " where id = " + id;

      // Update the password of this user in the database.
      executeUpdate(stmt, sql);

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
   * Validates the password of a user.
   *
   * @param id The ID of the user.
   *
   * @param password The user's password.
   *
   * @return <b>true</b> if the password matches.
   */
  public boolean
  userPasswordValidate(int id, String password)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to get the password.
      String sql = "select password from user where id = " + id;

      // Get the password of this user from the database.
      ResultSet result = executeQuery(stmt, sql);

      // If there is not a matching user, or the password for the user does not
      // match, return a failure.
      if((result.next() == false) ||
         !result.getString("password").equals(password))
      {
        stmt.close();
        return(false);
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
   * Validates a user and their password.
   *
   * @param name The name of the user.
   *
   * @param password The user's password.
   *
   * @return <b>true</b> if the user exists and the password matches.
   */
  public boolean
  userPasswordValidate(String name, String password)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to get the password.
      String sql = "select password from user where name = " +
                   stmt.enquoteLiteral(name);

      // Get the password of this user from the database.
      ResultSet result = executeQuery(stmt, sql);

      // If there is not a matching user, or the password for the user does not
      // match, return a failure.
      if((result.next() == false) ||
         !result.getString("password").equals(password))
      {
        stmt.close();
        return(false);
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
   * Removes a user from the database.
   *
   * @param id The ID of the user.
   *
   * @return <b>true</b> if the user is removed successfully.
   */
  public boolean
  userRemove(int id)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL statement to remove the user.
      String sql = "delete from user where id = " + id;

      // Delete this user from the database.
      executeUpdate(stmt, sql);

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
   * Adds a role to a user.
   *
   * @param id The ID of the user.
   *
   * @param role The role to add.
   *
   * @return <b>true</b> if the role was added successfully.
   */
  public boolean
  userRoleAdd(int id, String role)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to add the role to the user.
      String sql1 = "update user set admin = 1 where id = " + id;
      String sql2 = "update user set host = 1 where id = " + id;
      String sql3 = "update user set judge = 1 where id = " + id;
      String sql4 = "update user set referee = 1 where id = " + id;
      String sql5 = "update user set timekeeper = 1 where id = " + id;

      // See if this is the admin role.
      if(role.equals("admin"))
      {
        // Add the admin role to this user.
        executeUpdate(stmt, sql1);
      }

      // See if this is the host role.
      else if(role.equals("host"))
      {
        // Add the host role to this user.
        executeUpdate(stmt, sql2);
      }

      // See if this is the judge role.
      else if(role.equals("judge"))
      {
        // Add the judge role to this user.
        executeUpdate(stmt, sql3);
      }

      // See if this is the referee role.
      else if(role.equals("referee"))
      {
        // Add the referee role to this user.
        executeUpdate(stmt, sql4);
      }

      // See if this is the timekeeper role.
      else if(role.equals("timekeeper"))
      {
        // Add the timekeeper role to this user.
        executeUpdate(stmt, sql5);
      }

      // Otherwise, this is an unknown role.
      else
      {
        stmt.close();
        return(false);
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
   * Determines if a user has a given role.
   *
   * @param id The ID of the user.
   *
   * @param role The role to query.
   *
   * @return <b>1</b> if the user has the given role.
   */
  public int
  userRoleGet(int id, String role)
  {
    ResultSet result = null;
    int hasRole = 0;

    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to remove the role to the user.
      String sql1 = "select admin from user where id = " + id;
      String sql2 = "select host from user where id = " + id;
      String sql3 = "select judge from user where id = " + id;
      String sql4 = "select referee from user where id = " + id;
      String sql5 = "select timekeeper from user where id = " + id;

      // See if this is the admin role.
      if(role.equals("admin"))
      {
        // Query the admin role for this user.
        result = executeQuery(stmt, sql1);
      }

      // See if this is the host role.
      else if(role.equals("host"))
      {
        // Query the host role for this user.
        result = executeQuery(stmt, sql2);
      }

      // See if this is the judge role.
      else if(role.equals("judge"))
      {
        // Query the judge role for this user.
        result = executeQuery(stmt, sql3);
      }

      // See if this is the referee role.
      else if(role.equals("referee"))
      {
        // Query the referee role for this user.
        result = executeQuery(stmt, sql4);
      }

      // See if this is the timekeeper role.
      else if(role.equals("timekeeper"))
      {
        // Query the timekeeper role for this user.
        result = executeQuery(stmt, sql5);
      }

      // See if there is a result, and it matched a user.
      if((result != null) && (result.next() == true))
      {
        // Determine if the user has the given role.
        hasRole = result.getInt(role);
      }

      // Close the SQL statement.
      stmt.close();
    }
    catch (Exception e)
    {
      System.out.println("JDBC error: " + e);
    }

    // Return the status of the user having this role.
    return(hasRole);
  }

  /**
   * Removes a role from a user.
   *
   * @param id The ID of the user.
   *
   * @param role The role to remove.
   *
   * @return <b>true</b> if the role was rmeoved successfully.
   */
  public boolean
  userRoleRemove(int id, String role)
  {
    // Catch (and ignore) any errors that may occur.
    try
    {
      // Create a SQL statement.
      Statement stmt = m_connection.createStatement();

      // The SQL command to remove the role to the user.
      String sql1 = "update user set admin = 0 where id = " + id;
      String sql2 = "update user set host = 0 where id = " + id;
      String sql3 = "update user set judge = 0 where id = " + id;
      String sql4 = "update user set referee = 0 where id = " + id;
      String sql5 = "update user set timekeeper = 0 where id = " + id;

      // See if this is the admin role.
      if(role.equals("admin"))
      {
        // Remove the admin role from this user.
        executeUpdate(stmt, sql1);
      }

      // See if this is the host role.
      else if(role.equals("host"))
      {
        // Remove the host role from this user.
        executeUpdate(stmt, sql2);
      }

      // See if this is the judge role.
      else if(role.equals("judge"))
      {
        // Remove the judge role from this user.
        executeUpdate(stmt, sql3);
      }

      // See if this is the referee role.
      else if(role.equals("referee"))
      {
        // Remove the referee role from this user.
        executeUpdate(stmt, sql4);
      }

      // See if this is the timekeeper role.
      else if(role.equals("timekeeper"))
      {
        // Remove the timekeeper role from this user.
        executeUpdate(stmt, sql5);
      }

      // Otherwise, this is an unknown role.
      else
      {
        stmt.close();
        return(false);
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

    // See if the connection to the database was successful.
    if(m_instance.m_connection != null)
    {
      // Create the required tables in the database.
      m_instance.createTables();

      // Get the value of the database debug configuration value.
      String debug = configValueGet("dbDebug");

      // If the configuration value exists and has a non-zero value, then
      // database debug logging is enabled.
      if((debug != null) && (Integer.parseInt(debug) != 0))
      {
        m_debug = true;
      }
    }
  }
}