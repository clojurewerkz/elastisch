---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Getting Started with ElasticSearch and Clojure"
layout: article
---

## About this guide

This guide combines an overview of Elastisch with a quick tutorial that helps you to get started with it.
It should take about 15 minutes to read and study the provided code examples. This guide covers:

 * Features of Elastisch
 * Clojure and ElasticSearch version requirements
 * How to add Elastisch dependency to your project
 * A very brief introduction to Elastisch capabilities
 * Examples of the most common operations with Elastisch

This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a> (including images & stylesheets). The source is available [on Github](https://github.com/clojurewerkz/elastisch.docs).


## What version of Elastisch does this guide cover?

This guide covers Elastisch 3.x releases, including preview releases.



## Elastisch Overview

Elastisch is an idiomatic Clojure client for [ElasticSearch](http://elasticsearch.org/), a modern, scalable, powerful full text search server. It is simple and easy to use,
has good baseline performance and strives to support every ElasticSearch feature. Elastisch is minimalistic: it mimics ElasticSearch operations in its API but does not add
any abstractions or ODM-like layers beyond that.

Elastisch supports HTTP and native transports with almost identical API, so switching
between the two is usually straightforward.


## Supported Clojure Versions

Elastisch requires Clojure 1.8+. The latest stable release is recommended.


## Supported ElasticSearch Versions

Elastisch 2.1 and 2.2 target ElasticSearch 1.x starting with 1.1 and through 1.7.x.
Elastisch **does not** currently support ElasticSearch 2.x. It will be our focus
for the (Elastisch) 3.0 release.


## Adding Elastisch Dependency To Your Project

Elastisch artifacts are [released to Clojars](https://clojars.org/clojurewerkz/elastisch).

### With Leiningen

``` clojure
[clojurewerkz/elastisch "3.0.0"]
```

### With Maven

Add Clojars repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>https://clojars.org/repo</url>
</repository>
```

And then the dependency:

``` xml
<dependency>
  <groupId>clojurewerkz</groupId>
  <artifactId>elastisch</artifactId>
  <version>3.0.0</version>
</dependency>
```

It is recommended to stay up-to-date with new versions. New releases
and important changes are announced
[@ClojureWerkz](https://twitter.com/ClojureWerkz).


## On HTTP or Native ElasticSeach Clients

### Pros and Cons

Elastisch provides both HTTP and native ElasticSearch clients.

HTTP client is easier to get started with (you don't need to know cluster name)
and will work with hosted and PaaS environments such as Heroku or CloudFoundry.
It is also known to work with a wide range of ElasticSearch versions.

Some hosted environments do not provide access to ports other than 80 and 8080,
so the native client may not work with them. The main benefit of the native client
is it's much higher throughput (up to 6-8 times on some common workloads).


### API Structure Conversion

Native client follows the same API structure but `rest` in namespace
names becomes `native`, e.g. `clojurewerkz.elastisch.rest` becomes
`clojurewerkz.elastisch.native` and
`clojurewerkz.elastisch.rest.index` becomes
`clojurewerkz.elastisch.native.index`.

Function arguments and options accepted by the native client are as close
as possible to those in the HTTP client. The native client also provides
asynchronous versions of several common operations, they return Clojure futures
instead of responses.


## Connecting Over HTTP (Specifying Node HTTP Endpoint)

Before you can index and search with Elastisch, it is necessary to
tell Elastisch what ElasticSearch node to use. To use the HTTP
transport, you use the `clojurewerkz.elastisch.rest/connect` function
that takes an endpoint and returns a connection
(client) record:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest :as esr]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")]
    ))
```
By default Elastisch will use the HTTP endpoint at `http://localhost:9200`.

### Specifying per-connection HTTP options

You may supply [clj-http options](https://github.com/dakrone/clj-http#usage) when connecting over HTTP

Examples:

``` clojure
;; Basic Authentication
(esr/connect "http://127.0.0.1:9200" {:basic-auth ["user" "pass"]})

;; HTTP Timeout
(esr/connect "http://127.0.0.1:9200" {:conn-timeout 5000})

;; Persistent connections
;; https://github.com/dakrone/clj-http#using-persistent-connections
(esr/connect "http://127.0.0.1:9200"
             {:connection-manager (clj-http.conn-mgr/make-reusable-conn-manager {:timeout 10})})
```

## Connecting Over Native Protocol

To use the native transport, you use the `clojurewerkz.elastisch.native/connect`
function that takes two arguments: a list of endpoints (each of which is a pair
of `[host port]`) and client settings:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.native :as es]))

(defn -main
  [& args]
  ;; a list of endpoints as pairs
  ;; and options. cluster.name is mandatory!
  (let [conn (es/connect  [["127.0.0.1" 9300]]
             {"cluster.name" "your-cluster-name"}])))
```

`cluster.name` is a required setting. Cluster name and transport node
addresses can be retrieved via HTTP API, for example:

```
curl http://localhost:9200/_nodes/cluster
{"ok":true,"cluster_name":"elasticsearch_antares","nodes":{}}
```


## Indexing

Before data can be searched over, it needs to be indexed. Indexing is
the process of scanning the text and building a list of search terms
and data structures called a **search index**. Search index allows
search engines such as ElasticSearch to retrieve relevant documents
for a query efficiently.

The process of indexing involves a few steps:

 * Create an index
 * [Optionally] Defining **mappings** (how documents should be indexed)
 * Submitting documents for indexing via HTTP or other APIs

Next we will take a look at each stage in more detail.


### Creating an Index With HTTP Client

ElasticSearch provides support for multiple indexes. Indexes can be thought of as databases in a DBMS.

To create an index, use the `clojurewerkz.elastisch.rest.index/create` function:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest       :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")]
    ;; creates an index with default settings and no custom mapping types
    (esi/create conn "myapp1_development")
    ;; creates an index with given settings and no custom mapping types.
    ;; Settings map structure is the same as in the ElasticSearch API reference at
    ;; https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html
    (esi/create conn "myapp2_development" :settings {"number_of_shards" 1})))
```

Most commonly used index settings are `"number_of_shards"` and
`"number_of_replicas"`. We won't go into details about them in this
guide, please refer to the [Indexing guide](/articles/indexing.html)
for more details.

### Creating an Index With Native Client

To create an index using the native client, use the same functions but in the
`clojurewerkz.elastisch.native.index` namespace:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.native       :as es]
            [clojurewerkz.elastisch.native.index :as esi]))

