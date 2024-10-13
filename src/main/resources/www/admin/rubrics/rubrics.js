// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Edits the rubric for a team.
function
rubricsEdit(id)
{
  // Open the referee scoring page for this team and match.
  window.open(window.location.origin + "/judge?team=" + id);
}

// Exchanges the rubric for a team.
function
rubricsExchange(id)
{
  var new_id = -1;

  // Starts, or restarts, the rubric exchange process.
  function
  onStart()
  {
    // Get the details on the first rubric.
    var team = $("#rubrics" + id + "_name").html().trim();

    // Construct the HTML for the rubric exchange dialog.
    var html = "";
    html += `
<div class="rubrics_exchange_container">
  <div class="title">
    <span>
      <!--#str_rubrics_exchange-->
    </span>
  </div>
  <div class="team1">
    <span>
      ${team}
    </span>
  </div>
  <div class="exchange">
    <span class="fa fa-exchange"></span>
  </div>
  <div class="team2">
    <select>`
    for(var i of $(".rubrics_container .row"))
    {
      var l_id = i.id.substring(7);
      var name = $(i).find("[id$=_name]").html().trim();
      var selected = (l_id == new_id) ? " selected" : "";
      html += `
      <option value="${l_id}"${selected}>${name}</option>`;
    }
    html += `
    </select>
  </div>
  <div class="buttons">
    <button id="rubrics_exchange_cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="rubrics_exchange_ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

    // Show the dialog as a popup.
    showPopup(html,
              {
                "rubrics_exchange_cancel": null,
                "rubrics_exchange_ok": exchangeRubrics
              });
  }

  // Called to exchange the rubrics once the details have been filled in.
  function
  exchangeRubrics()
  {
    // Get the values from the editor.
    new_id = $(".rubrics_exchange_container .team2 select").val();

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
        // Reload the rubrics.
        rubricsLoad();
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

    // Send a request to the server to exchange the rubrics.
    $.getJSON("/admin/rubrics/rubrics.json?action=exchange&id=" + id +
              "&id2=" + new_id)
      .done(onDone)
      .fail(onFail);
  }

  // Start the rubric exchange process.
  onStart();
}

// Deletes a rubric.
function
rubricsDelete(id)
{
  // Called once the delete has been confirmed.
  function
  deleteRubric()
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
        // Reload the rubrics.
        rubricsLoad();
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

    // Send a request to the server to delete the rubric.
    $.getJSON("/admin/rubrics/rubrics.json?action=delete&id=" + id)
      .done(onDone)
      .fail(onFail);
  }

  // Show a confirmation to make sure this rubric should be deleted.
  showConfirmation($("#rubrics" + id + "_number").html().trim() + " : " +
                   $("#rubrics" + id + "_name").html().trim() + "<br><br>" +
                   "<!--#str_rubrics_delete-->", "<!--#str_button_delete-->",
                   deleteRubric, null);
}

// Fetches the rubrics list from the server.
function
rubricsLoad()
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    var html = "";

    // See if there are any teams.
    if(result["rubrics"].length == 0)
    {
      // There are no teams, so add an "empty rubric" to indicate that there
      // are none.
      html += `
    <div class="row">
      <div class="number"><span>-</span></div>
      <div class="name"><span><!--#str_rubrics_none--></span></div>
      <div class="project"><span>-</span></div>
      <div class="robot"><span>-</span></div>
      <div class="core"><span>-</span></div>
      <div class="action"><span>-</span></div>
    </div>`;
    }

    // Loop through the rubrics (a NOP if there are no teams).
    for(var i = 0; i < result["rubrics"].length; i++)
    {
      // Get the ID of this team.
      var id = result["rubrics"][i]["id"];

      // Get the scores for this team.
      var project = result["rubrics"][i]["project"];
      var robot = result["rubrics"][i]["robot"];
      var core = result["rubrics"][i]["core"];

      // Add the row for this team.
      html += `
    <div id="rubrics${id}" class="row">
      <span id="rubrics${id}_number" class="number">
          ${result["rubrics"][i]["number"]}
      </span>
      <span id="rubrics${id}_name" class="name">
        ${result["rubrics"][i]["name"]}
      </span>
      <span class="project">
          ${project}
      </span>
      <span class="robot">
        ${robot}
      </span>
      <span class="core">
        ${core}
      </span>
      <div class="action">
        <span id="rubrics${id}_edit" class="fa fa-pencil"
              onclick="rubricsEdit(${id});" tabindex="0"></span>`;
      if((project != "-") || (robot != "-") || (core != "-"))
      {
        html += `
        <span id="rubrics${id}_exchange" class="fa fa-exchange"
              onclick="rubricsExchange(${id});" tabindex="0">
        </span>
        <span id="rubrics${id}_delete" class="fa fa-trash"
              onclick="rubricsDelete(${id});" tabindex="0">
        </span>`;
      }
      else
      {
        html += `
        <span class="fa fa-exchange disabled"></span>
        <span class="fa fa-trash disabled"></span>`;
      }
      html += `
      </div>
    </div>`;
    }

    // Insert the set of rows into the body of the rubrics list.
    $(".rubrics_container .body").html(html);

    // Loop through all of the teams.
    const suffix = [ "_edit", "_exchange", "_delete" ];
    for(var i = 0; i < result["rubrics"].length; i++)
    {
      // Get the ID of this team.
      var id = result["rubrics"][i]["id"];

      // Loop through the three buttons for this team's rubrics.
      for(var j = 0; j < 3; j++)
      {
        // Add a keyup handler to this button.
        const tag = "#rubrics" + id + suffix[j];
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

    // Refresh the search based on the new rubrics list.
    rubricsSearch();
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_rubrics_load_failed-->", null);
  };

  // Send a request to the server to get the list of rubrics.
  $.getJSON("/admin/rubrics/rubrics.json")
    .done(onDone)
    .fail(onFail);
}

// Searches for items in the team list.
function
rubricsSearch()
{
  var idx, found;
  var rubrics = $(".rubrics_container .body");
  var filter = $("#rubrics-search").val().toLowerCase();
  var filters = filter.split(" ");

  // Loop over the list of teams.
  rubrics.children().each(function(index)
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
rubricsKeydown(e)
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

  // See if Ctrl-R was pressed.
  if(((e.key == 'r') || (e.key == 'R')) && (e.ctrlKey == true) &&
     ($("dialog:visible").length == 0))
  {
    // Load the rubrics from the server.
    rubricsLoad();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog:visible").length == 0))
  {
    // Move the focus to the search bar.
    $("#rubrics-search").focus();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#rubrics-search").is(":focus"))
  {
    // Clear the search bar.
    $("#rubrics-search").val("");

    // Perform a "search" to get all the rubrics displayed.
    rubricsSearch();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }
}

// Handles setup of the rubrics tab.
function
rubricsSetup()
{
  // Load the rubrics from the server.
  rubricsLoad();

  // Add event handlers for elements on the rubrics tab.
  $("#rubrics-search").on("keyup", rubricsSearch);
  $("#rubrics-refresh").on("click", rubricsLoad);

  // Add a keydown event listener.
  document.addEventListener("keydown", rubricsKeydown);
}

// Handles cleanup of the rubrics tab.
function
rubricsCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", rubricsKeydown);
}