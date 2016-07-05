;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.range-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :query true} test-range-query-over-numerical-field
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search conn index-name mapping-type {:query (q/range :age {:from 27 :to 29})})
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"2" "4"} (set (map :_id hits))))))


(let [index-name   "tweets"
      mapping-type "tweet"]
  (deftest ^{:rest true :query true} test-range-query-over-string-field
    (let [response (doc/search conn index-name mapping-type {:query (q/range :username {:from "c" :to "j"})})
          ids      (ids-from response)]
      (is (= 2 (total-hits response)))
      (is (= #{"1" "2"} ids))))

  (deftest ^{:rest true :query true} test-range-query-over-date-time-field-with-from
    (let [response (doc/search conn index-name mapping-type {:query (q/range :timestamp {:from "20120801T160000+0100"})})
          ids      (ids-from response)]
      (is (= 2 (total-hits response)))
      (is (= #{"1" "2"} ids))))

  (deftest ^{:rest true :query true} test-range-query-over-date-time-field-with-from-and-to
     (let [response (doc/search conn index-name mapping-type {:query (q/range :timestamp {:from "20120801T160000+0100" :to "20120801T180000+0100"})})
           ids      (ids-from response)]
       (is (= 1 (total-hits response)))
       (is (= #{"2"} ids))))))
