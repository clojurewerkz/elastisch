---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Querying"
layout: article
---

## About this guide

This guide covers ElasticSearch search capabilities in depth, explains
how Elastisch presents them in the API and how some of the key
features are commonly used. This guide covers:

 * An overview of ElasticSearch search features
 * How to perform queries with Elastisch
 * How to work with responses
 * Different kinds of queries
 * How to use highlighting
 * Other topics related to querying

This work is licensed under a <a rel="license"
href="http://creativecommons.org/licenses/by/3.0/">Creative Commons
Attribution 3.0 Unported License</a> (including images &
stylesheets). The source is available [on
Github](https://github.com/clojurewerkz/elastisch.docs).


## What version of Elastisch does this guide cover?

This guide covers Elastisch 2.2.x releases, including preview releases.


## Overview

The whole point of a search server is to be able to run search queries
against it and ElasticSearch has a lot to offer in this area.

ES supports multiple kinds of queries plus **filters**, which can be
roughly thought as conditions in the `WHERE ...` clause in SQL.
Filters do not perform relevance scoring, only decide whether a
particular document should be included or excluded from final
results. Because there is no relevance calculation involved, they are
more efficient than compound queries with additional conditions. A
good use case for filters is filtering out results just for one
customer (or organization, or any related group of documents).

"Full text" queries are analyzed just like documents are during
indexing. ElasticSearch is distributed and lets users control request
routing on per-query basis.

Queries are submitted to ElasticSearch as JSON documents with a
certain structure. With Elastisch, you can either use Clojure maps
that have exactly the same structure, or use a few helpful functions
that make queries a little bit more concise. In all cases, whenever a
query requires nesting maps, Elastisch uses exactly the same structure
as described in [ElasticSearch documentation on query
DSL](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-body.html).


## On HTTP or Native ElasticSeach Clients

### Pros and Cons

Elastisch provides both HTTP and native ElasticSearch clients.

HTTP client is easier to get started with (you don't need to know
cluster name) and will work with hosted and PaaS environments such as
Heroku or CloudFoundry. It is also known to work with a wide range of
ElasticSearch versions.

Some hosted environments do not provide access to ports other than 80
and 8080, so the native client may not work with them. The main
benefit of the native client is it's much higher throughput (up to 6-8
times on some common workloads).

The native client **requires that the version of ElasticSearch is the
same as the version of ElasticSearch client Elastisch uses
internally** (currently `1.1.x`).


### API Structure Conversion

Native client follows the same API structure but `rest` in namespace
names becomes `native`, e.g. `clojurewerkz.elastisch.rest` becomes
`clojurewerkz.elastisch.native` and
`clojurewerkz.elastisch.rest.index` becomes
`clojurewerkz.elastisch.native.index`.

Function arguments and options accepted by the native client are as
close as possible to those in the HTTP client. The native client also
provides asynchronous versions of several common operations, they
return Clojure futures instead of responses.

Query functions in the `clojurewerkz.elastisch.query` work the same way for
both clients.


## Performing queries