(defn -main
  [& args]
  ;; cluster name WILL VARY BETWEEN MACHINES!
  (let [conn (es/connect [["127.0.0.1" 9300]] {"cluster.name" "elasticsearch_antares"})]
    ;; creates an index with default settings and no custom mapping types
    (esi/create conn "myapp1_development")
    ;; creates an index with given settings and no custom mapping types.
    ;; Settings map structure is the same as in the ElasticSearch API reference at
    ;; https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-create-index.html
    (esi/create conn "myapp2_development" :settings {"number_of_shards" 1})))

```


### Creating a Mapping With HTTP Client

Mappings define which fields in documents are indexed, if/how they are
tokenized, analyzed and so on. Each index in ElasticSearch may have
one or more **mapping types**. Mapping types can be thought of as
tables in a database (although this analogy does not always stand).

Mapping types are specified when an index is created using the
`:mapping` option:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]))

(defn -main
  [& args]
  ;; creates an index with given settings and no custom mapping types.
  ;; Mapping types map structure is the same as in the ElasticSearch API reference
  (let [conn (esr/connect "http://127.0.0.1:9200")
        mapping-types {"person" {:properties {:username   {:type "string" :store "yes"}
                                              :first-name {:type "string" :store "yes"}
                                              :last-name  {:type "string"}
                                              :age        {:type "integer"}
                                              :title      {:type "string" :analyzer "snowball"}
                                              :planet     {:type "string"}
                                              :biography  {:type "string" :analyzer "snowball" :term_vector "with_positions_offsets"}}}}]
    (esi/create conn "myapp2_development" :mappings mapping-types)))
```

