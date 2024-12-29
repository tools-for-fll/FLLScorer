// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The WebSocket for communicating with the server.
var ws = null;

// The state of the timer.
var state = "reset";

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

// Handles pre-loading sound files and attaching javascript actions to buttons
// in the timer.
function
ready()
{
  // Display the not connected match length as the starting timer value.
  displayTime(-1);

  // Add a keydown event listener.
  document.addEventListener("keydown", onKeydown);

  // Connect to the server via a WebSocket.
  wsConnect();
}

// Set the function to call when the page is ready.
$(document).ready(ready);