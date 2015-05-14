## Pre-requisites

The project uses [Leiningen 2](https://leiningen.org) and requires ElasticSearch `1.4.x` or more recent to be running
locally.

`script.disable_dynamic` must be `false` for scripting tests to pass. To set this, add the following to your `elasticsearch.yml` file:

    script:
        disable_dynamic: false

Make
sure you have those two installed and then run tests against all supported Clojure versions using

    lein all test

## Pull Requests

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, write a [good, detailed commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) and submit a pull request on GitHub.

Don't forget to add your changes to `ChangeLog.md` and credit yourself!

