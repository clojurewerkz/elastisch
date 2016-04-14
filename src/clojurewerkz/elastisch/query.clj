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
            [clojurewerkz.elastisch.escape    :as escape]
            [clojurewerkz.elastisch.arguments :as ar]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html"
  [key values & args]
  (merge { (if (coll? values) :terms :term) (hash-map key values) }
         (ar/->opts args)))

(defn terms
  "Terms Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-terms-query.html"
  [key values & args]
  (apply term key values args))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-range-query.html"
  [key & args]
  {:range (hash-map key (ar/->opts args)) })

(defn match
  "Match Query, before 0.19.9 known as Text Query.

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-query.html"
  [field query & args]
  {:match {field (merge {:query query} (ar/->opts args))}})

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html"
  [& args]
  {:bool (ar/->opts args)})


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-boosting-query.html"
  [& args]
  {:boosting (ar/->opts args)})

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-ids-query.html"
  [type ids]
  {:ids { :type type :values ids }})

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-constant-score-query.html"
  [& args]
  {:constant_score (ar/->opts args)})

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-dis-max-query.html"
  [& args]
  {:dis_max (ar/->opts args)})

(defn prefix
  "Prefix query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-prefix-query.html"
  [& args]
  {:prefix (ar/->opts args)})

(defn filtered
  "Filtered query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-filtered-query.html"
  [& args]
  {:filtered (ar/->opts args)})

(defn fuzzy
  "Fuzzy or Levenshtein (edit distance) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html"
  [& args]
  {:fuzzy (ar/->opts args)})

(defn match-all
  "Match All query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-match-all-query.html"
  ([]
     {:match_all {}})
  ([& args]
     {:match_all (ar/->opts args)}))

(defn more-like-this
  "MLT (More Like This) query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html"
  [& args]
  {:more_like_this (ar/->opts args)})

(def mlt more-like-this)

(defn query-string
  "Query String query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html"
  [& args]
  (let [opts      (ar/->opts args)
        escape-fn (or (:escape-with opts) escape/escape-query-string-characters)
        options (if-let [query (:query opts)]
                  (assoc opts :query (escape-fn query))
                  opts)]
    {:query_string options}))

(defn span-first
  "Span First query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-first-query.html"
  [& args]
  {:span_first (ar/->opts args)})

(defn span-near
  "Span Near query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-near-query.html"
  [& args]
  {:span_near (ar/->opts args)})

(defn span-not
  "Span Not query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-not-query.html"
  [& args]
  {:span_not (ar/->opts args)})

(defn span-or
  "Span Or query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-or-query.html"
  [& args]
  {:span_or (ar/->opts args)})

(defn span-term
  "Span Term query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-span-term-query.html"
  [& args]
  {:span_term (ar/->opts args)})

(defn wildcard
  "Wildcard query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-wildcard-query.html"
  [& args]
  {:wildcard (ar/->opts args)})

(defn indices
  "Indices query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-indices-query.html"
  [& args]
  {:indices (ar/->opts args)})

(defn has-child
  "Has Child query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html"
  [& args]
  {:has_child (ar/->opts args)})

(defn custom-filters-score
  "Custom Filters Score query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-custom-filters-score-query.html"
  [& args]
  {:custom_filters_score (ar/->opts args)})

(defn top-children
  "Top children query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-top-children-query.html"
  [& args]
  {:top_children (ar/->opts args)})

(defn nested
  "Nested document query

   For more information, please refer to http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-nested-query.html"
  [& args]
  {:nested (ar/->opts args)})

(defn sort
  "Sort query results."
  [attribute {:keys [ignore-unmapped order] :as v}]
  {attribute
   (cond (map? v)
         (set/rename-keys v {:ignore-unmapped :ignoreUnmapped})
         :default
         v)})
