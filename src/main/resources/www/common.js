// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Extend JQuery by adding a showModal() method (mimic-ing the corresponding
// method in the standard DOM model).
$.fn.extend({showModal: function()
                        {
                          return this.each(function()
                                           {
                                             if(this.tagName=== "DIALOG")
                                             {
                                               this.showModal();
                                             }
                                           });
                        }
            });

// Extend JQuery by adding a close() method (mimic-ing the corresponding method
// in the standard DOM model).
$.fn.extend({close: function()
                    {
                      return this.each(function()
                                       {
                                         if(this.tagName=== "DIALOG")
                                         {
                                           this.close();
                                         }
                                       });
                    }
            });

// Displays a popup window.
function
showPopup(html, buttons)
{
  // Generate a unique ID for this popup, allowing for popups to have popups.
  const id = crypto.randomUUID();

  // Called when the popup is closed.
  function
  onClose()
  {
    // Delete the popup element from the DOM.
    $("#" + id).remove();

    // Remove the keydown listener for this popup.
    document.removeEventListener("keydown", onKeydown);
  }

  // Called when a button on the popup is clicked.
  function
  onClick(fn)
  {
    // Call the button handler, if there is one.
    if(fn !== null)
    {
      fn();
    }

    // Close the popup.
    onClose();
  }

  // Called when a key is pressed.
  function
  onKeydown(e)
  {
    // Close the popup if the escape key is pressed.
    if(e.key == "Escape")
    {
      onClose();
    }
  }

  // Generate the dialog for the popup and append it to the DOM.
  var html = "<dialog id=\"" + id + "\">" + html + "</dialog>";
  $("body").append(html);

  // Loop through all the buttons.
  for(const [ btn_id, fn ] of Object.entries(buttons))
  {
    // Find this button.
    var btn = $("#" + id + " #" + btn_id);

    // Add a click handler to this button.
    btn.on("click", () => { onClick(fn); });
  }

  // Add a keydown listener to the document to override the default Escape key
  // handling for a modal dialog (which simply closes it, instead of deleting
  // it like needed here).
  document.addEventListener("keydown", onKeydown);

  // Display the dialog as a modal.
  $("#" + id)[0].showModal();
}

// Shows a confirmation dialog.
function
showConfirmation(message, button, ok, cancel)
{
  // Construct the confirmation message.
  var html = `
<div class="warning_container">
  <div>
    <p>
      ${message}
    </p>
    <br>
    <button id="confirmation-cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="confirmation-ok" class="red">
      ${button}
    </button>
  </div>
</div>`;

  // Show the warning message as a popup.
  showPopup(html,
            {
              "confirmation-cancel": cancel,
              "confirmation-ok": ok
            });
}

