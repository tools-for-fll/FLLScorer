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
  $.getJSON("/admin/seasons/seasons.json?action=set&year=" + season)
    .done(onDone)
    .fail(onError);
}

// Loads the list of seasons from the server.
function
seasonsLoad()
{
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
      // Generate a HTML fragment with tiles for each season.
      var html = "";

      // Loop through the seasons.
      for(var idx = result["seasons"].length - 1; idx >= 0; idx--)
      {
        // Get the pertinent details about this season.
        var index = result["seasons"][idx]["index"];
        var year = result["seasons"][idx]["year"];
        var name = result["seasons"][idx]["name"];
        var enabled = result["seasons"][idx]["enabled"];

        // Get the year of this season.
        var season = index.substring(0, 4);

        // Generate the HTML for this season's tile.
        html += `<div id="seasons_${season}" class="seasons_tile` +
                `${enabled ? "" : " disabled"}">`;
        html += `<img src="logos/${season}.png">`;

        // See if this season has an alternate game.
        if(index.includes(".0"))
        {
          // There is not an alternate game for this season, so simply have the
          // name of the season.
          html += `<p>${year} ${name}</p>`;
        }
        else
        {
          // There is at least one alternate game for this season, so create a
          // select for choosing the alternate game.
          html += `<select id="scoresheet_${season}">`;

          // Get the number of alternative games for this season.
          var idx2 = 0;
          while(result["seasons"][idx - idx2 - 1]["index"].substring(0, 4) ===
                season)
          {
            idx2++;
          }

          // Add an option for the standard game for the season.
          html += `<option value="0">` +
                  `${result["seasons"][idx - idx2]["year"]} ` +
                  `${result["seasons"][idx - idx2]["name"]}</option>`;

          // Loop through the alternate games.
          for(var idx3 = 1; idx3 <= idx2; idx3++)
          {
            // Add an option for this alternate game.
            html += `<option value="${idx3}">` +
                    `${result["seasons"][idx - idx2 + idx3]["name"]}</option>`;
          }

          // Close out the select.
          html += `</select>`;

          // Decrement the index into the seasons by the number of alternate
          // games (so they are not listed twice).
          idx -= idx2;
        }

        // Close out the tile.
        html += `</div>`;
      }

      // Add some space at the end of the list of seasons.
      html += `<br clear="all">`;
      html += `<br>`;
      html += `<br>`;

      // Insert the HTML fragment into the container, displaying the tiles for
      // all the seasons.
      $(".seasons_container").html(html);

      // Loop through the seasons, adding action handlers to each of the tiles
      // where appropriate.
      var prev = "";
      for(var idx = result["seasons"].length - 1; idx >= 0; idx--)
      {
        // Get the year of this season.
        const season = result["seasons"][idx]["index"].substring(0, 4);

        // If this is the same year as the previous season (meaning it is an
        // alternate game), it has already been handled and can be skipped.
        if(season === prev)
        {
          continue;
        }

        // Get the year of the next season.
        var season2;
        if(idx != 0)
        {
          season2 = result["seasons"][idx - 1]["index"].substring(0, 4);
        }
        else
        {
          season2 = "";
        }

        // See if this season is enabled.
        if(result["seasons"][idx]["enabled"])
        {
          // Add the click handlers to this tile, and set the tab index so that
          // it can be reached via keyboard navigation.
          $(`#seasons_${season}`).click(() => { seasonsSelect(`${season}`); });
          $(`#seasons_${season}`).attr("tabindex", "0");
          $(`#seasons_${season}`).on("keyup",
            (event) =>
            {
              if(event.key == "Enter")
              {
                $(`#seasons_${season}`).click();
                event.preventDefault();
              }
            });

          // Add the change event handler to the select if there are alternate
          // games for this season.
          if(season === season2)
          {
            $(`#scoresheet_${season}`).on("change",
              () => seasonsSelect(`${season}`));
          }
        }

        // This season is now the previous season.
        prev = season;
      }

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

  // Get the list of seasons from the server.
  $.getJSON("/admin/seasons/seasons.json?action=list")
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

    // Do not allow this key event to further propagate.
    e.stopPropagation();
  }

  // See if Ctrl-R was pressed.
  if(((e.key == 'r') || (e.key == 'R')) && (e.ctrlKey == true))
  {
    // Re-load the seasons from the server.
    seasonsLoad();

    // Do not allow this key event to further propagate.
    e.stopPropatation();
  }
}

// Handles setup of the season tab.
function
seasonsSetup()
{
  // Load the seasons.
  seasonsLoad();

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