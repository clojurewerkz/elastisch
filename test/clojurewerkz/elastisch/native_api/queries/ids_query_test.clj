(ns clojurewerkz.elastisch.native-api.queries.ids-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojure.set :as cs])
  (:use clojure.test clojurewerkz.elastisch.native.response))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-tweets-index)

;;
;; Tests
;;

(deftest ^{:query true :native true} test-basic-ids-query
  (let [response     (doc/search "tweets" "tweet" :query (q/ids "tweet" ["1" "2" "8ska88"]))]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "2"} (set (map :_id (hits-from response)))))))