// Shows a message popup.
function
showMessage(message)
{
  // Construct a message container.
  var html = `
<div class="warning_container">
  <div>
    <p>
      ${message}
    </p>
    <br>
    <button id="message-ok" class="green">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Show the error message as a popup.
  showPopup(html,
            {
              "message-ok": null
            });
}

// Shows a warning popup.
function
showError(message, fn)
{
  // Construct an error message.
  var html = `
<div class="warning_container">
  <div>
    <p>
      ${message}
    </p>
    <br>
    <button id="error-message-ok" class="red">
      <!--#str_button_ok-->
    </button>
  </div>
</div>`;

  // Show the error message as a popup.
  showPopup(html,
            {
              "error-message-ok": fn
            });
}

// Tracks if the click handlers have been added to the pop-up menu.
var popup_added = false;

// Shows the pop-up menu.
function
showMenu()
{
  // Get the dialog element.
  const dialog = $("#popup_menu");

  // Called when the menu hide animation completes.
  function
  hideMenuEnd()
  {
    // Remove the hide class (preparing the pop-up menu for the next time it is
    // displayed).
    dialog.removeClass("hide");

    // Close the modal dialog.
    dialog.close();

    // Remove the end of animation event listener.
    dialog.off("animationend", hideMenuEnd);

    // Remove the keydown event listener.
    $(document).off("keydown", showMenu.keyDownMenu);
  }

  // Hides the pop-up menu.
  function
  hideMenu()
  {
    // Add an end of animation event listener.
    dialog.on("animationend", hideMenuEnd);

    // Add the hide class to the dialog, starting the close animation.
    dialog.addClass("hide");
  }

  // Called when a key is pressed.
  function
  keyDownMenu(e)
  {
    // Close the pop-up menu if the escape key is pressed.
    if(e.key == "Escape")
    {
      // Start the animated close of the pop-up menu.
      hideMenu();

      // Prevent any further handling of this keystroke.
      e.preventDefault();
    }
  }

  // Handles starting the change password dialog.
  function
  passwd()
  {
    // Hide the menu.
    hideMenu();

    // Start the change password dialog.
    changePassword();
  }

  // See if the click handlers need to be added to the pop-up menu.
  if(!popup_added)
  {
    // Add the click handlers to the pop-up menu.
    dialog.find(".change_password button").on("click", passwd);
    dialog.find(".logout button").on("click", logout);
    dialog.find(".close span").on("click", hideMenu);

    // Remmber that the click handlers have been added.
    popup_added = true;
  }

  // Show the pop-up menu as a modal.
  dialog.showModal();

  // Add a keydown listener to the document to override the default Escape key
  // handling for a modal dialog (which simply closes it, instead of animating
  // the close like needed here).
  $(document).on("keydown", keyDownMenu);
}

// Shows a password change dialog.
function
changePassword()
{
  var pw = $(".old_input input").val();
  var new_pw = $(".new_input input").val();
  var verify_pw = $(".verify_input input").val();

  // Starts/re-starts the password chagne process.
  function
  onStart(error)
  {
    var container;

    // Select the container class based on the presence of an error message.
    if(error === "")
    {
      container = "password_container";
    }
    else
    {
      container = "password_error_container";
    }

    // Construct the password change dialog.
    var html =`
<div class="${container}">
  <div class="title">
    <span>
      <!--#str_password_change-->
    </span>
  </div>
  <div class="error">
    <span>
      ${error}
    </span>
  </div>
  <div class="old">
    <span>
      <!--#str_password_current-->
    </span>
  </div>
  <div class="old_input">
    <input id="old" type="password" placeholder="&#xf023;" />
  </div>
  <div class="new">
    <span>
      <!--#str_password_new-->
    </span>
  </div>
  <div class="new_input">
    <input id="new" type="text" placeholder="&#xf023;" />
    <span class="fa fa-eye-slash"
          onclick="$('.new_input input').toggleClass('show_pw');
                   $('.new_input span').toggleClass('fa-eye');
                   $('.new_input span').toggleClass('fa-eye-slash');">
    </span>
  </div>
  <div class="verify">
    <span>
      <!--#str_password_verify-->
    </span>
  </div>
  <div class="verify_input">
    <input id="verify" type="text" placeholder="&#xf023;" />
    <span class="fa fa-eye-slash"
          onclick="$('.verify_input input').toggleClass('show_pw');
                   $('.verify_input span').toggleClass('fa-eye');
                   $('.verify_input span').toggleClass('fa-eye-slash');">
    </span>
  </div>
  <div class="buttons">
    <button id="change_password_cancel" class="gray">
      <!--#str_button_cancel-->
    </button>
    <button id="change_password_change" class="green">
      <!--#str_button_change-->
    </button>
  </div>
</div>`;

    // Show the password change dialog.
    showPopup(html,
              {
                "change_password_cancel": null,
                "change_password_change": onSubmit
              });
  }

  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // See if the request was successful.
    if(result["result"] != "ok")
    {
      // Call the failure handler.
      onFail(result);
    }
    else
    {
      // Display a success message.
      showMessage("<!--#str_password_success-->");
    }
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Re-display the change password form with the error message.
    onStart(result["result"]);

    // Re-insert the values from the old form into the new form.
    $(".old_input input").val(pw);
    $(".new_input input").val(new_pw);
    $(".verify_input input").val(verify_pw);
  }

  // Called when the form is submitted.
  function
  onSubmit()
  {
    // Retrieve the values from the form.
    pw = $(".old_input input").val();
    new_pw = $(".new_input input").val();
    verify_pw = $(".verify_input input").val();

    // Send a request to the server to change the password.
    $.post("/password.json",
           {
             action: "change",
             old: pw,
             new: new_pw,
             verify: verify_pw
           })
      .done(onDone)
      .fail(onFail);
  }

  // Start the password change process.
  onStart("");
}

// Logs the user out of the system.
function
logout()
{
  // Redirect to the logout page.
  location.href = "/logout";
}