To perform a query with Elastisch, use the
`clojurewerkz.elastisch.rest.document/search` function. It takes index
name, mapping name and query (as a Clojure map):

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query using a convenience function
  (let [conn (esr/connect "http://127.0.0.1:9200" {:content-type :json})
        res  (esd/search conn "myapp_development" "person" :query (q/term :biography "New York"))
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

Search requests with Elastisch have exactly the same structure as JSON
documents in the [ElasticSearch Query API
guide](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-body.html)
but passed as Clojure maps. `:query`, `:sort`, `:facets` and other
keys that [ElasticSearch Search API
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-search.html)
mentions are passed as maps.

Because every search request contains query information (the `:query`
key), you can either pass an entire query as a map or use one or more
convenience functions from the `clojurewerkz.elastisch.query`
namespace (more on them later in this guide).

The example from above can also be written like so:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query using a convenience function
  (let [conn (esr/connect "http://127.0.0.1:9200" {:content-type :json})
        res  (esd/search conn "myapp_development" "person" {:query {:term {:city "New York"}}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

### Searching Using The Native Client

To query with the native client, use
`clojurewerkz.elastisch.native.document/search` and
`clojurewerkz.elastisch.native.response` functions:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.native            :as es]
            [clojurewerkz.elastisch.native.document   :as esd]
            [clojurewerkz.elastisch.query             :as q]
            [clojurewerkz.elastisch.native.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query using a convenience function
  (let [conn (es/connect [["127.0.0.1" 9300]] {"cluster.name" "your-cluster-name"})
        res  (esd/search conn "myapp_development" "person" :query (q/term :biography "New York"))
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```


### Searching Against Multiple Indexes or Mappings

To search against multiple indexes or mappings, pass them as vectors
to their respective function arguments:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query across two mapping types in the same index
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (doc/search conn "myapp_development" ["person" "checkin"] :query {:term {:city "New York"}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

To search against all mappings in an index, use
`clojurewerkz.elastisch.rest.document/search-all-types`. It works the
same as `clojurewerkz.elastisch.rest.document/search` but takes one
less argument because no need to specify mapping types:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query across all mapping types in the index
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (doc/search-all-types conn "myapp_development" :query {:term {:city "New York"}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

To search globally (across all indexes and mappings), use
`clojurewerkz.elastisch.rest.document/search-all-indexes-and-types`:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query across all indexes and mapping types. This is an expensive operation!
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (doc/search-all-indexes-and-types conn :query {:term {:city "New York"}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

Note that this operation may be **very expensive** and is generally
not recommended for medium and large data sets.

### Performing Multiple Searches Using the Multi Search API

The [multi search
API](http://www.elasticsearch.org/guide/reference/api/multi-search/)
allows you to execute several queries at once. While this doesn't mean
that Elasticsearch does less work, it will reduce the latency and thus
improve the performance of your application.

Each query must be preceded by a header which states what indices to
search on, and optionally mapping types, search type, preference and
routing.

``` clojure
(require '[clojurewerkz.elastisch.rest.multi :as multi]
         '[clojurewerkz.elastisch.query :as query])

(def queries-with-headers
  [{:index "people" :type "person"} {:query (query/match-all) :size 1}
   {:index "articles"}              {:query (query/match-all) :size 1}])

(def res (multi/search conn queries-with-headers))

(first res) ;; the result of the first query
(last res)  ;; the result of the last query
```

The result order matches the order of your queries.

## Checking results

### Using HTTP Client

Results returned by search functions have the same structure as
ElasticSearch JSON responses:

``` clojure
{:took 2, ;; how long did this request take
 ;; did the request time out?
 :timed_out false,
 ;; shard responses information
 :_shards {:total 5, :successful 5, :failed 0},
 ;; search hits information
 :hits {:total 1,
        :max_score 0.30685282,
        ;; search results
        :hits [{:_index "articles",
                :_type "article",
                ;; document id
                :_id "2",
                :_score 0.30685282,
                ;; actual document
                :_source {:latest-edit {:date "2012-03-11T02:19:00", :author "Thorwald"},
                          :number-of-edits 48,
                          :language "English",
                          :title "Apache Lucene",
                          :url "http://en.wikipedia.org/wiki/Apache_Lucene",
                          :summary "Apache Lucene is a free/open source information retrieval software library, originally created in Java by Doug Cutting. It in supported by the Apache Software Foundation and is released under the Apache Software License.",
                          :tags "technology, opensource, search, full-text search, distributed, software, lucene"}}]}}
```

Several functions in the `clojurewerkz.elastisch.rest.response`
namespace can be used to access more specific piece of information
from a response, such as total number of hits.

The most commonly used are:

 * `clojurewerkz.elastisch.rest.response/total-hits`: returns number of hits (documents found)
 * `clojurewerkz.elastisch.rest.response/hits-from`: returns a collection of hits (stored document plus id, score and index information)
 * `clojurewerkz.elastisch.rest.response/any-hits?`: returns true if there is at least one document found
 * `clojurewerkz.elastisch.rest.response/no-hits?` is a [complement function](http://clojuredocs.org/clojure_core/clojure.core/complement) to `any-hits?`
 * `clojurewerkz.elastisch.rest.response/ids-from`: returns a collection of document ids collected from hits

All of them take a response map as the only argument.

### Using The Native Client

Use `clojurewerkz.elastisch.native.response` functions instead of those in
`clojurewerkz.elastisch.rest.response`.


## Different kinds of queries

ElasticSearch is a feature rich search engine and it supports many
types of queries. Even though all queries can be passed as Clojure
maps, it is common to use convenient functions from the
`clojurewerkz.elastisch.query` to construct queries.

### Term and Terms Queries

The Term query is the most basic query type. It matches documents that
have a particular term.  A common use case for term queries is looking
up documents with unanalyzed identifiers such as usernames.

A close relative of the term query is the **terms query** which works
the same way but takes multiple term values.

With Elastisch, term query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "tweets" "tweet" :query {:term {:text "improved"}})
```

Elastisch provides a helper function for constructing term queries,
`clojurewerkz.elastisch.query/term`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "tweets" "tweet" :query (q/term :text "improved"))
```

If provided values is a collection, Elastisch will construct a terms query under the hood.


### Query String Query

The Query String (QS) query accepts text, runs it through Lucene Query
Language parser, analyzes it and performs query. It is the most
advanced query type with its own syntax, and also one of the most
commonly used thanks to the popularity of search engines like Google.

The difference with field and query string query types is that text
queries do not support field prefixes and do not attempt to Lucene
Query syntax parsing.  As such, QS queries can be seen as more
powerful and less efficient kind of text query, suitable for many apps
with technical audience.

With Elastisch, QS query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "tweets" "tweet" :query {:query_string {:query "(pineapple OR banana) dessert recipe"
                                                    :allow_leading_wildcard false
                                                    :default_operator "AND"}})
```

Elastisch provides a helper function for constructing QS text queries,
`clojurewerkz.elastisch.query/query-string`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

;; a full text query, the query text will be analyzed
(esd/search conn "tweets" "tweet" :query (q/query-string :query "(pineapple OR banana) dessert recipe"
                                                    :allow_leading_wildcard false
                                                    :default_operator "AND"))
```

For all the numerous options this query type accepts, see
[ElasticSearch documentation on the
subject](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html).


### Range Query

The Range query returns documents with fields that have numerical
values, dates or terms within a specific range. One example of such
query is retrieving all documents where the `:date` field value is
earlier than a particular moment in time, say,
`"20120801T160000+0100"`.

Range queries work for numerical values and dates the way you would
expect. For string and text fields, they match all documents with
terms in the given range (for example, `"cha"` to `"cze"`) in a
particular field.

With Elastisch, range query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:range {:age {:from 10 :to 20 :include_lower true :include_upper false}}}})
```

Elastisch provides a helper function for constructing range queries,
`clojurewerkz.elastisch.query/range`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/range :age {:from 10 :to 20 :include_lower true :include_upper false}))
```


### Boolean Query

A query that matches documents matching boolean combinations of other
queries. It is built using one or more boolean clauses, each clause
with a typed occurrence.  The occurrence types are documented on the
[ElasticSearch page on boolean
queries](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html).

With Elastisch, boolean query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:bool {:must     {:term {:user "kimchy"}}
                                             :must_not {:range {:age {:from 10 :to 20}}}
                                             :should   [{:term {:tag "wow"}}
                                                        {:term {:tag "elasticsearch"}}]
                                             :minimum_number_should_match 1}})
```

Elastisch provides a helper function for constructing boolean queries,
`clojurewerkz.elastisch.query/bool`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/bool {:must     {:term {:user "kimchy"}}
                                              :must_not {:range {:age {:from 10 :to 20}}}
                                              :should   [{:term {:tag "wow"}}
                                                         {:term {:tag "elasticsearch"}}]
                                              :minimum_number_should_match 1}))
```

`clojurewerkz.elastisch.query/bool` can be used in combination with
other query helpers, such as `clojure.elastisch.query/term`, because
they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/bool {:must     (q/term :user "kimchy")
                                              :must_not (q/range :age :from 10 :to 20)
                                              :should   [(q/term :tag "wow")
                                                         (q/term :tag "elasticsearch")]
                                              :minimum_number_should_match 1}))
```


### Filtered Query

A query that applies a filter to the results of another query. Use it
if you need to narrow down results of an existing query efficiently
but the condition you filter on does not affect relevance ranking. One
example of that is searching over accounts that are active.

With Elastisch, filtered query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-filtered-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:filtered {:query  {:term {:interests "cooking"}}
                                                 :filter {:range {:age {:from 25 :to 30}}}}})
```

Elastisch provides a helper function for constructing filtered
queries, `clojurewerkz.elastisch.query/filtered`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/filtered :query  {:term {:interests "cooking"}}
                                                 :filter {:range {:age {:from 25 :to 30}}}))
```

`clojurewerkz.elastisch.query/filtered` can be used in combination
with other query helpers, such as `clojure.elastisch.query/term`,
because they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/filtered :query  (q/term :interests "cooking")
                                                 :filter (q/range :age :from 25 :to 30)))
