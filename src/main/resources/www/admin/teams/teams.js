// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Whether or not divisions are enabled.
var divisions_enabled = false;

// The number of divisions, when enabled.
var division_count = 0;

// The names of the divisions, when enabled.
var division_name = [ "", "", "", "" ];

// Generates the generic HTML for a team editor, supporting both adding new
// teams and editing existing teams.
function
teamsEditorHtml(title, division, number, name)
{
  // Determine the division class for the editor.
  division_class = divisions_enabled ? " have_divisions" : "";

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
<div class="teams_editor_container${division_class}">
  <div class="title">
    ${title}
  </div>
  <div class="division">
    <div class="label">
      <!--#str_teams_list_division-->:
    </div>
    <div class="input">
      <select id="teams-editor-division">`;
  for(var i = 0; i < division_count; i++)
  {
    var select = (division == (i + 1)) ? " selected" : "";
    html += `<option value="${i + 1}"${select}>${division_name[i]}</option>`;
  }
  html += `</select>
    </div>
  </div>
  <div class="number">
    <div class="label">
      <!--#str_teams_list_number-->:
    </div>
    <div class="input">
      <input id="teams-editor-number" type="text" ${number}
             placeholder="&#xf11c;" />
    </div>
  </div>
  <div class="name">
    <div class="label">
      <!--#str_teams_list_name-->:
    </div>
    <div class="input">
      <input id="teams-editor-name" type="text" ${name}
             placeholder="&#xf11c;" >
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
  var division = "";
  var number = "";
  var name = "";

  // Starts, or restarts, the team add process.
  function
  onStart()
  {
    // Construct an editor for adding a team.
    var html =
      teamsEditorHtml("<!--#str_teams_add-->", division, number, name);

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
    division = $("#teams-editor-division").val();
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
    if(number === "")
    {
      onFail("<!--#str_teams_invalid_number-->");
    }

    // Fail if the name is invalid.
    else if(name === "")
    {
      onFail("<!--#str_teams_invalid_name-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to create the team.
      $.getJSON("/admin/teams/teams.json?action=add&division=" + division +
                "&number=" + number_enc + "&name=" + name_enc)
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
  var division = $("#team" + id + "_division").data("division");
  var number = $("#team" + id + "_number").html().trim();
  var name = $("#team" + id + "_name").html().trim();

  // Starts, or restarts, the team edit process.
  function
  onStart()
  {
    // Construct an editor for editing a team.
    var html =
      teamsEditorHtml("<!--#str_teams_edit-->", division, number, name);

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
    division = $("#teams-editor-division").val();
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
      $.getJSON("/admin/teams/teams.json?action=edit&id=" + id + "&division=" +
                division + "&number=" + number_enc + "&name=" + name_enc)
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
    $.getJSON("/admin/teams/teams.json?action=delete&id=" + id)
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

  // Called when the select on the team should be inverted.
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
      $.getJSON("/admin/teams/teams.json?action=not_in_event&id=" + id)
        .done(onDone)
        .fail(onFail);
    }
    else
    {
      $.getJSON("/admin/teams/teams.json?action=in_event&id=" + id)
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
  let data;
  let division = null;
  let number = null;
  let name = null;
  let idx;

  // Called when a failure occurs.
  function
  onFail(result)
  {
    // See if the result exists and contains a result string.
    if((result !== undefined) && (result["result"] !== undefined))
    {
      // Display the error returned by the server.
      showError(result["result"], null);
    }
    else
    {
      // Display a generic server connection error.
      showError("<!--#str_connect_error-->", null);
    }
  }

  // Called when adding a team to the current event is complete.
  function
  onTeamAtEventDone()
  {
    // Increment to the next team.
    idx++;

    // See if there are more teams to process.
    if(idx < data.data.length)
    {
      // Add the next team.
      teamNext();
    }
    else
    {
      // All the teams are added, so reload the team list.
      teamsLoad();

      // Update the status bar.
      updateStatus();
    }
  }

  // Called when a team action (add or edit) is complete.
  function
  onTeamDone(result)
  {
    // See if the team already exists.
    if(result["result"] === "<!--#str_teams_already_exists-->")
    {
      // Get the details about this team.
      let id = result["id"];
      let division_enc = divisions_enabled ? data.data[idx][division] : 1;
      let number_enc = encodeURIComponent(data.data[idx][number]);
      let name_enc = encodeURIComponent(data.data[idx][name]);

      // Send a request to the server to edit the team.
      $.getJSON("/admin/teams/teams.json?action=edit&id=" + id + "&division=" +
                division_enc + "&number=" + number_enc + "&name=" + name_enc)
        .done(onTeamDone)
        .fail(onFail);
    }

    // Otherwise, see if the request was successful.
    else if(result["result"] === "ok")
    {
      // Send a request to the server to add this team to the current event.
      $.getJSON("/admin/teams/teams.json?action=in_event&id=" + result["id"])
        .done(onTeamAtEventDone)
        .fail(onFail);
    }

    // Otherwise, another failure occurred.
    else
    {
      // Handle the failure.
      onFail(result);
    }
  }

  // Loads the next team into the database.
  function
  teamNext()
  {
    // Get the details about this team.
    let division_enc = divisions_enabled ? data.data[idx][division] : 1;
    let number_enc = encodeURIComponent(data.data[idx][number]);
    let name_enc = encodeURIComponent(data.data[idx][name]);

    // Send a request to the server to create the team.
    $.getJSON("/admin/teams/teams.json?action=add&division=" + division_enc +
              "&number=" + number_enc + "&name=" + name_enc)
      .done(onTeamDone)
      .fail(onFail);
  }

  // Called when the CSV file has been read and parsed.
  function
  fileLoaded(result)
  {
    // Save the result for importing in the teams.
    data = result;

    // Fail if there were errors parsing the CSV file.
    if(result.errors.length !== 0)
    {
      showError("<!--#str_teams_csv_error--><br>" + result.errors[0].message,
                null);
      return;
    }

    // Loop through the fields in this CSV file.
    for(var i = 0; i < result.meta.fields.length; i++)
    {
      // See if this is the division column.
      if(result.meta.fields[i].normalize("NFD").toLowerCase() ===
         "<!--#str_teams_csv_division_heading-->")
      {
        // Save the name of the division column.
        division = result.meta.fields[i];
      }

      // See if this is the number column.
      if(result.meta.fields[i].normalize("NFD").toLowerCase() ===
         "<!--#str_teams_csv_number_heading-->")
      {
        // Save the name of the number column.
        number = result.meta.fields[i];
      }

      // See if this is the name column.
      if(result.meta.fields[i].normalize("NFD").toLowerCase() ===
         "<!--#str_teams_csv_name_heading-->")
      {
        // Save the name of the name column.
        name = result.meta.fields[i];
      }
    }

    // Generate an error if divisions are enabled and a division column is not
    // present.
    if(divisions_enabled && (division === null))
    {
      showError("<!--#str_teams_csv_division_error-->", null);
      return;
    }

    // Generate an error if the number column is not present.
    if(number === null)
    {
      showError("<!--#str_teams_csv_number_error-->", null);
      return;
    }

    // Generate an error if the name column is not present.
    if(name === null)
    {
      showError("<!--#str_teams_csv_name_error-->", null);
      return;
    }

    // Start the team import process.
    idx = 0;
    teamNext();
  }

  // Called when a CSV file has been selected.
  function
  fileSelected()
  {
    // Read and parse this CSV file.
    $(this).parse({
      config: {
        complete: fileLoaded,
        dynamicTyping: true,
        header: true
      }
    });
  }

  // Create a file input element and click on it, allowing the user to select
  // the CSV file to import.
  const link = document.createElement("input");
  link.type = "file";
  link.accept = ".csv";
  link.addEventListener("change", fileSelected);
  link.click();
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

    // Update the division information.
    divisions_enabled = result["divisions_enabled"];
    division_count = result["division_count"];
    division_name[0] = result["division1_name"];
    division_name[1] = result["division2_name"];
    division_name[2] = result["division3_name"];
    division_name[3] = result["division4_name"];

    // See if there are any teams.
    if(result["teams"].length == 0)
    {
      // There are no teams, so add an "empty team" to indicate that there are
      // none.
      html += `
    <div class="row">
      <span class="division">-</span>
      <span class="number">-</span>
      <span class="name"><!--#str_teams_none--></span>
      <span class="at_event">-</span>
      <span class="actions">-</span>
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
      <span id="team${id}_division" class="division" data-division="${result["teams"][i]["division"]}">
        ${division_name[result["teams"][i]["division"] - 1]}
      </span>
      <span id="team${id}_number" class="number">
        ${result["teams"][i]["number"]}
      </span>
      <span id="team${id}_name" class="name">
        ${result["teams"][i]["name"]}
      </span>
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
  $.getJSON("/admin/teams/teams.json")
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
     ($("dialog:visible").length == 0))
  {
    // Add a team.
    teamsAdd();
  }

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

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog:visible").length == 0))
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