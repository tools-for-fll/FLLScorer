// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The length of a match.
var match_len = <!--#match_len-->;

// Variables containing the timer's sound effects.
var snd_cancel;
var snd_end_game;
var snd_end;
var snd_start;

// The timer itself.
var timer = null;
var time;

// Displays the time remaining in the match.
function displayTime(time) {
  var sheet, color;

  // Delete the timer style sheet if it exists.
  sheet = document.getElementById("timekeeper_css");
  if(sheet != null) {
    sheet.parentNode.removeChild(sheet);
  }

  // Create a new timer style sheet.
  sheet = document.createElement("style");
  sheet.setAttribute("id", "timekeeper_css");

  if(time == 0)
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
  sheet.innerHTML = ".m" + parseInt(time / 60) + "{background-color: " +
                    color + ";" + "margin: 0.1vw;border-radius: 1vw;}.t" +
                    parseInt((time % 60) / 10) + "{background-color: " +
                    color + ";margin: 0.1vw;border-radius: 1vw;}.o" +
                    (time % 10) + "{background-color: " + color +
                    ";margin: 0.1vw;border-radius: 1vw;}";

  // Add the style sheet to the document.
  document.body.appendChild(sheet);
}

// Loads a sound file.
function loadSound(filename) {
  // Create an Audio object, using the specified sound file.
  var audio = new Audio(filename);

  // Configure the object.
  audio.preload = "auto";
  audio.volume = 1;

  // Return the object.
  return audio;
}

// Stops the playback of any sounds.
function stopAll() {
  // Pause and rewind all of the audio objects.
  snd_cancel.pause();
  snd_cancel.currentTime = 0;
  snd_end_game.pause();
  snd_end_game.currentTime = 0;
  snd_end.pause();
  snd_end.currentTime = 0;
  snd_start.pause();
  snd_start.currentTime = 0;
}

// Starts the playback of a sound.
function play(element) {
  // Stop any audio playback.
  stopAll();

  // Start playback of the given sound.
  element.play();
}

// Opens the test screen, simplifying audio system testing.
function openTest() {
  // Show the test screen.
  $(".test_bg").removeClass("hidden");
  $(".test_main").removeClass("hidden");
}

// Closes the test screen, returning to the timer start screen.
function closeTest() {
  // Stop any audio playback.
  stopAll();

  // Hide the test screen.
  $(".test_main").addClass("hidden");
  $(".test_bg").addClass("hidden");
}

// Starts the timer.
function startTimer() {
  // Disable the timer start button.
  $(".button_start").prop("disabled", true);

  // Enable the timer cancel button.
  $(".button_cancel").prop("disabled", false);

  // Set the time to the match length + 1 and manually call the timer handler
  // to start the timer. The +1 is there since the manual call of the timer
  // handler will decrement the time at the start.
  time = match_len + 1;
  runTimer();
}

// Runs the timer, executed every second.
function runTimer() {
  // Decrement the count of seconds.
  time--;

  // Format the seconds and place it onto the screen.
  displayTime(time);

  // See if this is the start of the match.
  if(time == match_len) {
    // Play the match start sound.
    play(snd_start);

    // Set a timer to run this function every second (until the timer is
    // cancelled).
    timer = setInterval(runTimer, 1000);
  }

  // See if there are 30 seconds left in the match.
  if(time == 30) {
    // Play the end game sound.
    play(snd_end_game);
  }

  // See if the match is over.
  if(time == 0) {
    // Cancel the timer.
    cancelTimer(snd_end);
  }
}

// Cancels the timer.
function cancelTimer(sound) {
  // See if the timer is active.
  if(timer) {
    // Stop the timer.
    clearInterval(timer);
    timer = null;
  }

  // Play the match cancelled sound.
  play(sound);

  // Disable the timer cancel button.
  $(".button_cancel").prop("disabled", true);

  // Enable the timer reset button.
  $(".button_reset").prop("disabled", false);
}

// Resets the timer.
function resetTimer() {
  // Display the starting time.
  displayTime(match_len);

  // Disable the timer reset button.
  $(".button_reset").prop("disabled", true);

  // Enable the timer start button.
  $(".button_start").prop("disabled", false);
}

// Handles pre-loading sound files and attaching javascript actions to buttons
// in the timer.
function ready() {
  // Pre-load the sound files used by the timer.
  snd_cancel = loadSound("timekeeper/cancel.mp3");
  snd_end_game = loadSound("timekeeper/end-game.mp3");
  snd_end = loadSound("timekeeper/end.mp3");
  snd_start = loadSound("timekeeper/start.mp3");

  // Set the function to call when the "test" button is pressed.
  $(".button_test").click(openTest);

  // Set the function to call when the "start" button is pressed.
  $(".button_start").click(startTimer);

  // Set the function to call when the "cancel" button is pressed.
  $(".button_cancel").click(function() {cancelTimer(snd_cancel); });

  // Set the function to call when the "reset" button is pressed.
  $(".button_reset").click(resetTimer);

  // Set the functions to call when the audio test screen buttons are pressed.
  $(".test_cancel").click(function() { play(snd_cancel); });
  $(".test_end_game").click(function() { play(snd_end_game); });
  $(".test_end").click(function() { play(snd_end); });
  $(".test_start").click(function() { play(snd_start); });
  $(".test_bg").click(closeTest);
  $(".test_exit").click(closeTest);

  // Display the match length as the starting timer value.
  displayTime(match_len);

  // Enable the start button and disable the cancel and reset buttons.
  $(".button_start").prop("disabled", false);
  $(".button_cancel").prop("disabled", true);
  $(".button_reset").prop("disabled", true);

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

  // A key listener to open the test menu with Ctrl+T is pressed.
  $(document).keypress(function(e)
  {
    if((e.keyCode == 20) && ($(".test_main").hasClass("hidden") == true))
    {
      openTest();
      return(false);
    }
  });

  // A key listener to close the test menu when the escape key is pressed.
  $(document).keydown(function(e)
  {
    if((e.keyCode == 27) && ($(".test_main").hasClass("hidden") == false))
    {
      closeTest();
      return(false);
    }
  });
}

// Set the function to call when the page is ready.
$(document).ready(ready);