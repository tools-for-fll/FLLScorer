// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

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

  if(time == 0)
  {
    color = "var(--color-red)";
  }
  else if(time <= 30)
  {
    color = "var(--color-yellow)";
  }
  else
  {
    color = "var(--color-green)";
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

// Handles pre-loading sound files and attaching javascript actions to buttons
// in the timer.
function
ready()
{
  // Display the match length as the starting timer value.
  displayTime(150);

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
}

// Set the function to call when the page is ready.
$(document).ready(ready);