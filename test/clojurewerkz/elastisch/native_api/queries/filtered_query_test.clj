(ns clojurewerkz.elastisch.native-api.queries.filtered-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

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
