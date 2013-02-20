# Elastisch, a Clojure client for ElasticSearch

Elastisch is a minimalistic [Clojure client for
ElasticSearch](http://clojureelasticsearch.info), a modern distributed
RESTful search engine.


## Project Goals

 * Be reasonably feature complete
 * Be [well documented](http://clojureelasticsearch.info)
 * Be [well tested](https://github.com/clojurewerkz/elastisch/tree/master/test)
 * Closely follow [ElasticSearch API structure](http://www.elasticsearch.org/guide/reference/api/), no new abstractions introduced
 * Follow recent ElasticSearch releases & developments
 * Target Clojure 1.3.0 and later from the ground up


## Community

[Elastisch has a mailing
list](https://groups.google.com/forum/#!forum/clojure-elasticsearch). Feel
free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so
on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz)
on Twitter.



## Project Maturity

Elastisch is a not a young project. About a year old, it has been used
in production since early days.  Elastisch API is stabilized. 1.0 has
been released in September 2012.



## Artifacts

Elastisch artifacts are [released to Clojars](https://clojars.org/clojurewerkz/elastisch).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

    [clojurewerkz/elastisch "1.0.2"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch</artifactId>
      <version>1.0.2</version>
    </dependency>



## Documentation & Examples

Please see our [documentation guides site](http://clojureelasticsearch.info/).

Our [test suite](https://github.com/clojurewerkz/elastisch/tree/master/test/clojurewerkz/elastisch) also has many code examples.



## Supported Clojure Versions

Elastisch is built from the ground up for Clojure 1.3 and up.
The most recent stable release is highly recommended.


## Elastisch Is a ClojureWerkz Project

Elastisch is part of the group of libraries known as ClojureWerkz, together with

 * [Monger](https://clojuremongodb.info)
 * [Welle](https://clojureriak.info)
 * [Langohr](https://clojurerabbitmq.info)
 * [Neocons](https://clojureneo4j.info)
 * [Quartzite](https://clojurequartz.info)
 * [Titanium](https://titanium.clojurewerkz.org)

and several others.


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/elastisch.png)](http://travis-ci.org/clojurewerkz/elastisch)


## Development

### ElasticSearch Setup

Elastisch needs ElasticSearch running locally (`127.0.0.1`). `ES_CLUSTER_NAME` need to be exported
with the name of the local cluster. To find it out, use

```
curl http://localhost:9200/_cluster/nodes
```

### Leiningen

Elastisch uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

    lein2 all test

Then create a branch and make your changes on it. Once you are done
with your changes and all tests pass, submit a pull request on GitHub.



## License

Copyright (C) 2011-2013 Alex Petrov, Michael S. Klishin

Distributed under the Eclipse Public License, the same as Clojure.



## FAQ

### Will Native Java API Be Supported?

Elastisch will support ES' native (non-HTTP) Java API in one of the future
releases.
