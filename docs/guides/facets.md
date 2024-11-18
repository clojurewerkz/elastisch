---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Using ElasticSearch Facets with Elastisch and Clojure"
layout: article
---

## About this guide

This guide covers ElasticSearch search faceting capabilities and
explains how Elastisch presents them in the API:

 * An overview of faceted search
 * How to perform queries with facets
 * Other topics related to facets

This work is licensed under a <a rel="license"
href="http://creativecommons.org/licenses/by/3.0/">Creative Commons
Attribution 3.0 Unported License</a> (including images &
stylesheets). The source is available [on
Github](https://github.com/clojurewerkz/elastisch.docs).


## What version of Elastisch does this guide cover?

This guide covers Elastisch 2.2.x releases, including preview releases.



## Overview

Faceted search (also called *faceted navigation*) is a feature that
lets the user refine search results using data from [faceted
classification](http://en.wikipedia.org/wiki/Faceted_classification)
(categorization or labelling).

It is best demonstrated with an example from a real Web site (amazon.com):

![Faceted Search Example](https://img.skitch.com/20120831-crq5j2ea2yyawbgmgrx7bcjkyx.jpg)

Here a search for "Information Retrieval" in the books section
produces 993 results, 12 of which are displayed on the first page
(clipped). The left hand side has a **faceted navigation** UI that
lets us refine the query:

 * View only books released in the last 30 days
 * View only unreleased books
 * View results across deparments (in this case, Kindle Store has only one department to offer)
 * Filter out results that have a certain minimum average custom review rating

This feature is useful when dealing with large amounts of data that
can be easily classified by different criteria: products in online
stores, correspondence (emails), legal documents, geo and location
data are just a few examples.

ElasticSearch lets you build faceted search for your applications
using a feature commonly referred to as *facets* or
*faceting*. Because facets are additional information to a query,
ElasticSearch implements them as a feature piggiebacked on top of
search queries: a few additional parameters are specified with a
search query and ElasticSearch returns information relevant for
building faceted navigation with the query response.

The field used for facet calculations must be of type numeric,
date/time or be analyzed as a single token (see the [Indexing
guide](/articles/indexing.html) on that).  ElasticSearch supports
multiple [types of
facets](http://www.elasticsearch.org/guide/reference/api/search/facets/):

 * [Terms](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-terms-facet.html)
 * [Range](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-range-facet.html)
 * [Histogram](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-histogram-facet.html)
 * [Date Histogram](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-date-histogram-facet.html)
 * [Filter](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-filter-facet.html)
 * [Query](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-query-facet.html)
 * [Statistical](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-statistical-facet.html)
 * [Terms Stats](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-terms-stats-facet.html)
 * [Geo Distance](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets-geo-distance-facet.html)

Next we will see how it works with Elastisch.


## Performing Queries with Faceting

Facets piggyback on search queries and as such, performed with the
`clojurewerkz.elastisch.rest.document/search` function discussed in
the [Querying guide](/articles/querying.html).

With Elastisch, facet query structure is the same as described in the
[ElasticSearch
documentation](http://www.elasticsearch.org/guide/reference/api/search/facets/):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query         :as q])

(doc/search conn "articles" "article" :query (q/match-all) :facets {:tags {:terms {:field :tags}}})
```

You can give the facet a custom name (in this example, *tags*) and
return multiple facets in a single request.

To retrieve the results, access the `:facets` key on the response
(which is just a Clojure map with Elastisch). It will contain a map of
facets:

``` clojure
{:tags {:_type "terms"
        :missing 0
        :total 26
        :other 6
        :terms [{:term "text" :count 2}
                {:term "technology" :count 2}
                {:term "software" :count 2}
                {:term "search" :count 2}
                {:term "opensource" :count 2}
                {:term "norteamérica" :count 2}
                {:term "lucene" :count 2}
                {:term "historia" :count 2}
                {:term "geografía" :count 2}
                {:term "full" :count 2}]}}
```

The exact structure will vary between different facet types.


## Facet Scope

Facets can have *scope*. By default, facet computation is restricted
to the scope of the current query (the so called `main` scope). It is
possible to use `global` scope, in which case it will return values
computed across all documents in the index:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query         :as q])

(doc/search conn "articles" "article" :query (q/query-string :query "T*") :facets {:tags {:terms {:field :tags} :global true}})
```

Different facets in a query can use different scopes if needed.


## Facet Filters

All facets can be configured with an additional filter, which will
reduce the documents they use for computing results. This is very
similar in purpose to query filters:

 * You need to filter out some results
 * You need to narrow results to a particular account

Facet filters can, for example, help you make sure results only
contain documents that belong to a particular user account or
organization.

In the example below we use a filter to limit facets to articles
published in a particular time period (range of years):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query         :as q])

(doc/search conn "articles" "article" :query (q/query-string :query "T*") :facets {:tags {:terms {:field :tags}
                                                                                   :facet_filter {:range {:year {:from 1990 :to 2012}}}}})
```

Different kinds of filters and their structure are described in the
[Querying guide](/articles/querying.html). Their structure is the same
for queries and facets.


## Wrapping Up

Faceted search can be a very useful feature for . ElasticSearch has
very good faceted search support with multiple kinds of facets,
scoping and filtering.  Facets support sits on top of search
queries. With Elastisch facet requests have exactly the same structure
as JSON in the ElasticSearch documentation.


## What to Read Next

The documentation is organized as [a number of guides](/articles/guides.html), covering different topics in depth:

 * [Percolation](/articles/percolation.html)
 * [Routing and Distribution](/articles/distribution.html)
