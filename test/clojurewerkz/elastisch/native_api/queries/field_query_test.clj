(ns clojurewerkz.elastisch.native-api.queries.field-query-test
  (:require [clojurewerkz.elastisch.native.document   :as doc]
            [clojurewerkz.elastisch.native.index      :as idx]
            [clojurewerkz.elastisch.query             :as q]
            [clojurewerkz.elastisch.fixtures          :as fx]
            [clojurewerkz.elastisch.test.helpers      :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)

;;
;; field query
;;

(deftest ^{:query true :native true} test-basic-field-query
  (let [index-name   "articles"
        mapping-type "article"
        response (doc/search index-name mapping-type :query (q/field "latest-edit.author" "Thorwald"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "2" (-> hits first :_id)))))


;; note that in practical terms, this query does not make much sense and just serves as an
;; example of the fact that many types of queries work the same way for non-analyzed fields. MK.
(deftest ^{:query true :native true} test-field-query-over-not-analyzed-fields
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/field "location.state" "CA"))
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "5" (-> hits first :_id)))))
