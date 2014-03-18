;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.query
  (:refer-clojure :exclude [range])
  (:require [clojurewerkz.elastisch.escape :as escape]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html"
  [key values & { :as options }]
  (merge { (if (coll? values) :terms :term) (hash-map key values) }
         options))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-query.html"
  [key & { :as options}]
  {:range (hash-map key options) })

(defn match
  "Match Query, before 0.19.9 known as Text Query.

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-query.html"
  [field query & { :as options}]
  {:match {field (merge { :query query } options) }})

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html"
  [& { :as options}]
  {:bool options})


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html"
  [& { :as options}]
  {:boosting options})

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-ids-query.html"
  [type ids]
  {:ids { :type type :values ids }})

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-constant-score-query.html"
  [& {:as options}]
  {:constant_score options})

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-dis-max-query.html"
  [& {:as options}]
  {:dis_max options})

(defn prefix
  "Prefix query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html"
  [& {:as options}]
  {:prefix options})

(defn filtered
  "Filtered query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-filtered-query.html"
  [& {:as options}]
  {:filtered options})

(defn fuzzy-like-this
  "FLT (fuzzy like this) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-flt-query.html"
  [& {:as options}]
  {:fuzzy_like_this options})

(def flt fuzzy-like-this)

(defn fuzzy-like-this-field
  "FLT (fuzzy like this) query for a single field

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-flt-field-query.html"
  [& {:as options}]
  {:fuzzy_like_this_field options})

(def flt-field fuzzy-like-this-field)

(defn fuzzy
  "Fuzzy or Levenshtein (edit distance) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html"
  [& {:as options}]
  {:fuzzy options})

(defn match-all
  "Match All query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-all-query.html"
  ([]
     {:match_all {}})
  ([& {:as options}]
     {:match_all options}))

(defn more-like-this
  "MLT (More Like This) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html"
  [& {:as options}]
  {:more_like_this options})

(def mlt more-like-this)

(defn more-like-this-field
  "MLT (More Like This) query that works for a single field

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-field-query.html"
  [& {:as options}]
  {:more_like_this_field options})

(def mlt-field more-like-this-field)

(defn query-string
  "Query String query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html"
  [& {:as options}]
  (let [escape-fn (or (:escape-with options) escape/escape-query-string-characters)
        options (if-let [query (:query options)]
                  (assoc options :query (escape-fn query))
                  options)]
    {:query_string options}))

(defn span-first
  "Span First query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-first-query.html"
  [& {:as options}]
  {:span_first options})

(defn span-near
  "Span Near query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-near-query.html"
  [& {:as options}]
  {:span_near options})

(defn span-not
  "Span Not query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-not-query.html"
  [& {:as options}]
  {:span_not options})

(defn span-or
  "Span Or query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-or-query.html"
  [& {:as options}]
  {:span_or options})

(defn span-term
  "Span Term query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-term-query.html"
  [& {:as options}]
  {:span_term options})

(defn wildcard
  "Wildcard query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-wildcard-query.html"
  [& {:as options}]
  {:wildcard options})

(defn indices
  "Indices query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-indices-query.html"
  [& {:as options}]
  {:indices options})

(defn has-child
  "Has Child query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html"
  [& {:as options}]
  {:has_child options})

(defn custom-filters-score
  "Custom Filters Score query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-custom-filters-score-query.html"
  [& {:as options}]
  {:custom_filters_score options})

(defn top-children
  "Top children query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-top-children-query.html"
  [& {:as options}]
  {:top_children options})

(defn nested
  "Nested document query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-nested-query.html"
  [& {:as options}]
  {:nested options})
