;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.aggregations.histogram-aggregation-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.query       :as q]
            [clojurewerkz.elastisch.aggregation :as a]
            [clojurewerkz.elastisch.fixtures    :as fx]
            [clojurewerkz.elastisch.test.helpers :as th]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.native.response :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true :aggregation true} test-histogram-aggregation
    (let [index-name   "people"
          mapping-type "person"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :aggregations {:age_histograms (a/histogram "age" 5)}})
          agg          (aggregation-from response :age_histograms)]
      (is (:buckets agg))))

    (deftest ^{:native true :aggregation true} test-nested-histogram-aggregation
    (let [index-name   "people"
          mapping-type "person"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :aggregations {:age_histograms (merge
                                                                    {:aggs {:avg_age (a/avg "age")}}
                                                                    (a/histogram "age" 5))}})
          agg          (aggregation-from response :age_histograms)]
      (is (= (count (:buckets agg)) (count (filter #(contains? % :avg_age) (:buckets agg))))))))
