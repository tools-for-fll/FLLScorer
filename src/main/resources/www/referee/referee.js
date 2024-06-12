// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Tracks if there are changes to the scoresheet.
var modified = false;

// The ID of the team that is being scored.
var teamID = null;

// The match that is being scored.
var theMatch = null;

// Set to true if the window should be closed on exiting the scoresheet
// (instead of returning to the score list).
var closeOnExit = false;

// Enable/disable the compute button based on the completeness of the
// scoresheet.
function
computeEnable()
{
  // See if there are selections for the entire scoresheet.
  if($(".mission_sel:not(#CV_1").length ==
     $(".mission_sel:not(#CV_1) .selected").length)
  {
    // The entire scoresheet has selections, so enable the compute button.
    $("#compute").removeAttr("disabled");
  }
  else
  {
    // There are missing selections on the scoresheet, so disable the compute
    // button.
    $("#compute").attr("disabled", "disabled");
  }
}

// Sets the score on the display.
function
setScore(score)
{
  // See if there is a score to display.
  if(score == -1)
  {
    // Remove the computed score if it exists.
    if(!$("#compute span").hasClass("has_score"))
    {
      $("#compute").removeClass("has_score");
      $("#compute").html("");
    }

    // Disable the publish button.
    $("#publish").attr("disabled", "disabled");
  }
  else
  {
    // Update the score.
    $("#compute").addClass("has_score");
    $("#compute").html(score);

    // Enable the publish button.
    $("#publish").removeAttr("disabled");
  }
}

// Toggles the state of a scoresheet item/button.
function
itemToggle(mission_sel, button)
{
  var select = true;

  // See if the button is currently selected.
  if($(button).hasClass("selected"))
  {
    select = false;
  }

  // Un-select all buttons for this mission selection.
  $(mission_sel + " button").removeClass("selected");

  // Select this button if it was not selected before.
  if(select)
  {
    $(button).addClass("selected");
  }

  // Update the publish and compute buttons.
  computeEnable();

  // Remove the computed score if it exists.
  setScore(-1);

  // The scoresheet has been modified.
  modified = true;
}

// Gets the JSON representation of the current selection state of the
// scoresheet.
function
getSheetJSON()
{
  var result = {};
  var idx;

  // Loop through all of the mission selections.
  for(var mission of $(".mission_sel"))
  {
    // Get all of the buttons for this selection.
    var buttons = $(mission).find("button");

    // Loop through all of the buttons.
    for(idx = 0; idx < buttons.length; idx++)
    {
      // See if this button is selected.
      if($(buttons[idx]).hasClass("selected"))
      {
        // Stop looking.
        break;
      }
    }

    // See if one of the buttons is selected.
    if(idx != buttons.length)
    {
      // Save the button selection into the scoresheet state.
      result[mission.id] = idx;
    }
  }

  // Convert the scoresheet state into a JSON string and return it.
  return(JSON.stringify(result));
}

// Handles discarding the scoresheet.
function
discard()
{
  // Called when the scoresheet discard has been confirmed.
  function
  discardSheet()
  {
    // Either close this window or load the current scores.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      loadScores();
    }
  }

  // See if the scoresheet has been modified.
  if(modified)
  {
    // Display a confirmation that the scoresheet should be discarded.
    showConfirmation("<!--#str_referee_discard_conf-->",
                     "<!--#str_referee_discard-->", discardSheet, null);
  }
  else
  {
    // The scoresheet has not been modified, so discard it.
    discardSheet();
  }
}

// Handles saving the scoresheet state.
function
save()
{
  // Called when the query to the server has completed.
  function
  done(result)
  {
    // See if the request was successful.
    if((result != null) && (result["result"] != "ok"))
    {
      // It was not, so call the failure function.
      fail(result);
      return;
    }

    // Either close this window or load the current scores.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      loadScores();
    }
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // See if the scoresheet has been modified.
  if(modified)
  {
    // Get the JSON representation of the current scoresheet selections.
    var json = getSheetJSON();

    // Send a request to the server to save the scoresheet.
    $.getJSON("/referee/referee.json?action=save&id=" + teamID + "&match=" +
              theMatch + "&json=" + json)
      .done(done)
      .fail(fail)
  }
  else
  {
    // The scoresheet has not been modified, so bypass sending the scoresheet
    // to the server and act as though it were successful.
    done(null);
  }
}

