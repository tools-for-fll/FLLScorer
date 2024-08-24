<!--
Copyright (c) 2024 Brian Kircher

Open Source Software; you can modify and/or share it under the terms of BSD
license file in the root directory of this project.
-->

# Overview

The scoresheet is contained in a JSON structure that provides detailed
information about each mission, including its objectives, scoring criteria,
constraints, and game pieces used (when there are fewer game pieces than
scoring locations for that game piece).

Within the scoresheet, all strings that are presented to the user are
represented as an associative array where the key (referred to as the locale)
is the ISO language code (ISO-639) followed by the ISO country code (ISO-3166),
and the values are the corresponding string translated to that locale.  When a
string is requested for a particular locale and if that locale does not exist,
the "en_US" locale is used as the default.

## Scoresheet Description

The structure of the scoresheet is as follows:

* "missions" - An array of missions

  * "mission" - The short identifier for the mission, for example "M01", "M02",
                and so on.

  * "name" - An associative array containing the name of the mission name in
             various languages.

  * "items" - An array of scoring criteria for this mission.

    * "id" - A unique identifier for the item within the mission.

    * "description" - An associative array containing the description of this
                      mission item in various languages.

    * "type" - Type of item, such as "yesno" (yes/no question) or "enum"
               (multiple-choice question).

    * "choices" - If the item type is "enum", this associative array lists the
                  available choices in various languages.

    * "score" - Scoring criteria for the item, represented as an array
                indicating the score for each possible outcome.

    * "pieces" - (Optional) An array of information about specific game pieces
                 required for the item, such as quantity and name.

      * "name" - The name of the game piece.

      * "quantity" - An array of the number of the game piece corresponding to
                     each of the choicess.

  * "constraints" - (Optional) An array of constraints for the mission.

    * "description" - An associative array containing the description of the
                      error condition when this constraint is not met, in
                      various languages.

    * "rule" - An expression that evalutes to 0 or more when the constraint is
               met and less than 0 when the constraint is violated.

  * "no_touch" - (Optional) A boolean that indicates that team equipment is not
                 allowed to touch the mission model for this mission to score.
                 This is used as a reminder to the referee during scoring.

* "pieces" - An array of the constrained quantity game pieces.

  * "name" - The name of the game piece.

  * "description" - An associative array containing the description of the game
                    piece in various languages.

  * "mission" - The mission identifier for the mission where an error is placed
                if this game piece is over allocated in the scoring of the
                scoresheet.

  * "quantity" - The number of instances of this game piece.

## Rule Syntax

A set of variables is made available for the rules to perform calculations
based on the scoresheet selections that have been made.  They take the form
"mission_id"; for example, "M01_1" is the variable that represents the choice
made on M01 for the item with an ID of 1, and its value is the selection that
was made.

For the "yesno" item type, no has a value of 0 and yes has a value of 1.  For
the "enum" item type, the choices are numbered 0 through N - 1, in order, for
the N choices provided in the scoresheet.

Using these variables, any mathmatical expression is constructed to validate
the state of a particular mission, where a positive result is a valid state and
a negative result is an invalid state.  For example, if a mission (M) has two
yes/no questions, and the second answer can only be yes if the first answer is
yes, a rule of "M_1 - M_2" validates the mission.  To see how, it is easy to
list all the possiblities, the computed value, and the validation state:

  | M_1     | M_2     | M_1 - M_2  | Valid? |
  | ------- | ------- | ---------- | ------ |
  | no (0)  | no (0)  | 0 - 0 = 0  | true   |
  | yes (1) | no (0)  | 1 - 0 = 1  | true   |
  | no (0)  | yes (1) | 0 - 1 = -1 | false  |
  | yes (1) | yes (1) | 1 - 1 = 0  | true   |

Only the M_1 = 0 and M_2 = 1 case results in a negative number, which is
invalid as desired.  All the other cases are valid.

[Javaluator](https://github.com/fathzer/javaluator) is used to evaluate the
rule expression; it can be consulted for details on what expressions are
available (if something more complicated than addition, subtraction,
multiplication, division, and parenthesis is needed).