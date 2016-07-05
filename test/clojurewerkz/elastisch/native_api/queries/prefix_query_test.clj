;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.queries.prefix-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:query true :native true} test-basic-prefix-query
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search conn index-name mapping-type {:query (q/prefix {:username "esj"})})
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "3"} (set (map :_id hits))))))

(deftest ^{:query true :native true} test-full-word-prefix-query-over-a-text-field-analyzed-with-the-standard-analyzer
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search conn index-name mapping-type {:query (q/prefix {:text "why"})})
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "4" (-> hits first :_id)))))

(deftest ^{:query true :native true} test-partial-prefix-query-over-a-text-field
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search conn index-name mapping-type {:query (q/prefix {:text "congr"})})
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "3" (-> hits first :_id))))))