```


### Prefix Query

The Prefix query is similar to the term query but matches documents
that have at least one term that begins with the given prefix.  One
use case for prefix queries is providing text autocompletion results
(works best for non-analyzed fields).

With Elastisch, prefix query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "tweets" "tweet" :query {:prefix {:username "cloj"}})
```

Elastisch provides a helper function for constructing prefix queries,
`clojurewerkz.elastisch.query/prefix`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "tweets" "tweet" :query (q/prefix :username "cloj"))
```


### Wildcard Query

The Wildcard query is a generalized version of Prefix query and
usually is applicable in the same cases. Note that wildcard suffix
queries such as `"*werkz"` have very poor performance characteristics
on large data sets.

With Elastisch, wildcard query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-wildcard-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

;; please note: on large data sets, wildcard prefix queries won't perform
;; well. Prefix wildcard queries like cloj*, however, will work just fine.
(esd/search conn "tweets" "tweet" :query {:wildcard {:username "*werkz"}})
```

Elastisch provides a helper function for constructing wildcard
queries, `clojurewerkz.elastisch.query/wildcard`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

;; please note: on large data sets, wildcard prefix like *werkz queries won't perform
;; well. Prefix wildcard queries like cloj*, however, will work just fine.
(esd/search conn "tweets" "tweet" :query (q/wildcard :username "*werkz"))
```


### IDs Query

The IDs query is searches for documents by their IDs (`:_id` field
values). It is similar to `WHERE ... IN (...)` in SQL.

With Elastisch, IDs query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-ids-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

;; search for 3 tweets by their ids
(esd/search conn "tweets" "tweet" :query {:ids {:type "tweet" :values ["1" "722" "633"]}})
```

Elastisch provides a helper function for constructing IDs queries,
`clojurewerkz.elastisch.query/ids`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

