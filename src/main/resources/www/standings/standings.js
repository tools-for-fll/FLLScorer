// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The data to display on the standings.
var data = null;

// The division that is being displayed; is null when divisions are not
// enabled.
var division = null;

// The standings that are being displayed.
var standings = null;

// The timer.
var timer = null;

// The current screen of data to display.
var index = 0;

// Whether the standings display is paused.
var paused = false;

// Loads the standings data from the server.
function
loadData()
{
  // Called when the latest standings is fetched successfully.
  function
  onDone(newData)
  {
    // Save the newly retreived data.
    data = newData;

    // See if divisions are enabled, and set the division and standings
    // appropriately.
    if(data["divisions"] == null)
    {
      division = null;
      standings = data["standings"];
    }
    else
    {
      division = 0;
      standings = data["standings"][0];
    }
  }

  // Called when the standings fetch fails.
  function
  onFail()
  {
    // Since the fetch failed, clear the data since it is stale.
    data = null;
    division = null;
    standings = null;
  }

  // Called after every standings fetch, regardless of status.
  function
  onAlways()
  {
    // See if the timer has been started.
    if(timer == null)
    {
      // "Trigger" the timer so that the display is updated immediately.
      runTimer();

      // Start the timer, which will trigger for the first time after the
      // interval.
      timer = setInterval(runTimer, 10000);
    }
  }

  // Fetch the latest standings from the server.
  $.getJSON("/standings/standings.json")
    .done(onDone)
    .fail(onFail)
    .always(onAlways);
}

// Runs the timer, executed every ten seconds.
function
runTimer()
{
  // Do nothing if the standings update is paused.
  if(paused)
  {
    return;
  }

  // See if the first screen of standings is about to be displayed.
  if(index == 0)
  {
    // Set the standings title.
    if(data != null)
    {
      if(division != null)
      {
        var html = "<div class='divisions'>";
        html += "<span><!--#str_standings_subtitle--></span>";
        html += "<span class='division'>" + data["divisions"][division] +
                "</span>";
        html += "</div>";
        $(".event").html(html);
        document.documentElement.style.setProperty("--accent-color",
                                                   "var(" +
                                                   data["colors"][division] +
                                                   ")");
      }
      else
      {
        $(".event").html("<!--#str_standings_subtitle-->");
        document.documentElement.style.setProperty("--accent-color",
                                                   "var(" + data["color"] +
                                                   ")");
      }
    }
    else
    {
      $(".event").html("<!--#str_connecting-->");
    }
  }

  // Loop through the eight possible standings on this screen.
  for(i = 0; i < 8; i++)
  {
    // Remove the background from this row. It will be added if necessary.
    $(".pl" + i).html("").removeClass("gray");
    $(".num" + i).html("").removeClass("gray");
    $(".name" + i).html("").removeClass("gray");
    $(".scr" + i).html("").removeClass("gray");
    $(".e1" + i).html("").removeClass("gray");
    $(".e2" + i).html("").removeClass("gray");

    // See if there is an entry for this position.
    if((data != null) && (standings != null) && (standings[index + i] != null))
    {
      // Update this row with this entry's information.
      $(".pl" + i).html(standings[index + i]["place"]);
      $(".num" + i).html(standings[index + i]["num"]);
      $(".name" + i).html(standings[index + i]["name"]);
      if(standings[index + i]["score"] != null)
      {
        $(".scr" + i).html(standings[index + i]["score"]);
      }
      if(standings[index + i]["event1"] != null)
      {
        $(".e1" + i).html(standings[index + i]["event1"]);
      }
      if(standings[index + i]["event2"] != null)
      {
        $(".e2" + i).html(standings[index + i]["event2"]);
      }

      // If this is an odd row, set the background to gray.
      if(i & 1)
      {
        $(".pl" + i).addClass("gray");
        $(".num" + i).addClass("gray");
        $(".name" + i).addClass("gray");
        $(".scr" + i).addClass("gray");
        $(".e1" + i).addClass("gray");
        $(".e2" + i).addClass("gray");
      }
    }
  }

  // The next timer iteration should show the next screen, or the first screen
  // if all of the teams have been displayed.
  index += 8;
  if((data == null) || (standings == null) || (standings[index] == null))
  {
    // See if divisions are enabled and there is another division.
    if((data != null) && (division != null) &&
        (data["divisions"][division + 1] != null))
    {
      // Start at the beginning of the next division.
      division++;
      standings = data["standings"][division];
      index = 0;
    }
    else
    {
      // Go back to the first screen.
      index = 0;

      // Fetch updated standings data from the server.
      loadData();
    }
  }
}

// Handles keydown events.
function
onKeydown(e)
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

  // See if Ctrl-P was pressed.
  if(((e.key == 'p') || (e.key == 'P')) && (e.ctrlKey == true))
  {
    // Toggle the pause state.
    paused = !paused;

    // Toggle the state of the pause indicator.
    if(paused)
    {
      $(".paused").css("display", "flex");
    }
    else
    {
      $(".paused").css("display", "none");
    }

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }
}

// Handles setup of the standings.
function
ready()
{
  // Add a keydown event listener.
  document.addEventListener("keydown", onKeydown);

  // Manually run the standings the first time.
  runTimer();
}

// Set the function to call when the page is ready.
$(document).ready(ready);