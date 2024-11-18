---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Aggregation"
layout: article
---

## About this guide

This guide covers ElasticSearch [aggregation
feature](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html),
explains how Elastisch presents them in the API and how some of the
key features are commonly used. This guide covers:

 * An overview of ElasticSearch aggregation
 * How to perform aggregation queries with Elastisch
 * How to work with responses
 * Different kinds of aggregations

This work is licensed under a <a rel="license"
href="http://creativecommons.org/licenses/by/3.0/">Creative Commons
Attribution 3.0 Unported License</a> (including images &
stylesheets). The source is available [on
Github](https://github.com/clojurewerkz/elastisch.docs).


## What version of Elastisch does this guide cover?

This guide covers Elastisch 2.2.x releases, including preview releases.


## Overview

Aggregate functions compute a single result from a set of
documents. Naturally in ElasticSearch the input documents are
retrieved using search queries. Aggregation queries (also known as
"aggregations") can be used to do the following (some examples):

 * Calculate maximum/minimum/average value of a field (e.g. product price) from a set of documents
 * Calculate range and percentiles value of a field
 * Retrieve a set of terms in a field
 * Produce a histogram of numerical values

and so on.

In the API, aggregations piggyback on search queries. The documents returned
by the query are aggregation inputs.


## On HTTP or Native ElasticSeach Clients

Aggregations are currently only supported in the REST client.


## Performing queries

### Using HTTP Client

To perform a query with Elastisch, use the
`clojurewerkz.elastisch.rest.document/search` function and pass it an
aggregation map. For convenience, aggregation maps can be created
using functions from `clojurewerkz.elastisch.aggregation` (similar to
`clojurewerkz.elastisch.query`):

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.aggregation   :as a]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query using a convenience function
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person" {:query (q/term :biography "New York")
                                                       :aggregations {:avg_age (a/avg "age")})]
    (println (esrsp/aggregation-from res))))
```

## Checking results

### Using HTTP Client

Results returned by search functions have the same structure as
ElasticSearch JSON responses.

`clojurewerkz.elastisch.rest.response/aggregations-from`
is a convenience functions for accessing aggregation response:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query         :as q])
(require '[clojurewerkz.elastisch.aggregation   :as a])
(require '[clojurewerkz.elastisch.rest.response :as esrsp])

(let [res (esd/search conn "myapp_development" "person" {:query (q/term :biography "New York")
                                                    :aggregations {:avg_age (a/avg "age")})]
  (esrsp/aggregation-from res))
```

## Wrapping Up

ElasticSearch supports multiple aggregation functions that piggyback on top of
search queries.

Elastisch follows ElasticSearch REST API structure (for example, the
query DSL) and is strives to be as feature complete as possible when
it comes to querying.


## What to Read Next

The documentation is organized as [a number of
guides](/articles/guides.html), covering different topics in depth:

 * [Facets](/articles/facets.html)
 * [Percolation](/articles/percolation.html)
 * [Routing and Distribution](/articles/distribution.html)
