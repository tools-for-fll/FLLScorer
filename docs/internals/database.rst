..
   Copyright (c) 2024 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

Database Design
===============

The system is broken up into a set of unique seasons (challenges), each having
a set of teams and events.  Even though the same team may participate for
multiple seasons, it is treated as unique each season since some/many teams
change their name to match the challenge theme.

Given a season, event, and team, there are robot game scores and judging
scores for that season/event/team combination.

The tables are setup to support this structure, and to only provide entries
for teams that participate in a given season and at a given event.


Season table
------------

Each row of the *season* table contains details about a single season.  The
columns are:

id
    Definition: *integer primary key*

    A unique ID for each row of the table; other tables link to this ID with a
    **season_id** column

year
    Definition: *char*

    The year for the season.  Examples are *2023-2024*, *2024-2025*, and so on.

name
    Definition: *char*

    The name of the game for the season.  Examples are *MasterPiece*,
    *Submerged*, and so on.


Event table
-----------

Each row of the *event* table contains details about a single event.  The
columns are:

id
    Definition: *integer primary key*

    A unique ID for each row of the table; other tables link to this ID with an
    **event_id** column.

season_id
    Definition: *integer*

    The ID for an entry in the season table for this event.

date
    Definition: *date*

    The date on which the event was held.  Examples are *Nov 11, 2023*,
    *Nov 18, 2023*, and so on.

matches
    Definition: *integer*

    The number of robot game matches at this event. The units digit contains
    the number of scoring rounds, and the hundreds digit contains the number of
    practice rounds. For example, *4* is an event with four scoring rounds and
    no practice round, while *103* is an event with three scoring rounds and
    one practice round.

name
    Definition: *char*

    The name of the event.


Team table
----------

Each row of the *team* table contains details about a single team.  The columns
are:

id
    Definition: *integer primary key*

    A unique ID for each row of the table; other tables link to this ID with a
    **team_id** column.

season_id
    Definition: *integer*

    The ID for an entry in the season table for this team.

division
    Definition: *integer*

    The team's division (stored as **1** if divisions are not enabled).

number
    Definition: *integer*

    The team's number, as assigned by FIRST.

name
    Definition: *char*

    The team's name.


Team at Event table
-------------------

Each row of the *teamAtEvent* table contains an assignment of a team to an
event. The columns are:

season_id
    Definition: *integer*

    The ID of the season.

event_id
    Definition: *integer*

    The ID of the event.

team_id
    Definition: *integer*

    The ID of the team.


Score table
-----------

Each row of the *score* table contains details about a single team's robot game
scores at a single event.  The columns are:

id
    Definition: *integer primnary key*

    A unique ID for each row of the table.

season_id
    Definition: *integer*

    The ID for an entry in the season table for ths score.

event_id
    Definition: *integer*

    The ID for an entry in the event table for this score.

team_id
    Definition: *integer*

    The ID for an entry in the team table for this score.

match0
    Definition: *float*

    The robot game score for the practice match.

match0_cv
    Definition: *integer*

    The robot game core values score for the practice match.

match0_sheet
    Definition: *char*

    The robot game scoresheet (JSON) for the practice match.

match1
    Definition: *float*

    The robot game score for the first match.

match1_cv
    Definition: *integer*

    The robot game core values score for the first match.

match1_sheet
    Definition: *char*

    The robot game scoresheet (JSON) for the first match.

match2
    Definition: *float*

    The robot game score for the second match.

match2_cv
    Definition: *integer*

    The robot game core values score for the second match.

match2_sheet
    Definition: *char*

    The robot game scoresheet (JSON) for the second match.

match3
    Definition: *float*

    The robot game score for the third match.

match3_cv
    Definition: *integer*

    The robot game core values score for the third match.

match3_sheet
    Definition: *char*

    The robot game scoresheet (JSON) for the third match.

match4
    Definition: *float*

    The robot game score for the fourth match.

match4_cv
    Definition: *integer*

    The robot game core values score for the fourth match.

match4_sheet
    Definition: *char*

    The robot game scoresheet (JSON) for the fourth match.

Note that the *score* table supports up to one practice round and four scoring
rounds; supporting more requires adding additional columns to this table (in
addition to changes in the application itself).


Judging table
-------------

Each row of the *judging* table contains details about a single team's judging
scores at a single event.  The columns are:

id
    Definition: *integer primnary key*

    A unique ID for each row of the table.

season_id
    Definition: *integer*

    The ID for an entry in the season table for ths score.

event_id
    Definition: *integer*

    The ID for an entry in the event table for this score.

team_id
    Definition: *integer*

    The ID for an entry in the team table for this score.

project
    Definition: *integer*

    The innovation project score.

robot_design
    Definition: *integer*

    The robot design score.

core_values
    Definition: *integer*

    The core values score.

rubric
    Definition: *char*

    The judging rubric (JSON).


Users table
-----------

Each row of the *user* table contains details about a single user.  The columns
are:

id
    Definition: *integer primnary key*

    A unique ID for each row of the table.

name
    Definition: *char*

    The user's name.

password
    Definition: *char*

    The user's password, hashed.

admin
    Definition: *integer*

    *True* if the user has the *admin* role.

host
    Definition: *integer*

    *True* if the user has the *host* role.

judge
    Definition: *integer*

    *True* if the user has the *judge* role.

referee
    Definition: *integer*

    *True* if the user has the *referee* role.

timekeeper
    Definition: *integer*

    *True* if the user has the *timekeeper* role.