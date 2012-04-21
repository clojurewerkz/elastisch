# Elastisch, a Clojure client for ElasticSearch

Elastisch is a Clojure client for ElasticSearch, a modern distributed RESTful search engine.

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/elastisch.png)](http://travis-ci.org/clojurewerkz/elastisch)


## Project Goals

 * Be reasonably feature complete
 * Be well documented
 * Be well tested
 * Closely follow recent Elastic Search releases & developments
 * Be well maintained
 * Learn from other clients like the Java and Ruby ones
 * Target Clojure 1.3.0 and later from the ground up


## Documentation & Examples

We are working on documentation guides & examples site for the 1.0 release. In the meantime, please refer to the [test suite](https://github.com/michaelklishin/elastisch/tree/master/test/elastisch/test) for code examples.


## Community

[Elastisch has a mailing list](https://groups.google.com/forum/#!forum/clojure-elasticsearch). Feel free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.



## This is a Work In Progress

Core Elastisch APIs are not fully stabilized, it is still largely a work in progress. Keep that in mind. 1.0 will be released when we are confident
in the public API, reached feature completeness and have documentation guides on a dedicated website.


## Artifacts

With Leiningen:

    [clojurewerkz/elastisch "1.0.0-alpha4"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch</artifactId>
      <version>1.0.0-alpha4</version>
    </dependency>

If you feel comfortable using snapshots:

With Leiningen:

    [clojurewerkz/elastisch "1.0.0-SNAPSHOT"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>

Snapshot artifacts are [released to Clojars](https://clojars.org/clojurewerkz/elastisch) every few days.


## Supported Clojure versions

Elastisch is built from the ground up for Clojure 1.3 and up.


## Elastisch Is a ClojureWerkz Project

Elastisch is part of the group of libraries known as ClojureWerkz, together with
[Monger](https://github.com/michaelklishin/monger), [Langohr](https://github.com/michaelklishin/langohr), [Neocons](https://github.com/michaelklishin/neocons), [Quartzite](https://github.com/michaelklishin/quartzite), [Urly](https://github.com/michaelklishin/urly) and several others.



## Development

Elastisch uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

    lein2 all test

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, submit a pull request on Github.



## License

Copyright (C) 2011-2012 Alex Petrov, Michael S. Klishin

Distributed under the Eclipse Public License, the same as Clojure.



## FAQ

### Why Not Wrap the Java Client?

Wrapping ES Java client may produce a performance gain but we don't see that to be very useful, and
following Java client conventions would likely make Elastish less Clojurish.