;; search for 3 tweets by their ids
(esd/search conn "tweets" "tweet" :query (q/ids "tweet" ["1" "722" "633"]))
```


### Match All Query

The Match All query does what it sounds like: matches every single
document in the index. Used almost exclusively during development or
in combination with other queries in compound queries (e.g. filtered).

With Elastisch, match-all query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-all-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:match_all {:boost 1.2}})
```

Elastisch provides a helper function for constructing match-all
queries, `clojurewerkz.elastisch.query/match-all`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/match-all))
```

An example of match-all query being used as part of a filtered query
to find all people in a particular age bracket:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/filtered :query  (q/match-all)
                                                 :filter (q/range :age :from 25 :to 30)))
```


### Dis-Max Query

Dis-Max or Disjunction Max query is a compound query where only the
max score clause is used for ranking (as opposed to boolean queries,
where scores are combined).  This is useful when searching for a word
in multiple fields with different boost factors (so that the fields
cannot be combined equivalently into a single search field).

Like other compound queries, Dis-Max returns the union of documents
produced by subqueries.

With Elastisch, dis-max query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-dis-max-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "venues" "venue" :query {:dis_max {:queries [{:field {"address.street" "Lafayette"}}
                                                         {:field {"description"    "Lafayette"}}]
                                               :tie_breaker 1.5}})
```

Elastisch provides a helper function for constructing dis-max queries,
`clojurewerkz.elastisch.query/dis-max`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "venues" "venue" :query {:dis_max {:queries [{:field {"address.street" "Lafayette"}}
                                                         {:field {"description"    "Lafayette"}}]
                                               :tie_breaker 1.5}})
```

`clojurewerkz.elastisch.query/dis-max` can be used in combination with other query helpers, such as `clojure.elastisch.query/field`, because they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "venues" "venue" :query (q/dis-max :queries [(q/field "address.street" "Lafayette")
                                                         (q/field "description"    "Lafayette")]
                                               :tie_breaker 1.5})
```


### Boosting Query

Boosting queries are used to demote results that match a particular
query. For example, when searching for `"Berlin"`, most likely the
intent is to find information about Berlin in Germany, not one of the
towns in North America. Boosting queries can be used to lower
relevancy of some documents without affecting scoring of the most.

With Elastisch, boosting query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:boosting {:positive {:term {:city "Berlin"}}
                                                 :negative {:term {:country "USA"}}
                                                 :negative_boost 0.5}})
```

Elastisch provides a helper function for constructing boosting
queries, `clojurewerkz.elastisch.query/boosting`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/boosting {:positive {:term {:city "Berlin"}}
                                                  :negative {:term {:country "USA"}}
                                                  :negative_boost 0.5}))
```

`clojurewerkz.elastisch.query/boosting` can be used in combination
with other query helpers, such as `clojure.elastisch.query/term`,
because they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/boosting {:positive (q/term :city "Berlin")
                                                  :negative (q/term :country "USA")
                                                  :negative_boost 0.5}))
```


### More Like This Query

More Like This (MLT) query find documents that are “like” provided
text by running it against one or more fields.

With Elastisch, More Like This query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:more_like_this {:fields ["name.first" "name.last"]
                                                       :like_text "Ryan"
                                                       :min_term_freq 1
                                                       :max_query_terms 12}})
```

Elastisch provides a helper function for constructing MLT queries,
`clojurewerkz.elastisch.query/mlt`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/mlt :fields ["name.first" "name.last"]
                                            :like_text "Ryan"
                                            :min_term_freq 1
                                            :max_query_terms 12))
```


### More Like This Field Query

More Like This Field is very similar to the More Like This query but
operates on a single field.

With Elastisch, More Like This query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-field-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:more_like_this_field {:last_name {:like_text "Ryan"
                                                                         :min_term_freq 1
                                                                         :max_query_terms 12}}})
```

Elastisch provides a helper function for constructing MLT field
queries, `clojurewerkz.elastisch.query/mlt-field`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/mlt-field :last_name {:like_text "Ryan"
                                                              :min_term_freq 1
                                                              :max_query_terms 12}))
```


### Fuzzy Query

A fuzzy based query that uses similarity based on Levenshtein (edit
distance) algorithm. **Warning**: this query uses prefix length of 0
by default.  This will cause a full scan on all terms.

With Elastisch, fuzzy query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:fuzzy {:user {:value "ki"
                                                     :boost 1.2
                                                     :min_similarity 0.5
                                                     :prefix_length 0}}})
```

Elastisch provides a helper function for constructing fuzzy queries,
`clojurewerkz.elastisch.query/fuzzy`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/fuzzy :user {:value "ki"
                                                     :boost 1.2
                                                     :min_similarity 0.5
                                                     :prefix_length 0}))
```


### Fuzzy Like This Query

A cross between Fuzzy and More Like This queries.

With Elastisch, Fuzzy Like This (FLT) query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-like-this-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:fuzzy_like_this {:fields ["name.first" "name.last"]
                                                        :like_text "Ryan"
                                                        :max_query_terms 12}})
```

Elastisch provides a helper function for constructing FLT queries,
`clojurewerkz.elastisch.query/fuzzy-like-this`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/fuzzy-like-this :fields ["name.first" "name.last"]
                                                        :like_text "Ryan"
                                                        :max_query_terms 12))
```


