// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Strings for the headings.
const placeHeading = "<!--#str_scoreboard_place-->";
const teamNumber = "<!--#str_scoreboard_number-->";
const teamName = "<!--#str_scoreboard_name-->";
const highestScore = "<!--#str_scoreboard_high-->";
const match1Score = "<!--#str_scoreboard_match1-->";
const match2Score = "<!--#str_scoreboard_match2-->";
const match3Score = "<!--#str_scoreboard_match3-->";
const match4Score = "<!--#str_scoreboard_match4-->";

// The data to display on the scoreboard.
var data = null;

// The timer.
var timer = null;

// The current screen of data to display.
var index = 0;

// The current enable state for the timer display.
var timer_enable = "<!--#timer-enable-->";

// The current location for the timer display.
var timer_location = "<!--#timer-location-->";

// The WebSocket for communicating with the server.
var ws = null;

// The state of the timer display.
var state = "reset";

// Loads the scoreboard data from the server.
function
loadData()
{
  // Called when the latest scoreboard is fetched successfully.
  function
  onDone(newData)
  {
    // Save the newly retreived data.
    data = newData;
  }

  // Called when the scoreboard fetch fails.
  function
  onFail()
  {
    // Since the fetch failed, clear the data since it is stale.
    data = null;
  }

  // Called after every scoreboard fetch, regardless of status.
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

  // Fetch the latest scoreboard from the server.
  $.getJSON("/scoreboard/scoreboard.json")
    .done(onDone)
    .fail(onFail)
    .always(onAlways);
}

// Runs the timer, executed every ten seconds.
function
runTimer()
{
  // See if the first screen of scores is about to be displayed.
  if(index == 0)
  {
    // Set the event title.
    if((data != null) && (data["event"] != null))
    {
      $(".event").html(data["event"]);
    }
    else
    {
      $(".event").html("Connecting...");
    }

    // Set the number of matches in the scoreboard.
    if((data != null) && (data["matches"] != null) && (data["matches"] == 4))
    {
      $(".container3").removeClass("container3").addClass("container4");
      $(".m4t").show();
      $(".m40").show();
      $(".m41").show();
      $(".m42").show();
      $(".m43").show();
      $(".m44").show();
      $(".m45").show();
      $(".m46").show();
      $(".m47").show();
    }
    else
    {
      $(".container4").removeClass("container4").addClass("container3");
      $(".m4t").hide();
      $(".m40").hide();
      $(".m41").hide();
      $(".m42").hide();
      $(".m43").hide();
      $(".m44").hide();
      $(".m45").hide();
      $(".m46").hide();
      $(".m47").hide();
    }
  }

  // Loop through the eight possible scores on this screen.
  for(i = 0; i < 8; i++)
  {
    // Remove the background from this row. It will be added if necessary.
    $(".pl" + i).html("").removeClass("gray");
    $(".num" + i).html("").removeClass("gray");
    $(".name" + i).html("").removeClass("gray");
    $(".high" + i).html("").removeClass("gray");
    $(".m1" + i).html("").removeClass("gray");
    $(".m2" + i).html("").removeClass("gray");
    $(".m3" + i).html("").removeClass("gray");
    $(".m4" + i).html("").removeClass("gray");

    // See if there is an entry for this position.
    if((data != null) && (data["scores"] != null) &&
       (data["scores"][index + i] != null))
    {
      // Update this row with this entry's information.
      $(".pl" + i).html(data["scores"][index + i]["place"]);
      $(".num" + i).html(data["scores"][index + i]["num"]);
      $(".name" + i).html(data["scores"][index + i]["name"]);
      if(data["scores"][index + i]["high"] != null)
      {
        $(".high" + i).html(data["scores"][index + i]["high"]);
      }
      if(data["scores"][index + i]["m1"] != null)
      {
        $(".m1" + i).html(data["scores"][index + i]["m1"]);
      }
      if(data["scores"][index + i]["m2"] != null)
      {
        $(".m2" + i).html(data["scores"][index + i]["m2"]);
      }
      if(data["scores"][index + i]["m3"] != null)
      {
        $(".m3" + i).html(data["scores"][index + i]["m3"]);
      }
      if(data["scores"][index + i]["m4"] != null)
      {
        $(".m4" + i).html(data["scores"][index + i]["m4"]);
      }

      // If this is an odd row, set the background to gray.
      if(i & 1)
      {
        $(".pl" + i).addClass("gray");
        $(".num" + i).addClass("gray");
        $(".name" + i).addClass("gray");
        $(".high" + i).addClass("gray");
        $(".m1" + i).addClass("gray");
        $(".m2" + i).addClass("gray");
        $(".m3" + i).addClass("gray");
        $(".m4" + i).addClass("gray");
      }
    }
  }

  // The next timer iteration should show the next screen, or the first screen
  // if all of the teams have been displayed.
  index += 8;
  if((data == null) || (data["scores"] == null) ||
     (data["scores"][index] == null))
  {
    // Go back to the first screen.
    index = 0;

    // Fetch updated scoreboard data from the server.
    loadData();
  }
}

// Enables the timer display.
function
showTimer()
{
  // Show the timer display in the current location.
  $(".timer_bg").addClass("timer_bg_" + timer_location);
  $(".timer").addClass("timer_" + timer_location);
}