// Handles publishing the scoresheet.
function
publish()
{
  // Called when the query to the server has completed.
  function
  done(result)
  {
    // See if the request was successful.
    if((result != null) && (result["result"] != "ok"))
    {
      // It was not, so call the failure function.
      fail(result);
      return;
    }

    // Either close this window or load the current scores.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      loadScores();
    }
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // Get the JSON representation of the current scoresheet selections.
  var json = getSheetJSON();

  // Send a request to the server to publish the scoresheet.
  $.getJSON("/referee/referee.json?action=publish&id=" + teamID + "&match=" +
            theMatch + "&json=" + json)
    .done(done)
    .fail(fail)
}

// Handles computing the score for the scoresheet.
function
compute()
{
  // Called when the query to the server has completed.
  function
  done(result)
  {
    // See if the request was successful.
    if((result != null) && (result["result"] != "ok"))
    {
      // It was not, so call the failure function.
      fail(result);
      return;
    }

    // Hide all the mission error indicators.
    $(".error").hide();

    // Update the score.
    setScore(result["score"]);
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // See if there is a result, and it contains at least one colon.
    if(Object.hasOwn(result, "result") &&
       (result["result"].indexOf(":") != -1))
    {
      // Start with no detected errors.
      var count = 0;

      // Hide all the mission error messages.
      $(".error").hide();

      // Split the result into lines.
      var items = result["result"].split("\n");

      // Loop through the result lines.
      for(var item of items)
      {
        // Ignore this line if it does not contain a colon.
        if(item.indexOf(":") == -1)
        {
          continue;
        }

        // Split the line around the colon.
        var fragments = item.split(":");

        // Set the error text to the mission error and show it.
        $("#" + fragments[0] + " .error  .mission_error").html(fragments[1]);
        $("#" + fragments[0] + " .error").show();

        // Increment the count of errors.
        count++;
      }

      // Change the response to a general error message for the popup.
      if(count == 1)
      {
        result["result"] = "<!--#str_referee_error-->";
      }
      else
      {
        result["result"] = `<!--#str_referee_errors-->`;
      }
    }

    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // Get the JSON representation of the current scoresheet selections.
  var json = getSheetJSON();

  // Send a request to the server to score the scoresheet.
  $.getJSON("/referee/referee.json?action=score&json=" + json)
    .done(done)
    .fail(fail)
}

// Loads the current list of scores from the server.
function
loadScores()
{
  // Called when the query to the server has completed.
  function
  done(result)
  {
    // See if the request was successful.
    if(result["result"] != "ok")
    {
      // It was not, so call the failure function.
      fail(result);
      return;
    }

    // Set the number of matches based on the response.
    if(result["matches"] == 3)
    {
      $(".body").addClass("three_matches");
    }
    else
    {
      $(".body").removeClass("three_matches");
    }

    // Start constructing the HTML for the list of scores.
    var html = "";

    // Loop through all the teams.
    for(var i = 0; i < result["scores"].length; i++)
    {
      // Get the team ID for this team.
      var id = result["scores"][i]["id"];

      // Get the match statuses for this team.
      var m1 = result["scores"][i]["match1"];
      var m2 = result["scores"][i]["match2"];
      var m3 = result["scores"][i]["match3"];
      var m4 = result["scores"][i]["match4"];

      // Convert the match statuses into the color for each match; red for a
      // scoresheet without a score, green for a scoresheet and a score, and
      // yellow for a match that has not been scored.
      m1 = (m1 == 1) ? "red" : ((m1 == 2) ? "green" : "yellow");
      m2 = (m2 == 1) ? "red" : ((m2 == 2) ? "green" : "yellow");
      m3 = (m3 == 1) ? "red" : ((m3 == 2) ? "green" : "yellow");
      m4 = (m4 == 1) ? "red" : ((m4 == 2) ? "green" : "yellow");

      // Add the HTML for this row/team.
      html += `
        <div class="row">
          <div class="name">
            <span>
              ${result["scores"][i]["number"]} : ${result["scores"][i]["name"]}
            </span>
          </div>
          <div class="match1">
            <button class="${m1}" onclick="loadMatch(${id}, 1);"></button>
          </div>
          <div class="match2">
            <button class="${m2}" onclick="loadMatch(${id}, 2);"></button>
          </div>
          <div class="match3">
            <button class="${m3}" onclick="loadMatch(${id}, 3);"></button>
          </div>
          <div class="match4">
            <button class="${m4}" onclick="loadMatch(${id}, 4);"></button>
          </div>
        </div>`;
    }

    // Insert the resulting HTML into the list body.
    $(".list_container .body").html(html);

    // Hide the scoresheet and show the score list.
    $("#list").removeClass("hidden");
    $("#score").addClass("hidden");

    // Scroll to the top of the score list.
    $(".list_container .body").scrollTop(0);
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // Send a request to the server to get the score list.
  $.getJSON("/referee/referee.json?action=list")
    .done(done)
    .fail(fail);
}

// Loads a particular scoresheet from the server.
function
loadMatch(id, match)
{
  // Called when the query to the server has completed.
  function
  done(result)
  {
    // See if the request was successful.
    if(result["result"] != "ok")
    {
      // It was not, so call the failure function.
      fail(result);
      return;
    }

    // Replace the score container body with the HTML scoresheet from the
    // server.
    $(".score_container .body").html(result["scoresheet"]);

    // See if this match has already been scored (partially or fully).
    if(result["sheet"] != null)
    {
      // Parse the JSON match data.
      var sheet = JSON.parse(result["sheet"]);

      // Loop through the keys of the match data.
      for(var key of Object.keys(sheet))
      {
        // Select the appropriate button.
        $("#" + key + " button").eq(sheet[key]).addClass("selected");
      }
    }

    // Update the compute button.
    computeEnable();

    // Save the team ID and match.
    teamID = id;
    theMatch = match;

    // Hide all the match number indicators and show the appropriate one for
    // this match.
    $(".score_container [class^='match'").hide();
    $(".score_container .match" + match).show();

    // Set the team number and name.
    $(".score_container .number").html(result["number"]);
    $(".score_container .name").html(result["name"]);

    // Show the score, if there is one.
    setScore(Object.hasOwn(result, "score") ? result["score"] : -1);

    // Hide any error messages that may have been present from a previous
    // sccoresheet.
    $(".error").hide();

    // There are no unsaved modifications to the scoresheet.
    modified = false;

    // Hide the score list and show the scoresheet.
    $("#list").addClass("hidden");
    $("#score").removeClass("hidden");

    // Scroll to the top of the scoresheet.
    $(".score_container .body").scrollTop(0);
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // Send a request to the server to get the scoresheet for this match.
  $.getJSON(`/referee/referee.json?action=get&id=${id}&match=${match}`)
    .done(done)
    .fail(fail);
}

// Searches for items in the referee list.
function
search()
{
  var idx, found;
  var teams = $(".list_container .body");
  var filter = $("#search").val().toLowerCase();
  var filters = filter.split(" ");

  // Do nothing if the list is not being displayed.
  if($("#list").hasClass("hidden"))
  {
    return;
  }

  // Loop over the list of teams.
  teams.children().each(function(index)
  {
    // Hide this row by default.
    var row = $(this);
    row.hide();

    // Loop through the items in the filter.
    for(idx = 0; idx < filters.length; idx++)
    {
      // See if this filter exists in the name.
      if($(row).find(".name").html().toLowerCase().indexOf(filters[idx]) == -1)
      {
        break;
      }
    }

    // Show this row if all the filter items were found.
    if(idx == filters.length)
    {
      row.show();
    }
  });
}

// Handles keydown events.
function
keydown(e)
{
  // See if Ctrl-R was pressed.
  if(((e.key == 'r') || (e.key == 'R')) && (e.ctrlKey == true) &&
     !$("#list").hasClass("hidden") && ($("dialog").length == 0))
  {
    // Load the scores from the server.
    loadScores();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     !$("#list").hasClass("hidden") && ($("dialog").length == 0))
  {
    // Move the focus to the search bar.
    $("#search").focus();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#search").is(":focus"))
  {
    // Clear the search bar.
    $("#search").val("");

    // Perform a "search" to get all the teams displayed.
    search();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }
}

// Handles setup of the referee page.
function
ready()
{
  var params = window.location.search;
  var team = -1;
  var match = -1;

  // See if there are any parameters to this page (as will be the case when
  // editing a score from the scores admin page).
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

      // Save the team ID if this parameter is the team.
      if(param[0] == "team")
      {
        team = param[1];
      }

      // Save the match number if this parameter is the match.
      if(param[0] == "match")
      {
        match = param[1];
      }
    }
  }

  // Rewrite the URL for the current page, in case there are parameters in the
  // URL (that shouldn't be exposed).
  window.history.replaceState(null, "", window.location.origin +
                                        window.location.pathname);

  // See if a team and a match were provided.
  if((team != -1) && (match != -1))
  {
    // Load the given team/match from the server.
    loadMatch(team, match);

    // Close the current page when the scoresheet is exited.
    closeOnExit = true;
  }
  else
  {
    // Load the scores from the server.
    loadScores();
  }

  // Add event handlers for elements on the referee page.
  $("#search").on("keyup", search);
  $("#refresh").on("click", loadScores);
  $("#discard").on("click", discard);
  $("#save").on("click", save);
  $("#publish").on("click", publish);
  $("#compute").on("click", compute);

  // Add a keydown event listener.
  document.addEventListener("keydown", keydown);
}

// Set the function to call when the page is ready.
$(document).ready(ready);