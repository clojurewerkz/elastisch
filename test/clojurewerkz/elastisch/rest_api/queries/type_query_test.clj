;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.type-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:query true :rest true} test-basic-type-query
    (let [response (doc/search conn "tweets" "tweet"
                               {:query (q/type "tweet")})]
      (is (any-hits? response))
      (is (= 5 (total-hits response)))
      (is (= #{"tweet"} (set (map :_type (hits-from response))))))))
