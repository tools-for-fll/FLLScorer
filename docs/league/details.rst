..
   Copyright (c) 2025 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

Ranking Details
===============

The following describes how points are awarded for each of the four sections of
a competition.  The team's overall score is the sum of their points for each
section.

In each section, there are two steps; determine the ranking of the teams (from
first to last), then assign points based on the placements.  Assigning points
is done in the same way for each section.


Robot Game Ranking
------------------

The ranking for robot game are based on the team's best score.  If there is a
tie, the second best scores are used as a tie-breaker.  If there is still a
tie, the third best score, and so on for as many matches as there are at the
event.  If more than one team has the same set of scores, they all are given
the same ranking (there is no coin-toss, playoff, or other means to separate
the tie).  In that case, the next ranking simply "doesn't exist".  For example,
if two teams tie for first place, the next best team is awarded third place (as
if there were a tie-breaker and the two first place teams were actually first
and second).


Innovation Project Ranking
--------------------------

A team's score in the innovation project is the sum of the values for each the
items on their innovation project rubric.  So, for example, if a team scored a
1 on all twelve of the innovation rubric items, their innovation project score
is 12; if they scored a 3 on all of them, their score is 36.

Once each team's innovation project score is determined (these are actually
listed on the :doc:`../admin/rubrics/rubrics` panel), the teams are ranked from
first to last based on score; the highest score is first, and the lowest score
is last.  There is no tie-breaker; if more than one team has the same score,
they share a ranking, and the subsequent ranking "doesn't exist".  For example,
if two teams tie for first place, the next best team is awarded third place (as
if there were a tie-breaker and the two first place teams were actually first
and second).


Robot Design Ranking
--------------------

Robot design ranking is done the exact same way as innovation project ranking.


Core Values Ranking
-------------------

Core Values ranking is done the exact same way as innovation project ranking,
with the addition that the Core Values scores from the referees are also
included in the score (but are not displayed in the
:doc:`../admin/rubrics/rubrics` panel).


Assigning Points
----------------

For each individual ranking above, points are assigned to each team based on
the following formula:

   ``100 + (100 * (N - R) / (N - 1))``

Where ``N`` is the number of teams at the event, and ``R`` is the team's rank.

Taking some simple examples:

- There are three teams, and they ranked 1, 2, and 3.  The first team scores:

     ``100 + (100 * (3 - 1) / (3 - 1)) = 100 + (100 * 2 / 2) = 200``

  The second team scores:

     ``100 + (100 * (3 - 2) / (3 - 1)) = 100 + (100 * 1 / 2) = 150``

  The third team scores:

     ``100 + (100 * (3 - 3) / (3 -1 )) = 100 + (100 * 0 / 2) = 100``

- There are three teams, and they ranked 1, 1, and 3 (a two-way tie for first
  place).  The first and second team both score 200 (as in the above example),
  and the third team scores 100 (as above).

- There are three teams, and they ranked 1, 2, and 2 (a two-way tie for second
  place).  The first team scores 200, and the two place teams in second both
  score 150.

A team that is in first place in all four sections receives 800 points, and a
team that is in last place in all four sections receives 400 points.  The
thinking being that there is no possibility of a team scoring 0, and the
separation between first (800) and last (400) is not quite as overwhelming as
it would be if first received 400 points and last received 0 points (even
though the point difference is identical).