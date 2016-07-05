;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.span-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-people-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :query true} test-span-first-query
    ;; Finding the document that contains the word “eating” (i.e.,
    ;; [[clojurewerkz.elastisch.fixtures/person-jack]]) requires searching for
    ;; the word “eat” —not “eating”— because the `biography` field is indexed
    ;; with the `snowball` analyzer.
    (let [response (doc/search conn "people" "person" {:query (q/span-first {:match {:span_term {:biography "eat"}} :end 5})})
          hits     (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= #{"1"} (set (map :_id hits))))))

  (deftest ^{:rest true :query true} test-span-near-query
    ;; Similarly to the previous test, we search for “document” instead of
    ;; “documents” (which is what [[clojurewerkz.elastisch.fixtures/article-on-elasticsearch]])
    ;; *actually* contains).
    (let [response (doc/search conn "articles" "article" {:query (q/span-near {:clauses [{:span_term {:summary "search"}}
                                                                                         {:span_term {:summary "document"}}] :slop 5 :in_order true})})
          hits (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= #{"1"} (set (map :_id hits)))))))
