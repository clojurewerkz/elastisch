;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.query-string-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :query true} test-query-string-query
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/query-string {:query "Austin" :default_field "title"})})]
      (is (= 1 (total-hits response)))
      (is (= #{"4"} (ids-from response)))))

  (deftest ^{:rest true :query true} test-query-string-query-across-all-mapping-types
    (let [index-name   "articles"
          response     (doc/search-all-types conn index-name {:query (q/query-string {:query "Austin" :default_field "title"})})]
      (is (= 1 (total-hits response)))
      (is (= #{"4"} (ids-from response)))))

  (deftest ^{:rest true :query true} test-query-string-query-across-all-indexes-and-mapping-types
    (let [response     (doc/search-all-indexes-and-types conn {:query (q/query-string {:query "Austin" :default_field "title"})})]
      (is (= 1 (total-hits response)))
      (is (= #{"4"} (ids-from response)))))

  (deftest ^{:rest true :query true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case1
    (let [index-name   "tweets"
          mapping-type "tweet"
          response (doc/search conn index-name mapping-type {:query (q/query-string {:query "cloud+"})})
          hits     (hits-from response)]
      (is (= 1 (total-hits response)))
      (is (= "5" (-> hits first :_id)))))

  (deftest ^{:rest true :query true} test-query-string-query-over-a-text-field-analyzed-with-the-standard-analyzer-case2
    (let [index-name   "tweets"
          mapping-type "tweet"
          response (doc/search conn index-name mapping-type {:query (q/query-string {:query "cloud AND (NOT adoption)"})})
          hits     (hits-from response)]
      (is (= 0 (total-hits response))))))
