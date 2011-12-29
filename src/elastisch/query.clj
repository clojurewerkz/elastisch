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

;; text
;; bool
;; boosting
;; ids
;; custom-score
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