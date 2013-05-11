## Changes between Elastisch 1.1.0-RC2 and 1.1.0-RC3

### Updates With Scripts

`clojurewerkz.elastisch.rest.document/update-with-script` is a new function
that updates a document with a provided script:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])

;; initializes a counter at 1
(doc/update-with-script index-name mapping-type "1"
      "ctx._source.counter = 1")

;; increments the counter by 4
(doc/update-with-script index-name mapping-type "1"
      "ctx._source.counter += inc"
      {"inc" 4})
```


## Changes between Elastisch 1.1.0-RC1 and 1.1.0-RC2

### Native Client Performance Improvements

Native client is now over 50% faster on most commonly used operations
thanks to much lower conversion overhead from ElasticSearch native client
data structures to Clojure maps.

Contributed by Jon Pither.


## Changes between Elastisch 1.0.0 and 1.1.0-RC1

### Native Client

Elastisch `1.1.0` includes a major new feature: native ElasticSearch client.
The client uses ElasticSearch's Java API, and can be used with
both transport and node clients.

#### Rationale

Native client is more bandwidth efficient. It also can use SMILE (binary JSON format) to be more
efficient on the wire.

#### Namespace Layout

Native client API in Elastisch is nearly identical to that of the REST API client
and resides in `clojurewerkz.elastisch.native` and `clojurewerkz.elastisch.native.*`
namespaces (similarly to how `clojurewerkz.elastisch.rest` `clojurewerkz.elastisch.rest.*`
namespaces are organized).

#### Connections

Transport client (used for TCP/remote connections) connections are set up using
`clojurewerkz.elastisch.native/connect!`. Note that you need to provide node
configuration that at least has cluster name in it:

``` clojure
(require '[clojurewerkz.elastisch.native :as es])

;; note that transport client uses port 9300 by default.
;; it also can connect to multiple cluster nodes
(es/connect! [["127.0.0.1" 9300]]
             {"cluster.name" "elasticsearch_antares" })
```
Cluster name and transport node addresses can be retrieved via HTTP API, for example:

```
curl http://localhost:9200/_cluster/nodes
{"ok":true,"cluster_name":"elasticsearch_antares","nodes":...}}
```

#### Performing Operations

The Native client tries to be as close as possible to the existing REST client API.
For example, document operation functions in `clojurewerkz.elastisch.native.document`,
such as `clojurewerkz.elastisch.native.document/create`,
follow `clojurewerkz.elastisch.rest.document` function signatures as closely as
possible:

``` clojure
;; in the REPL
(require '[clojurewerkz.elastisch.native :as es])
(require '[clojurewerkz.elastisch.native.document :as doc])

(es/connect! [["127.0.0.1" 9300]]
             {"cluster.name" "elasticsearch_antares" })

(doc/put index-name index-type id document)
(doc/get index-name index-type id)
```

The same with returned results. Note, however, that ES transport client
does have (very) minor differences with the REST API and it is not always possible
for Elastisch to completely cover such differences.

#### Async Operations

Native client offers a choice of synchronous (blocking calling thread until a response
is received) and asynchronous (returns a future) versions of multiple API operations:

``` clojure
;; in the REPL
(require '[clojurewerkz.elastisch.native :as es])
(require '[clojurewerkz.elastisch.native.document :as doc])

(es/connect! [["127.0.0.1" 9300]]
             {"cluster.name" "elasticsearch_antares" })

(doc/put index-name index-type id document)

;; returns a response
(doc/get index-name index-type id)
;; returns a future that will eventually
;; contain a response
(doc/async-get index-name index-type id)
```

One notable exception to this is administrative operations (such as opening or closing
an index). The rationale for this is that they are rarely executed on the hot
code path (e.g. in tight loops), so convenience and better error visibility is more
important for them.

GH issues: #17, #18, #20.

Note that native ElasticSearch client currently relies on [ElasticSearch 0.90.0.Beta1](http://www.elasticsearch.org/blog/2013/02/26/0.90.0.Beta1-released.html)
client libraries and some operations will only work with that version.


### Clojure 1.5 By Default

Elastisch now depends on `org.clojure/clojure` version `1.5.0`. It is still compatible with Clojure 1.3+ and if your `project.clj` depends
on 1.3 or 1.4, it will be used, but 1.5 is the default now.

We encourage all users to upgrade to 1.5, it is a drop-in replacement for the majority of projects out there.


### Bulk Request Support

Bulk requests are now supported. All the relevant code is in the `clojurewerkz.elastisch.rest.bulk`
namespace. Here is a small example of bulk document indexing using this new API:

``` clojure
(require '[clojurewerkz.elastisch.rest.bulk :as eb])

(eb/bulk (eb/bulk-index doc1 doc2 doc3) :refresh true)
```

Contributed by Davie Moston.


### Scroll Queries Support

Scroll queries are now easier to perform thanks to the new `clojurewerkz.elastisch.rest.document/scroll`
function that takes a scroll id and amount of time retrieved documents and related information
will be kept in memory for future retrieval. They are analogous to database cursors.

A short code example:

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query :as q])
(require '[clojurewerkz.elastisch.rest.response :refer [hits-from]])

(let [index-name   "articles"
      mapping-type "article"
      response     (doc/search index-name mapping-type
                               :query (q/query-string :query "*")
                               :search_type "scan"
                               :scroll "1m"
                               :size 1)
      scroll-id     (:_scroll_id response)
      scan-response (doc/scroll scroll-id :scroll "1m")
      scan-hits     (hits-from scan-response)]
  (println scan-hits))
```

Contributed by Davie Moston.


### Cheshire Update

[Cheshire](https://github.com/dakrone/cheshire/) dependency has been upgraded to version `5.1.1`.

### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.7.2`.

### Count API No Longer Ignores Mapping Types

`clojurewerkz.elastisch.rest.document/count` no longer ignores mapping types.

GH issue: #6.


### Count API now uses GET requests

`clojurewerkz.elastisch.rest.document/count` now correctly uses `GET` for requests without
the query part and`POST` for request that have it.

GH issue: #5.



## Changes between Elastisch 1.0.0-rc2 and 1.0.0

### URL-encoded Document IDs

Elastisch will now URL-encode document ids.


### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.5.5`.


### Cheshire Update

[Cheshire](https://github.com/dakrone/cheshire/) dependency has been upgraded to version `4.0.3`.


## Changes between Elastisch 1.0.0-rc1 and 1.0.0-rc2

### Query Validation Support

`clojurewerkz.elastisch.rest.document/validate-query` is a new function that implements support for the [Validation API](http://www.elasticsearch.org/guide/reference/api/validate.html):

``` clojure
(require '[clojurewerkz.elastisch.rest.document :as doc])
(require '[clojurewerkz.elastisch.query :as q])
(require '[clojurewerkz.elastisch.rest.response :as r])

(let [response (doc/validate-query "myproduct_development" (q/field "latest-edit.author" "Thorwald") :explain true)]
  (println response)
  (println (r/valid? response)))
```

Query validation does not execute the query.



### Percolation Support

`clojurewerkz.elastisch.rest.percolation` is a new namespace with functions that implement the [Percolation API](http://www.elasticsearch.org/guide/reference/api/percolate.html).

``` clojure
(require '[clojurewerkz.elastisch.rest.percolation :as pcl])
(require '[clojurewerkz.elastisch.rest.response :as r])

;; register a percolator query for the given index
(pcl/register-query  "myapp" "sample_percolator" :query {:term {:title "search"}})

;; match a document against the percolator
(let [response (pcl/percolate "myapp" "sample_percolator" :doc {:title "You know, for search"})]
  (println (r/ok? response))
  ;; print matches
  (println (r/matches-from response)))

;; unregister the percolator
(pcl/unregister-query "myapp" "sample_percolator")
```


### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.5.2`.


### Cheshire For JSON Serliazation

Elastisch now uses (and depends on) [Cheshire](https://github.com/dakrone/cheshire) for JSON serialization.
[clojure.data.json](https://github.com/clojure/data.json) is no longer a dependency.


### New Function for Accessing Facet Responses

`clojurewerkz.elastisch.rest.response/facets-from` is a new convenience function that returns the facets section of a response.
The exact response format will vary between facet types and queries but it is always returned as an immutable map and has the same
structure as in the respective ElasticSearch response JSON document.


### Clojure 1.4 By Default

Elastisch now depends on `org.clojure/clojure` version `1.4.0`. It is still compatible with Clojure 1.3 and if your `project.clj` depends
on 1.3, it will be used, but 1.4 is the default now.

We encourage all users to upgrade to 1.4, it is a drop-in replacement for the majority of projects out there.


### Match Query Support

[ElasticSearch 0.19.9](http://www.elasticsearch.org/blog/2012/08/23/0.19.9-released.html) renames Text Query to Match Query. Elastisch adapts by introducing `clojurewerkz.elastisch.query/match` that
is effectively an alias for `clojurewerkz.elastisch.query/text` (ElasticSearch still supports `:text` in the query DSL for backwards
compatibility).



## Changes between Elastisch 1.0.0-beta4 and 1.0.0-rc1

### Documentation improvements

[Documentation guides](http://clojureelasticsearch.info) were greatly improved.



## Changes between Elastisch 1.0.0-beta3 and 1.0.0-beta4

### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.5.1`.


### Breaking: Text Query Helper Supports Any Field

`clojurewerkz.elastisch.query/text` now takes two arguments, the first being the name of the field.
It was mistakenly hardcoded previously.¯



## Changes between Elastisch 1.0.0-beta2 and 1.0.0-beta3

### Index Templates

`clojurewerkz.elastisch.rest.index/create-template`, `clojurewerkz.elastisch.rest.index/delete-template`
and `clojurewerkz.elastisch.rest.index/get-template` are new functions that implement support for [index templates](http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.html):

``` clojure
(clojurewerkz.elastisch.rest.index/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})

(clojurewerkz.elastisch.rest.index/get-template "accounts")

(clojurewerkz.elastisch.rest.index/delete-template "accounts")
```


### Index Aliases

`clojurewerkz.elastisch.rest.index/update-aliases` and `clojurewerkz.elastisch.rest.index/get-aliases`
are new functions that implement support for [index aliases](http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html):

``` clojure
(clojurewerkz.elastisch.rest.index/update-aliases [{:add {:index "client0000001" :alias "alias1"}}
                                                   {:add {:index "client0000002" :alias "alias2"}}])

(clojurewerkz.elastisch.rest.index/get-aliases "client0000001")
```


### More Index Operations

#### Optimize

`clojurewerkz.elastisch.rest.index/optimize` is a function that optimizes an index:

``` clojure
(clojurewerkz.elastisch.rest.index/optimize "my-index" :refresh true :max_num_segments 48)
```

#### Flush

`clojurewerkz.elastisch.rest.index/flush` is a function that flushes an index:

``` clojure
(clojurewerkz.elastisch.rest.index/flush "my-index" :refresh true)
```

#### Snapshot

`clojurewerkz.elastisch.rest.index/snapshot` is a function that takes a snapshot of an index or multiple indexes:

``` clojure
(clojurewerkz.elastisch.rest.index/snapshot "my-index")
```

#### Clear Cache

`clojurewerkz.elastisch.rest.index/clear-cache` is a function that can be used to clear index caches:

``` clojure
(clojurewerkz.elastisch.rest.index/clear-cache "my-index" :filter true :field_data true)
```

It takes the same options as documented in the [ElasticSearch guide on the Clear Cache Index operation](http://www.elasticsearch.org/guide/reference/api/admin-indices-clearcache.html)

#### Status

`clojurewerkz.elastisch.rest.index/status` is a function that returns status an index or multple indexes:

``` clojure
(clojurewerkz.elastisch.rest.index/status "my-index" :recovery true :snapshot true)
```

#### Segments

`clojurewerkz.elastisch.rest.index/segments` is a function that returns segments information for an index or multiple indexes:

``` clojure
(clojurewerkz.elastisch.rest.index/segments "my-index")
```

#### Stats

`clojurewerkz.elastisch.rest.index/stats` is a function that returns statistics for an index or multiple indexes:

``` clojure
(clojurewerkz.elastisch.rest.index/stats "my-index" :docs true :store true :indexing true)
```

It takes the same options as documented in the [ElasticSearch guide on the Stats Index operation](http://www.elasticsearch.org/guide/reference/api/admin-indices-stats.html)


### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.5.0`.


## Changes between Elastisch 1.0.0-beta1 and 1.0.0-beta2

### Functions that delete documents by query across all types or globally

`clojurewerkz.elastisch.rest.document/delete-by-query-across-all-types` is a new function that searches across
one or more indexes and all mapping types:

``` clojure
(doc/delete-by-query-across-all-types index-name (q/term :username "esjoe"))
```

`clojurewerkz.elastisch.rest.document/delete-by-query-across-all-indexes-and-types` is another new function that searches across all indexes and all mapping types:

``` clojure
(doc/delete-by-query-across-all-indexes-and-types (q/term :username "esjoe"))
```



### Search functions that can search across all types or globally

`clojurewerkz.elastisch.rest.document/search-all-types` is a new function that searches across
one or more indexes and all mapping types:

``` clojure
(doc/search-all-types ["customer1_index" "customer2_index"] :query (q/query-string :query "Austin" :default_field "title"))
```

`clojurewerkz.elastisch.rest.document/search-all-indexes-and-types` is another new function that searches across all indexes and all mapping types:

``` clojure
(doc/search-all-indexes-and-types :query (q/query-string :query "Austin" :default_field "title"))
```



## Changes between Elastisch 1.0.0-alpha4 and 1.0.0-beta1

### Indexes can be created without mappings

It is now possible to create indexes without specifying mapping types: `clojurewerkz.elastisch.rest.index/create`
no longer requires `:mapping` to be passed.


### clj-http upgraded to 0.4.x

Elastisch now uses clj-http 0.4.x.


## Changes between Elastisch 1.0.0-alpha3 and 1.0.0-alpha4

### HTTP API namespaces renamed

HTTP/REST API namespaces are now grouped under `clojurewerkz.elastisch.rest`, for example, what used to be `clojurewerkz.elastisch.document` is now
`clojurewerkz.elastisch.rest.document`. This is done to leave room for [Memcached transport](http://www.elasticsearch.org/guide/reference/modules/memcached.html) support in the future.


### Custom Filters Score Query Support

Elastisch now supports [Custom Filters Score](http://www.elasticsearch.org/guide/reference/query-dsl/custom-filters-score-query.html) queries.


### Nested Query Support

Elastisch now supports [Nested](http://www.elasticsearch.org/guide/reference/query-dsl/nested-query.html) queries.


### Indices Query Support

Elastisch now supports [Indices](http://www.elasticsearch.org/guide/reference/query-dsl/indices-query.html) queries.


### Top Children Query Support

Elastisch now supports [Top Children](http://www.elasticsearch.org/guide/reference/query-dsl/top-children-query.html) queries.


### Has Child Query Support

Elastisch now supports [Has Child](http://www.elasticsearch.org/guide/reference/query-dsl/has-child-query.html) queries.


### Wildcard Query Support

Elastisch now supports [Wildcard](http://www.elasticsearch.org/guide/reference/query-dsl/wildcard-query.html) queries.


### Span Queries Support

Elastisch now supports [span queries](http://www.lucidimagination.com/blog/2009/07/18/the-spanquery/):

 * [Span First](http://www.elasticsearch.org/guide/reference/query-dsl/span-first-query.html)
 * [Span Near](http://www.elasticsearch.org/guide/reference/query-dsl/span-near-query.html)
 * [Span Not](http://www.elasticsearch.org/guide/reference/query-dsl/span-not-query.html)
 * [Span Or](http://www.elasticsearch.org/guide/reference/query-dsl/span-or-query.html)
 * [Span Term](http://www.elasticsearch.org/guide/reference/query-dsl/span-term-query.html)

Span queries are used for [proximity search](http://en.wikipedia.org/wiki/Proximity_search_(text)).


### Query String Query Support

Elastisch now supports [Query String](http://www.elasticsearch.org/guide/reference/query-dsl/query-string-query.html) queries.


### MTL (More Like This) Field Query Support

Elastisch now supports [More Like This Field](http://www.elasticsearch.org/guide/reference/query-dsl/mtl-field-query.html) queries.


### clj-time Upgraded to 0.4.1

[clj-time](https://github.com/seancorfield/clj-time) dependency has been upgraded to version 0.4.1.


### MTL (More Like This) Query Support

Elastisch now supports [More Like This](http://www.elasticsearch.org/guide/reference/query-dsl/mtl-query.html) queries.


### Match All Query Support

Elastisch now supports [match all](http://www.elasticsearch.org/guide/reference/query-dsl/match-all-query.html) queries.


### Fuzzy (Edit Distance) Query Support

Elastisch now supports [fuzzy (edit distance)](http://www.elasticsearch.org/guide/reference/query-dsl/fuzzy-query.html) queries.



### Fuzzy Like This Query Support

Elastisch now supports [fuzzy like this](http://www.elasticsearch.org/guide/reference/query-dsl/flt-query.html) and [fuzzy like this field](http://www.elasticsearch.org/guide/reference/query-dsl/flt-field-query.html) queries.


### Prefix, Field, Filtered Query Support

Elastisch now supports [prefix queries](http://www.elasticsearch.org/guide/reference/query-dsl/prefix-query.html), see `clojurewerkz.elastisch.query/prefix`,
`clojurewerkz.elastisch.query/field`, `clojurewerkz.elastisch.query/filtered`, and `clojurewerkz.elastisch.document/search` to learn more.


## Changes between Elastisch 1.0.0-alpha2 and 1.0.0-alpha3

### elastisch.document/more-like-this

`clojurewerkz.elastisch.document/more-like-this` provides access to the [Elastic Search More Like This API](http://www.elasticsearch.org/guide/reference/api/more-like-this.html) for
documents and returns documents similar to a given one:

``` clojure
(doc/more-like-this "people" "person" "1" :min_term_freq 1 :min_doc_freq 1)
```

Please note that `:min_doc_freq` and `:min_term_freq` parameters may be very important for small data sets.
If you observe responses with no results, try lowering them.



### elastisch.document/count

`clojurewerkz.elastisch.document/count` provides access to the [Elastic Search count API](http://www.elasticsearch.org/guide/reference/api/count.html)
and is almost always used with a query, for example:

``` clojure
(doc/count "people" "person" (q/term :username "clojurewerkz"))
```

### elastisch.document/delete-by-query

`clojurewerkz.elastisch.document/delete-by-query` provides access to the [Delete by query API](http://www.elasticsearch.org/guide/reference/api/delete-by-query.html) of Elastic Search, for example:

``` clojure
(doc/delete-by-query "people" "person" (q/term :tag "mongodb"))
```



## Changes between Elastisch 1.0.0-alpha1 and 1.0.0-alpha2

### elastisch.document/replace

`clojurewerkz.elastisch.document/replace` deletes a document by id and immediately adds a new one
with the same id.


### elastisch.response

`clojurewerkz.elastisch.response` was extracted from `clojurewerkz.elastisch.utils`


### Leiningen 2

Elastisch now uses [Leiningen 2](https://github.com/technomancy/leiningen/wiki/Upgrading).
