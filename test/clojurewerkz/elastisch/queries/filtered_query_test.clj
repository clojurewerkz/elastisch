(ns clojurewerkz.elastisch.queries.filtered-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response
        [clj-time.core :only [months ago now from-now]]))

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn prepopulate-index
  [f]
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)


;;
;; filtered query
;;

(deftest ^{:query true} test-basic-filtered-query
  (let [response (doc/search index-name index-type :query (q/filtered :query  {:term {:planet "earth"}}
                                                                      :filter {:range {:age {:from 20 :to 30}}}))]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))))