Please refer to the [Indexing guide](/articles/indexing.html) for more information about mapping types, analyzers and so on.



### Indexing documents

To add a document to an index, use the `clojurewerkz.elastisch.rest.document/create` function. This will cause document id to be generated automatically:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn -main
  [& args]
  (let [conn          (esr/connect "http://127.0.0.1:9200")
        mapping-types {"person" {:properties {:username   {:type "string" :store "yes"}
                                              :first-name {:type "string" :store "yes"}
                                              :last-name  {:type "string"}
                                              :age        {:type "integer"}
                                              :title      {:type "string" :analyzer "snowball"}
                                              :planet     {:type "string"}
                                              :biography  {:type "string" :analyzer "snowball" :term_vector "with_positions_offsets"}}}}
        doc           {:username "happyjoe" :first-name "Joe" :last-name "Smith" :age 30 :title "Teh Boss" :planet "Earth" :biography "N/A"}]
    (esi/create conn "myapp2_development" :mappings mapping-types)
    ;; adds a document to the index, id is automatically generated by ElasticSearch
    ;= {:ok true, :_index people, :_type person, :_id "2vr8sP-LTRWhSKOxyWOi_Q", :_version 1}
    (println (esd/create conn "myapp2_development" "person" doc))))
```

`clojurewerkz.elastisch.rest.document/put` will add a document to the index but expects document id to be provided:

``` clojure
;; adds a document to the index, id is provided as "happyjoe"
(let [conn (esr/connect "http://127.0.0.1:9200")
      doc  {:username "happyjoe" :first-name "Joe" :last-name "Smith" :age 30 :title "Teh Boss" :planet "Earth" :biography "N/A"}]
  ;= {:ok true, :_index people, :_type person, :_id "happyjoe", :_version 1}
  (println (esd/put conn "myapp2_development" "person" "happyjoe" doc)))
```

There is much more to indexing that we won't cover in this guide. A
separate [guide on indexing](/articles/indexing.html) will go into
much more detail on various aspects related to indexing.


### Fetching a Single Document By Id

To [fetch a single document by
id](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-get.html),
use `clojurewerkz.elastisch.rest.document/get`:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")]
    ;; fetch a single document by a known id
    (esd/get conn "myapp" "articles" "521f246bc6d67300f32d2ed60423dec4740e50f5")))
```

Additional parameters such as `preference` are passed as options:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")]
    ;; fetch a single document by a known id
    (esd/get conn "myapp" "articles" "521f246bc6d67300f32d2ed60423dec4740e50f5" :preference "_primary")))
```


### Checking responses

`clojurewerkz.elastisch.rest.document/create`, `clojurewerkz.elastisch.rest.document/put`, `clojurewerkz.elastisch.rest.index/create` and other functions
return ElasticSearch responses as Clojure maps. To check if they are successful, use functions in the `clojurewerkz.elastisch.rest.response` namespace,
for example, `clojurewerkz.elastisch.rest.response/ok?` or `clojurewerkz.elastisch.rest.response/conflict?`:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.index :as esi]
            [clojurewerkz.elastisch.response :as esrsp]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")]
    ;; creates an index and check if it was successful
    (println (esrsp/ok? (esi/create conn "myapp1_development")))
    (println (esrsp/conflict? (esi/create conn "myapp1_development")))))
```


## Querying

### Overview

