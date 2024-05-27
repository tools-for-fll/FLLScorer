// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.nio.charset.StandardCharsets;

/**
 * Handles the scoreboard.
 */
public class Scoreboard
{
  /**
   * The object for the Scoreboard singleton.
   */
  private static Scoreboard m_instance = null;

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
   * Serves the JSON data for the current state of the scoreboard.
   *
   * @param path The path that was requested.
   *
   * @param parameters The parameters associated with the request.
   *
   * @return A byte array containing the JSON data for the scoreboard.
   */
  public byte[]
  serveScoreJson(String path, String parameters)
  {
    String str = new String();

    str += "{";
    str += "  \"event\": \"Lakeline Mall League Event\",";
    str += "  \"matches\": 3,";
    str += "  \"scores\":";
    str += "  [";
    str += "    {";
    str += "      \"place\": 1,";
    str += "      \"num\": 13202,";
    str += "      \"name\": \"RoboSaders\",";
    str += "      \"high\": 550,";
    str += "      \"m1\": 520,";
    str += "      \"m2\": 550";
    str += "    },";
    str += "    {";
    str += "      \"place\": 2,";
    str += "      \"num\": 16747,";
    str += "      \"name\": \"BioBots\",";
    str += "      \"high\": 550,";
    str += "      \"m1\": 550,";
    str += "      \"m2\": 500,";
    str += "      \"m3\": 500";
    str += "    },";
    str += "    {";
    str += "      \"place\": 3,";
    str += "      \"num\": 33040,";
    str += "      \"name\": \"CyberWolves\",";
    str += "      \"high\": 400,";
    str += "      \"m1\": 400";
    str += "    },";
    str += "    {";
    str += "      \"place\": 4,";
    str += "      \"num\": 14938,";
    str += "      \"name\": \"Club Oreo\",";
    str += "      \"high\": 390,";
    str += "      \"m1\": 390";
    str += "    },";
    str += "    {";
    str += "      \"place\": 5,";
    str += "      \"num\": 43050,";
    str += "      \"name\": \"Smart Cookies\",";
    str += "      \"high\": 380,";
    str += "      \"m1\": 380";
    str += "    },";
    str += "    {";
    str += "      \"place\": 6,";
    str += "      \"num\": 45539,";
    str += "      \"name\": \"Trefoil Techies\",";
    str += "      \"high\": 370,";
    str += "      \"m1\": 370";
    str += "    },";
    str += "    {";
    str += "      \"place\": 7,";
    str += "      \"num\": 100,";
    str += "      \"name\": \"Fire Breathing Burritos\",";
    str += "      \"high\": 360,";
    str += "      \"m1\": 360";
    str += "    },";
    str += "    {";
    str += "      \"place\": 8,";
    str += "      \"num\": 200,";
    str += "      \"name\": \"Apple 3.14\",";
    str += "      \"high\": 350,";
    str += "      \"m1\": 350";
    str += "    },";
    str += "    {";
    str += "      \"place\": 9,";
    str += "      \"num\": 51687,";
    str += "      \"name\": \"Perseverance\",";
    str += "      \"high\": 340,";
    str += "      \"m1\": 340";
    str += "    },";
    str += "    {";
    str += "      \"place\": 10,";
    str += "      \"num\": 60,";
    str += "      \"name\": \"Chocolate Covered Llamas with Ice Cream on Top\",";
    str += "      \"high\": 330,";
    str += "      \"m1\": 330";
    str += "    },";
    str += "    {";
    str += "      \"place\": 10,";
    str += "      \"num\": 70,";
    str += "      \"name\": \"Fellowship of the Brick\",";
    str += "      \"high\": 330,";
    str += "      \"m1\": 330";
    str += "    },";
    str += "    {";
    str += "      \"place\": 12,";
    str += "      \"num\": 80,";
    str += "      \"name\": \"Pythoneers\",";
    str += "      \"high\": 320,";
    str += "      \"m1\": 320,";
    str += "      \"m2\": 320";
    str += "    }";
    str += "  ]";
    str += "}";

    // Convert the string object into a byte array and return it.
    return(str.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Performs initial setup for the scoreboard.
   */
  public void
  setup()
  {
    // Register the dynamic handler for the scoreboard.json file.
    WebServer.getInstance().registerDynamicFile("/scoreboard/scoreboard.json",
                                                this::serveScoreJson);
  }
}