// Disables the timer display.
function
hideTimer()
{
  // Remove the timer display in both locations.
  $(".timer_bg").removeClass("timer_bg_top").removeClass("timer_bg_center");
  $(".timer").removeClass("timer_top").removeClass("timer_center");
}

// Displays the time remaining in the match.
function
displayTime(time)
{
  var sheet, color;

  // Delete the timer style sheet if it exists.
  sheet = document.getElementById("timer_css");
  if(sheet != null)
  {
    sheet.parentNode.removeChild(sheet);
  }

  // Create a new timer style sheet.
  sheet = document.createElement("style");
  sheet.setAttribute("id", "timer_css");

  // Determine the color to use for the timer.
  if((time <= 0) || (state === "stop"))
  {
    color = "var(--color-bright-red)";
  }
  else if(time <= 30)
  {
    color = "var(--color-bright-yellow)";
  }
  else
  {
    color = "var(--color-bright-green)";
  }

  // Set the styles based on the current values of the time digits.
  if(time == -1)
  {
    sheet.innerHTML = ".nc {background-color: " + color +
                      ";margin: 0.1vw;border-radius: 1vw;}";
  }
  else
  {
    sheet.innerHTML = ".m" + parseInt(time / 60) + "{background-color: " +
                      color + ";" + "margin: 0.1vw;border-radius: 1vw;}.t" +
                      parseInt((time % 60) / 10) + "{background-color: " +
                      color + ";margin: 0.1vw;border-radius: 1vw;}.o" +
                      (time % 10) + "{background-color: " + color +
                      ";margin: 0.1vw;border-radius: 1vw;}";
  }

  // Add the style sheet to the document.
  document.body.appendChild(sheet);
}

// Connects to the server WebSocket interface.
function
wsConnect()
{
  // Create a new WebSocket.
  ws = new WebSocket(window.location.origin.replace("https", "wss") +
                     "/timer/timer.ws");

  // Set the functions to call when a message is received for the WebSocket is
  // closed.
  ws.onmessage = wsMessage;
  ws.onclose = wsClose;
}

// Called when a message is received from the WebSocket.
function
wsMessage(e)
{
  // See if this is a mode run message.
  if(e.data.substring(0, 7) === "m:run")
  {
    // Set the state to run.
    state = "run";

    // Show the timer display if it is enabled.
    if(timer_enable === "1")
    {
      showTimer();
    }
  }

  // See if this is a mode stop message.
  else if(e.data.substring(0, 6) === "m:stop")
  {
    // Set the state to stop.
    state = "stop";
  }

  // See if this is a mode reset message.
  else if(e.data.substring(0, 7) === "m:reset")
  {
    // Set the state to reset.
    state = "reset";

    // Hide the timer display (which is a NOP if the timer display is
    // disabled).
    hideTimer();
  }

  // See if this is a timer display enable message.
  else if(e.data.substring(0, 14) === "s:timer_enable")
  {
    // Enable the timer display.
    timer_enable = "1";

    // Show the timer display if the timer is not in the reset state.
    if(state !== "reset")
    {
      showTimer();
    }
  }

  // See if this is a timer display disable message.
  else if(e.data.substring(0, 15) === "s:timer_disable")
  {
    // Disable the timer display.
    timer_enable = "0";

    // Hide the timer display.
    hideTimer();
  }

  // See if this is a timer top message.
  else if(e.data.substring(0, 11) === "s:timer_top")
  {
    // Change the timer display location to the top.
    timer_location = "top";

    // Move the timer display to the new location if the timer display is
    // enabled and the timer is not reset.
    if((timer_enable === "1") && (state !== "reset"))
    {
      hideTimer();
      showTimer();
    }
  }

  // See if this is a timer center message.
  else if(e.data.substring(0, 14) === "s:timer_center")
  {
    // Change the timer display location to the center.
    timer_location = "center";

    // Move the timer display to the new location if the timer display is
    // enabled and the timer is not reset.
    if(state !== "reset")
    {
      hideTimer();
      showTimer();
    }
  }

  // See if this is a time message.
  else if(e.data.substring(0, 2) === "t:")
  {
    // Display the provided time.
    displayTime(parseInt(e.data.substring(2)));
  }
}

// Called when the WebSocket closes.
function
wsClose()
{
  // Change the time display to indicate that the server connection has been
  // lost.
  displayTime(-1);

  // Attempt to reconnect to the server after a second (to avoid flooding the
  // network with requests).
  setTimeout(wsConnect, 1000);
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
}

// Handles setup of the scoreboard.
function
ready()
{
  // Insert the headings for the scoreboard.
  $(".plt").html(placeHeading);
  $(".numt").html(teamNumber);
  $(".namet").html(teamName);
  $(".hight").html(highestScore);
  $(".m1t").html(match1Score);
  $(".m2t").html(match2Score);
  $(".m3t").html(match3Score);
  $(".m4t").html(match4Score);

  // Add a keydown event listener.
  document.addEventListener("keydown", onKeydown);

  // Manually run the scoreboard the first time.
  runTimer();

  // Connect to the server via a WebSocket.
  wsConnect();
}

// Set the function to call when the page is ready.
$(document).ready(ready);