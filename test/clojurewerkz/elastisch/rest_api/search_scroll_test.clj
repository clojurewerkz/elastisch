;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.search-scroll-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(let [conn (rest/connect)]
  (defn fetch-scroll-results
    [scroll-id results]
    (let [scroll-response (doc/scroll conn scroll-id {:scroll "1m"})
          hits            (hits-from scroll-response)]
      (if (seq hits)
        (recur (:_scroll_id scroll-response) (concat results hits))
        (concat results hits))))

  (deftest ^{:rest true :scroll true} test-basic-scan-query
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :search_type "scan"
                                    :scroll "1m"
                                    :size 1})
          initial-hits  (hits-from response)
          scroll-id     (:_scroll_id response)
          scan-response (doc/scroll conn scroll-id {:scroll "1m"})
          scan-hits     (hits-from scan-response)]
      (is (any-hits? response))
      (is (= 4 (total-hits response)))
      ;; scan queries don't return any hits from the initial
      ;; search request
      (is (= 0 (count initial-hits)))
      (is (= 4 (total-hits scan-response)))
      ;;scroll query returns the next results & skips the 1st page
      (is (= 3 (count scan-hits)))))

  (deftest ^{:rest true :scroll true} test-basic-scroll-query
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :search_type "query_then_fetch"
                                    :scroll "1m"
                                    :size 2})
          initial-hits    (hits-from response)
          scroll-id       (:_scroll_id response)
          scroll-response (doc/scroll conn scroll-id {:scroll "1m"})
          scroll-hits     (hits-from scroll-response)]
      (is (any-hits? response))
      (is (= 4 (total-hits response)))
      (is (= 2 (count initial-hits)))
      (is (= 4 (total-hits scroll-response)))
      (is (= 2 (count scroll-hits)))))

  (deftest ^{:rest true :scroll true} test-scroll-query-more-than-one-page
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :search_type "query_then_fetch"
                                    :scroll "1m"
                                    :size 1})
          initial-hits (hits-from response)
          scroll-id    (:_scroll_id response)
          all-hits     (fetch-scroll-results scroll-id initial-hits)]
      (is (any-hits? response))
      (is (= 4 (total-hits response)))
      (is (= 4 (count all-hits)))))

  (deftest ^{:rest true :scroll true} test-scroll-seq
    (let [index-name   "articles"
          mapping-type "article"
          res-seq      (doc/scroll-seq conn
                                       (doc/search conn index-name mapping-type
                                                   {:query (q/match-all)
                                                    :search_type "query_then_fetch"
                                                    :scroll "1m"
                                                    :size 2}))]
      (is (not (realized? res-seq)))
      (is (= 4 (count res-seq)))
      (is (= 4 (count (distinct res-seq))))
      (is (realized? res-seq))))

  (deftest ^{:rest true :scroll true} test-scroll-seq-scan
    (let [index-name   "articles"
          mapping-type "article"
          res-seq      (doc/scroll-seq conn
                                       (doc/search conn index-name mapping-type
                                                   {:query (q/match-all)
                                                    :search_type "scan"
                                                    :scroll "1m"
                                                    :size 2})
                                       {:search_type "scan"})]
      (is (not (realized? res-seq)))
      (is (= 4 (count res-seq)))
      (is (= 4 (count (distinct res-seq))))
      (is (realized? res-seq))))

  (deftest ^{:rest true :scroll true} test-scroll-seq-with-no-results
    (let [index-name   "articles"
          mapping-type "article"
          res-seq      (doc/scroll-seq conn
                                       (doc/search conn index-name mapping-type
                                                   {:query (q/term :title "Emptiness")
                                                    :search_type "query_then_fetch"
                                                    :scroll "1m"
                                                    :size 2}))]
      (is (= 0 (count res-seq)))
      (is (coll? res-seq)))))
