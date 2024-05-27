// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Generates the generic HTML for a team editor, supporting both adding new
// teams and editing existing teams.
function
teamsEditorHtml(title, number, name)
{
  // Change the number input to an appropriate HTML input value specifier.
  if(number !== "")
  {
    number = "value=\"" + number + "\" ";
  }

  // Change the name input to an appropriate HTML input value specifier.
  if(name !== "")
  {
    name = "value=\"" + name + "\" ";
  }

  // Construct the HTML for the editor.
  var html = `
<div class="teams_editor_container">
  <div class="title">
    ${title}
  </div>
  <div class="number">
    <div class="label">
      <!--#str_teams_list_number-->:
    </div>
    <div class="input">
      <input id="teams-editor-number" type="text" ${number}
             placeholder="&#61724;" />
    </div>
  </div>
  <div class="name">
    <div class="label">
      <!--#str_teams_list_name-->:
    </div>
    <div class="input">
      <input id="teams-editor-name" type="text" ${name}
             placeholder="&#61724;" >
    </div>
  </div>
  <div class="buttons">
    <button id="teams-editor-cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="teams-editor-ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Return the HTML.
  return(html);
}

// Adds a team.
function
teamsAdd()
{
  var number = "";
  var name = "";

  // Starts, or restarts, the team add process.
  function
  onStart()
  {
    // Construct an editor for adding a team.
    var html = teamsEditorHtml("<!--#str_teams_add-->", number, name);

    // Show the editor as a popup.
    showPopup(html,
              {
                "teams-editor-cancel": null,
                "teams-editor-ok": addTeam
              });
  }

  // Called to add the team once the details have been filled in.
  function
  addTeam()
  {
    // Get the values from the editor.
    number = $("#teams-editor-number").val();
    name = $("#teams-editor-name").val();

    // Encode the values for safe transmission to the server.
    var number_enc = encodeURIComponent(number);
    var name_enc = encodeURIComponent(name);

    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] != "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Reload the teams.
        teamsLoad();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", onStart);
    }

    // Fail if the number is invalid.
    if(number == "")
    {
      onFail("<!--#str_teams_invalid_number-->");
    }

    // Fail if the name is invalid.
    else if(name == "")
    {
      onFail("<!--#str_teams_invalid_name-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to create the team.
      $.getJSON("/teams/teams.json?action=add&number=" + number_enc +
                "&name=" + name_enc)
        .done(onDone)
        .fail(onFail);
    }
  };

  // Start the team add process.
  onStart();
}

// Called when a keyup event occurs with the teams add element selected.
function
teamsAddKeyUp(event)
{
  // See if the enter key was pressed.
  if(event.key == "Enter")
  {
    // Translate this into a click.
    $("#teams-add").click();

    // Prevent the default handling of this event.
    event.preventDefault();
  }
}

// Edits an existing team.
function
teamsEdit(id)
{
  var number = $("#team" + id + "_number").html().trim();
  var name = $("#team" + id + "_name").html().trim();

  // Starts, or restarts, the team edit process.
  function
  onStart()
  {
    // Construct an editor for editing a team.
    var html = teamsEditorHtml("<!--#str_teams_edit-->", number, name);

    // Show the editor as a popup.
    showPopup(html,
              {
                "teams-editor-cancel": null,
                "teams-editor-ok": editTeam
              });
  }

  // Called to edit the team once the details have been filled in.
  function
  editTeam()
  {
    // Get the values from the editor.
    number = $("#teams-editor-number").val();
    name = $("#teams-editor-name").val();

    // Encode the values for safe transmission to the server.
    var number_enc = encodeURIComponent(number);
    var name_enc = encodeURIComponent(name);

    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] != "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Reload the teams.
        teamsLoad();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", onStart);
    }

    // Fail if the number is invalid.
    if(number == "")
    {
      onFail("<!--#str_teams_invalid_number-->");
    }

    // Fail if the name is invalid.
    else if(name == "")
    {
      onFail("<!--#str_teams_invalid_name-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to edit the team.
      $.getJSON("/teams/teams.json?action=edit&id=" + id + "&number=" +
                number_enc + "&name=" + name_enc)
        .done(onDone)
        .fail(onFail);
    }
  }

  // Start the team edit process.
  onStart();
}

// Deletes a team.
function
teamsDelete(id)
{
  var html, warning;

  // Called once the delete has been confirmed.
  function
  deleteTeam()
  {
    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] != "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Reload the teams.
        teamsLoad();

        // Update the status bar (in case this team was associated with the
        // selected event).
        updateStatus();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", null);
    }

    // Send a request to the server to delete the team.
    $.getJSON("/teams/teams.json?action=delete&id=" + id)
      .done(onDone)
      .fail(onFail);
  };

  // Get the appropriate warning message, based on the presence of scores.
  if($("#team" + id + "_seasonScores").length)
  {
    warning = "<!--#str_teams_delete_season_score_warning-->";
  }
  else
  {
    warning = "<!--#str_teams_delete_warning-->";
  }

  // Show a confirmation to make sure this team should be deleted.
  showConfirmation($("#team" + id + "_number").html() + ": " +
                   $("#team" + id + "_name").html() + "<br><br>" + warning,
                   "<!--#str_button_delete-->", deleteTeam, null);
}

// Selects a team.
function
teamsSelect(id)
{
  // Determine if the team is current part of the event.
  var enabled = $("#team" + id + "_select").hasClass("fa-check-square-o");

  // Called when the seelct on the team should be inverted.
  function
  selectTeam()
  {
    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] != "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Toggle the check mark on this team.
        if(enabled)
        {
          $("#team" + id + "_select")
            .removeClass("fa-check-square-o")
            .addClass("fa-square-o");
        }
        else
        {
          $("#team" + id + "_select")
            .removeClass("fa-square-o")
            .addClass("fa-check-square-o");
        }

      // Update the status bar (since the teams in this event have changed).
        updateStatus();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", null);
    }

    // Send a request to the server to add or remove the team from the event.
    if(enabled)
    {
      $.getJSON("/teams/teams.json?action=not_in_event&id=" + id)
        .done(onDone)
        .fail(onFail);
    }
    else
    {
      $.getJSON("/teams/teams.json?action=in_event&id=" + id)
        .done(onDone)
        .fail(onFail);
    }
  }

  // See if the team is not part of the event, or it is but has no scores.
  if(!enabled || ($("#team" + id + "_eventScores").length == 0))
  {
    // Remove the team from the event.
    selectTeam();
  }
  else
  {
    // Show a confirmation to make sure this team should be removed from the
    // event.
    showConfirmation($("#team" + id + "_number").html() + ": " +
                     $("#team" + id + "_name").html() +
                     "<br><br><!--#str_teams_event_score_warning-->",
                     "<!--#str_button_delete-->", selectTeam, null);
  }
}

// Import teams from the previous season.
function
teamsImport()
{
  // TBD...
}

// Fetches the team list from the server.
function
teamsLoad()
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    var html = "";

    // See if there are any teams.
    if(result["teams"].length == 0)
    {
      // There are no teams, so add an "empty team" to indicate that there are
      // none.
      html += `
    <div class="row">
      <div class="number">-</div>
      <div class="name"><!--#str_teams_none--></div>
      <div class="actions">-</div>
    </div>`;
    }

    // Loop through the teams (a NOP if there are no teams).
    for(var i = 0; i < result["teams"].length; i++)
    {
      // Get the ID of this team.
      var id = result["teams"][i]["id"];
      var icon = result["teams"][i]["inEvent"] ? "fa-check-square-o" : "fa-square-o";

      // Add the row for this team.
      html += `
    <div id="team${id}" class="row">
      <div id="team${id}_number" class="number">
        ${result["teams"][i]["number"]}
      </div>
      <div id="team${id}_name" class="name">
        ${result["teams"][i]["name"]}
      </div>
      <div class="at_event">
        <span id="team${id}_select" class="fa fa-fw ${icon}"
              onclick="teamsSelect(${id});" tabindex="0"></span>
      </div>
      <div class="actions">
        <span id="team${id}_edit" class="fa fa-fw fa-pencil"
              onclick="teamsEdit(${id});" tabindex="0"></span>
        <span id="team${id}_delete"  class="fa fa-fw fa-trash"
              onclick="teamsDelete(${id});" tabindex="0"></span>`;
      if(result["teams"][i]["seasonScores"] === true)
      {
        html += `
        <span id="team${id}_seasonScores" class="hidden"></span>`;
      }
      if(result["teams"][i]["eventScores"] === true)
      {
        html += `
        <span id="team${id}_eventScores" class="hidden"></span>`;
      }
      html += `
      </div>
    </div>`;
    }

    // Insert the set of rows into the body of the team list.
    $(".teams_container .body").html(html);

    // Loop through all of the teams.
    const suffix = [ "_select", "_edit", "_delete" ];
    for(var i = 0; i < result["teams"].length; i++)
    {
      // Get the ID of this team.
      var id = result["teams"][i]["id"];

      // Loop through the three buttons for this team.
      for(var j = 0; j < 3; j++)
      {
        // Add a keyup handler to this button.
        const tag = "#team" + id + suffix[j];
        $(tag).on("keyup", (event) =>
                           {
                             // See if the enter key was pressed.
                             if(event.key == "Enter")
                             {
                               // "Convert" the key press to a mouse click.
                               $(tag).click();

                               // Prevent the normal handling of this key.
                               event.preventDefault();
                             }
                           });
      }
    }

    // Refresh the search based on the new team list.
    teamsSearch();
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_teams_load_failed-->", null);
  };

  // Send a request to the server to get the list of teams.
  $.getJSON("/teams/teams.json")
    .done(onDone)
    .fail(onFail);
}

// Searches for items in the team list.
function
teamsSearch()
{
  var idx, found;
  var teams = $(".teams_container .body");
  var filter = $("#teams-search").val().toLowerCase();
  var filters = filter.split(" ");

  // Loop over the list of teams.
  teams.children().each(function(index)
  {
    // Hide this row by default.
    var row = $(this);
    row.hide();

    // Loop through the items in the filter.
    for(idx = 0; idx < filters.length; idx++)
    {
      // Loop through each of the columns of this row.
      found = false;
      row.children().each(function(index)
      {
        // Only look at the first two columns.
        if(index < 2)
        {
          // See if this filter item exists in this cell.
          if($(this).html().toLowerCase().indexOf(filters[idx]) != -1)
          {
            // The filter item was found.
            found = true;
          }
        }
      });

      // Stop looking if this filter item was not found.
      if(found == false)
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
teamsKeydown(e)
{
  // See if Ctrl-A was pressed.
  if(((e.key == 'a') || (e.key == 'A')) && (e.ctrlKey == true) &&
     ($("dialog").length == 0))
  {
    // Add a team.
    teamsAdd();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog").length == 0))
  {
    // Move the focus to the search bar.
    $("#teams-search").focus();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#teams-search").is(":focus"))
  {
    // Clear the search bar.
    $("#teams-search").val("");

    // Perform a "search" to get all the teams displayed.
    teamsSearch();
  }
}

// Handles setup of the teams tab.
function
teamsSetup()
{
  // Load the teams from the server.
  teamsLoad();

  // Add event handlers for elements on the teams tab.
  $("#teams-search").on("keyup", teamsSearch);
  $("#teams-add").on("click", teamsAdd);
  $("#teams-import").on("click", teamsImport);

  // Import isn't available yet, so hide the icon.
  $("#teams-import").remove();

  // Add a click handler for the add button.
  $("#teams-add").on("keyup", teamsAddKeyUp);

  // Add a keydown event listener.
  document.addEventListener("keydown", teamsKeydown);
}

// Handles cleanup of the teams tab.
function
teamsCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", teamsKeydown);
}