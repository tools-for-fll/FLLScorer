// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

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
    var btn = $("#" + btn_id);

    // Add a click handler to this button.
    btn.on("click", () => { onClick(fn); });
  }

  // Add a keydown listener to the document.
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