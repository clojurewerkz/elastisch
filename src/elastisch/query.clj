(ns elastisch.query
  (:require [elastisch.utils :as utils]))

(defn term
  [key values & { :keys [minimum-match] :as options}]
  (let [json-opts (utils/clj-to-json-options options)]
    (merge
     (if (vector? values)
       { :terms (hash-map key values) }
       { :term (hash-map key values) })
     json-opts)))

(defn range
  [key & { :keys [from to include-lower include-upper gt gte lt lte boost] :as options}]
  { :range (hash-map key (utils/clj-to-json-options options)) })

(defn text
  [query & { :keys [analyzer operator type] :as options}]
  (let [json-opts (utils/clj-to-json-options options)
        params    (merge { :query query } json-opts)]
    { :text { :message params } }))

(defn- options-query
  [key options]
  (let [json-opts (utils/clj-to-json-options options)]
    (hash-map key json-opts)))

(defn bool
  [& { :keys [must should must-not minimum-number-should-match boost disable-coord] :as options}]
  (options-query :bool options))


(defn boosting
  [& { :keys [positive negative positive-boost negative-boost] :as options}]
  (options-query :boosting options))

(defn ids
  [type ids]
  { :ids { :type type :values  ids } })

(defn custom-score
  [& {:keys [query params script] :as options}]
  (options-query :custom_score options))


;; constant-score
;; dis-max
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