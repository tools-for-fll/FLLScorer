..
   Copyright (c) 2024 Brian Kircher

   Open Source Software; you can modify and/or share it under the terms of BSD
   license file in the root directory of this project.

Scoresheet Format
=================

The scoresheet is contained in a JSON structure that provides detailed
information about each mission, including its objectives, scoring criteria,
constraints, and game pieces used (when there are fewer game pieces than
scoring locations for that game piece).

Within the scoresheet, all strings that are presented to the user are
represented as an associative array where the key (referred to as the locale)
is the ISO language code (ISO-639) followed by the ISO country code (ISO-3166),
and the values are the corresponding string translated to that locale.  When a
string is requested for a particular locale and that locale does not exist, the
``en_US`` locale is used as the default.


Description
-----------

The structure of the scoresheet is as follows:

missions
    An array of missions

    mission
        The short identifier for the mission, for example ``M01``, ``M02``, and
        so on.

    name
        An associative array containing the name of the mission name in various
        languages.

    items
        An array of scoring criteria for this mission.

        id
            A unique identifier for the item within the mission.

        description
            An associative array containing the description of this mission
            item in various languages.

        type
            Type of item, such as ``yesno`` (yes/no question) or ``enum``
            (multiple-choice question).

        choices
            If the item type is ``enum``, this associative array lists the
            available choices in various languages.

        score
            (Optional) Scoring for the item, represented as an array indicating
            the score for each possible choice (with two entries for a
            ``yesno`` type and N entries for an ``enum`` type, where N is the
            length of the ``choices`` array).

        pieces
            (Optional) An array of information about specific game pieces
            required for the item, such as quantity and name.

            name
                The name of the game piece.

            quantity
                An array of the number of the game piece corresponding to each
                of the choices.

    score
        (Optional) Scoring for this mission, where each item can not be scored
        individually (because the scoring is dependent upon the various items).
        Each entry in this array indicates the score based on a mathematical
        encoding of the item choices (see :ref:`mathematical_encoding`). For
        invalid choices (as encoded via the constraints), the score should be
        entered as zero.

    score_rule
        (Optional) An expression that computes the score for this mission (see
        :ref:`score_rule`).

    constraints
        (Optional) An array of constraints for the mission.

        description
            An associative array containing the description of the error
            condition when this constraint is not met, in various languages.

        rule
            An expression that evalutes to 0 or more when the constraint is met
            and less than 0 when the constraint is violated (see
            :ref:`validation_rule`).

    no_touch
        (Optional) A boolean that indicates that team equipment is not allowed
        to touch the mission model for this mission to score. This is used as a
        reminder to the referee during scoring.

pieces
    An array of the constrained quantity game pieces.

    name
        The name of the game piece.

    description
        An associative array containing the description of the game piece in
        various languages.

    mission
        The mission identifier for the mission where an error is placed if this
        game piece is over allocated in the scoring of the scoresheet.

    quantity
        The number of instances of this game piece.


Rule Syntax
-----------

A set of variables is made available for the rules to perform calculations
based on the scoresheet selections that have been made.  They take the form
``mission_id``; for example, ``M01_1`` is the variable that represents the
choice made on M01 for the item with an ID of 1, and its value is the selection
that was made.

For the ``yesno`` item type, **no** has a value of **0** and **yes** has a
value of **1**.  For the ``enum`` item type, the choices are numbered **0**
through **N - 1**, in order, for the **N** choices provided in the scoresheet.

Javaulator_ is used to evaluate the rule expression; it can be consulted for
details on what expressions are available (if something more complicated than
addition, subtraction, multiplication, division, and parenthesis is needed).


.. _validation_rule:

Validation Rule
~~~~~~~~~~~~~~~

Using these variables, any mathmatical expression is constructed to validate
the state of a particular mission, where a positive result is a valid state and
a negative result is an invalid state.  For example, if a mission (M) has two
yes/no questions, and the second answer can only be **yes** if the first answer
is **yes**, a rule of ``M_1 - M_2`` validates the mission.  To see how, it is
easy to list all the possiblities, the computed value, and the validation
state:

 +---------+---------+------------+--------+
 | M_1     | M_2     | M_1 - M_2  | Valid? |
 +=========+=========+============+========+
 | no (0)  | no (0)  | 0 - 0 = 0  | true   |
 +---------+---------+------------+--------+
 | yes (1) | no (0)  | 1 - 0 = 1  | true   |
 +---------+---------+------------+--------+
 | no (0)  | yes (1) | 0 - 1 = -1 | false  |
 +---------+---------+------------+--------+
 | yes (1) | yes (1) | 1 - 1 = 0  | true   |
 +---------+---------+------------+--------+

Only the **M_1** = **0** and **M_2** = **1** case results in a negative number,
which is invalid as desired.  All the other cases are valid.


.. _score_rule:

Score Rule
~~~~~~~~~~

Similarly, an expression is constructed using these variables to compute the
score of a mission based on their values. Since this is evaluated after all the
mission variables have been determined, a scoring rule can evalute based on all
the other missions (as needed by the Engagement model from World Class, for
example).


.. _mathematical_encoding:

Mathematical Encoding
---------------------

A mathematical encoding of the item choices is used to minimize the size of the
scoring table for missions that have interdependent item scores. The process is
best understood by taking a hypothetical mission with a yes/no choice for its
first item and a three choice enumeration for its second item. There are six
possible outcomes, and the two choices are combined via the formula:

   ``(b * 2) + a``

Where ``a`` is the choice for the first item (yes/no) and ``b`` is the choice
for the second item. ``b`` is multiplied by ``2`` since the first item has two
possible choices. This provides 6 unique values (0..5) for the possible choices
for the two mission items.

For a slightly more complicated set of items, if the first item is an enum with
three choices, the second item is a yes/no choice, and the third item is an
enum with three choices, the choices are combined via the formula:

   ``(c * 3 * 2) + (b * 3) + a``

In this, ``b`` is multiplied by the number of choices for ``a``, and ``c`` is
multiplied by the number of choices for ``a`` times the number of choices for
``b``. Therefore, any combination of valid values for ``a``, ``b``, and ``c``
encode to a unique value, and there are no "holes" in the sequence of values.

To extract the values of ``a``, ``b``, and ``c`` from the resulting value
(``n``), using integer arithmetic:

   ``a = n % 3``

   ``n = n / 3``

   ``b = n % 2``

   ``c = n / 2``

This can be extended to any number of items in a mission.


.. _Javaulator: https://github.com/fathzer/javaluator