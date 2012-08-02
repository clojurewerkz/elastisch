## Changes between Elastisch 1.0.0-beta3 and 1.0.0-beta4

### clj-http Update

[clj-http](https://github.com/dakrone/clj-http/) dependency has been upgraded to version `0.5.1`.



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
