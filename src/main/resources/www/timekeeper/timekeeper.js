// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The WebSocket for communicating with the server.
var ws = null;

// Displays the time remaining in the match.
function
displayTime(time)
{
  var sheet, color;

  // Delete the timer style sheet if it exists.
  sheet = document.getElementById("timekeeper_css");
  if(sheet != null)
  {
    sheet.parentNode.removeChild(sheet);
  }

  // Create a new timer style sheet.
  sheet = document.createElement("style");
  sheet.setAttribute("id", "timekeeper_css");

  // Determine the color to use for the timer.
  if((time <= 0) || ($("#button_reset").prop("disabled") == false))
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

// Tracks if the click handlers have been added to the test screen.
var test_added = false;

// Shows the test screen, simplifying audio system testing.
function
showTest()
{
  // Get the dialog element.
  const dialog = $("#test");

  // Called when the test hide animation completes.
  function
  hideTestEnd()
  {
    // Remove the hide class (preparing the test screen for the next time it is
    // displayed).
    dialog.removeClass("hide");

    // Close the modal dialog.
    dialog.close();

    // Remove the end of animation event listener.
    dialog.off("animationend", hideTestEnd);

    // Remove the keydown event listener.
    $(document).off("keydown", showTest.keyDownTest);
  }

  // Hides the test screen.
  function
  hideTest()
  {
    // Add an end of animation event listener.
    dialog.on("animationend", hideTestEnd);

    // Add the hide class to the dialog, starting the close animation.
    dialog.addClass("hide");
  }

  // Called when a key is pressed.
  function
  keyDownTest(e)
  {
    // Close the test screen if the escape key is pressed.
    if(e.key == "Escape")
    {
      // Start the animated close of the test screen.
      hideTest();

      // Prevent any further handling of this keystroke.
      e.preventDefault();
    }
  }

  // Tests the start of match sound.
  function
  testStart()
  {
    // Send a message to the server to test the start of match sound.
    ws.send("test start");
  }

  // Tests the end game sound.
  function
  testEndGame()
  {
    // Send a message to the server to test the end game sound.
    ws.send("test end game");
  }

  // Tests the end sound.
  function
  testEnd()
  {
    // Send a message to the server to test the end sound.
    ws.send("test end");
  }

  // Tests the cancel sound.
  function
  testCancel()
  {
    // Send a message to the server ot test the cancel sound.
    ws.send("test cancel");
  }

  // See if the click handlers need to be added to the test screen.
  if(!test_added)
  {
    // Add the click handlers to the test screen.
    dialog.find(".test_start").on("click", testStart);
    dialog.find(".test_end_game").on("click", testEndGame);
    dialog.find(".test_end").on("click", testEnd);
    dialog.find(".test_cancel").on("click", testCancel);
    dialog.find(".test_exit").on("click", hideTest);

    // Remmber that the click handlers have been added.
    test_added = true;
  }

  // Show the test screen as a modal.
  dialog.showModal();

  // Add a keydown listener to the document to override the default Escape key
  // handling for a modal dialog (which simply closes it, instead of animating
  // the close like needed here).
  $(document).on("keydown", keyDownTest);
}

// Starts the match.
function
startMatch()
{
  // Send a message to the server to start the match.
  ws.send("start");
}

// Cancels the match.
function
cancelMatch()
{
  // Send a message to the server to cancel the match.
  ws.send("stop");
}

// Resets the match.
function
resetMatch()
{
  // Send a message to the server to reset the match timer.
  ws.send("reset");
}

// Connects to the server WebSocket interface.
function
wsConnect()
{
  // Create a new WebSocket.
  ws = new WebSocket(window.location.origin.replace("https", "wss") +
                     "/timekeeper/timekeeper.ws");

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
    // Enable the timer cancel button.
    $("#button_start").prop("disabled", true);
    $("#button_cancel").prop("disabled", false);
    $("#button_reset").prop("disabled", true);
  }

  // See if this is a mode stop message.
  else if(e.data.substring(0, 6) === "m:stop")
  {
    // Enable the timer reset button.
    $("#button_start").prop("disabled", true);
    $("#button_cancel").prop("disabled", true);
    $("#button_reset").prop("disabled", false);
  }

  // See if this is a mode reset message.
  else if(e.data.substring(0, 7) === "m:reset")
  {
    // Enable the timer start button.
    $("#button_start").prop("disabled", false);
    $("#button_cancel").prop("disabled", true);
    $("#button_reset").prop("disabled", true);
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

// Handles attaching javascript actions to buttons in the timer.
function
ready()
{
  // Set the function to call when the "menu" button is pressed.
  $("#button_menu").click(showMenu);

  // Set the function to call when the "test" button is pressed.
  $("#button_test").click(showTest);

  // Set the function to call when the "start" button is pressed.
  $("#button_start").click(startMatch);

  // Set the function to call when the "cancel" button is pressed.
  $("#button_cancel").click(cancelMatch);

  // Set the function to call when the "reset" button is pressed.
  $("#button_reset").click(resetMatch);

  // Display the not connected match length as the starting timer value.
  displayTime(-1);

  // Enable the start button and disable the cancel and reset buttons.
  $("#button_start").prop("disabled", false);
  $("#button_cancel").prop("disabled", true);
  $("#button_reset").prop("disabled", true);

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
    if((e.keyCode == 20) && ($("#test").is(":visible") == false))
    {
      showTest();
      return(false);
    }
  });

  // Connect to the server via a WebSocket.
  wsConnect();
}

// Set the function to call when the page is ready.
$(document).ready(ready);