..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

Scores
======

The scores panel provides a way to monitor the entry of scoresheets during an
event, correct mistakes (such as entering a scoresheet for the wrong team or
wrong match), or for a scorekeeper to enter paper scoresheets.  When using a
scorekeeper and paper scoresheets, this is where the scorekeeper spends the
majority of their time; when the referees are doing electronic scoring at the
table, this is hopefully never used!

.. image:: scores.webp
   :alt: Scores panel
   :align: center

The score panel shows a list of the teams that are attending the event, and
their scores (if any) for the event.  If doing electronic scoring at the table,
the scores appear (automatically) on this panel when they are published.

For each team, there is a column for each match at the event, with the score
(if it exists) and a set of buttons for editing, exchanging, and deleting a
score for that match.


Ediing a Scoresheet
-------------------

The :fa:`pencil` button brings up the scoresheet for a team's match (the same
scoresheet display that a referee would see).  The scoresheet can then be
entered or changed, following the same process as :doc:`../../referee/referee`
(since it is the same thing).

If the event is being scored with paper scoresheets and entered by a
scorekeeper, this is where the scorekeeper spends their day.  If there is a
scoring question, this is also a place to review the scoresheet.


Exchanging Scores
-----------------

On occasion, a scoresheet is entered for the wrong team or round.  Instead of
re-entering the scoresheet for the correct team/round, the :fa:`exchange`
button allows the scoresheet (and score) to be swapped with another scoresheet.
It performs an exchange; so it is safe to use if two team's scoresheets were
entered for each other.


Deleting a Score
----------------

The :fa:`trash` button deletes the scoresheet (and score) for a team and round.
A confirmation dialog ensures that the scoresheet should be deleted, and if
confirmed the scoresheet is permanently deleted.

.. danger::
   There is no undo!


Search For Teams
----------------

The search bar at the bottom of the panel provides a means to search for a
team.  While the teams are displayed in numerical order, it is sometimes easier
to search for them (either by name or by number).  Click in the search bar, or
press *Ctrl-S*, then start typing.  The list of team is re-filtered with each
change to the search.  When the search bar has the keyboard focus, pressing
*Escape* clears the contents of the search bar, displaying all the teams.


Refresh Scores
--------------

The :fa:`refresh` button causes the list of teams and scores to be refreshed.
This *should not* be required, but it can be used if there is a question.  The
list can also be refreshed by pressing *Ctrl-R*.


Download Scores
---------------

The :fa:`download` button generates a CSV with all the match data and downloads
it to the local computer.  The CSV contains:

- Division (if division support is enabled)
- Team number
- Team name
- Robot game place
- Top score
- Score from every round (including practice)
- Core Values score from every round (including practice)

This CSV file can be used to easily add the robot game results into the judging
system (OJS or similar) to determine Champions Award and team advancements.