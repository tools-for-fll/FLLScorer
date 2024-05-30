// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the events tab.
 */
public class Events
{
  /**
   * The object for the Events singleton.
   */
  private static Events m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The Database object.
   */
  private Database m_database = null;

  /**
    * The Config object.
    */
  private Config m_config = null;

  /**
   * The Season object.
   */
  private Seasons m_season = null;

  /**
   * Gets the Events singleton object, creating it if necessary.
   *
   * @return Returns the Events singleton.
   */
  public static Events
  getInstance()
  {
    // Create the Events object if required.
    if(m_instance == null)
    {
      m_instance = new Events();
    }

    // Return the Events object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Events()
  {
  }

  /**
   * Gets the event_id for the currently selected event.
   *
   * @return The event_id for the currently selected event.
   */
  public int
  eventIdGet()
  {
    // Get the currently selected event.
    String event = m_config.eventGet();

    // Return the event_id.
    return((event == null) ? -1 : Integer.parseInt(m_config.eventGet()));
  }

  /**
   * Gets the number of matches at the currently selected event.
  *
   * @return The number of matches at the currently selected event.
   */
  public int
  eventMatchesGet()
  {
    // Get the event_id for this event, returning an error if there is not an
    // event selected.
    int event_id = eventIdGet();
    if(event_id == -1)
    {
      return(-1);
    }

    // Get the number of matches at this event.
    return(m_database.eventGetMatches(event_id));
  }

  /**
   * Gets the list of events for a season.
   *
   * @param season_id The ID of the season to query.
   *
   * @param ids An array that is filled in with the IDs of the events.
   *
   * @param dates An array that is filled in with the date of the events.
   *
   * @param matches An array that is filled in with the number of robot game
   *                matches at the events.
   *
   * @param names An array that is filled in with the names of the events.
   *
   * @param scores An array that is filled in with a boolean indicating if
   *               the event has scores.
   */
  private void
  getEvents(int season_id, ArrayList<Integer> ids, ArrayList<String> dates,
            ArrayList<Integer> matches, ArrayList<String> names,
            ArrayList<Boolean> scores)
  {
      // Enumerate the events from the database for this season.
      m_database.eventEnumerate(season_id,
                                (id, l_season_id, date, l_matches, name) ->
        {
          // Find the place in the list to insert this date in chronological
          // order.
          int i;
          for(i = 0; i < dates.size(); i++)
          {
            if(date.compareTo(dates.get(i)) < 0)
            {
              break;
            }
          }

          // Add this event to the lists.
          if(ids != null)
          {
            ids.add(i, id);
          }
          dates.add(i, date);
          if(matches != null)
          {
            matches.add(i, l_matches);
          }
          if(names != null)
          {
            names.add(i, name);
          }
          if(scores != null)
          {
            scores.add(i, m_database.eventHasScores(id));
          }
        });
  }

  /**
   * Handles requests for /events/events.json.
   *
   * @param path The path from the request.
   *
   * @param parameters The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveEvents(String path, String parameters)
  {
    String action = null, id = null, date = null, matches = null, name = null;
    JSONObject result = new SimpleJSONObject();
    int season_id = m_season.seasonIdGet();
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

        // See if this is the "date" key.
        if(items[0].equals("date"))
        {
          // Convert and save the date for later use.
          date = URLDecoder.decode(items[1], StandardCharsets.UTF_8);
        }

        // See if this is the "matches" key.
        if(items[0].equals("matches"))
        {
          // Save the number of matches for later use.
          matches = items[1];
        }

        // See if this is the "name" key.
        if(items[0].equals("name"))
        {
          // Convert and save the name for later use.
          name = URLDecoder.decode(items[1], StandardCharsets.UTF_8);
        }
      }
    }

    // See if the action was specified and is "add", for adding an event.
    if("add".equals(action) && (date != null) && (matches != null) &&
       (name != null))
    {
      // See if there is already an event with the given date and number.
      if(m_database.eventGetId(season_id, date, Integer.parseInt(matches),
                             name) >= 0)
      {
        // Return an error since the event already exists.
        result.set("result", m_webserver.getSSI("str_events_already_exists"));
      }

      // Add the event to the database.
      else if(m_database.eventAdd(season_id, date, Integer.parseInt(matches),
                                  name) >= 0)
      {
        // Ensure that there is a selected event.
        selectDefault();

        // Return success since the event was added.
        result.set("result", "ok");
      }

      // Otherwise, an error occurred when adding the event.
      else
      {
        // Return an error.
        result.set("result", m_webserver.getSSI("str_events_add_failed"));
      }
    }

    // See if the action was specified and is "delete", for deleting an event.
    else if("delete".equals(action) && (id != null))
    {
      // See if the event exists.
      if(Integer.parseInt(id) < 0)
      {
        // Return an error since the event does not exist.
        result.set("result", m_webserver.getSSI("str_events_doesnt_exist"));
      }
      else
      {
        // Delete the event from the database.
        m_database.eventRemove(Integer.parseInt(id));

        // Ensure that there is a selected event.
        selectDefault();

        // Return success since the event was deleted.
        result.set("result", "ok");
      }
    }

    // See if the action was specified and is "edit", for editing an event.
    else if("edit".equals(action) && (id != null) && (date != null) &&
            (matches != null) && (name != null))
    {
      // Edit the event in the database.
      if(m_database.eventEdit(Integer.parseInt(id), date,
                              Integer.parseInt(matches), name) == true)
      {
        // Return success since the event was edited.
        result.set("result", "ok");
      }

      // Otherwise, an error occurred when editing the event.
      else
      {
        // Return an error since the event could not be edited.
        result.set("result", m_webserver.getSSI("str_events_edit_failed"));
      }
    }

    // See if the action was specified and is "select", for selecting an event.
    else if("select".equals(action) && (id != null))
    {
      // Select this event.
      m_config.eventSet(id);

      // Return success since the event was selected.
      result.set("result", "ok");
    }

    // Otherwise, return a list of the events.
    else
    {
      // A list of information about events, maintained in event date order.
      ArrayList<Integer> ids = new ArrayList<Integer>();
      ArrayList<String> dates = new ArrayList<String>();
      ArrayList<Integer> matchList = new ArrayList<Integer>();
      ArrayList<String> names = new ArrayList<String>();
      ArrayList<Boolean> scores = new ArrayList<Boolean>();

      // Enumerate the events from the database for this season.
      getEvents(season_id, ids, dates, matchList, names, scores);

      // Get the ID of the currently selected event.
      int event_id = ((m_config.eventGet() != null) ?
                      Integer.parseInt(m_config.eventGet()) : -1);

      // Loop through the events.
      JSONArray events = new SimpleJSONArray();
      for(int i = 0; i < dates.size(); i++)
      {
        // Add this event to the event array.
        JSONObject event = new SimpleJSONObject();
        event.set("id", ids.get(i));
        event.set("date", dates.get(i));
        event.set("matches", matchList.get(i));
        event.set("name", names.get(i));
        if(scores.get(i))
        {
          event.set("hasScores", true);
        }
        if(ids.get(i) == event_id)
        {
          event.set("selected", true);
        }
        events.addEntry(event);
      }

      // Add the event array to the JSON response.
      result.set("events", events);
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
   * Updates the currently selected event.
   */
  public void
  selectDefault()
  {
    int season_id = m_season.seasonIdGet();
    int event_id = (m_config.eventGet() != null) ?
                   Integer.parseInt(m_config.eventGet()) : -1;

    // A list of information about events, maintained in event date order.
    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<String> dates = new ArrayList<String>();

    // Enumerate the events from the database for this season.
    getEvents(season_id, ids, dates, null, null, null);

    // Loop through the events.
    for(int idx = 0; idx < ids.size(); idx++)
    {
      // If this is the currently selected event, then the default does not
      // need to be changed.
      if(ids.get(idx) == event_id)
      {
        return;
      }
    }

    // Select the first event from the list.
    if(ids.size() == 0)
    {
      m_config.eventSet(null);
    }
    else
    {
      m_config.eventSet(ids.get(0).toString());
    }
  }

  /**
   * Performs initial setup for the events handler.
   */
  public void
  setup()
  {
    // Get references to the web server, database, and the season objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();
    m_config = Config.getInstance();
    m_season = Seasons.getInstance();

   // Register the dynamic handler for the events.json file.
   m_webserver.registerDynamicFile("/events/events.json", this::serveEvents);
  }
}