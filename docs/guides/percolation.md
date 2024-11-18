---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Percolation"
layout: article
---

## About this guide

This guide covers ElasticSearch search capabilities in depth, explains
how Elastisch presents them in the API and how some of the key
features are commonly used. This guide covers:

 * An overview of the ElasticSearch [percolation feature](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-percolate.html)
 * What is percolation
 * How to register a query for percolation with Elastisch
 * How to perform a query percolation
 * How to unregister a query from percolation

This work is licensed under a <a rel="license"
href="http://creativecommons.org/licenses/by/3.0/">Creative Commons
Attribution 3.0 Unported License</a> (including images &
stylesheets). The source is available [on
Github](https://github.com/clojurewerkz/elastisch.docs).



## What version of Elastisch does this guide cover?

This guide covers Elastisch 2.2.x releases, including preview releases.



## Overview

ElasticSearch Percolator is a feature that lets you register a number
of queries and determine which of them match a particular
document. Percolation can be thought of as "inverse search". It is
most commonly used for features such as alerts: notifying the user
that a new document that matches particular (stored) queries is now
available.

For example, combined with ElasticSearch's support for geo queries,
this can include alerts about new properties in a particular area in a
real estate application.

Documents submitted for percolation are not indexed, only matched
against a list of pre-registered queries.


## Registering a Query with Percolator

Before using the percolation API, one or more queries needs to be
registered against the percolator. Queries are grouped by index (or,
to put it differently, each index has a percolator associated with
it). With Elastisch,
`clojurewerkz.elastisch.rest.percolation/register-query` is the
function that does that:

``` clojure
(require '[clojurewerkz.elastisch.rest.percolation :as pcl])

;; register a percolator query for the given index
(pcl/register-query conn "myapp" "sample_percolator" :query {:term {:title "search"}})
```


## Submitting Documents for Percolation

After one or more queries are registered, you can submit documents for
percolation with the `clojurewerkz.elastisch.rest.percolation/percolate` function:

``` clojure
(require '[clojurewerkz.elastisch.rest.percolation :as pcl])
(require '[clojurewerkz.elastisch.rest.response :as r])

;; register a percolator query for the given index
(pcl/register-query conn "myapp" "sample_percolator" :query {:term {:title "search"}})

;; match a document against the percolator
(let [response (pcl/percolate conn "myapp" "sample_percolator" :doc {:title "You know, for search"})]
  (println (r/ok? response))
  ;; print matches
  (println (r/matches-from response)))
```

which returns the response as an immutable Clojure map.

The `clojurewerkz.elastisch.rest.response/matches-from` function
returns names of matched queries as a collection.

`clojurewerkz.elastisch.rest.response/ok?` can be used to check if
response is successful.


## Unregistering a Query with Percolator

Queries can be unregistered with the
`clojurewerkz.elastisch.rest.percolation/unregister-query` function:

``` clojure
(require '[clojurewerkz.elastisch.rest.response :as r])

;; register a percolator query for the given index
(pcl/register-query conn "myapp" "sample_percolator" :query {:term {:title "search"}})

;; unregister the percolator
(pcl/unregister-query conn "myapp" "sample_percolator")
```


## Wrapping Up

ElasticSearch Percolator is a specialized feature that is most useful
for event notifications about index changes. It uses Lucene's powerful
querying capabilities to match documents against queries.

Elastisch follows ElasticSearch Percolation API structure closely.


## What to Read Next

The documentation is organized as [a number of
guides](/articles/guides.html), covering different topics in depth.

 * [Routing and Distribution](/articles/distribution.html)
