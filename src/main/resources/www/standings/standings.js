// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Strings for the headings.
const placeHeading = "<!--#str_standings_place-->";
const teamNumber = "<!--#str_standings_number-->";
const teamName = "<!--#str_standings_name-->";
const scoreHeading = "<!--#str_standings_score-->";
const event1Heading = "<!--#str_standings_event1-->";
const event2Heading = "<!--#str_standings_event2-->";

// The data to display on the standings.
var data = null;

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
  }

  // Called when the standings fetch fails.
  function
  onFail()
  {
    // Since the fetch failed, clear the data since it is stale.
    data = null;
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
      $(".event").html("<!--#str_standings_subtitle-->");
    }
    else
    {
      $(".event").html("Connecting...");
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
    if((data != null) && (data["standings"] != null) &&
       (data["standings"][index + i] != null))
    {
      // Update this row with this entry's information.
      $(".pl" + i).html(data["standings"][index + i]["place"]);
      $(".num" + i).html(data["standings"][index + i]["num"]);
      $(".name" + i).html(data["standings"][index + i]["name"]);
      if(data["standings"][index + i]["score"] != null)
      {
        $(".scr" + i).html(data["standings"][index + i]["score"]);
      }
      if(data["standings"][index + i]["event1"] != null)
      {
        $(".e1" + i).html(data["standings"][index + i]["event1"]);
      }
      if(data["standings"][index + i]["event2"] != null)
      {
        $(".e2" + i).html(data["standings"][index + i]["event2"]);
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
  if((data == null) || (data["standings"] == null) ||
     (data["standings"][index] == null))
  {
    // Go back to the first screen.
    index = 0;

    // Fetch updated standings data from the server.
    loadData();
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
  // Insert the headings for the standings.
  $(".plt").html(placeHeading);
  $(".numt").html(teamNumber);
  $(".namet").html(teamName);
  $(".scrt").html(scoreHeading);
  $(".e1t").html(event1Heading);
  $(".e2t").html(event2Heading);

  // Add a keydown event listener.
  document.addEventListener("keydown", onKeydown);

  // Manually run the standings the first time.
  runTimer();
}

// Set the function to call when the page is ready.
$(document).ready(ready);