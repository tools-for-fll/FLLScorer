// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

public class Main
{
  public static void
  main(String[] args)
  {
    System.setProperty("slf4j.internal.verbosity", "ERROR");
    Database.getInstance().setup();
    Config.getInstance().setup();
    WebServer.getInstance().setup();
    Config.getInstance().finishSetup();
    Scoreboard.getInstance().setup();
    Standings.getInstance().setup();
    Seasons.getInstance().setup();
    Events.getInstance().setup();
    Teams.getInstance().setup();
    Scores.getInstance().setup();
    Rubrics.getInstance().setup();
    Users.getInstance().setup();
    Links.getInstance().setup();
    Judge.getInstance().setup();
    Referee.getInstance().setup();
    TimeKeeper.getInstance().setup();
    Timer.getInstance().setup();
    WebServer.getInstance().run();
    Gui.getInstance().setup();
  }
}