(ns clojurewerkz.elastisch.query
  (:refer-clojure :exclude [range])
  (:require [clojurewerkz.elastisch.utils :as utils]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/term-query.html"
  [key values & { :as options }]
  (merge { (if (coll? values) :terms :term) (hash-map key values) }
         options))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/range-query.html"
  [key & { :as options}]
  {:range (hash-map key options) })

(defn text
  "Text Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/text-query.html"
  [query & { :as options}]
  {:text { :message (merge { :query query } options) } })

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/bool-query.html"
  [& { :as options}]
  {:bool options})


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/boosting-query.html"
  [& { :as options}]
  {:boosting options})

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/ids-query.html"
  [type ids]
  {:ids { :type type :values ids }})

(defn custom-score
  "Custom Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/custom-score-query.html"
  [& {:as options}]
  {:custom_score options})

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/constant-score-query.html"
  [& {:as options}]
  {:constant_score options})

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/dis-max-query.html"
  [& {:as options}]
  {:dis_max options})

(defn prefix
  "Prefix query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/prefix-query.html"
  [& {:as options}]
  {:prefix options})

(defn field
  "Field query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/field-query.html"
  [& {:as options}]
  {:field options})

(defn filtered
  "Filtered query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/filtered-query.html"
  [& {:as options}]
  {:filtered options})

(defn fuzzy-like-this
  "FLT (fuzzy like this) query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/flt-query.html"
  [& {:as options}]
  {:fuzzy_like_this options})

(defn fuzzy-like-this-field
  "FLT (fuzzy like this) query for a single field

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/flt-field-query.html"
  [& {:as options}]
  {:fuzzy_like_this_field options})



;; flt-field
;; fuzzy
;; has-child
;; match-all
;; mlt
;; mlt-field
;; query-string
;; range
;; span-first
;; span-near
;; span-not
;; span-or
;; span-term
;; top-children
;; wildcard
;; nested
;; custom-filters-score