### Fuzzy Like This Field Query

Same as FTL query but works over a single field.

With Elastisch, Fuzzy Like This Field query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-like-this-field-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:fuzzy_like_this_field {"name.first" {:like_text "Michael"
                                                                            :max_query_terms 12}}})
```

Elastisch provides a helper function for constructing Fuzzy Like This
Field queries, `clojurewerkz.elastisch.query/fuzzy-like-this-field`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/fuzzy-like-this-field "name.first" {:like_text "Ryan"
                                                                            :max_query_terms 12}))
```


### Nested Query

Nested query allows to query nested objects/documents. The query is
executed against the nested objects as if they were indexed as
separate docs. The root parent document is returned.

With Elastisch, Nested query structure is the same as described in the
[ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-nested-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:nested {:path "obj1"
                                               :score_mode "avg"
                                               :query {:bool {:must [{:text  {"obj1.name" "blue"}}
                                                                     {:range {"obj1.count" {:gt 5}}}]}}}})
```

Elastisch provides a helper function for constructing nested queries,
`clojurewerkz.elastisch.query/nested`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/nested :path "obj1"
                                               :score_mode "avg"
                                               :query {:bool {:must [{:text  {"obj1.name" "blue"}}
                                                                     {:range {"obj1.count" {:gt 5}}}]}}))
```

`clojurewerkz.elastisch.query/nested` can be used in combination with
other query helpers, such as `clojure.elastisch.query/term`, because
they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/nested :path "obj1"
                                               :score_mode "avg"
                                               :query {:bool {:must [(q/term "obj1.name" "blue")
                                                                     (q/range "obj1.count" :gt 5)]}}))
```


### Span First Query

Matches spans near the beginning of a field.

With Elastisch, Span First query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-first-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:span_first {:match {:span_term {:user "kimchy"}}
                                                   :end   3}}})
```

Elastisch provides a helper function for constructing Span First
queries, `clojurewerkz.elastisch.query/span-first`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/span-first :match {:span_term {:user "kimchy"}}
                                                   :end   3))
```


### Span Near Query

Matches spans which are near one another. One can specify slop, the
maximum number of intervening unmatched positions, as well as whether
matches are required to be in-order.

With Elastisch, Span Near query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-near-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:span_near {:clauses [{:span_term {:field "value1"}}
                                                            {:span_term {:field "value2"}}
                                                            {:span_term {:field "value3"}}
                                                            {:span_term {:field "value4"}}]
                                                  :slop 12
                                                  :in_order false
                                                  :collect_payloads false}})
```

Elastisch provides a helper function for constructing Span Near
queries, `clojurewerkz.elastisch.query/span-near`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/span-near :clauses [{:span_term {:field "value1"}}
                                                            {:span_term {:field "value2"}}
                                                            {:span_term {:field "value3"}}
                                                            {:span_term {:field "value4"}}]
                                                  :slop 12
                                                  :in_order false
                                                  :collect_payloads false))
```


### Span Not Query

Removes matches which overlap with another span query.

With Elastisch, Span Not query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-not-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:span_not {:include {:span_term {:field1 "value1"}}
                                                 :exclude {:span_term {:field1 "value2"}}})
```

Elastisch provides a helper function for constructing Span Not
queries, `clojurewerkz.elastisch.query/span-not`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/span-not :include {:span_term {:field1 "value1"}}
                                                 :exclude {:span_term {:field1 "value2"}}))
```


### Span Or Query

Matches the union of its span clauses.

With Elastisch, Span Or query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-or-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:span_or {:clauses [{:span_term {:field "value1"}}
                                                          {:span_term {:field "value2"}
                                                          {:span_term {:field "value3"}]}})
```

Elastisch provides a helper function for constructing Span Or queries,
`clojurewerkz.elastisch.query/span-or`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/span-or :clauses [{:span_term {:field "value1"}}
                                                          {:span_term {:field "value2"}
                                                           {:span_term {:field "value3"}]))
```


### Span Term Query

Matches spans containing a term.

With Elastisch, Span Term query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-term-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:span_term {:user "kimchy"}})
```

Elastisch provides a helper function for constructing Span Term
queries, `clojurewerkz.elastisch.query/span-term`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/span-term :user :kimchy))
```


### Indices Query

Indices Query executes different queries against different indexes and
combine the results.

With Elastisch, indices query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-indices-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:indices {:indices ["index1" "index2"]
                                                :query   {:term {:city "Berlin"}}
                                                :no_match_query {:term {:country "USA"}}}})
```

Elastisch provides a helper function for constructing fuzzy queries,
`clojurewerkz.elastisch.query/indices`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/indices :indices ["index1" "index2"]
                                                :query   {:term {:city "Berlin"}}
                                                :no_match_query {:term {:country "USA"}}))
```


### Top Children Query

Top Children Query performs a query against child documents and
aggregates scores of hits into the parent document.

With Elastisch, Top Children query structure is the same as described
in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-top-children-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:top_children {:type "blog_tag"
                                                     :query {:term {:tag "cooking"}}
                                                     :score "max"
                                                     :factor 5
                                                     incremental_factor 2}})
```

