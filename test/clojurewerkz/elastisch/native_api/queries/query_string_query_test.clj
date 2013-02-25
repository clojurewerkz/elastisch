(ns clojurewerkz.elastisch.native-api.queries.query-string-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response
        [clj-time.core :only [months ago now from-now]]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)


;;
;; query string query
;;

(deftest ^{:query true :native true} test-query-string-query
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

;; ES native client seems to ignore special index and type names such as _all. MK.
(deftest ^{:query true :native true} test-query-string-query-across-all-mapping-types
  (let [index-name   "articles"
        response     (doc/search-all-types index-name :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true :native true} test-query-string-query-across-all-indexes-and-mapping-types
  (let [response     (doc/search-all-indexes-and-types :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true :native true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case1
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/query-string :query "cloud+"))
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "5" (-> hits first :_id)))))

(deftest ^{:query true :native true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case1
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/query-string :query "cloud AND (NOT adoption)"))
        hits     (hits-from response)]
    (is (= 0 (total-hits response)))))
