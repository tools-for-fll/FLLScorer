<!--
Copyright (c) 2024 Brian Kircher

Open Source Software; you can modify and/or share it under the terms of BSD
license file in the root directory of this project.
-->

# Overview

The rubric is contained in a JSON structure that provides detailed information
about each judging area and the judged items within each.  Individual items are
identified as being Core Values items, to support rubrics up to Masterpiece
(that have an individual Core Values rubric) and Submerged and beyond (that
have items from the Innovation Project and Robot Design rubrics that also count
as Core Values items).

Within the rubric, all strings that are presented to the user are represented
as an associative array where the key (referred to as the locale) is the ISO
language code (ISO-639) followed by the ISO country code (ISO-3166), and the
values are the corresponding string translated to that locale.  When a string
is requested for a particular locale and if that locale does not exist, the
"en_US" locale is used as the default.

## Rubric Description

The structure of the scoresheet is as follows:

* "areas" - An array of judging areas.

  * "name" - An associative array containing the name of the judging area in
             various languages.

  * "sections" - An array of sections within the rubric for this judging area.

    * "name" - An associative array containing the name of the section in
               various languages.

    * "items" - An array of judging items within this section.

      * "isCoreValues" - A boolean that is **true** if this item counts towards
                         the team's Core Values score; if **false** or missing,
                         it does not apply to Core Values.

      * "1" - "4" - An associative array containing the description of the
                    corresponding score for this judging item, in various
                    languages.