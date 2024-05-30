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
    $(".pl" + i).html("").removeClass("grey");
    $(".num" + i).html("").removeClass("grey");
    $(".name" + i).html("").removeClass("grey");
    $(".high" + i).html("").removeClass("grey");
    $(".m1" + i).html("").removeClass("grey");
    $(".m2" + i).html("").removeClass("grey");
    $(".m3" + i).html("").removeClass("grey");
    $(".m4" + i).html("").removeClass("grey");

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

      // If this is an odd row, set the background to grey.
      if(i & 1)
      {
        $(".pl" + i).addClass("grey");
        $(".num" + i).addClass("grey");
        $(".name" + i).addClass("grey");
        $(".high" + i).addClass("grey");
        $(".m1" + i).addClass("grey");
        $(".m2" + i).addClass("grey");
        $(".m3" + i).addClass("grey");
        $(".m4" + i).addClass("grey");
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

  // A key listener to enter/exit fullscreen mode when Ctrl+F is pressed.
  $(document).keypress(function(e)
  {
    if(e.keyCode == 6)
    {
      if(!document.fullscreenElement)
      {
        document.documentElement.requestFullscreen();
      }
      else
      {
        document.exitFullscreen();
      }
      return(false);
    }
  });

  // Manually run the scoreboard the first time.
  runTimer();
}

// Set the function to call when the page is ready.
$(document).ready(ready);