(ns clojurewerkz.elastisch.rest-api.queries.ids-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.set :as cs])
  (:use clojure.test clojurewerkz.elastisch.rest.response))

(use-fixtures :each fx/reset-indexes fx/prepopulate-tweets-index)

;;
;; Tests
;;

(deftest ^{:query true} test-basic-ids-query
  (let [response     (doc/search "tweets" "tweet" :query (q/ids "tweet" ["1" "2" "8ska88"]))]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "2"} (set (map :_id (hits-from response)))))))