Elastisch provides a helper function for constructing Top Children
queries, `clojurewerkz.elastisch.query/top-children`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query (q/top-children :type "blog_tag"
                                                     :query {:term {:tag "cooking"}}
                                                     :score "max"
                                                     :factor 5
                                                     incremental_factor 2))
```


### Has Child Query

Has Child Query returns parent documents that have child documents of
the given type.

With Elastisch, Has Child query structure is the same as described in
the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:has_child {:type "blog_tag"
                                                  :query {:term {:tag "cooking"}}}})
```

Elastisch provides a helper function for constructing Has Child queries, `clojurewerkz.elastisch.query/has-child`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query (q/has-child :type "blog_tag"
                                                  :query {:term {:tag "cooking"}}))
```


### Constant Score Query

A query that wraps a filter or another query and simply returns a
constant score equal to the query boost for every document in the
filter.  The filter object can hold only filter elements, not
queries. Filters can be much faster compared to queries since they
don’t perform any scoring, especially when they are cached.

With Elastisch, Constant Score query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-constant-score-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:constant_score {:filter {:term {:user "happyjoe"}}
                                                       :boost 1.2}})
```

Elastisch provides a helper function for constructing Constant Score
queries, `clojurewerkz.elastisch.query/constant-score`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query (q/constant-score :filter {:term {:user "happyjoe"}}
                                                       :boost 1.2))
```


### Custom Score Query

Custom Score query allows to wrap another query and customize the
scoring of it by providing a script expression.

With Elastisch, Custom Score query structure is the same as described
in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-custom-score-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:custom_score {:query {:term {:city "Moscow"}}
                                                     :script "_score * doc['upvotes'].value"}})
```

Elastisch provides a helper function for constructing Custom Score
queries, `clojurewerkz.elastisch.query/custom-score`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query (q/custom-score :query {:term {:city "Moscow"}}
                                                     :script "_score * doc['upvotes'].value"))
```


### Custom Filters Score Query

A Custom Filters Score query allows to execute a query, and if the hit
matches a provided filter (ordered), use either a boost or a script
associated with it to compute the score.  This kind of query allows
for very efficient parametrized scoring because filters do not perform
any scoring and their results can be cached.

With Elastisch, Custom Filters Score query structure is the same as
described in the [ElasticSearch query DSL
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-custom-filters-score-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:custom_filters_score {:query {:match_all {}}
                                                             :filters [{:filter {:range {:age {:from 26 :to 30}}}
                                                                        :boost 3}
                                                                       {:filter {:range {:age {:from 31 :to 35}}}
                                                                        :boost 5}]
                                                             :score_mode "first"}})
```

Elastisch provides a helper function for constructing Custom Filters
Score queries, `clojurewerkz.elastisch.query/custom-filter-score`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query (q/custom-filters :query {:match_all {}}
                                                       :filters [{:filter {:range {:age {:from 26 :to 30}}}
                                                                  :boost 3}
                                                                 {:filter {:range {:age {:from 31 :to 35}}}
                                                                  :boost 5}]
                                                       :score_mode "first"))
```


## Scrolling (Pagination) of Search Results

Search queries can potentially return many documents. Retrieval of
documents in a result set in chunks, commonly known as pagination, is
called **scrolling* in ElasticSearch parlance.


### Using HTTP Client

To scroll search results using HTTP API client, use
`clojurewerkz.elastisch.rest.document/scroll` or
`clojurewerkz.elastisch.rest.document/scroll-seq`. The former requires
you to first obtain a **scroll id** (cursor id) from a response:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.rest.response :refer [hits-from]])

(let [scroll-id  (:_scroll_id response)
      next-page  (doc/scroll conn scroll-id :scroll "1m")]
  (hits-from next-page))
```

`scroll-seq` is more convenient: it takes a response and produces a
lazy sequence of hits in the entire result set:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])

(doc/scroll-seq conn
  (doc/search conn index-name mapping-type
              :query (q/term :title "Emptiness")
              :search_type "query_then_fetch"
              :scroll "1m"
              :size 2))
;= lazy sequence of hits
```


### Using The Native Client

Scrolling with Elastisch's native client is effectively the same as
with the HTTP one.  The only difference is that you use
`clojurewerkz.elastisch.native.document/scroll-seq` and
`clojurewerkz.elastisch.native.document/scroll`.



## Filters

Often search results need to be filtered (scoped): for example, to
make sure results only contain documents that belong to a particular
user account or organization. Such filtering conditions do not play
any role in the relevance ranking and just used as a way of excluding
certain documents from search results.

*Filters* is an ElasticSearch feature that lets you decide what
documents should be included or excluded from a results of a
query. Filters are similar to the way term queries work but because
filters do not participate in document ranking, they are significantly
more efficient. Furthermore, filters can be cached, improving
efficiency even more.

To specify a filter, pass the `:filter` option to
`clojurewerkz.elastisch.rest.document/search`:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a search query that returns results filtered on location type
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (doc/search conn "myapp_development" "location"
                         :query (q/query-string :biography "New York OR Austin")
                         :filter {:term {:kind "hospital"}})
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

ElasticSearch provides many filters out of the box.

### Term and Terms Filter

Term filter is very similar to the Term query covered above but like
all filters, does not contribute to relevance scoring and is more
efficient. Terms filter works the same way but for multiple terms.

With Elastisch, term filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "location"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:term {:kind "hospital"}})
```

