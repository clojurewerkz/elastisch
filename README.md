# Elastisch, a Clojure client for Elasticsearch

Elastisch is a minimalistic [Clojure client for
Elasticsearch](http://clojureelasticsearch.info), a modern distributed search engine.


## Project Goals

 * Be reasonably feature complete
 * Be [well documented](http://clojureelasticsearch.info)
 * Be [well tested](https://github.com/clojurewerkz/elastisch/tree/master/test)
 * Closely follow [Elasticsearch API structure](http://www.elasticsearch.org/guide/reference/api/), no new abstractions introduced
 * Support multiple transports: HTTP, native ES client, possibly more (e.g. Memcached)
 * Follow recent Elasticsearch releases & developments


## Community

[Elastisch has a mailing
list](https://groups.google.com/forum/#!forum/clojure-elasticsearch). Feel
free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so
on, please follow [@ClojureWerkz](https://twitter.com/#!/clojurewerkz)
on Twitter.



## Project Maturity

Elastisch is a not a young project. Started in late 2011, it has been used
in production since the early days. Elastisch API is stable. 1.0 was
released in September 2012, 2.0 in June 2014, 3.0 in December 2018.


## Supported ElasticSearch Versions

Elastisch 6.x (master) targets ElasticSearch 6.x (and 5.x to the extent possible).

Elastisch 3.x focuses on Elasticsearch 2.x support.


## Artifacts

Elastisch artifacts are [released to Clojars](https://clojars.org/clojurewerkz/elastisch).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent 3.x Release

Note: Elastisch documentation is not currently updated for version 3.0.

With Leiningen:

    [clojurewerkz/elastisch "3.0.1"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch</artifactId>
      <version>3.0.1</version>
    </dependency>


## Documentation & Examples

Please see our [documentation guides site](http://clojureelasticsearch.info/).
Note: Elastisch documentation is not currently updated for version 3.0.

Our [test suite](https://github.com/clojurewerkz/elastisch/tree/master/test/clojurewerkz/elastisch) also has many code examples.



## Supported Clojure Versions

Elastisch requires Clojure 1.6.
The most recent stable release is highly recommended.

## Supported Elasticsearch Versions

Elastisch 2.1 and 2.2 target Elasticsearch 1.x starting with 1.1 and through 1.7.x.
Elastisch **master** provides Elasticsearch 3.x compatibility but not
every ElasticSearch 3.0+ feature.

ElasticSearch 5.0 support will be available in a future version.

## REST and Native Clients

Elastisch provides HTTP and native (transport) clients with nearly
identical API as of `1.1.0`.



## Elastisch Is a ClojureWerkz Project

Elastisch is part of the group of [Clojure libraries](http://clojurewerkz.org) known as ClojureWerkz, together with

 * [Langohr](http://clojurerabbitmq.info)
 * [Cassaforte](http://clojurecassandra.info)
 * [Monger](http://clojuremongodb.info)
 * [Neocons](http://clojureneo4j.info)
 * [Quartzite](http://clojurequartz.info)
 * [Titanium](http://titanium.clojurewerkz.org)

and several others.


## Continuous Integration

[![Continuous Integration status](https://secure.travis-ci.org/clojurewerkz/elastisch.png)](http://travis-ci.org/clojurewerkz/elastisch)
[![Dependencies Status](https://versions.deps.co/clojurewerkz/elastisch/status.svg)](https://versions.deps.co/clojurewerkz/elastisch)

## Development

### Elasticsearch Setup

Elastisch needs Elasticsearch running locally (`127.0.0.1`). `ES_CLUSTER_NAME` need to be exported
with the name of the local cluster. To find it out, use

```
curl http://localhost:9200/_nodes/cluster
```

### Running Tests

To run Elastisch tests, make sure you have Elasticsearch running. It must be the same
version that Elastisch depends on due to binary protocol changes between releases.
[project.clj](project.clj) or [project page on Clojars](https://clojars.org/clojurewerkz/elastisch)
can help find that out.

Finally, run all tests with

    lein all test

To run only HTTP client tests, use

    lein all test :ci


### Leiningen

Elastisch uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against all supported Clojure versions using

    lein all test

Then create a branch and make your changes on it. Once you are done
with your changes and all tests pass, submit a pull request on GitHub.



## License

Copyright (c) 2011-2019 Alex Petrov, Michael S. Klishin, and the ClojureWerkz Team.

Double-licensed under the [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) and
[Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
