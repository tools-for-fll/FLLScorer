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

    // Get the locale from the response.
    var locale = result["locale"];

    // Get the team's rubric choices, if they exist.
    var choices = result.hasOwnProperty("choices") ? result["choices"] : null;

    // Construct the HTML for the rubric starting with an empty string.
    var html = "";

    // Get the judging areas from the rubric.
    var areas = result["rubric"]["areas"];

    // Generate the header, listing the judging areas.
    html += `<div class="area">`;

    // Loop through the areas.
    for(var i = 0; i < areas.length; i++)
    {
      // Get the JSON object for this area.
      var area = areas[i];

      // Get the long name for this area.
      var area_name;
      if(area["name"].hasOwnProperty(locale))
      {
        area_name = area["name"][locale];
      }
      else
      {
        area_name = area["name"]["en_US"];
      }

      // Get the short name for this area.
      var area_name_short;
      if(area["short_name"].hasOwnProperty(locale))
      {
        area_name_short = area["short_name"][locale];
      }
      else
      {
        area_name_short = area["short_name"]["en_US"];
      }

      // Generate the header item for this judging area.
      html += `<div class="area_name" tabindex="0">`;
      html += `<span class="name">${area_name}</span>`;
      html += `<span class="short_name">${area_name_short}</span>`;
      html += `</div>`;
    }

    // Close the header div.
    html += `</div>`;

    // Loop through the areas.
    for(var i = 0; i < areas.length; i++)
    {
      // Create a div for this judging area.
      html += `<div class="items" style="display: none;">`;

      // Get the JSON object for sections in this area.
      var sections = areas[i]["sections"];

      // Loop through the sections in this area.
      for(var j = 0; j < sections.length; j++)
      {
        // Get the JSON object for this section.
        var section = sections[j];

        // Get the name for this section.
        var name;
        if(section["name"].hasOwnProperty(locale))
        {
          name = section["name"][locale];
        }
        else
        {
          name = section["name"]["en_US"];
        }

        // Get the description for this section.
        var desc;
        if(section["description"].hasOwnProperty(locale))
        {
          desc = section["description"][locale];
        }
        else
        {
          desc = section["description"]["en_US"];
        }

        // Create the header for this section.
        html += `<div class="section">`;
        html += `<span class="name">${name}</span>`;
        html += `<hr>`;
        html += `<span class="description">${desc}</span>`;

        // Get the items for this section.
        var items = section["items"];

        // Loop through the items.
        for(var k = 0; k < items.length; k++)
        {
          // Get the JSON object for this item.
          var item = items[k];

          // If this is a Core Values item, set the Core Values class to add
          // to the buttons.
          var core = item.hasOwnProperty("isCoreValues") ? " core" : "";

          // Construct the ID for this item.
          var item_id = `R${i}_${j}_${k}`;

          // Determine if this item has a selection in this team's rubric.
          var selected = -1;
          if((choices != null) && choices.hasOwnProperty(item_id))
          {
            selected = choices[item_id];
          }

          // Create a div for this item.
          html += `<hr>`;
          html += `<div id=${item_id}" class="select">`;

          // Add the first select for this item.
          if(item["1"].hasOwnProperty(locale))
          {
            desc = item["1"][locale];
          }
          else
          {
            desc = item["1"]["en_US"];
          }
          html += `<button onclick="itemToggle('#${item_id}', this);" ` +
                  `class="sel1${core}${(selected == 0) ? " selected" : ""}">` +
                  `<span>1</span></button>`;
          html += `<span class="desc1">${desc}</span>`;

          // Add the second select for this item.
          if(item["2"].hasOwnProperty(locale))
          {
            desc = item["2"][locale];
          }
          else
          {
            desc = item["2"]["en_US"];
          }
          html += `<button onclick="itemToggle('#${item_id}', this);" ` +
                  `class="sel2${core}${(selected == 1) ? " selected": ""}">` +
                  `<span>2</span></button>`;
          html += `<span class="desc2">${desc}</span>`;

          // Add the third select for this item.
          if(item["3"].hasOwnProperty(locale))
          {
            desc = item["3"][locale];
          }
          else
          {
            desc = item["3"]["en_US"];
          }
          html += `<button onclick="itemToggle('#${item_id}', this);" ` +
                  `class="sel3${core}${(selected == 2) ? " selected" : ""}">` +
                  `<span>3</span></button>`;
          html += `<span class="desc3">${desc}</span>`;

          // Add the fourth select for this item.
          if(item["4"].hasOwnProperty(locale))
          {
            desc = item["4"][locale];
          }
          else
          {
            desc = item["4"]["en_US"];
          }
          html += `<button onclick="itemToggle('#${item_id}', this);" ` +
                  `class="sel4${core}${(selected == 3) ? " selected" : ""}">` +
                  `<span>4</span></button>`;
          html += `<span class="desc4">${desc}</span>`;

          // Close the div for this select.
          html += `</div>`;
        }

        // Close the div for this section.
        html += `</div>`;
      }

      // Close the div for this juging area.
      html += `</div>`;
    }

    // Replace the rubric container body with the HTML rubric.
    $(".rubric_container .body").html(html);

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
    if(areas.length != 3)
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
      areas.eq(i).on("keyup", (event) =>
                              {
                                // See if the space or enter key was pressed.
                                if((event.key == " ") ||
                                   (event.key == "Enter"))
                                {
                                  // "Convert" the key press to a mouse click.
                                  areas.eq(i).click();

                                  // Prevent the normal handling of this key.
                                  event.preventDefault();
                                }
                              });
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