### Range Filter

Range filter filters documents out on a range of values, similarly to
the Range query. Supports numerical values, dates and strings.

With Elastisch, range filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "person"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:range {:age {:from 25 :to 30}}})
```

### Exists Filter

Exists filter filters documents that have a specific field set. This
filter always uses caching.

With Elastisch, Exists filter structure is the same as described in
the [ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-exists-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "location"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:exists {:field :open_roof}})
```

### Missing Filter

Exists filter filters documents that do not have a specific field set,
that is, the opposite of the Exists filter.

With Elastisch, Missing filter structure is the same as described in
the [ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-missing-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "location"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:missing {:field :under_construction}})
```

### And Filter

The And filter matches documents using `AND` boolean operator on
multiple subqueries. This filter does not use caching by default.

With Elastisch, And filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-and-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term {"name.first" "Shay"}}
                               :filter {:and {:filters [{:range  {:post_date {:from "2010-03-01"
                                                                              :to   "2010-04-01"}}}
                                                        {:prefix {"name.second" "Ba"}}]}}))
```

### Or Filter

The Or filter is similar to the And filter but matches documents using
`OR` boolean operator on multiple subqueries. It does not use caching
by default.

With Elastisch, Or filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-or-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term {"name.first" "Shay"}}
                               :filter {:or {:filters [{:term {"name.second"   "Banon"}}
                                                       {:term {"name.nickname" "kimchy"}}]}}))
```

### Not Filter

The Not filter filters out document that match its subquery. This
filter does not use caching by default.

With Elastisch, Not filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-not-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term {"name.first" "Shay"}}
                               :filter {:not {:range  {:post_date {:from "2010-03-01"
                                                                   :to   "2010-04-01"}}}}))
```

### Bool Filter

The Bool filter matches documents using boolean combinations of its
subqueries. Similar in concept to the Boolean query, except that the
clauses are other filters.

With Elastisch, Bool filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term {"name.first" "Shay"}}
                               :filter {:bool {:must {:range  {:post_date {:from "2010-03-01"
                                                                           :to   "2010-04-01"}}}
                                               :must_not {:prefix {"name.second" "Ba"}}}}))
```

### Limit Filter

The Limit filter limits the number of documents (per shard) that are
taken for ranking.

With Elastisch, Limit filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-limit-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term  {"name.first" "Shay"}}
                               :filter {:limit {:value 100}}))
```

### Type Filter

This filter filters out documents based on their `_type` field
value. It can work even if the `_type` field is not indexed.

With Elastisch, Type filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-type-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term  {"name.first" "Shay"}}
                               :filter {:type  {:value "draft"}}))
```

### Prefix Filter

This filter matches documents with fields that have terms starting
with the given prefix (**not analyzed**).

With Elastisch, Prefix filter structure is the same as described in
the [ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-prefix-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:term  {"name.first" "Shay"}}
                               :filter {:prefix {"name.second" "Ba"}}))
```


### Geo Distance Filter

Allows to filter hits based on a point location using a bounding box
(the `pin.location` nested attribute in this example).

With Elastisch, And filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:match_all {}}
                               :filter {:geo_bounding_box {"pin.location" {:top_left     {:lat 40.73
                                                                                          :lon -74.1}
                                                                           :bottom_right {:lat 40.717
                                                                                          :lot -73.99}}}}))
```


### Geo Distance Range Filter

Filters documents that include only hits that exists within a specific
distance from a geo point.

With Elastisch, And filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-range-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "posts" "post"
            :query (q/filtered :query  {:match_all {}}
                               :filter {:geo_distance {:distance      "200km"
                                                       "pin.location" {:lat 40.73
                                                                       :lon -74.1}}}))
```


### Geo Polygon Filter

Filters documents that exists within a range from a specific point.

With Elastisch, And filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-range-filter.html):

``` clojure
(esd/search conn "posts" "post"
            :query (q/filtered :query  {:match_all {}}
                               :filter {:geo_distance_range {:from          "200km"
                                                             :to            "400km"
                                                             "pin.location" {:lat 40.73
                                                                             :lon -74.1}}}))
```


### Geo Bounding Box Filter

A filter allowing to include hits that only fall within a polygon of
points.

With Elastisch, And filter structure is the same as described in the
[ElasticSearch Filter
documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-range-filter.html):

``` clojure
(esd/search conn "posts" "post"
            :query (q/filtered :query  {:match_all {}}
                               :filter {:geo_polygon {"person.location" {:points [{:lat 40 :lon -70}
                                                                                  {:lat 30 :lon -80}
                                                                                  {:lat 20 :lon -90}]}}}))
```


