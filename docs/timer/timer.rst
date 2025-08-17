..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

Timer
=====

The timer page is an audience display that shows the current match time.  It
does *not* play match sounds, so it is not an alternative/secondary means of
presenting match sounds (due to the time uncertainty of the timer display over
the network, and the complete failure of match sounds if the network connection
were lost for some reason).

.. image:: timer.webp
   :alt: Audience timer display
   :align: center

When the timer is the active window, pressing *Ctrl-F* puts it into fullscreen
mode, hiding all the window decorations, browser address bar, browser tool bar,
etc.  This makes for a much cleaner audience display.  Pressing *Ctrl-F* again,
or *Escape*, leaves fullscreen mode.

The time is displayed in green for the first portion of the match, changes to
yellow at the 30 second mark in the match (when the 30 second warning sound
plays), and changed to red at the end of the match.

There are no controls as this simply displays the match timer; use the
:doc:`../timekeeper/timekeeper` page to control the timer.