ElasticSearch supports [multiple kinds of
queries](http://www.elasticsearch.org/guide/reference/query-dsl/):
from simple like term and prefix query to compound like the bool
query.  Queries can also have filters associated with them.

### Performing queries

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
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person" :query (q/term :biography "New York"))
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```

Search requests with Elastisch have exactly the same structure as JSON
documents in the [ElasticSearch Query API
guide](http://www.elasticsearch.org/guide/reference/query-dsl/) but
passed as Clojure maps. `:query`, `:sort`, `:facets` and other keys
that [ElasticSearch Search API
documentation](http://www.elasticsearch.org/guide/reference/api/search/)
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
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a term query using a convenience function
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person" :query {:term {:city "New York"}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```


### Searching Against Multiple Indexes or Mappings

To search against multiple indexes or mappings, pass them as vectors to their respective function arguments:

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
        res  (esd/search conn "myapp_development" ["person" "checkin"] :query {:term {:city "New York"}})
        n    (esrsp/total-hits res)
        hits (esrsp/hits-from res)]
    (println (format "Total hits: %d" n))
    (pp/pprint hits)))
```


### Checking results

Results returned by search functions have the same structure as ElasticSearch JSON responses:

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

Several functions in the `clojurewerkz.elastisch.rest.response` namespace can be used to access more specific piece of information from a response,
such as total number of hits.

The most commonly used are:

 * `clojurewerkz.elastisch.rest.response/total-hits`: returns number of hits (documents found)
 * `clojurewerkz.elastisch.rest.response/hits-from`: returns a collection of hits (stored document plus id, score and index information)
 * `clojurewerkz.elastisch.rest.response/any-hits?`: returns true if there is at least one document found
 * `clojurewerkz.elastisch.rest.response/no-hits?` is a [complement function](http://clojuredocs.org/clojure_core/clojure.core/complement) to `any-hits?`
 * `clojurewerkz.elastisch.rest.response/ids-from`: returns a collection of document ids collected from hits

All of them take a response map as the only argument.


### Different kinds of queries

ElasticSearch is a feature rich search engine and it supports many types of queries. Even though
all queries can be passed as Clojure maps, it is common to use convenient functions from the
`clojurewerkz.elastisch.query` to construct queries.

We won't cover all the functions in this guide but will take a closer look at a few of them. For more information, see our [guide on Querying](/articles/querying.html).

#### Term query

The Term query is the most basic query type. It matches documents that have a particular term.
A common use case for term queries is looking up documents with unanalyzed identifiers
such as usernames.

A close relative of the term query is the **terms query** which works the same way but takes multiple term values.

With Elastisch, term query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "tweets" "tweet" :query {:term {:text "improved"}})
```

Elastisch provides a helper function for constructing term queries, `clojurewerkz.elastisch.query/term`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "tweets" "tweet" :query (q/term :text "improved"))
```

If provided values is a collection, Elastisch will construct a terms query under the hood.


#### Query string query

The Query String (QS) query accepts text, runs it through Lucene Query Language parser, analyzes it and performs query. It is the most advanced query
type with

The difference with field and query string query types is that text queries do not support field prefixes and do not attempt to Lucene Query syntax parsing.
As such, QS queries can be seen as more powerful and less efficient kind of text query, suitable for many apps with technical audience.

With Elastisch, QS query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "tweets" "tweet" :query {:query_string {:query "(pineapple OR banana) dessert recipe"
                                                    :allow_leading_wildcard false
                                                    :default_operator "AND"}})
```

Elastisch provides a helper function for constructing QS text queries, `clojurewerkz.elastisch.query/query-string`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

;; a full text query, the query text will be analyzed
(esd/search conn "tweets" "tweet" :query (q/query-string :query "(pineapple OR banana) dessert recipe"
                                                    :allow_leading_wildcard false
                                                    :default_operator "AND"))
```

For all the numerous options this query type accepts, see [ElasticSearch documentation on the subject](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html).


#### Range query

The Range query returns documents with fields that have numerical values, dates or terms within a specific range. One example of such query is retrieving
all documents where the `:date` field value is earlier than a particular moment in time, say, `"20120801T160000+0100"`.

Range queries work for numerical values and dates the way you would expect. For string and text fields, they match all documents with terms in the given range
(for example, `"cha"` to `"cze"`) in a particular field.

With Elastisch, range query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:range {:age {:from 10 :to 20 :include_lower true :include_upper false}}}})
```

