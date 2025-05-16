// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Edits the score for a team.
function
scoresEdit(id, match)
{
  // Open the referee scoring page for this team and match.
  window.open(window.location.origin + "/referee?team=" + id + "&match=" +
              match);
}

// Exchanges scores for a team.
function
scoresExchange(id, match)
{
  var new_id = -1;
  var new_match = -1;

  // Starts, or restarts, the score exchange process.
  function
  onStart()
  {
    // Get the details on the first score.
    var team = $("#score" + id + "_name").html().trim();
    var match_name = $(".match" + match).html().trim();
    var score = $("#score" + id + "_match" + match + "_score").html().trim();

    // Construct the HTML for the score exchange dialog.
    var html = "";
    html += `
<div class="scores_exchange_container">
  <div class="title">
    <span>
      <!--#str_scores_exchange-->
    </span>
  </div>
  <div class="team1">
    <span>
      ${team}
    </span>
  </div>
  <div class="match1">
    <span>
      ${match_name} : ${score}
    </span>
  </div>
  <div class="exchange">
    <span class="fa fa-exchange"></span>
  </div>
  <div class="team2">
    <select>`
    for(var i of $(".scores_container .row"))
    {
      var l_id = i.id.substring(5);
      var name = $(i).find("[id$=_name]").html().trim();
      var selected = (l_id == new_id) ? " selected" : "";
      html += `
      <option value="${l_id}"${selected}>${name}</option>`;
    }
    html += `
    </select>
  </div>
  <div class="match2">
    <select>`;
    for(var i = 1; i <= 4; i++)
    {
      if((i == 3) && $(".scores_container .heading").hasClass("two_matches"))
      {
        break;
      }
      if((i == 4) && $(".scores_container .heading").hasClass("three_matches"))
      {
        break;
      }
      var match_name = $(".match" + i).html().trim();
      var selected = (i == new_match) ? " selected" : "";
      html += `
      <option value="${i}"${selected}>${match_name}</option>`;
    }
    html += `
    </select>
  </div>
  <div class="buttons">
    <button id="scores_exchange_cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="scores_exchange_ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

    // Show the dialog as a popup.
    showPopup(html,
              {
                "scores_exchange_cancel": null,
                "scores_exchange_ok": exchangeScores
              });
  }

  // Called to exchange the scores once the details have been filled in.
  function
  exchangeScores()
  {
    // Get the values from the editor.
    new_id = $(".scores_exchange_container .team2 select").val();
    new_match = $(".scores_exchange_container .match2 select").val();

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
        // Reload the scores.
        scoresLoad();
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

    // Send a request to the server to exchange the scores.
    $.getJSON("/admin/scores/scores.json?action=exchange&id=" + id +
              "&match=" + match + "&id2=" + new_id + "&match2=" + new_match)
      .done(onDone)
      .fail(onFail);
  }

  // Start the score exchange process.
  onStart();
}

// Deletes a score.
function
scoresDelete(id, match)
{
  // Called once the delete has been confirmed.
  function
  deleteScore()
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
        // Reload the scores.
        scoresLoad();
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

    // Send a request to the server to delete the score.
    $.getJSON("/admin/scores/scores.json?action=delete&id=" + id + "&match=" +
              match)
      .done(onDone)
      .fail(onFail);
  }

  // Show a confirmation to make sure this event should be deleted.
  showConfirmation($("#score" + id + "_number").html().trim() + " : " +
                   $("#score" + id + "_name").html().trim() + " : " +
                   $(".match" + match).html().trim() +
                   " : " + $("#score" + id +
                   "_match" + match + "_score").html().trim() + "<br><br>" +
                   "<!--#str_scores_delete-->", "<!--#str_button_delete-->",
                   deleteScore, null);
}

// Fetches the score list from the server.
function
scoresLoad()
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    var html = "";

    // Set the number of matches in the score list based on the number of
    // matches at this event.
    if(result["matches"] == 2)
    {
      $(".scores_container .heading").addClass("two_matches");
      $(".scores_container .heading").removeClass("three_matches");
      $(".scores_container .body").addClass("two_matches");
      $(".scores_container .body").removeClass("three_matches");
    }
    else if(result["matches"] == 3)
    {
      $(".scores_container .heading").removeClass("two_matches");
      $(".scores_container .heading").addClass("three_matches");
      $(".scores_container .body").removeClass("two_matches");
      $(".scores_container .body").addClass("three_matches");
    }
    else
    {
      $(".scores_container .heading").removeClass("two_matches");
      $(".scores_container .heading").removeClass("three_matches");
      $(".scores_container .body").removeClass("two_matches");
      $(".scores_container .body").removeClass("three_matches");
    }

    // See if there are any scores.
    if(result["scores"].length == 0)
    {
      // There are no scores, so add an "empty score" to indicate that there
      // are none.
      html += `
    <div class="row">
      <span class="number">-</span>
      <span class="name"><!--#str_scores_none--></span>
      <span class="match1">-</span>
      <span class="match2">-</span>
      <span class="match3">-</span>
      <span class="match4">-</span>
    </div>`;
    }

    // Adds a score to the display.
    function
    addScore(idx, id, match)
    {
      // Get the score for this match.
      var score = result["scores"][idx]["match" + match];
      if((score !== "") && (score == Math.floor(score)))
      {
        score = parseInt(score);
      }

      // Add the HTML for this score column.
      html += `
      <div id="score${id}_match${match}" class="match${match}">
        <div class="match_score">
          <span id="score${id}_match${match}_score">${score}</span>
          <span class="match_action">
            <span id="score${id}_edit${match}" class="fa fa-pencil"
                  onclick="scoresEdit(${id}, ${match});" tabindex="0"></span>`;
      if(score !== "")
      {
        html += `
            <span id="score${id}_exchange${match}" class="fa fa-exchange"
                  onclick="scoresExchange(${id}, ${match});" tabindex="0">
            </span>
            <span id="score${id}_delete${match}" class="fa fa-trash"
                  onclick="scoresDelete(${id}, ${match});" tabindex="0">
            </span>`;
      }
      else
      {
        html += `
            <span class="fa fa-exchange disabled"></span>
            <span class="fa fa-trash disabled"></span>`;
      }
      html += `
          </span>
        </div>
      </div>`;
    }

    // Loop through the scores (a NOP if there are no scores).
    for(var i = 0; i < result["scores"].length; i++)
    {
      // Get the ID of this team.
      var id = result["scores"][i]["id"];

      // Add the row for this score.
      html += `
    <div id="score${id}" class="row">
      <span id="score${id}_number" class="number">
        ${result["scores"][i]["number"]}
      </span>
      <span id="score${id}_name" class="name">
        ${result["scores"][i]["name"]}
      </span>`;
      addScore(i, id, 1);
      addScore(i, id, 2);
      addScore(i, id, 3);
      addScore(i, id, 4);
      html += `
    </div>`;
    }

    // Insert the set of rows into the body of the score list.
    $(".scores_container .body").html(html);

    // Loop through all of the teams.
    const suffix = [ "_edit", "_exchange", "_delete" ];
    for(var i = 0; i < result["scores"].length; i++)
    {
      // Get the ID of this team.
      var id = result["scores"][i]["id"];

      // Loop through the three buttons for this team's scores.
      for(var j = 0; j < 3; j++)
      {
        // Loop through the four scores.
        for(var k = 0; k < 4; k++)
        {
          // Add a keyup handler to this button.
          const tag = "#score" + id + suffix[j] + (k + 1);
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
    }

    // Refresh the search based on the new score list.
    scoresSearch();
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_scores_load_failed-->", null);
  };

  // Send a request to the server to get the list of scores.
  $.getJSON("/admin/scores/scores.json")
    .done(onDone)
    .fail(onFail);
}

// Downloads the scores as a CSV.
function
scoresDownload()
{
  // Create a link element and click it, triggering the CSV download.
  Object.assign(document.createElement('a'),
                {
                  href: "/admin/scores/scores.csv",
                  download: "scores.csv"
                }).click();
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
  if((fields.length == 4) && ((fields[0] === "m1") || (fields[0] === "m2") ||
                              (fields[0] === "m3") || (fields[0] === "m4")))
  {
    // Get the score from the update.
    var score = fields[3];
    if((score !== "") && (score == Math.floor(score)))
    {
      score = parseInt(score);
    }

    // Update the score.
    $("#score" + fields[1] + "_match" + fields[0].substring(1, 2) + "_score").
      html(score);
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

// Searches for items in the score list.
function
scoresSearch()
{
  var idx, found;
  var scores = $(".scores_container .body");
  var filter = $("#scores-search").val().toLowerCase();
  var filters = filter.split(" ");

  // Loop over the list of scores.
  scores.children().each(function(index)
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
scoresKeydown(e)
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
    // Load the scores from the server.
    scoresLoad();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Ctrl-S was pressed.
  if(((e.key == 's') || (e.key == 'S')) && (e.ctrlKey == true) &&
     ($("dialog:visible").length == 0))
  {
    // Move the focus to the search bar.
    $("#scores-search").focus();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key == "Escape") && $("#scores-search").is(":focus"))
  {
    // Clear the search bar.
    $("#scores-search").val("");

    // Perform a "search" to get all the scores displayed.
    scoresSearch();

    // Do not allow this key event to further propagated.
    e.stopPropagation();
  }
}

// Handles setup of the scores tab.
function
scoresSetup()
{
  // Load the scores from the server.
  scoresLoad();

  // Add event handlers for elements on the scores tab.
  $("#scores-search").on("keyup", scoresSearch);
  $("#scores-refresh").on("click", scoresLoad);
  $("#scores-download").on("click", scoresDownload);

  // Add a keydown event listener.
  document.addEventListener("keydown", scoresKeydown);

  // Connect to the server via a WebSocket.
  wsConnect();
}

// Handles cleanup of the events tab.
function
scoresCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", scoresKeydown);
}