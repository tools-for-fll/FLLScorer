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
  var missions;

  // Get the selector for the core values selection.
  if($(".mission_sel#GP_1").length == 1)
  {
    missions = ".mission_sel:not(#GP_1)";
  }
  else
  {
    missions = ".mission_sel:not(#CV_1)";
  }

  // Get the number of mission items, number of mission items with selections
  // make, and the number of missions items with non-empty inputs.
  var mission_count = $(missions).length;
  var mission_select = $(missions + " .selected").length;
  var mission_input = 0;
  for(var idx = 0; idx < $(missions + " input").length; idx++)
  {
    if($(missions + " input").eq(idx).val() !== "")
    {
      mission_input++;
    }
  }

  // See if there are selections for the entire scoresheet.
  if((mission_select + mission_input) === mission_count)
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
    $("#compute").html(parseInt(score));

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

// Called when a text input changes.
function
itemChange()
{
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
    // See if this mission item is an input field.
    var input = $(mission).find("input");
    if(input.length !== 0)
    {
      // Get the value of this input.
      var value = parseFloat(input.val());

      // If there is a minimum value, bump the value up to the minimum if it is
      // less than the minimum (on some browsers, the minimum is not honored
      // when directly typing a value).
      if((input.attr("min") !== undefined) && (value < input.attr("min")))
      {
        value = input.attr("min");
      }

      // If there is a maximum value, bump the value down to the maximum if it
      // is greater than the maximum (on some browsers, the maximum is not
      // honored hwne directly typing a value).
      if((input.attr("max") !== undefined) && (value > input.attr("max")))
      {
        value = input.attr("max");
      }

      // Save the value into the scoresheet state.
      result[mission.id] = value;

      // Go to the next mission item.
      continue;
    }

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
    // Either close this window or display the score list.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      $("#score").addClass("hidden");
      $("#list").removeClass("hidden");
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

    // Either close this window or display the score list.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      $("#score").addClass("hidden");
      $("#list").removeClass("hidden");
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

    // Either close this window or display the score list.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      $("#score").addClass("hidden");
      $("#list").removeClass("hidden");
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
    $("body").removeClass("matches2").removeClass("matches3").
      removeClass("matches103").removeClass("matches4").
      addClass("matches" + result["matches"]);

    // Start constructing the HTML for the list of scores.
    var html = "";

    // Loop through all the teams.
    for(var i = 0; i < result["scores"].length; i++)
    {
      // Get the team ID for this team.
      var id = result["scores"][i]["id"];

      // Get the match statuses for this team.
      var m0 = result["scores"][i]["match0"];
      var m1 = result["scores"][i]["match1"];
      var m2 = result["scores"][i]["match2"];
      var m3 = result["scores"][i]["match3"];
      var m4 = result["scores"][i]["match4"];

      // Convert the match statuses into the color for each match; red for a
      // scoresheet without a score, green for a scoresheet and a score, and
      // yellow for a match that has not been scored.
      m0 = (m0 === 1) ? "red" : ((m0 === 2) ? "green" : "yellow");
      m1 = (m1 === 1) ? "red" : ((m1 === 2) ? "green" : "yellow");
      m2 = (m2 === 1) ? "red" : ((m2 === 2) ? "green" : "yellow");
      m3 = (m3 === 1) ? "red" : ((m3 === 2) ? "green" : "yellow");
      m4 = (m4 === 1) ? "red" : ((m4 === 2) ? "green" : "yellow");

      // Add the HTML for this row/team.
      html += `
        <div class="row" id="team${id}">
          <div class="name">
            <span>
              ${result["scores"][i]["number"]} : ${result["scores"][i]["name"]}
            </span>
          </div>
          <div class="match0">
            <button class="${m0}" onclick="loadMatch(${id}, 0);"></button>
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

    // Construct the HTML for the scoresheet starting with an empty string.
    var html = "";

    // Get the locale from the response.
    var locale = result["locale"];

    // Get the missions from the scoresheet.
    var missions = result["scoresheet"]["missions"];

    // Loop through the missions.
    for(var i = 0; i < missions.length; i++)
    {
      // Get the JSON object for this mission, and the mission ID from that.
      var mission = missions[i];
      var mission_id = mission["mission"];

      // Get the name of the mission.  If it is not available in the current
      // locale, default to en_US.
      var name;
      if(mission["name"].hasOwnProperty(locale))
      {
        name = mission["name"][locale];
      }
      else
      {
        name = mission["name"]["en_US"];
      }

      // See if this mission has a no touch requirement (team equipment can not
      // be touching the mission model).
      var noTouch = mission.hasOwnProperty("no_touch");

      // Add the mission, ID, and name to the HTML.
      html += `<div id="${mission_id}" class="mission">`;
      html += `<div class="mission_id">`;
      html += `<span>${mission_id}</span>`;
      html += `</div>`;
      html += `<div class="mission_name">`;
      html += `<span>${name}</span>`;
      if(noTouch)
      {
        html += `<img class="no_touch" src="referee/no_touch.png"></img>`;
      }
      html += `</div>`;

      // Get the mission items and loop through them.
      var items = mission["items"];
      for(var j = 0; j < items.length; j++)
      {
        // Get the JSON object for this mission item, and the ID for it.
        var item = items[j];
        var item_id = item["id"];

        // Get the description for this mission item.  If the description is
        // not available in the current locale, default back to en_US.
        var description;
        if(item["description"].hasOwnProperty(locale))
        {
          description = item["description"][locale];
        }
        else
        {
          description = item["description"]["en_US"];
        }

        // Add the description of this item to the HTML.
        html += `<hr class="mission_item">`;
        html += `<div class="mission_desc">`;
        html += `<span>${description}</span>`;
        html += `</div>`;
        html += `<div id="${mission_id}_${item_id}" class="mission_sel">`;

        // See if this item has a yes/no selection.
        if(item["type"] === "yesno")
        {
          // Add a yes and no button to the selection HTML.
          html += `<button onclick="itemToggle('#${mission_id}_${item_id}', ` +
                  `this);"><!--#str_no--></button>`;
          html += `<button onclick="itemToggle('#${mission_id}_${item_id}', ` +
                  `this);"><!--#str_yes--></button>`;
        }

        // Otherwise, see if this item has an enumeration selection.
        else if(item["type"] === "enum")
        {
          // Get the array of choices.  If they are not available in the current
          // locale, default back to en_US.
          var choices;
          if(item["choices"].hasOwnProperty(locale))
          {
            choices = item["choices"][locale];
          }
          else
          {
            choices = item["choices"]["en_US"];
          }

          // Loop through the item choices.
          for(var k = 0; k < choices.length; k++)
          {
            // Add a button for this choice to the HTML.
            html += `<button onclick="itemToggle('#${mission_id}_` +
                    `${item_id}', this);">${choices[k]}</button>`;
          }
        }

        // Otherwise, see if this item has a number input.
        else if(item["type"] === "number")
        {
          // Get the minimum and maximum for this input, if they are provided.
          let min = (item["min"] !== undefined) ? ` min="${item["min"]}"` : "";
          let max = (item["max"] !== undefined) ? ` max="${item["max"]}"` : "";

          // Add the input for this item to the HTML.
          html += `<input type="number"${min}${max} oninput="itemChange();">` +
                  `</input>`;
        }

        // Otherwise, the selection type is unknown.
        else
        {
          html += `<button>ERROR!</button>`;
        }

        // End this item.
        html += `</div>`;
      }

      // Add the mission error message contaianer.
      html += `<div class="error">`;
      html += `<hr class="mission_item">`;
      html += `<div class="mission_error">`;
      html += `</div>`;
      html += `</div>`;

      // End this mission.
      html += `</div>`;
    }

    // Replace the score container body with the HTML scoresheet.
    $(".score_container .body").html(html);

    // See if this match has already been scored (partially or fully).
    if(result["sheet"] != null)
    {
      // Parse the JSON match data.
      var sheet = JSON.parse(result["sheet"]);

      // Loop through the keys of the match data.
      for(var key of Object.keys(sheet))
      {
        // See if this item is a number input.
        var input = $(`#${key} input[type="number"]`);
        if(input.length !== 0)
        {
          // Set the value of the number input.
          input.val(sheet[key]);
        }
        else
        {
          // Select the appropriate button.
          $("#" + key + " button").eq(sheet[key]).addClass("selected");
        }
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

// Connects to the server WebSocket interface.
function
wsConnect()
{
  // Create a new WebSocket.
  ws = new WebSocket(window.location.origin.replace("https", "wss") +
                     "/referee/referee.ws");

  // Set the functions to call when a message is received for the WebSocket is
  // closed.
  ws.onmessage = wsMessage;
  ws.onclose = wsClose;
}

// Called when a message is received from the WebSocket.
function
wsMessage(e)
{
  // Split the message into based on a colon as a separator.
  var fields = e.data.split(":");

  // See if there are the correct number of fields and a valid match number.
  if((fields.length == 4) && ((fields[0] === "m0") || (fields[0] === "m1") ||
                              (fields[0] === "m2") || (fields[0] === "m3") ||
                              (fields[0] === "m4")))
  {
    // Get the button corresponding to this team/match.
    var button = $("#team" + fields[1] + " .match" +
                   fields[0].substring(1, 2) + " button");

    // Remove any colors from this button.
    button.removeClass("red yellow green");

    // Set the button color based on this match's state.
    if(fields[2] == 2)
    {
      button.addClass("green");
    }
    else if(fields[2] == 1)
    {
      button.addClass("red");
    }
    else
    {
      button.addClass("yellow");
    }
  }
}

// Called when the WebSocket closes.
function
wsClose()
{
  // Attempt to reconnect to the server after a second (to avoid flooding the
  // network with requests).
  setTimeout(wsConnect, 1000);
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
     !$("#list").hasClass("hidden") && ($("dialog:visible").length == 0))
  {
    // Load the scores from the server.
    loadScores();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     !$("#list").hasClass("hidden") && ($("dialog:visible").length == 0))
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
  $("#btn_menu").on("click", showMenu);
  $("#search").on("keyup", search);
  $("#refresh").on("click", loadScores);
  $("#discard").on("click", discard);
  $("#save").on("click", save);
  $("#publish").on("click", publish);
  $("#compute").on("click", compute);

  // Add a keydown event listener.
  document.addEventListener("keydown", keydown);

  // Connect to the server via a WebSocket.
  wsConnect();
}

// Set the function to call when the page is ready.
$(document).ready(ready);