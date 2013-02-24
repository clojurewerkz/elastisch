(ns clojurewerkz.elastisch.native-api.queries.wildcard-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)

;;
;; Tests
;;

(deftest ^{:query true :native true} test-trailing-wildcard-query-with-nested-fields
  (let [response     (doc/search "articles" "article" :query (q/wildcard "latest-edit.author" "Thorw*"))
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "2" (-> hits first :_id)))))


(deftest ^{:query true :native true} test-leading-wildcard-query-with-non-analyzd-field
  (let [response     (doc/search "tweets" "tweet" :query (q/wildcard :username "*werkz"))
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "1" (-> hits first :_id)))))
