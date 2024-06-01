// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The currently displayed season.
var seasonCurrent = "<!--#season_selected-->";

// Updates the display based on the current season.
function
seasonsUpdate()
{
  // Deactivate all the season tiles.
  $(".seasons_tile").removeClass("active");

  // Activate the tile for the requested season.
  $("#seasons_" + seasonCurrent).addClass("active");
}

// Selected the requested season.
function
seasonsSelect(season)
{
  // There is nothing to be done if the season hasn't changed.
  if(season == seasonCurrent)
  {
    return;
  }

  // Called when the JSON request has completed.
  function
  onDone(result)
  {
    // See if an error was reported by the server.
    if(result["result"] == "error")
    {
      // Reflect the error to the user.
      onError();
    }
    else
    {
      // Switch to the new season and update the display.
      seasonCurrent = season;

      // Update the display to show the new season as active.
      seasonsUpdate();

      // Update the status since the season has changed.
      updateStatus();
    }
  };

  // Called when the JSON request fails.
  function
  onError()
  {
    // Display an error message.
    showError("<!--#str_seasons_error-->", null);
  }

  // Send the updated season to the server.
  $.getJSON("/admin/seasons/seasons.json?year=" + season)
    .done(onDone)
    .fail(onError);
}

// Handles setup of the season tab.
function
seasonsSetup()
{
  // Add the click handlers, or disable the tiles, for the various seasons.
<!--#seasons_js-->

  // Select the default season.
  seasonsUpdate();
}