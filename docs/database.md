<!--
Copyright (c) 2024 Brian Kircher

Open Source Software; you can modify and/or share it under the terms of BSD
license file in the root directory of this project.
-->

# Database Design

The system is broken up into a set of unique seasons (challenges), each having
a set of teams and events.  Even though the same team may participate for
multiple seasons, it is treated as unique each season since some/many teams
change their name to match the challenge theme.

Given a season, event, and team, there are robot game scores and judging
scores for that season/event/team combination.

The tables are setup to support this structure, and to only provide entries
for teams that participate in a given season and at a given event.


## Season table

Each row of the season table contains details about a single season.  The
columns are:

**id** &rarr; A unique ID for each row of the table; other tables link to this
              ID with a **season_id** column

**year** &rarr; The year for the season.  Examples are _2023-2024_,
                _2024-2025_, and so on.

**name** &rarr; The name of the game for the season.  Examples are
                 _MasterPiece_, _Submerged_, and so on.


## Event table

Each row of the event table contains details about a single event.  The columns
are:

**id** &rarr; A unique ID for each row of the table; other tables link to this
              ID with an **event_id** column.

**season_id** &rarr; The ID for an entry in the season table for this event.

**date** &rarr; The date on which the event was held.  Examples are _Nov 11,
                2023_, _Nov 18, 2023_, and so on.

**matches** &rarr; The number of robot game matches at this event.

**name** &rarr; The name of the event.


## Team table

Each row of the team table contains details about a single team.  The columns
are:

**id** &rarr; A unique ID for each row of the table; other tables link to this
              ID with a **team_id** column.

**season_id** &rarr; The ID for an entry in the season table for this team.

**number** &rarr; The team's number, as assigned by FIRST.

**name** &rarr; The team's name.


## Team at Event table

Each row of the teamAtEvent table contains an assignment of a team to an event.
The columns are:

**event_id** &rarr; The ID of the event.

**team_id** &rarr; The ID of the team.


## Score table

Each row of the score table contains details about a single team's robot game
scores at a single event.  The columns are:

**id** &rarr; A unique ID for each row of the table.

**season_id** &rarr; The ID for an entry in the season table for ths score.

**event_id** &rarr; The ID for an entry in the event table for this score.

**team_id** &rarr; The ID for an entry in the team table for this score.

**match1** &rarr; The robot game score for the first match.

**match1_cv** &rarr; The robot game core values score for the first match.

**match1_sheet** &rarr; The robot game scoresheet (JSON) for the first match.

**match2** &rarr; The robot game score for the second match.

**match2_cv** &rarr; The robot game core values score for the second match.

**match2_sheet** &rarr; The robot game scoresheet (JSON) for the second match.

**match3** &rarr; The robot game score for the third match.

**match3_cv** &rarr; The robot game core values score for the third match.

**match3_sheet** &rarr; The robot game scoresheet (JSON) for the third match.

**match4** &rarr; The robot game score for the fourth match.

**match4_cv** &rarr; THe robot game core values score for the fourth match.

**match4_sheet** &rarr; The robot game scoresheet (JSON) for the fourth match.


## Judging table

Each row of the score table contains details about a single team's judging
scores at a single event.  The columns are:

**id** &rarr; A unique ID for each row of the table.

**season_id** &rarr; The ID for an entry in the season table for ths score.

**event_id** &rarr; The ID for an entry in the event table for this score.

**team_id** &rarr; The ID for an entry in the team table for this score.

**project** &rarr; The innovation project score.

**robot_design** &rarr; The robot design score.

**core_values** &rarr; The core values score.

**rubric** &rarr; The judging rubric (JSON).


## Users table

Each row of the users table contains details about a single user.  The columns
are:

**id** &rarr; A unique ID for each row of the table.

**name** &rarr; The user's name.

**password** &rarr; The user's password, hashed.

**admin** &rarr; True if the user has the *admin* role.

**host** &rarr; True if the user has the *host* role.

**judge** &rarr; True if the user has the *judge* role.

**referee** &rarr; True if the user has the *referee* role.

**timekeeper** &rarr; True if the user has the *timekeeper* role.