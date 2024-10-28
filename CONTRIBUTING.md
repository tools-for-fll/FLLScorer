# Contributing to FLLScorer

Welcome, and thank you for your interest in contributing to FLLScorer!

There are many ways in which you can contribute, beyond writing code.  The goal
of this document is to provide a high-level overview of how you can get
involved.

## Reporting Issues

Have you identified a reproducible problem in FLLScorer?  Have a feature
request?  We want to hear about it!  Here's how you can make reporting your
issue as effective as possible.

### Look For an Existing Issue

Before you create a new issue, please do a search in
[open issues](https://github.com/bckircher/FLLScorer/issues) to see if the
issue or feature request has already been filed.

If you find your issue already exists, make relevant comments or use a reaction
in place of a "+1" comment:

* 👍 - upvote
* 👎 - downvote

If you cannot find an existing issue that describes your bug or feature, create
a new issue using the guidelines below.

### Writing Good Bug Reports and Feature Requests

File a single issue per problem and feature request.  Do not enumerate multiple
bugs or feature requests in the same issue.

Do not add your issue as a comment to an existing issue unless it's for the
identical input.  Many issues look similar, but have different causes.

The more information you can provide, the more likely someone will be
successful at reproducing the issue and finding a fix.

### Final Checklist

Please remember to do the following:

* [ ] Search the issue repository to ensure your report is a new issue

* [ ] Simplify your setup around the issue to better isolate the problem

Don't feel bad if the developers can't reproduce the issue right away.  They
will simply ask for more information!

## Contributing Fixes

If you are interested in writing code to fix issues/add features, first check
the [issues](https://github.com/bckircher/FLLScorer/issues) to be sure that
nobody else is already working on this issue/feature.  If nobody is working on
it, claim the issue, fork the repository, and write your code in a branch of
this forked repository.

Please remember to do the following

* [ ] Create a JUnit test that tests your new code.

* [ ] Try to match the style of existing code (your style is maybe better, but
      FLLScorer developers will maintain your piece of code, not you ...).

* [ ] Ensure that all the JUnit tests for the whole project still succeed.

When your contribution is ready, create a merge request.

## Contributing Translations

If you are interested in translating FLLScorer into another language, first
follow the same steps as contributing fixes.  There are two places where
translatable strings live:

* src/main/resources/strings/_locale_.txt - This contains all the application
  specific strings.  New strings may get added over time if/when new features
  are added to the application.  Each locale has its own file.

* src/main/resources/seasons/_year_/info.json, rubric.json, and scoresheet.json
  - These contain the season/game specific strings.  Each locale has it's own
  sections within these files, along side the translations for the other
  locales.  This is an on-going translation need, as there is a new game (with
  new strings) each year!

In all cases, every single string does not need to be translated.  If a string
does not exist in a particulare locale, the "en_US" version of the string is
used instead.  If a string does not need to be translated, it does not need to
be reflected in that locale's file/section (reducing file size and effort).

The "justification" for defaulting to "en_US" is two-fold:

1. The authors/maintainers are based in the US, and the bulk of the new strings
   will appear in English first as that is the language they speak.

2. English is the governing rulebook language of World Festival.

Is this US/English-biased?  Perhaps.  And with apologies!  🙂  Any default
locale choice is going to be sub-optimal for someone.  As the saying goes:
"You can please some of the people all of the time, you can please all of the
people some of the time, but you can’t please all of the people all of the
time."

# Thank You!

Your contributions to open source, large or small, make great projects
possible.  Thank you for taking the time to contribute.