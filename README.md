Copyright &copy; 2024 Brian Kircher

Open Source Software; you can modify and/or share it under the terms of BSD
license file in the root directory of this project.

![Build status](https://github.com/bckircher/FLLScorer/actions/workflows/gradle.yml/badge.svg)

# Overview

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

Note:
   While the system supports capturing the judging rubrics, it does **not**
   include other judging features such as:

   - Filling out the "Judging Session Feedback" page
   - Capturing an explanation of why a score of 4 was given
   - Viewing judging rankings

   Captured rubrics are used **only** for calculating league standings; if
   league support is not being used, rubric entry can be ignored.


## System Components

The system consists of two main components:

1. The  Java application that runs on the scoring computer and appears as
   follows:

   ![The main FLL Scorer window](docs/main_window.webp)

2. The web interface that is the primary interface for managing the event,
   served and supported by the Java applcation.  All event operations are
   conducted through this website.


## Documentation

Complete documentation is found [here](src/main/resources/www/docs/index.html).