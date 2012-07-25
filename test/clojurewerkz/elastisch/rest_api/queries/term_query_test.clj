(ns clojurewerkz.elastisch.rest-api.queries.term-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response
        [clj-time.core :only [months ago now from-now]]))

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")

(defn prepopulate-index
  [f]
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name mapping-type "1" fx/person-jack)
  (doc/put index-name mapping-type "2" fx/person-mary)
  (doc/put index-name mapping-type "3" fx/person-joe)

  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)


;;
;; term query
;;

(deftest ^{:query true} test-basic-term-query
  (let [result (doc/search index-name mapping-type :query (q/term :biography "avoid"))]
    (is (any-hits? result))
    (is (= fx/person-jack (:_source (first (hits-from result)))))))


(deftest ^{:query true} test-term-query-with-a-limit
  (let [result (doc/search index-name mapping-type :query (q/term :planet "earth") :size 2)]
    (is (any-hits? result))
    (is (= 2 (count (hits-from result))))
    ;; but total # of hits is reported w/o respect to the limit. MK.
    (is (= 3 (total-hits result)))))
