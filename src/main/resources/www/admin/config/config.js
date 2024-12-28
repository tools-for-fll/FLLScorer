// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Handles changes to the accent color.
function
configAccent(evt)
{
  // There is nothing to do if the current accent color was selected.
  if($(evt.target).hasClass("selected"))
  {
    return;
  }

  // Get the new accent color name.
  var color = "--color-" + $(evt.target).attr("id").substring(11);

  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // Display a failure if the result was not success.
    if(result["result"] !== "ok")
    {
      onFail(result);
      return;
    }

    // Update the accent color selection.
    $(".config_container button[id^=btn_accent_]").removeClass("selected");
    $(evt.target).addClass("selected");

    // Add the requested color to the style sheet.
    document.documentElement.style.setProperty("--accent-color",
                                               "var(" + color + ")");
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the accent color.
  $.getJSON("/admin/config/config.json?action=set&accent=" + color)
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the error color.
function
configError(evt)
{
  // There is nothing to do if the current error color was selected.
  if($(evt.target).hasClass("selected"))
  {
    return;
  }

  // Get the new error color name.
  var color = "--color-" + $(evt.target).attr("id").substring(10);

  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // Display a failure if the result was not success.
    if(result["result"] !== "ok")
    {
      onFail(result);
      return;
    }

    // Update the error color selection.
    $(".config_container button[id^=btn_error_]").removeClass("selected");
    $(evt.target).addClass("selected");

    // Add the requested color to the style sheet.
    document.documentElement.style.setProperty("--error-color",
                                               "var(" + color + ")");
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the error color.
  $.getJSON("/admin/config/config.json?action=set&error=" + color)
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the WiFi SSID.
function
configWiFiSSID(evt)
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // Display a failure if the result was not success.
    if(result["result"] !== "ok")
    {
      onFail(result);
      return;
    }
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the WiFi SSID.
  $.getJSON("/admin/config/config.json?action=set&wifi_ssid=" +
            $(evt.target).val())
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the WiFi password.
function
configWiFiPassword(evt)
{
  // Called when the query to the server has completed.
  function
  onDone(result)
  {
    // Display a failure if the result was not success.
    if(result["result"] !== "ok")
    {
      onFail(result);
      return;
    }
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the WiFi password.
  $.getJSON("/admin/config/config.json?action=set&wifi_password=" +
            $(evt.target).val())
    .done(onDone)
    .fail(onFail);
}

// Handles hiding and showing sections of the configuration screen.
function
configHideShow(evt)
{
  // Get the section to hide/show.
  var target = $(evt.target);

  // See if the section is currently being shown.
  if(target.hasClass("fa-chevron-down"))
  {
    // Swap the hide/show chevron.
    target.removeClass("fa-chevron-down").addClass("fa-chevron-right");

    // Hide the corresponding section.
    target.parent().next().hide();
  }

  // Otherwise, the section is currently hidden.
  else
  {
    // Swap the hide/show chevron.
    target.removeClass("fa-chevron-right").addClass("fa-chevron-down");

    // Show the corresponding section.
    target.parent().next().show();
  }
}

// Fetches the configuration from the server.
function
configLoad()
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

    // If the accent color is present, set it in the configuration screen.
    $(".config_container button[id^=btn_accent_]").removeClass("selected");
    if(result.hasOwnProperty("accent"))
    {
      $(".config_container #btn_accent_" + result["accent"].substring(8)).
        addClass("selected");
    }

    // If the error color is present, set it in the configuration screen.
    $(".config_container button[id^=btn_error_]").removeClass("selected");
    if(result.hasOwnProperty("error"))
    {
      $(".config_container #btn_error_" + result["error"].substring(8)).
        addClass("selected");
    }

    // If the WiFi SSID is present, set it in the configuration screen.
    if(result.hasOwnProperty("wifi_ssid"))
    {
      $(".config_container #wifi_ssid").val(result["wifi_ssid"]);
    }

    // If the WiFi password is present, set it in the configuration screen.
    if(result.hasOwnProperty("wifi_password"))
    {
      $(".config_container #wifi_password").val(result["wifi_password"]);
    }
  }

  // Called when the query to the server fails.
  function
  onFail()
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  };

  // Send a request to the server to get the accent color.
  $.getJSON("/admin/config/config.json?action=get")
    .done(onDone)
    .fail(onFail);
}

// Called when a keyup event occurs with a config element selected.
function
configKeyUp(event)
{
  // See if the enter key was pressed.
  if(event.key === "Enter")
  {
    // Translate this into a click.
    $(event.target).click();

    // Prevent the default handling of this event.
    event.preventDefault();
  }
}

// Handles keydown events.
function
configKeydown(e)
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
}

// Handles setup of the config tab.
function
configSetup()
{
  // Load the config from the server.
  configLoad();

  // Add the click and keydown handlers for the section headers.
  $(".section .fa-chevron-down").on("click", configHideShow);
  $(".section .fa-chevron-down").on("keyup", configKeyUp);

  // Add the key up handler for the color buttons.
  $(".config_container button[id^=btn_accent_]").on("click", configAccent);
  $(".config_container button[id^=btn_accent_]").on("keyup", configKeyUp);
  $(".config_container button[id^=btn_error_]").on("click", configError);
  $(".config_container button[id^=btn_error_]").on("keyup", configKeyUp);

  // Add the change handlers for the WiFi credential inputs.
  $(".config_container #wifi_ssid").on("change", configWiFiSSID);
  $(".config_container #wifi_password").on("change", configWiFiPassword);

  // Add a keydown event listener.
  document.addEventListener("keydown", configKeydown);
}

// Handles cleanup of the config tab.
function
configCleanup()
{
  // Remove the keydown event listener.
  document.removeEventListener("keydown", configKeydown);
}