Elastisch provides a helper function for constructing range queries, `clojurewerkz.elastisch.query/range`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/range :age {:from 10 :to 20 :include_lower true :include_upper false}))
```


#### Boolean (bool) query

A query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses, each clause with a typed occurrence.
The occurrence types are documented on the [ElasticSearch page on boolean queries](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html).

With Elastisch, boolean query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:bool {:must     {:term {:user "kimchy"}}
                                             :must_not {:range {:age {:from 10 :to 20}}}
                                             :should   [{:term {:tag "wow"}}
                                                        {:term {:tag "elasticsearch"}}]
                                             :minimum_number_should_match 1}})
```

Elastisch provides a helper function for constructing boolean queries, `clojurewerkz.elastisch.query/bool`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/bool {:must     {:term {:user "kimchy"}}
                                              :must_not {:range {:age {:from 10 :to 20}}}
                                              :should   [{:term {:tag "wow"}}
                                                         {:term {:tag "elasticsearch"}}]
                                              :minimum_number_should_match 1}))
```

`clojurewerkz.elastisch.query/bool` can be used in combination with other query helpers, such as `clojure.elastisch.query/term`, because they just return maps:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/bool {:must     (q/term :user "kimchy")
                                              :must_not (q/range :age :from 10 :to 20)
                                              :should   [(q/term :tag "wow")
                                                         (q/term :tag "elasticsearch")]
                                              :minimum_number_should_match 1}))
```


#### IDs query

The IDs query is searches for documents by their IDs (`:_id` field values). It is similar to `WHERE ... IN (...)` in SQL.

With Elastisch, IDs query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-ids-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

;; search for 3 tweets by their ids
(esd/search conn "tweets" "tweet" :query {:ids {:type "tweet" :values ["1" "722" "633"]}})
```

Elastisch provides a helper function for constructing IDs queries, `clojurewerkz.elastisch.query/ids`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

;; search for 3 tweets by their ids
(esd/search conn "tweets" "tweet" :query (q/ids "tweet" ["1" "722" "633"]))
```


#### Field query

A query that executes a query string against a specific field. It is a simplified version of query_string query (it is equivalent to setting the `:default_field`
to the field this query executed against).

With Elastisch, field query structure is the same as described in the [ElasticSearch query DSL documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-field-query.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])

(esd/search conn "people" "person" :query {:field {"address.street" "(Oak OR Elm)"}})
```

Elastisch provides a helper function for constructing field queries, `clojurewerkz.elastisch.query/field`:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd]
         '[clojurewerkz.elastisch.query :as q])

(esd/search conn "people" "person" :query (q/field "address.street" "(Oak OR Elm)"))
```


### Sorting Results

ElasticSearch supports flexible sorting of search results. To specify the desired sorting,
use the `:sort` option `clojurewerkz.elastisch.rest.document/search` accepts:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a search query with sorting
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person"
                         :query (q/query-string :biography "New York OR Austin")
                         :sort  {:name "desc"})
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

More examples can be found in this [ElasticSearch documentation section on sorting](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-sort.html).
`:sort` values that Elastisch accepts are structured exactly the same as JSON documents in that section.


### Using Offset, Limit

To limit returned number of results, use `:from` and `:size` options `clojurewerkz.elastisch.rest.document/search` accepts:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a search query that returns up to 20 results and skips first 10 documents
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person"
                         :query (q/query-string :biography "New York OR Austin")
                         :from 10 :size 20)
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

Default value of `:from` is 0, of `:size` is 10.


### Using Filters

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
        res  (esd/search conn "myapp_development" "location"
                         :query (q/query-string :biography "New York OR Austin")
                         :filter {:term {:kind "hospital"}})
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

