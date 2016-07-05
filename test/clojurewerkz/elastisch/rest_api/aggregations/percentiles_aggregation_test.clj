;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.aggregations.percentiles-aggregation-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.aggregation   :as a]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.rest.response :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :aggregation true} test-percentiles-aggregation
    (let [index-name   "people"
          mapping-type "person"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :aggregations {:percentiles_age (a/percentiles "age")}})
          agg          (aggregation-from response :percentiles_age)]
      (is (= #{:1.0 :5.0 :25.0 :50.0 :75.0 :95.0 :99.0}
             (set (keys (get agg :values))))))))
