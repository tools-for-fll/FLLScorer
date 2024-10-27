// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Strings for the menu items.
const seasonsButton = "<!--#str_menu_seasons-->";
const eventsButton = "<!--#str_menu_events-->";
const teamsButton = "<!--#str_menu_teams-->";
const scoresButton = "<!--#str_menu_scores-->";
const rubricsButton = "<!--#str_menu_rubrics-->";
const usersButton = "<!--#str_menu_users-->";
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

      // Rewrite the URL for the current page.
      if(tab != "seasons")
      {
        window.history.replaceState(null, "", window.location.origin +
                                              window.location.pathname +
                                              "?tab=" + tab);
      }
      else
      {
        window.history.replaceState(null, "", window.location.origin +
                                              window.location.pathname);
      }
    }
    else
    {
      // Display an error message.
      showError("<!--#str_connect_error-->", null);
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
                                      "<span>" + seasonName + "</span>" +
                                      "<span class=\"label\">" +
                                      "<!--#str_status_event-->:</span>" +
                                      "<span>" + eventName + "</span>" +
                                      "<span class=\"label\">" +
                                      "<!--#str_status_teams-->:</span>" +
                                      "<span>" + teamCount + "</span>");
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
  $.getJSON("/admin/seasons/seasons.json?action=get")
    .done(seasonDone)
    .fail(seasonFail);
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
    <span>
      <!--#str_about_title-->
    </span>
    <br>
    <span>
      <!--#str_about_version-->
    </span>
    <br>
    <br>
    <span>
      <!--#str_about_copyright-->
    </span>
    <br>
    <span>
      <!--#str_about_reserved-->
    </span>
    <br>
    <br>
    <button id="about-ok" class="accent">
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
  var params = window.location.search;
  var tab = "seasons";

  // See if there are any parameters to this page.
  if(params != "")
  {
    // Remove the "?" from the beginning of the parameters.
    params = params.substring(1);

    // Split the parameters on any "&", which separates unique parameters.
    params = params.split("&");

    // Loop through the parameters.
    for(var param of params)
    {
      // Split this parameter on the "=", which separates the name from the
      // value.
      param = param.split("=");

      // Save the tab name if this parameter is the tab, and it is a valid tab.
      if((param[0] == "tab") && ($("#btn_" + param[1]).length != 0) &&
         (param[1] != "about"))
      {
        tab = param[1];
      }
    }
  }

  // Insert the button names.
  $("#btn_seasons").html(seasonsButton);
  $("#btn_events").html(eventsButton);
  $("#btn_teams").html(teamsButton);
  $("#btn_scores").html(scoresButton);
  $("#btn_rubrics").html(rubricsButton);
  $("#btn_users").html(usersButton);
  $("#btn_about").html(aboutButton);

  // Add the click handlers for the various tabs.
  $("#btn_menu").click(showMenu);
  $("#btn_seasons").click(function() { showTab("seasons"); });
  $("#btn_events").click(function() { showTab("events"); });
  $("#btn_teams").click(function() { showTab("teams"); });
  $("#btn_scores").click(function() { showTab("scores"); });
  $("#btn_rubrics").click(function() { showTab("rubrics"); });
  $("#btn_users").click(function() { showTab("users"); });
  $("#btn_about").click(showAbout);

  // Show the seasons tab by default.
  showTab(tab);

  // Update the status bar.
  updateStatus();
}

// Set the function to call when the page is ready.
$(document).ready(ready);