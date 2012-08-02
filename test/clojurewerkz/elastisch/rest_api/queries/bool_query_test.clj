(ns clojurewerkz.elastisch.rest-api.queries.bool-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)


;;
;; filtered query
;;

(deftest ^{:query true} test-basic-bool-query
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search index-name mapping-type :query (q/bool :must   {:term {:planet "earth"}}
                                                                        :should {:range {:age {:from 20 :to 30}}}
                                                                        :minimum_number_should_match 1))]
    (is (any-hits? response))
    (is (= (sort (ids-from response)) (sort ["1" "2" "4"])))
    (is (= 3 (total-hits response)))))
