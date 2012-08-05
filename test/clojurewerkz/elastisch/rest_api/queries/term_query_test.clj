(ns clojurewerkz.elastisch.rest-api.queries.term-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response
        [clj-time.core :only [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)



;;
;; suite 1
;;

(deftest ^{:query true} test-basic-term-query-with-person-mapping
  (let [result (doc/search "people" "person" :query (q/term :biography "avoid"))]
    (is (any-hits? result))
    (is (= fx/person-jack (-> result hits-from first :_source)))))


(deftest ^{:query true} test-term-query-with-person-mapping-and-a-limit
  (let [result (doc/search "people" "person" :query (q/term :planet "earth") :size 2)]
    (is (any-hits? result))
    (is (= 2 (count (hits-from result))))
    ;; but total # of hits is reported w/o respect to the limit. MK.
    (is (= 4 (total-hits result)))))

(deftest ^{:query true} test-basic-terms-query-with-tweets-mapping
  (let [result (doc/search "tweets" "tweet" :query (q/term :text ["supported" "improved"]))]
    (is (any-hits? result))
    (is (= fx/tweet1 (-> result hits-from first :_source)))))


;;
;; suite 2
;;

(deftest ^{:query true} test-basic-term-query-over-non-analyzed-usernames
  (are [username id] (is (= id (-> (doc/search "tweets" "tweet" :query (q/term :username username))
                                   hits-from
                                   first
                                   :_id)))
       "clojurewerkz"   "1"
       "ifesdjeen"      "2"
       "michaelklishin" "4"
       "DEVOPS_BORAT"   "5"))

(deftest ^{:query true} test-basic-term-query-over-non-analyzed-embedded-fields
  (are [state id] (is (= id (-> (doc/search "tweets" "tweet" :query (q/term "location.state" state))
                                   hits-from
                                   first
                                   :_id)))
       "Moscow" "4"
       "CA"     "5"))
