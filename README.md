# Elastisch, a Clojure client for ElasticSearch

Elastisch is a minimalistic [Clojure client for ElasticSearch](http://clojureelasticsearch.info), a modern distributed RESTful search engine.


## Project Goals

 * Be reasonably feature complete
 * Be [well documented](http://clojureelasticsearch.info)
 * Be [well tested](https://github.com/michaelklishin/elastisch/tree/master/test/elastisch/test)
 * Closely follow [ElasticSearch API structure](http://www.elasticsearch.org/guide/reference/api/), no new abstractions introduced
 * Follow recent ElasticSearch releases & developments
 * Target Clojure 1.3.0 and later from the ground up


## Community

[Elastisch has a mailing list](https://groups.google.com/forum/#!forum/clojure-elasticsearch). Feel free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz) on Twitter.



## Project Maturity

Elastisch is a young project, although it has been used in production since early days. Elastisch API is mostly stabilized by now. 1.0 will be released
when we are confident in the public API, reached feature completeness and documentation guides site is in good shape.



## Artifacts

With Leiningen:

    [clojurewerkz/elastisch "1.0.0-rc1"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch</artifactId>
      <version>1.0.0-rc1</version>
    </dependency>



## Documentation & Examples

Please see our [documentation guides site](http://clojureelasticsearch.info/).

Our [test suite](https://github.com/clojurewerkz/elastisch/tree/master/test/clojurewerkz/elastisch) also has many code examples.



## Supported Clojure versions

Elastisch is built from the ground up for Clojure 1.3 and up.


## Elastisch Is a ClojureWerkz Project

Elastisch is part of the group of libraries known as ClojureWerkz, together with
[Monger](https://github.com/michaelklishin/monger), [Welle](https://github.com/michaelklishin/welle), [Langohr](https://github.com/michaelklishin/langohr), [Neocons](https://github.com/michaelklishin/neocons), [Quartzite](https://github.com/michaelklishin/quartzite) and several others.


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/elastisch.png)](http://travis-ci.org/clojurewerkz/elastisch)


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

### Why Not Just Wrap the Java Client?

Wrapping ES Java client may result in some performance gain but we don't see it to be great enough to be a significant factor for Elastisch.
Following Java client conventions would likely make Elastisch less Clojuric.
