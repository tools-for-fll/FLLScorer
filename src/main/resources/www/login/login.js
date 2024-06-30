// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

// Handles setup of the login page.
function
ready()
{
  // See if a login error has occurred.
  if(window.location.search.indexOf("error=yes") != -1)
  {
    // Switch from the login_container to the error_container, displaying the
    // error message.
    $(".login_container")
      .removeClass("login_container")
      .addClass("error_container");
  }
}

// Set the function to call when the page is ready.
$(document).ready(ready);