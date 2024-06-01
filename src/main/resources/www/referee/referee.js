// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

function
showMatch(team, match)
{
  console.log("Team: " + team + "  Match: " + match);
}

function
loadScores()
{
  function
  done(result)
  {
    if(result["matches"] == 3)
    {
      $(".body").addClass("three_matches");
    }
    else
    {
      $(".body").removeClass("three_matches");
    }

    var html = "";

    for(var i = 0; i < result["scores"].length; i++)
    {
      var id = result["scores"][i]["id"];

      var m1 = result["scores"][i]["match1"];
      var m2 = result["scores"][i]["match2"];
      var m3 = result["scores"][i]["match3"];
      var m4 = result["scores"][i]["match4"];

      m1 = (m1 == 1) ? "red" : ((m1 == 2) ? "green" : "yellow");
      m2 = (m2 == 1) ? "red" : ((m2 == 2) ? "green" : "yellow");
      m3 = (m3 == 1) ? "red" : ((m3 == 2) ? "green" : "yellow");
      m4 = (m4 == 1) ? "red" : ((m4 == 2) ? "green" : "yellow");

      html += `
        <div class="row">
          <div class="name">
            ${result["scores"][i]["number"]} : ${result["scores"][i]["name"]}
          </div>
          <div class="match1">
            <button class="${m1}" onclick="showMatch(${id}, 1);"></button>
          </div>
          <div class="match2">
            <button class="${m2}" onclick="showMatch(${id}, 2);"></button>
          </div>
          <div class="match3">
            <button class="${m3}" onclick="showMatch(${id}, 3);"></button>
          </div>
          <div class="match4">
            <button class="${m4}" onclick="showMatch(${id}, 4);"></button>
          </div>
        </div>`;
    }

    $(".list_container .body").html(html);
  }

  function
  fail(result)
  {
    console.log("error");
    console.log(result);
  }

  $.getJSON("/referee/referee.json?action=list")
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
  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog").length == 0))
  {
    // Move the focus to the search bar.
    $("#search").focus();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#search").is(":focus"))
  {
    // Clear the search bar.
    $("#search").val("");

    // Perform a "search" to get all the teams displayed.
    search();
  }
}

// Handles setup of the referee page.
function
ready()
{
  loadScores();

  // Add event handlers for elements on the referee page.
  $("#search").on("keyup", search);

  // Add a keydown event listener.
  document.addEventListener("keydown", keydown);
}

// Set the function to call when the page is ready.
$(document).ready(ready);