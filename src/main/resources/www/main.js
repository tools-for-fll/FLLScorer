// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Strings for the menu items.
const seasonsButton = "<!--#str_menu_seasons-->";
const eventsButton = "<!--#str_menu_events-->";
const teamsButton = "<!--#str_menu_teams-->";
const scoresButton = "<!--#str_menu_scores-->";
const judgingButton = "<!--#str_menu_judging-->";
const standingsButton = "<!--#str_menu_standings-->";
const passwordsButton = "<!--#str_menu_passwords-->";
const linksButton = "<!--#str_menu_links-->";
const aboutButton = "<!--#str_menu_about-->";

// HTML encodes an input string.
function
htmlEncode(input)
{
  // Convert &, <, and > to their HTML equivalents.
  return(input.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;'));
}

// Displays a popup window.
function
showPopup(html, buttons)
{
  // Generate a unique ID for this popup, allowing for popups to have popups.
  const id = crypto.randomUUID();

  // Called when the popup is closed.
  function
  onClose()
  {
    // Delete the popup element from the DOM.
    $("#" + id).remove();

    // Remove the keydown listener for this popup.
    document.removeEventListener("keydown", onKeydown);
  }

  // Called when a button on the popup is clicked.
  function
  onClick(fn)
  {
    // Call the button handler, if there is one.
    if(fn !== null)
    {
      fn();
    }

    // Close the popup.
    onClose();
  }

  // Called when a key is pressed.
  function
  onKeydown(e)
  {
    // Close the popup if the escape key is pressed.
    if(e.key == "Escape")
    {
      onClose();
    }
  }

  // Generate the dialog for the popup and append it to the DOM.
  var html = "<dialog id=\"" + id + "\">" + html + "</dialog>";
  $("body").append(html);

  // Loop through all the buttons.
  for(const [ btn_id, fn ] of Object.entries(buttons))
  {
    // Find this button.
    var btn = $("#" + btn_id);

    // Add a click handler to this button.
    btn.on("click", () => { onClick(fn); });
  }

  // Add a keydown listener to the document.
  document.addEventListener("keydown", onKeydown);

  // Display the dialog as a modal.
  $("#" + id)[0].showModal();
}

// Shows the requsted tab.
function
showTab(tab)
{
  // Get the ID of the current tab.
  var oldTab = $(".tab.show").attr("id");

  // Called when the requested tab is received from the server.
  function
  onTabLoad(responseText, textStatus, xhr)
  {
    // See if the request was successful.
    if(textStatus === "success")
    {
      // See if there was a tab already shown.
      if(oldTab != undefined)
      {
        // Call the tab's cleanup function if it exists.
        oldTab = oldTab.substring(4);
        if(typeof(window[oldTab + "Cleanup"]) === "function")
        {
          window[oldTab + "Cleanup"]();
        }
      }

      // Clear the content of the currently active tab.
      $(".tab.show").html("");

      // Deactivate all the buttons and hide all the tabs.
      $('button[id^="btn_"]').removeClass("active");
      $('div[id^="tab_"]').removeClass("show");

      // Activate the request button and show the requested tab.
      $("#btn_" + tab).addClass("active");
      $("#tab_" + tab).addClass("show");

      // Call the tab's setup function if it exists.
      if(typeof(window[tab + "Setup"]) === "function")
      {
        window[tab + "Setup"]();
      }
    }
    else
    {
      // Construct an error message.
      var html = `
<div class="warning_container">
  <div>
    <!--#str_connect_error-->
    <br>
    <br>
    <button id="tab-load-ok" class="red">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

      // Show the error message as a popup.
      showPopup(html,
                {
                  "tab-load-ok": null
                });
    }
  }

  // Do nothing if this is already the active tab.
  if($("button[class='active']").attr("id") == ("btn_" + tab))
  {
    return;
  }

  // Request the content for this tab.
  $("#tab_" + tab).load("/admin/" + tab + "/index.html", onTabLoad);
}

// Updates the status bar based on the selected season and event.
function
updateStatus()
{
  var seasonName = null;
  var eventName = null;
  var teamCount = null;

  // Called when the fetch of the team count completes.
  function
  teamDone(result)
  {
    // See if the result is a string (therefore redirected from teamFail) or
    // a JSON object from the server.
    if(typeof(result) == "string")
    {
      // Set the team count to the string from teamFail.
      teamCount = htmlEncode(result);
    }
    else
    {
      // Get the count of teams from the response.
      teamCount = result["count"];
    }

    // Update the status bar with the season name, event name, and team count.
    $(".main_container .status").html("<span class=\"label\">" +
                                      "<!--#str_status_season-->:</span>" +
                                      seasonName + "<span class=\"label\">" +
                                      "<!--#str_status_event-->:</span>" +
                                      eventName + "<span class=\"label\">" +
                                      "<!--#str_status_teams-->:</span>" +
                                      teamCount);
  }

  // Called when the fetch of the team count fails.
  function
  teamFail(result)
  {
    // Handle this as a completed transaction with an unknown team count.
    teamDone("<Unknown>");
  }

  // Called when the fetch of the event name completes.
  function
  eventDone(result)
  {
    // See if the result is a string (therefore redirected from eventFail) or
    // a JSON object from the server.
    if(typeof(result) == "string")
    {
      // Set the event name to the string from eventFail.
      eventName = htmlEncode(result);
    }
    else
    {
      // Extract the event date and name from the JSON object.
      var idx, length = ("events" in result) ? result["events"].length : 0;
      for(idx = 0; idx < length; idx++)
      {
        if(result["events"][idx]["selected"] === true)
        {
          eventName = htmlEncode(result["events"][idx]["date"] + " " +
                                 result["events"][idx]["name"]);
          break;
        }
      }
      if(idx == length)
      {
        eventName = htmlEncode("<Unknown>");
      }
    }

    // Fetch the team count from the server.
    $.getJSON("/admin/teams/teams.json?action=count")
      .done(teamDone)
      .fail(teamFail);
  }

  // Called when the fetch of the event name fails.
  function
  eventFail(result)
  {
    // Handle this as a completed transaction with an unknown event name.
    eventDone("<Unknown>");
  }

  // Called when the fecth of a season name completes.
  function
  seasonDone(result)
  {
    // See if the result is a string (therefore redirected from seasonFail) or
    // a JSON object from the server.
    if(typeof(result) == "string")
    {
      // Set the season name to the string from seasonFail.
      seasonName = htmlEncode(result);
    }
    else
    {
      // Extract the season year and name from the JSON object.
      seasonName = htmlEncode(result["year"] + " " + result["name"]);
    }

    // Fetch the event name from the server.
    $.getJSON("/admin/events/events.json")
      .done(eventDone)
      .fail(eventFail);
  }

  // Called when the fetch of the season name fails.
  function
  seasonFail(result)
  {
    // Handle this as a completed transaction with an unknown season name.
    seasonDone("<Unknown>");
  }

  // Fetch the season name from the server.
  $.getJSON("/admin/seasons/seasons.json")
    .done(seasonDone)
    .fail(seasonFail);
}

// Shows a confirmation dialog.
function
showConfirmation(message, button, ok, cancel)
{
  // Construct the confirmation message.
  var html = `
<div class="warning_container">
  <div>
    ${message}
    <br>
    <br>
    <button id="confirmation-cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="confirmation-ok" class="red">
      ${button}
    </button>
  </div>
</div>`;

  // Show the warning message as a popup.
  showPopup(html,
            {
              "confirmation-cancel": cancel,
              "confirmation-ok": ok
            });
}

// Shows a warning popup.
function
showError(message, fn)
{
  // Construct an error message.
  var html = `
<div class="warning_container">
  <div>
    ${message}
    <br>
    <br>
    <button id="error-message-ok" class="red">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Show the error message as a popup.
  showPopup(html,
            {
              "error-message-ok": fn
            });
}

// Shows the about popup.
function
showAbout()
{
  var html = "";

  // Construct the HTML for the about popup.
  html += `
<div class="about_container">
  <img src="favicon.ico">
  <div>
    <!--#str_about_title-->
    <br>
    <!--#str_about_version-->
    <br>
    <br>
    <!--#str_about_copyright-->
    <br>
    <!--#str_about_reserved-->
    <br>
    <br>
    <button id="about-ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Display the about popup.
  showPopup(html, { "about-ok": null });
}

// Handles setup of the main page.
function
ready()
{
  // Insert the button names.
  $("#btn_seasons").html(seasonsButton);
  $("#btn_events").html(eventsButton);
  $("#btn_teams").html(teamsButton);
  $("#btn_scores").html(scoresButton);
  $("#btn_judging").html(judgingButton);
  $("#btn_standings").html(standingsButton);
  $("#btn_passwords").html(passwordsButton);
  $("#btn_links").html(linksButton);
  $("#btn_about").html(aboutButton);

  // Add the click handlers for the various tabs.
  $("#btn_seasons").click(function() { showTab("seasons"); });
  $("#btn_events").click(function() { showTab("events"); });
  $("#btn_teams").click(function() { showTab("teams"); });
  $("#btn_scores").click(function() { showTab("scores"); });
  $("#btn_judging").click(function() { showTab("judging"); });
  $("#btn_standings").click(function() { showTab("standings"); });
  $("#btn_passwords").click(function() { showTab("passwords"); });
  $("#btn_links").click(function() { showTab("links"); });
  $("#btn_about").click(showAbout);

  // Show the seasons tab by default.
  showTab("seasons");

  // Update the status bar.
  updateStatus();
}

// Set the function to call when the page is ready.
$(document).ready(ready);