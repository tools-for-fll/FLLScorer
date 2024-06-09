// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Generates the generic HTML for an event editor, supporting both adding new
// events and editing existing events.
function
eventsEditorHtml(title, date, matches, name)
{
  var select3, select4;

  // Change the date input to an appropriate HTML input value specifier.
  if(date !== "")
  {
    date = "value=\"" + date + "\" ";
  }
  else
  {
    date = "value=\"" + new Date().toLocaleDateString("en-CA") + "\" ";
  }

  // Set the selected attributes based on the number of matches.
  if(matches == "4")
  {
    select3 = "";
    select4 = " selected";
  }
  else
  {
    select3 = " selected";
    select4 = "";
  }

  // Change the name input to an appropriate HTML input value specifier.
  if(name !== "")
  {
    name = "value=\"" + name + "\" ";
  }

  // Construct the HTML for the editor.
  var html = `
<div class="events_editor_container">
  <div class="title">
    ${title}
  </div>
  <div class="date">
    <div class="label">
      <!--#str_events_list_date-->:
    </div>
    <div class="input">
      <input id="events-editor-date" type="date" ${date} />
    </div>
  </div>
  <div class="matches">
    <div class="label">
      <!--#str_events_list_matches-->:
    </div>
    <div class="input">
      <select id="events-editor-matches">
        <option value=3${select3}><!--#str_events_list_three--></option>
        <option value=4${select4}><!--#str_events_list_four--></option>
      </select>
    </div>
  </div>
  <div class="name">
    <div class="label">
      <!--#str_events_list_name-->:
    </div>
    <div class="input">
      <input id="events-editor-name" type="text" ${name}
             placeholder="&#61724;" >
    </div>
  </div>
  <div class="buttons">
    <button id="events-editor-cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="events-editor-ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Return the HTML.
  return(html);
}

// Adds an event.
function
eventsAdd()
{
  var date = "";
  var matches = "";
  var name = "";

  // Starts, or restarts, the event add process.
  function
  onStart()
  {
    // Construct an editor for adding an event.
    var html = eventsEditorHtml("<!--#str_events_add-->", date, matches, name);

    // Show the editor as a popup.
    showPopup(html,
              {
                "events-editor-cancel": null,
                "events-editor-ok": addEvent
              });
  }

  // Called to add the event once the details have been filled in.
  function
  addEvent()
  {
    // Get the values from the editor.
    date = $("#events-editor-date").val();
    matches = $("#events-editor-matches").val();
    name = $("#events-editor-name").val();

    // Encode the values for safe transmission to the server.
    var date_enc = encodeURIComponent(date);
    var matches_enc = encodeURIComponent(matches);
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
        // Reload the events.
        eventsLoad();

        // Update the status bar (in case this is the first event and is
        // therefore selected by default).
        updateStatus();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", start);
    }

    // Fail if the date is invalid.
    if(date == "")
    {
      onFail("<!--#str_events_invalid_date-->");
    }

    // Fail if the number of matches is invalid.
    else if((matches != 3) && (matches != 4))
    {
      onFail("<!--#str_events_invalid_matches-->");
    }

    // Fail if the name is invalid.
    else if(name == "")
    {
      onFail("<!--#str_events_invalid_name-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to create the event.
      $.getJSON("/admin/events/events.json?action=add&date=" + date_enc +
                "&matches=" + matches_enc + "&name=" + name_enc)
        .done(onDone)
        .fail(onFail);
    }
  };

  // Start the event add process.
  onStart();
}

// Called when a keyup event occurs with the events add element selected.
function
eventsAddKeyUp(event)
{
  // See if the enter key was pressed.
  if(event.key == "Enter")
  {
    // Translate this into a click.
    $("#events-add").click();

    // Prevent the default handling of this event.
    event.preventDefault();
  }
}

// Edits an existing event.
function
eventsEdit(id)
{
  var date = $("#event" + id + "_date").html().trim();
  var matches = $("#event" + id + "_matches").html().trim();
  var name = $("#event" + id + "_name").html().trim();

  // Starts, or restarts, the event edit process.
  function
  onStart()
  {
    // Construct an editor for editing an event.
    var html = eventsEditorHtml("<!--#str_events_edit-->", date, matches,
                                name);

    // Show the editor as a popup.
    showPopup(html,
              {
                "events-editor-cancel": null,
                "events-editor-ok": editEvent
              });
  }

  // Called to edit the event once the details have been filled in.
  function
  editEvent()
  {
    // Get the values from the editor.
    date = $("#events-editor-date").val();
    matches = $("#events-editor-matches").val();
    name = $("#events-editor-name").val();

    // Encode the values for safe transmission to the server.
    var date_enc = encodeURIComponent(date);
    var matches_enc = encodeURIComponent(matches);
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
        // Reload the events.
        eventsLoad();

        // Update the status bar (in case this is the selected event and its
        // name has changed).
        updateStatus();
      }
    }

    // Called when the query to the server fails.
    function
    onFail(result)
    {
      // Display an error message.
      showError((typeof(result) === "string") ? result :
                "<!--#str_connect_error-->", start);
    }

    // Fail if the date is invalid.
    if(date == "")
    {
      onFail("<!--#str_events_invalid_date-->");
    }

    // Fail if the number of matches is invalid.
    else if((matches != 3) && (matches != 4))
    {
      onFail("<!--#str_events_invalid_matches-->");
    }

    // Fail if the name is invalid.
    else if(name == "")
    {
      onFail("<!--#str_events_invalid_name-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to edit the event.
      $.getJSON("/admin/events/events.json?action=edit&id=" + id + "&date=" +
                date + "&matches=" + matches + "&name=" + name)
        .done(onDone)
        .fail(onFail);
    }
  }

  // Start the event edit process.
  onStart();
}

// Deletes an event.
function
eventsDelete(id)
{
  var warning;

  // Called once the delete has been confirmed.
  function
  deleteEvent()
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
        // Reload the events.
        eventsLoad();

        // Update the status bar (in case this was the selected event).
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

    // Send a request to the server to delete the event.
    $.getJSON("/admin/events/events.json?action=delete&id=" + id)
      .done(onDone)
      .fail(onFail);
  };

  // Get the appropriate warning message, based on the presence of scores.
  if($("#event" + id + "_hasScores").length != 0)
  {
    warning = "<!--#str_events_delete_score_warning-->";
  }
  else
  {
    warning = "<!--#str_events_delete_warning-->";
  }

  // Show a confirmation to make sure this event should be deleted.
  showConfirmation($("#event" + id + "_date").html() + ": " +
                   $("#event" + id + "_name").html() + "<br><br>" + warning,
                   "<!--#str_button_delete-->", deleteEvent, null);
}

// Selects an event.
function
eventsSelect(id)
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
      // Remove the check mark from all the events.
      $("[id$=_select]")
        .removeClass("fa-check-square-o")
        .addClass("fa-square-o");

      // Add the check mark to the selected event.
      $("#event" + id + "_select")
        .removeClass("fa-square-o")
        .addClass("fa-check-square-o");

      // Update the status bar (since the selected event has changed).
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

  // Send a request to the server to select the event.
  $.getJSON("/admin/events/events.json?action=select&id=" + id)
    .done(onDone)
    .fail(onFail);
}

// Fetches the event list from the server.
function
eventsLoad()
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    var html = "";

    // See if there are any events.
    if(result["events"].length == 0)
    {
      // There are no events, so add an "empty event" to indicate that there
      // are none.
      html += `
    <div class="row">
      <div class="date">-</div>
      <div class="matches">-</div>
      <div class="name"><!--#str_events_none--></div>
      <div class="select">-</div>
      <div class="actions">-</div>
    </div>`;
    }

    // Loop through the events (a NOP if there are no events).
    for(var i = 0; i < result["events"].length; i++)
    {
      // Get the ID of this event.
      var id = result["events"][i]["id"];

      var _class = (result["events"][i]["selected"] === true) ?
                     "fa-check-square-o" : "fa-square-o";

      // Add the row for this event.
      html += `
    <div class="row">
      <div id="event${id}_date" class="date">
        ${result["events"][i]["date"]}
      </div>
      <div id="event${id}_matches" class="matches">
        ${result["events"][i]["matches"]}
      </div>
      <div id="event${id}_name" class="name">
        ${result["events"][i]["name"]}
      </div>
      <div class="select">
        <span id="event${id}_select" class="fa fa-fw ${_class}"
              onclick="eventsSelect(${id});" tabindex="0"></span>
      </div>
      <div class="actions">
        <span id="event${id}_edit" class="fa fa-fw fa-pencil"
              onclick="eventsEdit(${id});" tabindex="0"></span>
        <span id="event${id}_delete" class="fa fa-fw fa-trash"
              onclick="eventsDelete(${id});" tabindex="0"></span>`;
      if(result["events"][i]["hasScores"] === true)
      {
        html += `
        <span id="event${id}_hasScores" class="hidden"></span>`;
      }
      html += `
      </div>
    </div>`;
    }

    // Insert the set of rows into the body of the event list.
    $(".events_container .body").html(html);

    // Loop through all of the events.
    const suffix = [ "_select", "_edit", "_delete" ];
    for(var i = 0; i < result["events"].length; i++)
    {
      // Get the ID of this event.
      var id = result["events"][i]["id"];

      // Loop through the three buttons for this event.
      for(var j = 0; j < 3; j++)
      {
        // Add a keyup handler to this button.
        const tag = "#event" + id + suffix[j];
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

    // Refresh the search based on the new event list.
    eventsSearch();
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_events_load_failed-->", null);
  };

  // Send a request to the server to get the list of events.
  $.getJSON("/admin/events/events.json")
    .done(onDone)
    .fail(onFail);
}

// Searches for items in the event list.
function
eventsSearch()
{
  var idx, found;
  var events = $(".body");
  var filters = $("#events-search").val().toLowerCase().split(" ");

  // Loop over the list of events.
  events.children().each(function(index)
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
        // Only look at the first three columns.
        if(index < 3)
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
eventsKeydown(e)
{
  // See if Ctrl-A was pressed.
  if(((e.key == 'a') || (e.key == 'A')) && (e.ctrlKey == true) &&
     ($("dialog").length == 0))
  {
    // Add an event.
    eventsAdd();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog").length == 0))
  {
    // Move the focus to the search bar.
    $("#events-search").focus();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#events-search").is(":focus"))
  {
    // Clear the search bar.
    $("#events-search").val("");

    // Perform a "search" to get all the events displayed.
    eventsSearch();
  }
}

// Handles setup of the events tab.
function
eventsSetup()
{
  // Load the events from the server.
  eventsLoad();

  // Add event handlers for elements on the events tab.
  $("#events-search").on("keyup", eventsSearch);
  $("#events-add").on("click", eventsAdd);

  // Add a click handler for the add button.
  $("#events-add").on("keyup", eventsAddKeyUp);

  // Add a keydown event listener.
  document.addEventListener("keydown", eventsKeydown);
}

// Handles cleanup of the events tab.
function
eventsCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", eventsKeydown);
}