(ns clojurewerkz.elastisch.native-api.queries.filtered-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response
        [clj-time.core :only [months ago now from-now]]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)


;;
;; filtered query
;;

(deftest ^{:query true :native true} test-basic-filtered-query
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search index-name mapping-type :query (q/filtered :query  (q/term :planet "earth")
                                                                            :filter {:range {:age {:from 20 :to 30}}}))]
    (is (any-hits? response))
    (is (= 3 (total-hits response)))))
