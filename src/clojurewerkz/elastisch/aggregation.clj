;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

(defn histogram
  ([field ^long interval]
     {"histogram" {"field"    field
                   "interval" interval}})
  ([field ^long interval opts]
     {"histogram" (merge {"field"    field
                          "interval" interval}
                         opts)}))

(defn date-histogram
  ([field ^String interval]
     {"date_histogram" {"field"    field
                        "interval" interval}})
  ([field ^String interval opts]
     {"date_histogram" (merge {"field"    field
                               "interval" interval}
                              opts)}))
