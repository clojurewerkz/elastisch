(ns clojurewerkz.elastisch.rest-api.queries.query-string-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response
        [clj-time.core :only [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)


;;
;; query string query
;;

(deftest ^{:query true} test-query-string-query
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true} test-query-string-query-across-all-mapping-types
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search-all-types index-name :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true} test-query-string-query-across-all-mapping-types
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search-all-indexes-and-types :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case1
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/query-string :query "cloud+"))
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "5" (-> hits first :_id)))))

(deftest ^{:query true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case1
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/query-string :query "cloud AND (NOT adoption)"))
        hits     (hits-from response)]
    (is (= 0 (total-hits response)))))
