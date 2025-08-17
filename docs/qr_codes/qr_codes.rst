..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

QR Codes
========

When the web site is first launched from the FLL Scorer application, it goes to
the QR codes page.  This page contains a variety of QR codes, along with their
corresponding URLs.

.. image:: qr_codes.webp
   :alt: The QR code page, providing links to all the event tools
   :align: center


Opening Pages
-------------

Getting to the corresponding pages happens in one of three ways:

- Clicking on one of the QR codes opens the page in a new window/tab on the
  scoring computer.

- Entering the URL into the browser on another computer/device opens the page
  on that device (assuming that the two devices are able to communicate over
  the network; see :doc:`../quickstart/networking`).

- Scanning the QR code with another computer/device opens the page on that
  device, without having to type anything (with the same networking
  assumption)!


Available Pages
---------------

The following pages are available:

WiFi "Page"
   This isn't really a page; rather it is a means of simplifying the process of
   getting another device onto the network used by the scoring computer.  It
   encodes the WiFi credentials into the QR code, which the scanner app is able
   to use to connect to the WiFi network (if given permission).  The WiFi
   credentials are entered into the :doc:`../admin/config/config` panel of the
   :doc:`../admin/admin` page.

   .. note::
      This QR code only appears if the WiFi credentials are configured in the
      :doc:`../admin/config/config` panel; if they are blank, this QR code does
      not appear.

:doc:`../admin/admin` Page
   This page is where all the administration of the event occurs.  It is
   intended for use by the tournament director, head referee, judge advisor,
   scorekeeper (if the event has this position), and so on.  For the most part,
   the event will be setup through this page prior to the start of the event,
   and then it will not be used during the event (unless using paper
   scoresheets and a scorekeeper).

:doc:`../judge/judge` Page
   This page is used by the judges if using the :doc:`../league/league`;
   otherwise, it is not used.  It may be used by the judges to enter rubrics
   during the event, or it may be used by someone to enter the rubrics after
   the event is over.

:doc:`../referee/referee` Page
   This page is used by the referees to capture scoresheets during the event,
   if using electronic scoresheet capture.  If using a traditional scorekeeper
   mode, this page is not used.

:doc:`../scoreboard/scoreboard` Page
   This page shows the scoreboard audience display, allowing teams and
   spectators to track the progress of their favorite teams(s).  If enabled,
   it can also show the match timer.

:doc:`../standings/standings` Page
   This page shows the league standings audience display, if using the
   :doc:`../league/league`.  The league standings can change rather drastically
   throughout an event, and may be revealing of judging results (if the judges
   enter rubrics during the event), so it is probably best to only show the
   league standings at the end of the event (after all the awards have been
   handed out), though this is a decision for the league managers.

:doc:`../timekeeper/timekeeper` Page
   This page shows the current state of the match timer, and provides controls
   for the time keeper to operate the timer.  This should be utilized by the
   time keeper only, and not used as an audience display (there is a dedicated
   page for that!).

:doc:`../timer/timer` Page
   This page shows the match timer audience display, allowing teams, referees,
   and game announcers to keep track of the match time.