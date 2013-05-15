(ns clojurewerkz.elastisch.rest-api.queries.custom-score-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)

;;
;; Tests
;;

(deftest ^{:query true} test-custom-score-query-with-a-basic-script
  (let [response     (doc/search "people" "person" :query (q/custom-score :query (q/match-all)
                                                                          :script "doc['age'].value"))
        hits         (hits-from response)
        scores       (vec (map :_score hits))]
    (is (= [37.0 29.0 28.0 22.0] scores))))
