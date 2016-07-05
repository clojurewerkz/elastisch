;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.fuzzy-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :query true} test-basic-fuzzy-query-with-string-fields
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/fuzzy {:title "Nueva"})})
          hits         (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= #{"3"} (ids-from response)))))

  (deftest ^{:rest true :query true} test-basic-fuzzy-query-with-numeric-fields
    (let [index-name   "articles"
          mapping-type "article"
          response (doc/search conn index-name mapping-type {:query (q/fuzzy {:number-of-edits {:value 13000 :fuzziness 3}})})
          hits     (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= #{"4"} (ids-from response)))))

  (deftest ^{:rest true :query true} test-basic-fuzzy-query-with-date-fields
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/fuzzy {"latest-edit.date" {:value "2012-03-25T12:00:00" :fuzziness "1d"}})})
          hits         (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= #{"1"} (ids-from response))))))
