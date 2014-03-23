;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.aggregation
  "Convenience functions that build various aggregation types.

   All functions return maps and are completely optional (but recommended)."
  (:refer-clojure :exclude [min max sum filter range]))

(defn min
  [field]
  {"min" {"field" field}})

(defn max
  [field]
  {"max" {"field" field}})

(defn sum
  [field]
  {"sum" {"field" field}})

(defn avg
  [field]
  {"avg" {"field" field}})

(defn stats
  [field]
  {"stats" {"field" field}})

(defn extended-stats
  [field]
  {"extended_stats" {"field" field}})

(defn value-count
  [field]
  {"value_count" {"field" field}})

(defn percentiles
  [field]
  {"percentiles" {"field" field}})

(defn global
  []
  {})

(defn cardinality
  ([field]
     {"cardinality" {"field" field}})
  ([field opts]
     {"cardinality" (merge {"field" field} opts)}))

(defn filter
  [opts]
  {"filter" opts})

(defn missing
  [field]
  {"missing" {"field" field}})

(defn nested
  [opts]
  {"nested" opts})

(defn terms
  ([field]
     {"terms" {"field" field}})
  ([field opts]
     {"terms" (merge {"field" field} opts)}))

(defn range
  ([field ranges]
     {"range" {"field"  field
               "ranges" ranges}})
  ([field ranges opts]
     {"range" (merge {"field"  field
                      "ranges" ranges} opts)}))

(defn date-range
  [field ^String format ranges]
  {"date_range" {"field"  field
                 "ranges" ranges
                 "format" format}})

(defn ip-range
  [field ranges]
  {"ip_range" {"field"  field
               "ranges" ranges}})
