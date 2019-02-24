;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.queries.wildcard-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:query true :native true} test-trailing-wildcard-query-with-nested-fields
    (let [response     (doc/search conn "articles" "article" {:query (q/wildcard {"latest-edit.author" "Thorw*"})})
          hits         (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= "2" (-> hits first :_id)))))


  (deftest ^{:query true :native true} test-leading-wildcard-query-with-non-analyzd-field
    (let [response     (doc/search conn "tweets" "tweet" {:query (q/wildcard {:username "*werkz"})})
          hits         (hits-from response)]
      (is (any-hits? response))
      (is (= 1 (total-hits response)))
      (is (= "1" (-> hits first :_id))))))