ElasticSearch provides many filters out of the box. We will only cover
a few ones in this guide.

#### Term and Terms Filter

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

#### Range Filter

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

#### Exists Filter

Exists filter filters documents that have a specific field set. This filter always uses caching.

With Elastisch, Exists filter structure is the same as described in the [ElasticSearch Filter documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-exists-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "location"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:exists {:field :open_roof}})
```

#### Missing Filter

Exists filter filters documents that do not have a specific field set, that is, the opposite of the Exists filter.

With Elastisch, Missing filter structure is the same as described in the [ElasticSearch Filter documentation](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-missing-filter.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as esd])
(require '[clojurewerkz.elastisch.query :as q])

(esd/search conn "myapp_development" "location"
            :query (q/query-string :biography "New York OR Austin")
            :filter {:missing {:field :under_construction}})
```

Filters will be covered in more detail in the [Querying](/articles/querying.html) guide.


### Facets

Facets are covered in a separate guide on [ElasticSearch facets](/articles/querying.html) guide.


### Highlighting

Having search matches highlighted in the UI is very useful in many cases. ElasticSearch can highlight matched in search results. To enable highlighting,
use the `:highlight` option `clojurewerkz.elastisch.rest.document/search` accepts. In the example above, search matches in the `biography` field will
be highlighted (wrapped in `em` tags) and search hits will include one extra "virtual" field called `:highlight` that includes the highlighted fields and the highlighted fragments
that can be used by your application.

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

More examples can be found in this [ElasticSearch documentation section on highlighting fields](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-highlighting.html).
`:highlight` values that Elastisch accepts are structured exactly the same as JSON documents in that section.

Highlighting will be covered in more detail in the [Querying](/articles/querying.html) guide.


### Retrieving a Subset of Fields

To limit returned number of results, use the `:fields` option `clojurewerkz.elastisch.rest.document/search` accepts:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest          :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(defn -main
  [& args]
  ;; performs a search query that returns only a subset of (stored) fields
  (let [conn (esr/connect "http://127.0.0.1:9200")
        res  (esd/search conn "myapp_development" "person"
                         :query (q/query-string :biography "New York OR Austin")
                         :fields ["first_name" "last_name" "username" "location" "blurb"])
        hits (esrsp/hits-from res)]
    (pp/pprint hits)))
