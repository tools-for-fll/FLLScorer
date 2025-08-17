..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

League Model
============

In official FLL competitions, teams attend a qualifing event.  At that event,
roughly 35% of the teams advance to the next round (area championship, state
championship, and so on).  For the remaining 65% of teams, their season ends
after a single competition.  These percentages vary from region to region of
course.

With such a large percentage attending just a single competition, they do not
have the opportunity to learn from their experience and the judge's feedback,
make improvements, and attend another competition.  This is a missed learning
opportunity for the students.

FTC has a league model and FRC has a district model, where teams have the
opportunity to compete in more than one event.  The results from two events are
combined to determine the league/district rankings, and those are used to
determine the teams that advance.

The FLL league model supported by FLL Scorer is inspired by the FTC and FRC
systems.  It provides teams a second chance to complete, enabling that extra
learning opportunity of improving their project and robot based on feedback at
an event.

The league model may not work at a large scale.  Imagine the number of official
qualifiers in your region, the number of venues required, the number of event
days, the number of volunteers, and so on...then double that!  However, it is
quite doable at a smaller scale, such as in a series of "unoffical events"
(that would be officially classified as scrimmages).

If all of the events are scored with the same scoring computer, as a set of
events within the system, the league model portion of the system gathers the
scores from the first two events a team attends, assigns a composite score to
each event, then sums them for the team's league score.  The
:doc:`../standings/standings` page shows those scores (both the individual
event scores and the sum of the two event scores), along with a ranking of
teams based on the sumed score.

.. note::
   It is probably best to not display the league standings during an event.  It
   changes too dramatically (especially if judges are entering rubrics as part
   of judging; some teams will have full judging rubrics while others have
   nothing) and is more likely to induce added stress during the event.

If there are enough teams that participate in a league system like this, the
standings could be used to invite some number of the top teams in the league to
attend a league championship event (again, akin to the league championship
event in FTC or the distrct championship event in FRC). Or, the league rankings
can be used just for fun.  It is entirely up to the organizers how far they
want to go with the league model, and how much of their time they want to put
into it.

When using the league model, the team's rubrics must be captured in the
:doc:`../admin/rubrics/rubrics` panel of the :doc:`../admin/admin` page.  This
does not mean that the judges must use FLL Scorer to perform their judging,
just that the results need to be entered by someone.  The composite score is
based on the robot game results, the project judging rubric, the robot design
judging rubric, and the Core Values judging rubric (combined with the Core
Values scores awarded by referees at each match).  In other words, it is akin
to the Champions Award criteria.

.. note::
   The team's composite score from an event is not necessarily computed in the
   same manner as OJS, Event Hub, and so on.  It is *possible* that the
   rankings computed here are not in 100% agreement with the official rankings.
   Given that the rankings of the individual judging areas are used by not
   shown to teams, just like the judging rankings are not shared with teams, it
   is unlikely to be noticed.

In the unlikely case that a team is disqualified at an event, the only way to
reflect that here is to change their judging rubrics to all 1's.  Either for
all rubrics, or only one of them (if they were disqualified from only one
judging area).  This places them into "last place" in those judging areas,
awarding them the least number of composite points.

Displaying the :doc:`../standings/standings` page at the end of an event, after
awards have been announced, could be fun for the teams.  Or, the final results
can be shared with all the teams at the conclusion of all of the league events.
If or how this is utilized is up to your discretion and creativity!