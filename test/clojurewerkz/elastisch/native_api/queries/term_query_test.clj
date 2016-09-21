;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.queries.term-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:query true :native true} test-basic-term-query-with-person-mapping
    (let [result (doc/search conn "people" "person" {:query (q/term :biography "avoid")})]
      (is (any-hits? result))
      (is (= fx/person-jack (-> result hits-from first source-from)))))


  (deftest ^{:query true :native true} test-term-query-with-person-mapping-and-a-limit
    (let [result (doc/search conn "people" "person" {:query (q/term :planet "earth") :size 2})]
      (is (any-hits? result))
      (is (= 2 (count (hits-from result))))
      ;; but total # of hits is reported w/o respect to the limit. MK.
      (is (= 4 (total-hits result)))))

  (deftest ^{:query true :native true} test-basic-term-query-with-tweets-mapping
    (let [result (doc/search conn "tweets" "tweet" {:query (q/term :text "improved")})]
      (is (any-hits? result))
      (is (= fx/tweet1 (-> result hits-from first source-from)))))

  (deftest ^{:query true :native true} test-basic-terms-query-with-tweets-mapping
    (let [result (doc/search conn "tweets" "tweet" {:query (q/term :text ["supported" "improved"])})]
      (is (any-hits? result))
      (is (= fx/tweet1 (-> result hits-from first source-from)))))

  (deftest ^{:query true :native true} test-basic-term-query-over-non-analyzed-usernames
    (are [username id] (= id (-> (doc/search conn "tweets" "tweet" {:query (q/term :username username) :sort {:timestamp "asc"}})
                                     hits-from
                                     first
                                     :_id))
         "clojurewerkz"   "1"
         "ifesdjeen"      "2"
         "michaelklishin" "4"
         "DEVOPS_BORAT"   "5"))

  (deftest ^{:query true :native true} test-basic-term-query-over-non-analyzed-embedded-fields
    (are [state id] (= id (-> (doc/search conn "tweets" "tweet" {:query (q/term "location.state" state) :sort {:timestamp "asc"}})
                                  hits-from
                                  first
                                  :_id))
         "Moscow" "4"
         "CA"     "5")))