```

More examples can be found in this [ElasticSearch documentation
section on retrieving subsets of
fields](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-fields.html).
`:fields` values that Elastisch accepts are structured exactly the
same as JSON documents in that section.


## Updating Documents

ElasticSearch provides several ways of updating an indexed document:

 * Replace the entire document by first deleting it and then adding it back
 * Replace the entire document with a newer version
 * Run a script that updates one or more documents and causes them to be reindexed

Each has its own pros and cons: replacing the entire document is
easier but less efficient, using scripts usually requires more effort
on the developer side but offers higher efficiency and more
fine-grained control over concurrent updates.

### Replacing a Document

To replace the entire document, use the `clojurewerkz.elastisch.rest.document/replace` function:

``` clojure
(let [doc {:username "happyjoe" :first-name "Joe" :last-name "Smith" :age 30 :title "Teh Boss" :planet "Earth" :biography "N/A"}]
  ;; index a document
  (esd/put conn "myapp2_development" :person "happyjoe" doc)
  ;; replace it in the index
  (esd/replace conn "myapp2_development" :person "happyjoe" (assoc doc :title "Digital Marketing Genie"))
```

it will delete the document first and then add the new version. To
create a new version of a document, use
`clojurewerkz.elastisch.rest.document/put` with explicitly provided
id. If the document already exists, ElasticSearch will create a new
version.


### Using Update Scripts

Updates via scripts will be covered in the [Indexing guide](/articles/indexing.html).



## Deleting Documents

### Deleting Individual Documents

To delete a single document by id, use the
`clojurewerkz.elastisch.rest.document/delete` function:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.document :as esd]))

(defn -main
  [& args]
  (let [conn (esr/connect "http://127.0.0.1:9200")
        id   "johndoe"]
    ;; deletes a document from the index by id
    (println (esd/delete conn "myapp2_development" "person" id))))
```

This function takes additional options that are not covered in this guide. Please see the [Indexing guide](/articles/indexing.html) for more details.


### Deleting Multiple Documents via Query

It is also possible to delete multiple documents that match a query using the `clojurewerkz.elastisch.rest.document/delete-by-query` function:

``` clojure
(ns clojurewerkz.elastisch.docs.examples
  (:require [clojurewerkz.elastisch.rest  :as esr]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]))

(defn -main
  [& args]
  (let [conn          (esr/connect "http://127.0.0.1:9200")
        mapping-types {"person" {:properties {:username   {:type "string" :store "yes"}
                                              :first-name {:type "string" :store "yes"}
                                              :last-name  {:type "string"}
                                              :age        {:type "integer"}
                                              :title      {:type "string" :analyzer "snowball"}
                                              :planet     {:type "string"}
                                              :biography  {:type "string" :analyzer "snowball" :term_vector "with_positions_offsets"}}}}
        doc           {:username "happyjoe" :first-name "Joe" :last-name "Smith" :age 30 :title "Teh Boss" :planet "Earth" :biography "N/A"}]
    (esi/create conn "myapp2_development" :mappings mapping-types)
    ;; adds a document to the index, id is automatically generated by ElasticSearch
    ;= {:ok true, :_index people, :_type person, :_id "2vr8sP-LTRWhSKOxyWOi_Q", :_version 1}
    (println (esd/create conn "myapp2_development" "person" doc))))
```

it is possible to specify multiple mappings or indexes:

``` clojure
(esd/delete-by-query conn ["organization1" "organization2"] :person (q/term :username "happyjoe"))
```

it is also possible to delete documents across all mapping types or
indexes with
`clojurewerkz.elastisch.rest.document/delete-by-query-across-all-types`:

``` clojure
(esd/delete-by-query-across-all-types conn "myapp2_development" (q/term :username "happyjoe"))
```

as well as globally with
`clojurewerkz.elastisch.rest.document/delete-by-query-across-all-indexes-and-types`:

``` clojure
(esd/delete-by-query-across-all-indexes-and-types conn (q/term :username "happyjoe"))
```

Those functions take additional options that are not covered in this
guide. Please see the [Indexing guide](/articles/indexing.html) for
more details.


## Wrapping Up

Congratulations, you now can use Elastisch to work with
ElasticSearch. Now you know enough to start building a real
application. ElasticSearch is very feature rich and this guide does
not cover many topics (or does not cover them in detail):

 * Index operations
 * Advanced mapping features (e.g. settings or nested mappings)
 * Search across multiple indexes and/or mapping types
 * Filtering
 * Analyzers
 * Facets
 * "More like this" queries
 * Multi Search and Bulk Operations
 * Percolation
 * Index templates
 * Routing and distribution features

and more. They are covered in the rest of the guides, with links to
respective ElasticSearch documentation sections.

We hope you find Elastisch reliable, consistent and easy to use. In
case you need help, please ask on the [mailing
list](https://groups.google.com/forum/#!forum/clojure-elasticsearch)
and follow us on Twitter
[@ClojureWerkz](http://twitter.com/ClojureWerkz).


## What to Read Next

The documentation is organized as [a number of guides](/articles/guides.html), covering different topics in depth:

 * [Indexing](/articles/indexing.html)
 * [Querying](/articles/querying.html)
 * [Aggregation](/articles/aggregation.html)
 * [Facets](/articles/facets.html)
 * [Percolation](/articles/percolation.html)
 * [Routing and Distribution](/articles/distribution.html)

They also cover topics from this guide in more detail.
