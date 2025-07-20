// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// The number of divisions that are currently selected.
var configDivisions = 2;

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
  const name = $(evt.target).attr("id").substring(11);
  const color = "--color-" + name;

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

    // Change the favicon to match the new accent color.
    $("link[rel*='icon']").attr("href", "/favicon_" + name + ".ico");
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

// Handles changes to the division color.
function
configDivisionColor(evt)
{
  // There is nothing to do if the current division color was selected.
  if($(evt.target).hasClass("selected"))
  {
    return;
  }

  // Get the division.
  const division = parseInt($(evt.target).attr("id").substring(12));

  // Get the new division color name.
  const color = "--color-" + $(evt.target).attr("id").substring(14);

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
    $(".config_container button[id^=btn_divcolor" + division + "_]").
      removeClass("selected");
    $(evt.target).addClass("selected");
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the accent color.
  $.getJSON("/admin/config/config.json?action=set&division" + division +
            "_color=" + color)
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the division count.
function
configDivisionCount(evt)
{
  // There is nothing to do if the current division count was selected.
  if($(evt.target).hasClass("selected"))
  {
    return;
  }

  // Get the new division count.
  const count = $(evt.target).attr("id").substring(14);

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

    // Update the division count selection.
    $(".config_container button[id^=btn_divisions_]").removeClass("selected");
    $(evt.target).addClass("selected");
    configDivisions = parseInt($(evt.target).attr("id").substring(14));

    // Show the divisions, so that the list of division names gets updated.
    configDivisionShow();

    // Update the status bar.
    updateStatus();
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the division count.
  $.getJSON("/admin/config/config.json?action=set&division_count=" + count)
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the division enable.
function
configDivisionEnable()
{
  const target = $("#btn_divisions").find(".fa-check");

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

    // Toggle the visibility of the division enable and the disivion count.
    if(target.is(":visible"))
    {
      target.hide();
      configDivisionHide();
    }
    else
    {
      target.show();
      configDivisionShow();
    }

    // Update the status bar.
    updateStatus();
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the timer enable.
  $.getJSON("/admin/config/config.json?action=set&division_enable=" +
            (target.is(":visible") ? "0" : "1"))
    .done(onDone)
    .fail(onFail);
}

// Hides the division configuration.
function
configDivisionHide()
{
  $("#row_division_count").hide();
  $("tr[id^=row_division_name]").hide();
}

// Handles changes to a division name.
function
configDivisionName(evt)
{
  const target = $(evt.target);
  const idx = parseInt(target.attr("id").substring(7));

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

  $.getJSON("/admin/config/config.json?action=set&division" + idx + "_name=" +
            encodeURIComponent(target.val()))
    .done(onDone)
    .fail(onFail);
}

// Shows the division configuration.
function
configDivisionShow()
{
  $("#row_division_count").show();
  $("#row_division_name1").show();
  $("#row_division_name2").show();
  if(configDivisions > 2)
  {
    $("#row_division_name3").show();
  }
  else
  {
    $("#row_division_name3").hide();
  }
  if(configDivisions > 3)
  {
    $("#row_division_name4").show();
  }
  else
  {
    $("#row_division_name4").hide();
  }
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
  const color = "--color-" + $(evt.target).attr("id").substring(10);

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

// Handles changes to the timer enable.
function
configTimerEnable()
{
  const target = $("#btn_timer").find(".fa-check");

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

    // Toggle the visibility of the timer enable and the timer location.
    if(target.is(":visible"))
    {
      target.hide();
      $("#row_timer_location").hide();
    }
    else
    {
      target.show();
      $("#row_timer_location").show();
    }
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the timer enable.
  $.getJSON("/admin/config/config.json?action=set&timer_enable=" +
            (target.is(":visible") ? "0" : "1"))
    .done(onDone)
    .fail(onFail);
}

// Handles changes to the timer location.
function
configTimerLocation(evt)
{
  const choices = $("button[id^=btn_timer_]");
  const target = $(evt.currentTarget);

  // There is nothing to do if the currently selected location was clicked.
  if(target.hasClass("selected"))
  {
    return;
  }

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

    // Change the radio select to the selected location.
    choices.removeClass("selected");
    target.addClass("selected");
  }

  // Called when the query to the server fails.
  function
  onFail(result)
  {
    // Display an error message.
    showError("<!--#str_config_load_failed-->", null);
  }

  // Send a request to the server to change the timer location.
  $.getJSON("/admin/config/config.json?action=set&timer_location=" +
            target.attr("id").substring(10))
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
            encodeURIComponent($(evt.target).val()))
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
            encodeURIComponent($(evt.target).val()))
    .done(onDone)
    .fail(onFail);
}

