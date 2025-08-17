..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

FLL Scorer
==========

FLL Scorer is a comprehensive scoring system for FLL events, supporting the
following features:

- Capturing match scoresheets
- Computing match scores
- Determining team rankings
- Displaying a scoreboard of results
- Exporting robot game result for use in judging
- Running the match timer
- Displaying the match timer

Additionally, in order to support a league model similar to FTC/FRC (where
teams attend multiple events, and league standings are based on their first two
event results combined), the system also supports:

- Capturing judging rubrics
- Computing league standings
- Displaying league standings

.. note::
   While the system supports capturing the judging rubrics, it does **not**
   include other judging features such as:

   - Filling out the "Judging Session Feedback" page
   - Capturing an explanation of why a score of 4 was given
   - Viewing judging rankings

   Captured rubrics are used **only** for calculating league standings; if
   league support is not being used, rubric entry can be ignored.


System Components
-----------------

The system consists of two main components:

#. The  Java application that runs on the scoring computer and appears as
   follows:

   .. image:: main_window.webp
      :alt: The main FLL Scorer window
      :align: center

#. The web interface that is the primary interface for managing the event,
   served and supported by the Java applcation.  All event operations are
   conducted through this website.


Documentation Overview
----------------------

This documentation covers each page in the system, how it works, and how to use
it.  Additionally, there is a :doc:`quickstart/overview` that covers the
highlights of each portion of the system, describing what is needed to run an
event based on the way it is being scored (paper scoresheets versus electronic
scoresheets at the robot game tables).


.. toctree::
   :maxdepth: 2
   :caption: Quickstart
   :hidden:

   Overview <quickstart/overview>
   quickstart/common
   quickstart/scorekeeper
   quickstart/referee
   quickstart/networking

.. toctree::
   :maxdepth: 2
   :caption: Pages
   :hidden:

   qr_codes/qr_codes
   login/login
   admin/admin
   judge/judge
   referee/referee
   scoreboard/scoreboard
   standings/standings
   timekeeper/timekeeper
   timer/timer

.. toctree::
   :maxdepth: 2
   :caption: League Model
   :hidden:

   Overview <league/league>
   league/details

.. toctree::
   :maxdepth: 2
   :caption: Internals
   :hidden:

   internals/developer.rst
   internals/database.rst
   internals/scoresheet.rst
   internals/rubric.rst