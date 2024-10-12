// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Tracks if there are changes to the rubric.
var modified = false;

// The ID of the team that is being judged.
var teamID = null;

// Set to true if the window should be closed on exiting the rubric (instead of
// returning to the team list).
var closeOnExit = false;

// Selects a rubric area.
function
areaSelect(area)
{
  // Hide all of the rubric areas.
  $(".area_name").removeClass("selected");
  $(".items").hide();

  // Show the requested rubric area.
  $(".area_name:eq(" + area + ")").addClass("selected");
  $(".items:eq(" + area + ")").show();

  // Scroll to the top of the rubric.
  $(".items").scrollTop(0);
}

// Toggles the state of a rubric item/button.
function
itemToggle(rubric_sel, button)
{
  // See if the button is currently selected.
  var select = $(button).hasClass("selected");

  // Un-select all buttons for this rubric selection.
  $(rubric_sel + " button").removeClass("selected");

  // Select this button if it was not selected before.
  if(!select)
  {
    $(button).addClass("selected");
  }

  // The rubric has been modified.
  modified = true;
}

// Gets the JSON representation of the current selection state of the rubric.
function
getRubricJSON()
{
  var result = {};
  var idx;

  // Loop through all of the rubric selections.
  for(var select of $(".select"))
  {
    // Get all of the buttons for this selection.
    var buttons = $(select).find("button");

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
      // Save the button selection into the rubric state.
      result[select.id] = idx;
    }
  }

  // Convert the rubric state into a JSON string and return it.
  return(JSON.stringify(result));
}

// Handles discarding the rubric.
function
discard()
{
  // Called when the rubric discard has been confirmed.
  function
  confirm()
  {
    // Either close this window or load the team list.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      loadTeams();
    }
  }

  // See if the rubric has been modified.
  if(modified)
  {
    // Display a confirmation that the rubric should be discarded.
    showConfirmation("<!--#str_judge_discard_conf-->",
                     "<!--#str_judge_discard-->", confirm, null);
  }
  else
  {
    // The rubric has not been modified, so discard it.
    confirm();
  }
}

// Handles saving the rubric state.
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

    // Either close this window or load the team list.
    if(closeOnExit)
    {
      window.close();
    }
    else
    {
      loadTeams();
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

  // See if the rubric has been modified.
  if(modified)
  {
    // Get the JSON representation of the current rubric selections.
    var json = getRubricJSON();

    // Send a request to the server to save the rubric.
    $.getJSON(`/judge/judge.json?action=save&id=${teamID}&json=${json}`)
      .done(done)
      .fail(fail)
  }
  else
  {
    // The rubric has not been modified, so bypass sending the rubric to the
    // server and act as though it were successful.
    done(null);
  }
}

// Loads the team list from the server.
function
loadTeams()
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

    // Insert the HTML into the list body.
    $(".list_container .body").html(result["html"]);

    // Hide the rubric and show the team list.
    $("#list").removeClass("hidden");
    $("#rubric").addClass("hidden");

    // Scroll to the top of the team list.
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

  // Send a request to the server to get the team list.
  $.getJSON("/judge/judge.json?action=list")
    .done(done)
    .fail(fail);
}

// Loads a rubric from the server.
function
loadRubric(id)
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

    // Replace the rubric container body with the HTML rubric from the server.
    $(".rubric_container .body").html(result["rubric"]);

    // Save the team ID and match.
    teamID = id;

    // Set the team number and name.
    $(".rubric_container .header .number").html(result["number"]);
    $(".rubric_container .header .name").html(result["name"]);

    // There are no unsaved modifications to the rubric.
    modified = false;

    // Hide the team list and show the rubric.
    $("#list").addClass("hidden");
    $("#rubric").removeClass("hidden");

    // See if two or three areas are present.
    if(result["area_count"] != 3)
    {
      $(".area").addClass("area2");
    }
    else
    {
      $(".area").removeClass("area2");
    }

    // Add click handlers for all of the judging areas.
    areas = $(".area_name");
    for(let i = 0; i < areas.length; i++)
    {
      areas.eq(i).on("click", () => areaSelect(i));
    }

    // Select the first judging area.
    areaSelect(0);
  }

  // Called when the query to the server fails.
  function
  fail(result)
  {
    // Display an error message.
    showError(Object.hasOwn(result, "result") ? result["result"] :
              "<!--#str_connect_error-->", null);
  }

  // Send a request to the server to get the rubric for this team.
  $.getJSON(`/judge/judge.json?action=get&id=${id}`)
    .done(done)
    .fail(fail);
}

// Searches for items in the team list.
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
    // Load the teams from the server.
    loadTeams();

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

// Handles setup of the judge page.
function
ready()
{
  var params = window.location.search;
  var team = -1;

  // See if there are any parameters to this page (as will be the case when
  // editing a rubric from the judges admin page).
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
    }
  }

  // Rewrite the URL for the current page, in case there are parameters in the
  // URL (that shouldn't be exposed).
  window.history.replaceState(null, "", window.location.origin +
                                        window.location.pathname);

  // See if a team was provided.
  if(team != -1)
  {
    // Load the given team's rubric from the server.
    loadRubric(team);

    // Close the current page when the rubric is exited.
    closeOnExit = true;
  }
  else
  {
    // Load the team list from the server.
    loadTeams();
  }

  // Add event handlers for elements on the judge page.
  $("#btn_menu").on("click", showMenu);
  $("#search").on("keyup", search);
  $("#refresh").on("click", loadTeams);
  $("#discard").on("click", discard);
  $("#save").on("click", save);

  // Add a keydown event listener.
  document.addEventListener("keydown", keydown);
}

// Set the function to call when the page is ready.
$(document).ready(ready);