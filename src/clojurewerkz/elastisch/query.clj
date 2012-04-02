(ns clojurewerkz.elastisch.query
  (:refer-clojure :exclude [range])
  (:require [clojurewerkz.elastisch.utils :as utils]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/term-query.html"
  [key values & { :as options }]
  (let [json-opts (utils/clj-to-json-options options)]
    (merge
     (if (vector? values)
       { :terms (hash-map key values) }
       { :term (hash-map key values) })
     json-opts)))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/range-query.html"
  [key & { :as options}]
  { :range (hash-map key (utils/clj-to-json-options options)) })

(defn text
  "Text Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/text-query.html"
  [query & { :as options}]
  (let [json-opts (utils/clj-to-json-options options)
        params    (merge { :query query } json-opts)]
    { :text { :message params } }))

(defn- options-query
  [key options]
  (let [json-opts (utils/clj-to-json-options options)]
    (hash-map key json-opts)))

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/bool-query.html"
  [& { :as options}]
  (options-query :bool options))


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/boosting-query.html"
  [& { :as options}]
  (options-query :boosting options))

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/ids-query.html"
  [type ids]
  { :ids { :type type :values ids } })

(defn custom-score
  "Custom Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/custom-score-query.html"
  [& {:as options}]
  (options-query :custom_score options))

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/constant-score-query.html"
  [& {:as options}]
  (options-query :constant_score options))

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/dis-max-query.html"
  [& {:as options}]
  (options-query :dis_max options))

;; field
;; filtered
;; flt
;; flt-field
;; fuzzy
;; has-child
;; match-all
;; mlt
;; mlt-field
;; prefix
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
