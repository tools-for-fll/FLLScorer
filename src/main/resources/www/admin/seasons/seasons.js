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

  // Get the year for the current season.
  var year = parseInt(seasonCurrent);

  // Get the alternate scoresheet for the current season.
  var game = parseInt(seasonCurrent.substring(5));

  // Activate the tile for the requested season.
  $("#seasons_" + year).addClass("active");

  // If there is an alternate scoresheet, select it.
  $("#scoresheet_" + year).val(game);
}

// Select the requested season.
function
seasonsSelect(season)
{
  // See if there is an alternate scoresheet for this season.
  game = $("#scoresheet_" + season);
  if(game.length != 0)
  {
    season += "." + game.val();
  }
  else
  {
    season += ".0";
  }

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

// Handles keydown events.
function
seasonsKeydown(e)
{
  // See if Ctrl-F was pressed.
  if(((e.key == 'f') || (e.key == 'F')) && (e.ctrlKey == true))
  {
    // Toggle the full screen state of the window.
    if(!document.fullscreenElement)
    {
      document.documentElement.requestFullscreen();
    }
    else
    {
      document.exitFullscreen();
    }

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }
}

// Handles setup of the season tab.
function
seasonsSetup()
{
  // Add the click handlers, or disable the tiles, for the various seasons.
<!--#seasons_js-->

  // Select the default season.
  seasonsUpdate();

  // Add a keydown event listener.
  document.addEventListener("keydown", seasonsKeydown);
}

// Handles cleanup of the seasons tab.
function
seasonsCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", seasonsKeydown);
}