### Filter Caching

Filters are often good candidates for caching that improves their
performance further. Some filter types use caching by default:

 * term/terms
 * prefix
 * range
 * exists

Others are not cached by default:

 * numeric_range
 * script
 * various geo filters
 * compound filters (and, or, not)

It is possible to use `_cache` and `_cache_key` parameters to control
caching behavior: disable caching or use custom cache key.

For more information, see [Filters and
Caching](http://www.elasticsearch.org/guide/reference/query-dsl/)in
ElasticSearch documentation.



## Highlighting

Having search matches highlighted in the UI is very useful in many
cases. ElasticSearch can highlight matched in search results. To
enable highlighting, use the `:highlight` option
`clojurewerkz.elastisch.rest.document/search` accepts. In the example
above, search matches in the `biography` field will be highlighted
(wrapped in `em` tags) and search hits will include one extra
"virtual" field called `:highlight` that includes the highlighted
fields and the highlighted fragments that can be used by your
application.

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a search query with highlighting over the biography field
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person"
                         :query (q/query-string :biography "New York OR Austin")
                         :highlight {:fields {:biography {}}})
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

More examples can be found in this [ElasticSearch documentation
section on highlighting
fields](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-highlighting.html).
`:highlight` values that Elastisch accepts are structured exactly the
same as JSON documents in that section.

For example, to override highlighting tags (`em` by default):

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; uses custom highlighting tags
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person"
                         :query (q/query-string :biography "New York OR Austin")
                         :highlight {:fields {:biography {}}
                                     :pre_tags  ["<span class='highlighted'>"]
                                     :post_tags ["</span>"]})
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```



## Query Examples

### Term Query

Given an index with the following mapping type:

``` clojure
{:tweet {:properties {:username  {:type "string" :index "not_analyzed"}
                      :text      {:type "string" :analyzer "standard"}
                      :timestamp {:type "date" :include_in_all false :format "basic_date_time_no_millis"}
                      :retweets  {:type "integer" :include_in_all false}
                      :promoted  {:type "boolean" :default false :boost 10.0 :include_in_all false}
                      :location  {:type "object" :include_in_all false :properties {:country {:type "string" :index "not_analyzed"}
                                                                                    :state   {:type "string" :index "not_analyzed"}
                                                                                    :city    {:type "string" :index "not_analyzed"}}}}}}
```

and indexed documents

``` clojure
{:username  "clojurewerkz"
 :text      "Elastisch beta3 is out, several more @elasticsearch features supported github.com/clojurewerkz/elastisch, improved docs http://clojureelasticsearch.info #clojure"
 :timestamp "20120802T101232+0100"
 :retweets  1
 :location  {:country "Russian Federation"
             :state   "Moscow"
             :city    "Moscow"}}


{:username  "ifesdjeen"
 :text      "Did I mention that Glitch Mob is amazing?"
 :timestamp "20120801T174722+0100"
 :retweets  0
 :location  {:country "Germany"
             :state   "Bavaria"
             :city    "Munich"}}

{:username  "michaelklishin"
 :text      "I am late to the party but congrats to both @old_sound and VMware on getting him on the team"
 :timestamp "20120731T223900+0300"
 :retweets  2
 :location  {:country "Russian Federation"
             :state   "Moscow"
             :city    "Moscow"}}
```

The following term query

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "tweets" "tweet" :query (q/term :text "improved"))
```

Will return the 1st document in hits and

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "tweets" "tweet" :query (q/term :text ["supported" "improved"]))
```

will also return the 1st document.


### Prefix Query

TBD


### Query String Query

TBD


### Range Query

TBD


### Boolean Query

TBD




## Validating Queries

ElasticSearch provides an API operation that validates queries without
executing them. Elastisch exposes it as the
`clojurewerkz.elastisch.rest.document/validate-query` function:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query :as q])
(require '[clojurewerkz.elastisch.rest.response :as r])

(let [response (doc/validate-query conn "myproduct_development" (q/field "latest-edit.author" "Thorwald") :explain true)]
  (println response)
  (println (r/valid? response)))
```

Note that unlike `clojurewerkz.elastisch.rest.document/search`, this
function does not take mapping type as a parameter.

Query Validation requests with Elastisch have exactly the same
structure as JSON documents in the [ElasticSearch Validation API
guide](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-validate.html)
but passed as Clojure maps.



## Wrapping Up

ElasticSearch querying capabilities are just as rich as the indexing
ones. With multiple kinds of queries, filtering, ability to query
multiple indexes or mapping types at once and features like ad-hoc
boosting, you have plenty of tools and knobs for making search work
exactly the way your domain model requires.

Elastisch follows ElasticSearch REST API structure (for example, the
query DSL) and is strives to be as feature complete as possible when
it comes to querying.


## What to Read Next

The documentation is organized as [a number of guides](/articles/guides.html), covering different topics in depth:

 * [Aggregation](/articles/aggregation.html)
 * [Facets](/articles/facets.html)
 * [Percolation](/articles/percolation.html)
 * [Routing and Distribution](/articles/distribution.html)
