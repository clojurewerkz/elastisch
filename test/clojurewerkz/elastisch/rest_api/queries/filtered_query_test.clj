(ns clojurewerkz.elastisch.rest-api.queries.filtered-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)


;;
;; filtered query
;;

(deftest ^{:query true} test-basic-filtered-query
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search index-name mapping-type :query (q/filtered :query  (q/term :planet "earth")
                                                                            :filter {:range {:age {:from 20 :to 30}}}))]
    (is (any-hits? response))
    (is (= 3 (total-hits response)))))
