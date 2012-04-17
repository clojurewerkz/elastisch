(ns clojurewerkz.elastisch.queries.bool-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document :as doc]
            [clojurewerkz.elastisch.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response))

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn prepopulate-index
  [f]
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)
  (doc/put index-name index-type "4" fx/person-tony)  

  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)


;;
;; filtered query
;;

(deftest ^{:query true} test-basic-bool-query
  (let [response (doc/search index-name index-type :query (q/bool :must   {:term {:planet "earth"}}
                                                                  :should {:range {:age {:from 20 :to 30}}}
                                                                  :minimum_number_should_match 1))]
    (is (any-hits? response))
    (is (= (sort (ids-from response)) (sort ["1" "2" "4"])))
    (is (= 3 (total-hits response)))))
