;; Copyright 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.query
  "Convenience functions that build various query types.

   All functions return maps and are completely optional (but recommended)."
  (:refer-clojure :exclude [range sort])
  (:require [clojure.set :as set]
            [clojurewerkz.elastisch.escape    :as escape]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html"
  ([key values] (term key values nil))
  ([key values opts]
   (merge { (if (coll? values) :terms :term) (hash-map key values) }
          opts)))

(defn terms
  "Terms Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-terms-query.html"
  ([key values] (terms key values nil))
  ([key values opts]
   (term key values opts)))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-query.html"
  [key opts]
  {:range (hash-map key opts) })

(defn match
  "Match Query, before 0.19.9 known as Text Query.

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-query.html"
  ([field query] (match field query nil))
  ([field query opts]
   {:match {field (merge {:query query} opts)}}))

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html"
  [opts]
  {:bool opts})


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html"
  [opts]
  {:boosting opts})

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-ids-query.html"
  [type ids]
  {:ids { :type type :values ids }})

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-constant-score-query.html"
  [opts]
  {:constant_score opts})

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-dis-max-query.html"
  [opts]
  {:dis_max opts})

(defn prefix
  "Prefix query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html"
  [opts]
  {:prefix opts})

(defn filtered
  "Filtered query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-filtered-query.html"
  [opts]
  {:filtered opts})

(defn fuzzy
  "Fuzzy or Levenshtein (edit distance) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html"
  [opts]
  {:fuzzy opts})

(defn match-all
  "Match All query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-all-query.html"
  ([]
     {:match_all {}})
  ([opts]
     {:match_all opts}))

(defn more-like-this
  "MLT (More Like This) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html"
  [opts]
  {:more_like_this opts})

(def mlt more-like-this)

(defn query-string
  "Query String query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html"
  [opts]
  (let [escape-fn (or (:escape-with opts) escape/escape-query-string-characters)
        options (if-let [query (:query opts)]
                  (assoc opts :query (escape-fn query))
                  opts)]
    {:query_string options}))

(defn span-first
  "Span First query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-first-query.html"
  [opts]
  {:span_first opts})

(defn span-near
  "Span Near query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-near-query.html"
  [opts]
  {:span_near opts})

(defn span-not
  "Span Not query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-not-query.html"
  [opts]
  {:span_not opts})

(defn span-or
  "Span Or query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-or-query.html"
  [opts]
  {:span_or opts})

(defn span-term
  "Span Term query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-term-query.html"
  [opts]
  {:span_term opts})

(defn wildcard
  "Wildcard query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-wildcard-query.html"
  [opts]
  {:wildcard opts})

(defn indices
  "Indices query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-indices-query.html"
  [opts]
  {:indices opts})

(defn has-child
  "Has Child query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html"
  [opts]
  {:has_child opts})

(defn custom-filters-score
  "Custom Filters Score query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-custom-filters-score-query.html"
  [opts]
  {:custom_filters_score opts})

(defn top-children
  "Top children query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-top-children-query.html"
  [opts]
  {:top_children opts})

(defn nested
  "Nested document query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-nested-query.html"
  [opts]
  {:nested opts})

(defn type
  "Type Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-type-query.html"
  [type]
  {:type {:value type}})

(defn sort
  "Sort query results."
  [attribute {:keys [ignore-unmapped order] :as v}]
  {attribute
   (cond (map? v)
         (set/rename-keys v {:ignore-unmapped :ignoreUnmapped})
         :default
         v)})