// Handles hiding and showing sections of the configuration screen.
function
configHideShow(evt)
{
  // Get the section to hide/show.
  const target = $(evt.target);

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

    // If the division count is present, set it in the configuration screen.
    $(".config_container button[id^=btn_divisions_]").removeClass("selected");
    if(result.hasOwnProperty("division_count"))
    {
      $(".config_container #btn_divisions_" + result["division_count"]).
        addClass("selected");
      configDivisions = result["division_count"];
    }
    else
    {
      $(".config_container #btn_divisions_2").addClass("selected");
      configDivisions = 2;
    }

    // If the division enable is present, set it in the configuration screen.
    if(result.hasOwnProperty("division_enable") &&
       (result["division_enable"] == true))
    {
      $(".config_container #btn_divisions .fa-check").show();
      configDivisionShow();
    }
    else
    {
      $(".config_container #btn_divisions .fa-check").hide();
      configDivisionHide();
    }

    // If the division 1 color is provided, set it in the configuration screen.
    $(".config_container button[id^=btn_divcolor1_]").removeClass("selected");
    if(result.hasOwnProperty("division1_color"))
    {
      $(".config_container #btn_divcolor1_" +
        result["division1_color"].substring(8)).addClass("selected");
    }

    // If the division 1 name is provided, set it in the configuration screen.
    if(result.hasOwnProperty("division1_name"))
    {
      $(".config_container #divname1").val(result["division1_name"]);
    }
    else
    {
      $(".config_container #divname1").
        val("<!--#str_config_division_name1_default-->");
    }

    // If the division 2 color is provided, set it in the configuration screen.
    $(".config_container button[id^=btn_divcolor2_]").removeClass("selected");
    if(result.hasOwnProperty("division2_color"))
    {
      $(".config_container #btn_divcolor2_" +
        result["division2_color"].substring(8)).addClass("selected");
    }

    // If the division 2 name is provided, set it in the configuration screen.
    if(result.hasOwnProperty("division2_name"))
    {
      $(".config_container #divname2").val(result["division2_name"]);
    }
    else
    {
      $(".config_container #divname2").
        val("<!--#str_config_division_name2_default-->");
    }

    // If the division 3 color is provided, set it in the configuration screen.
    $(".config_container button[id^=btn_divcolor3_]").removeClass("selected");
    if(result.hasOwnProperty("division3_color"))
    {
      $(".config_container #btn_divcolor3_" +
        result["division3_color"].substring(8)).addClass("selected");
    }

    // If the division 3 name is provided, set it in the configuration screen.
    if(result.hasOwnProperty("division3_name"))
    {
      $(".config_container #divname3").val(result["division3_name"]);
    }
    else
    {
      $(".config_container #divname3").
        val("<!--#str_config_division_name3_default-->");
    }

    // If the division 4 color is provided, set it in the configuration screen.
    $(".config_container button[id^=btn_divcolor4_]").removeClass("selected");
    if(result.hasOwnProperty("division4_color"))
    {
      $(".config_container #btn_divcolor4_" +
        result["division4_color"].substring(8)).addClass("selected");
    }

    // If the division 4 name is provided, set it in the configuration screen.
    if(result.hasOwnProperty("division4_name"))
    {
      $(".config_container #divname4").val(result["division4_name"]);
    }
    else
    {
      $(".config_container #divname4").
        val("<!--#str_config_division_name4_default-->");
    }

    // If the error color is present, set it in the configuration screen.
    $(".config_container button[id^=btn_error_]").removeClass("selected");
    if(result.hasOwnProperty("error"))
    {
      $(".config_container #btn_error_" + result["error"].substring(8)).
        addClass("selected");
    }

    // If the timer enable is present, set it in the configuration screen.
    if(result.hasOwnProperty("timer_enable") &&
       (result["timer_enable"] == true))
    {
      $(".config_container #btn_timer .fa-check").show();
      $(".config_container #row_timer_location").show();
    }
    else
    {
      $(".config_container #btn_timer .fa-check").hide();
      $(".config_container #row_timer_location").hide();
    }

    // If the timer location is present, set it in the configuration screen.
    $(".config_container button[id^=btn_timer_]").removeClass("selected");
    if(result.hasOwnProperty("timer_location") &&
       (result["timer_location"] === "center"))
    {
      $(".config_container #btn_timer_center").addClass("selected");
    }
    else
    {
      $(".config_container #btn_timer_top").addClass("selected");
    }

    // If the WiFi password is present, set it in the configuration screen.
    if(result.hasOwnProperty("wifi_password"))
    {
      $(".config_container #wifi_password").val(result["wifi_password"]);
    }

    // If the WiFi SSID is present, set it in the configuration screen.
    if(result.hasOwnProperty("wifi_ssid"))
    {
      $(".config_container #wifi_ssid").val(result["wifi_ssid"]);
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
  $(".section .fa-chevron-down").on("click", configHideShow).
    on("keyup", configKeyUp);

  // Add the click and key up handler for the color buttons.
  $(".config_container button[id^=btn_accent_]").on("click", configAccent).
    on("keyup", configKeyUp);
  $(".config_container button[id^=btn_error_]").on("click", configError).
    on("keyup", configKeyUp);

  // Add the click and key up handlers for the division buttons.
  $(".config_container #btn_divisions").on("click", configDivisionEnable).
    on("keyup", configKeyUp);
  $(".config_container button[id^=btn_divisions_]").
    on("click", configDivisionCount).on("keyup", configKeyUp);
  $(".config_container input[id^=divname]").on("change", configDivisionName);
  $(".config_container button[id^=btn_divcolor").
    on("click", configDivisionColor).on("keyup", configKeyUp);

  // Add the click and key up handlers for the timer buttons.
  $(".config_container #btn_timer").on("click", configTimerEnable).
    on("keyup", configKeyUp);
  $(".config_container #btn_timer_top").on("click", configTimerLocation).
    on("keyup", configKeyUp);
  $(".config_container #btn_timer_center").on("click", configTimerLocation).
    on("keyup", configKeyUp);

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