// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Generates the generic HTML for a user editor, supporting both adding new
// users and editing existing users.
function
usersEditorHtml(title, name, password)
{
  // Change the name and password inputs to an appropriate HTML input value
  // specifier.
  if(name !== "")
  {
    name = "value=\"" + name + "\" ";
  }
  if(password !== "")
  {
    password = "value=\"" + password + "\" ";
  }

  // Construct the HTML for the editor.
  var html = `
<div class="users_editor_container">
  <div class="title">
    ${title}
  </div>
  <div class="name">
    <div class="label">
      <!--#str_users_list_name-->:
    </div>
    <div class="input">
      <input id="users-editor-name" type="text" ${name}
             placeholder="&#xf11c;" >
    </div>
  </div>
  <div class="password">
    <div class="label">
      <!--#str_users_password-->:
    </div>
    <div class="input">
      <input id="users-editor-password" type="password" ${password}
             placeholder="&#xf023;" >
    </div>
  </div>
  <div class="buttons">
    <button id="users-editor-cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="users-editor-ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Return the HTML.
  return(html);
}

// Adds a user.
function
usersAdd()
{
  var name = "";
  var password = "";

  // Starts, or restarts, the user add process.
  function
  onStart()
  {
    // Construct an editor for adding a user.
    var html = usersEditorHtml("<!--#str_users_add-->", name, password);

    // Show the editor as a popup.
    showPopup(html,
              {
                "users-editor-cancel": null,
                "users-editor-ok": addUser
              });
  }

  // Called to add the user once the details have been filled in.
  function
  addUser()
  {
    // Get the values from the editor.
    name = $("#users-editor-name").val();
    password = $("#users-editor-password").val();

    // Encode the values for safe transmission to the server.
    var name_enc = encodeURIComponent(name);
    var password_enc = encodeURIComponent(password);

    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] !== "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Reload the users.
        usersLoad();
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

    // Fail if the name is invalid.
    if(name === "")
    {
      onFail("<!--#str_users_invalid_name-->");
    }

    // Fail if the password is invalid.
    else if(password === "")
    {
      onFail("<!--#str_users_invalid_password-->");
    }

    // Otherwise, proceed.
    else
    {
      // Send a request to the server to create the user.
      $.getJSON("/admin/users/users.json?action=add&name=" + name_enc +
                "&password=" + password_enc)
        .done(onDone)
        .fail(onFail);
    }
  };

  // Start the user add process.
  onStart();
}

// Edits an existing user.
function
usersEdit(id)
{
  var oldName = $("#user" + id + "_name").html().trim();
  var name = oldName;
  var password = "";

  // Starts, or restarts, the user edit process.
  function
  onStart()
  {
    // Construct an editor for editing a user.
    var html = usersEditorHtml("<!--#str_users_edit-->", name, password);

    // Show the editor as a popup.
    showPopup(html,
              {
                "users-editor-cancel": null,
                "users-editor-ok": editUser
              });
  }

  // Called to edit the user once the details have been filled in.
  function
  editUser()
  {
    // Get the values from the editor.
    name = $("#users-editor-name").val();
    password = $("#users-editor-password").val();

    // Encode the values for safe transmission to the server.
    var name_enc = encodeURIComponent(name);
    var password_enc = encodeURIComponent(password);

    // Called when the query to change the user's name has completed.
    function
    onDoneName(result)
    {
      // See if the request was successful.
      if(result["result"] !== "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }

      // Otherwise, see if the password needs to be changed.
      else if(password !== "")
      {
        // Send a request to the server to change the user's password.
        $.post("/admin/users/users.json",
               { action: "changePassword", id: id, password: password_enc })
          .done(onDonePassword)
          .fail(onFail);
      }

      // Otherwise, reload the user list.
      else
      {
        usersLoad();
      }
    }

    // Called when the query to change the user's password has completed.
    function
    onDonePassword(result)
    {
      // See if the request was successful.
      if(result["result"] !== "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }

      // Otherwise, reload the user list if the user's name was changed.
      else if(name !== oldName)
      {
        usersLoad();
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

    // Fail if the name is invalid.
    if(name === "")
    {
      onFail("<!--#str_users_invalid_name-->");
    }

    // See if the user's name was changed.
    else if(name !== oldName)
    {
      // See if there is a new password specified.
      if(password === "")
      {
        // It is an error to change just the username.
        onFail("<!--#str_users_edit_error-->");
      }
      else
      {
        // Send a request to the server to change the user's name, which is
        // followed by a separate request to change their password.
        $.getJSON("/admin/users/users.json?action=changeName&id=" + id +
                  "&name=" + name_enc)
          .done(onDoneName)
          .fail(onFail);
      }
    }

    // See if the user's password was reset.
    else if(password !== "")
    {
      // Send a request to the server to change the user's password.
      $.post("/admin/users/users.json",
             { action: "changePassword", id: id, password: password_enc })
        .done(onDonePassword)
        .fail(onFail);
    }
  }

  // Start the team edit process.
  onStart();
}

// Deletes a user.
function
usersDelete(id)
{
  var html, warning;

  // Called once the delete has been confirmed.
  function
  deleteUser()
  {
    // Called when the query to the server has completed.
    function
    onDone(result)
    {
      // See if the request was successful.
      if(result["result"] !== "ok")
      {
        // It was not, so call the failure function.
        onFail(result["result"]);
      }
      else
      {
        // Reload the users.
        usersLoad();
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

    // Send a request to the server to delete the user.
    $.getJSON("/admin/users/users.json?action=delete&id=" + id)
      .done(onDone)
      .fail(onFail);
  };

  // Show a confirmation to make sure this user should be deleted.
  showConfirmation($("#user" + id + "_name").html() +
                   "<br><br><!--#str_users_delete_warning-->",
                   "<!--#str_button_delete-->", deleteUser, null);
}

// Selects a user role.
function
usersSelect(id, role)
{
  // Determine if the user currently has this role.
  var enabled = $("#user" + id + "_" + role).hasClass("fa-check-square-o");

  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // See if the request was successful.
    if(result["result"] !== "ok")
    {
      // It was not, so call the failure function.
      onFail(result["result"]);
    }
    else
    {
      // Toggle the check mark on this user role.
      if(enabled)
      {
        $("#user" + id + "_" + role)
          .removeClass("fa-check-square-o")
          .addClass("fa-square-o");
      }
      else
      {
        $("#user" + id + "_" + role)
          .removeClass("fa-square-o")
          .addClass("fa-check-square-o");
      }
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

  // Send a request to the server to add or remove the role from the user.
  if(enabled)
  {
    $.getJSON("/admin/users/users.json?action=removeRole&id=" + id + "&role=" +
              role)
      .done(onDone)
      .fail(onFail);
  }
  else
  {
    $.getJSON("/admin/users/users.json?action=addRole&id=" + id + "&role=" +
              role)
      .done(onDone)
      .fail(onFail);
  }
}

// Fetches the user list from the server.
function
usersLoad()
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // Display a failure if the result was not success.
    if(result["result"] !== "ok")
    {
      onFail();
      return;
    }

    // Start building an HTML fragment to hold the list of users.
    var html = "";

    // See if there are any users.
    if(result["users"].length === 0)
    {
      // There are no users, so add an "empty user" to indicate that there are
      // none.
      html += `
    <div class="row">
      <div class="name"><!--#str_users_none--></div>
      <div class="admin">-</div>
      <div class="host">-</div>
      <div class="judge">-</div>
      <div class="referee">-</div>
      <div class="timekeeper">-</div>
      <div class="actions">-</div>
    </div>`;
    }

    // Loop through the users (a NOP if there are no users).
    for(var i = 0; i < result["users"].length; i++)
    {
      // Get the ID and roles of this user.
      var id = result["users"][i]["id"];
      var admin =
        result["users"][i]["admin"] ? "fa-check-square-o" : "fa-square-o";
      var host =
        result["users"][i]["host"] ? "fa-check-square-o" : "fa-square-o";
      var judge =
        result["users"][i]["judge"] ? "fa-check-square-o" : "fa-square-o";
      var referee =
        result["users"][i]["referee"] ? "fa-check-square-o" : "fa-square-o";
      var timekeeper =
        result["users"][i]["timekeeper"] ? "fa-check-square-o" : "fa-square-o";

      // Add the row for this user.
      html += `
    <div id="user${id}" class="row">
      <div id="user${id}_name" class="name">
        ${result["users"][i]["name"]}
      </div>
      <div class="admin">
        <span id="user${id}_admin" class="fa fa-fw ${admin}"
              onclick="usersSelect(${id}, 'admin');" tabindex="0"></span>
      </div>
      <div class="host">
        <span id="user${id}_host" class="fa fa-fw ${host}"
              onclick="usersSelect(${id}, 'host');" tabindex="0"></span>
      </div>
      <div class="judge">
        <span id="user${id}_judge" class="fa fa-fw ${judge}"
              onclick="usersSelect(${id}, 'judge');" tabindex="0"></span>
      </div>
      <div class="referee">
        <span id="user${id}_referee" class="fa fa-fw ${referee}"
              onclick="usersSelect(${id}, 'referee');" tabindex="0"></span>
      </div>
      <div class="timekeeper">
        <span id="user${id}_timekeeper" class="fa fa-fw ${timekeeper}"
              onclick="usersSelect(${id}, 'timekeeper');" tabindex="0"></span>
      </div>
      <div class="actions">
        <span id="user${id}_edit" class="fa fa-fw fa-pencil"
              onclick="usersEdit(${id});" tabindex="0"></span>
        <span id="user${id}_delete"  class="fa fa-fw fa-trash"
              onclick="usersDelete(${id});" tabindex="0"></span>
      </div>
    </div>`;
    }

    // Insert the set of rows into the body of the user list.
    $(".users_container .body").html(html);

    // Loop through all of the users.
    const suffix = [ "_admin", "_host", "_judge", "_referee", "_timekeeper",
                     "_edit", "_delete" ];
    for(var i = 0; i < result["users"].length; i++)
    {
      // Get the ID of this user.
      var id = result["users"][i]["id"];

      // Loop through the seven buttons for this user.
      for(var j = 0; j < 7; j++)
      {
        // Add a keyup handler to this button.
        const tag = "#user" + id + suffix[j];
        $(tag).on("keyup", (event) =>
                           {
                             // See if the enter key was pressed.
                             if(event.key === "Enter")
                             {
                               // "Convert" the key press to a mouse click.
                               $(tag).click();

                               // Prevent the normal handling of this key.
                               event.preventDefault();
                             }
                           });
      }
    }

    // Refresh the search based on the new user list.
    usersSearch();
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_users_load_failed-->", null);
  };

  // Send a request to the server to get the list of users.
  $.getJSON("/admin/users/users.json")
    .done(onDone)
    .fail(onFail);
}

// Searches for items in the user list.
function
usersSearch()
{
  var idx, found;
  var users = $(".users_container .body");
  var filter = $("#users-search").val().toLowerCase();
  var filters = filter.split(" ");

  // Loop over the list of users.
  users.children().each(function(index)
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
        // Only look at the first column.
        if(index < 1)
        {
          // See if this filter item exists in this cell.
          if($(this).html().toLowerCase().indexOf(filters[idx]) !== -1)
          {
            // The filter item was found.
            found = true;
          }
        }
      });

      // Stop looking if this filter item was not found.
      if(found === false)
      {
        break;
      }
    }

    // Show this row if all the filter items were found.
    if(idx === filters.length)
    {
      row.show();
    }
  });
}

// Called when a keyup event occurs with the users add element selected.
function
usersAddKeyUp(event)
{
  // See if the enter key was pressed.
  if(event.key === "Enter")
  {
    // Translate this into a click.
    $("#users-add").click();

    // Prevent the default handling of this event.
    event.preventDefault();
  }
}

// Handles keydown events.
function
usersKeydown(e)
{
  // See if Ctrl-A was pressed.
  if(((e.key === 'a') || (e.key === 'A')) && (e.ctrlKey === true) &&
     ($("dialog:visible").length === 0))
  {
    // Add a user.
    usersAdd();
  }

  // See if Ctrl-S was pressed.
  if(((e.key === 's') || (e.key === 'S')) && (e.ctrlKey === true) &&
     ($("dialog:visible").length === 0))
  {
    // Move the focus to the search bar.
    $("#users-search").focus();
  }

  // See if Escape was pressed while the search bar is active.
  if((e.key === "Escape") && $("#users-search").is(":focus"))
  {
    // Clear the search bar.
    $("#users-search").val("");

    // Perform a "search" to get all the users displayed.
    usersSearch();
  }
}

// Handles setup of the users tab.
function
usersSetup()
{
  // Load the users from the server.
  usersLoad();

  // Add event handlers for elements on the users tab.
  $("#users-search").on("keyup", usersSearch);
  $("#users-add").on("click", usersAdd);

  // Add a click handler for the add button.
  $("#users-add").on("keyup", usersAddKeyUp);

  // Add a keydown event listener.
  document.addEventListener("keydown", usersKeydown);
}

// Handles cleanup of the users tab.
function
usersCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", usersKeydown);
}