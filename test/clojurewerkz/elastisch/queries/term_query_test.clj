(ns clojurewerkz.elastisch.queries.term-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response
        [clj-time.core :only [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes)
(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")


;;
;; term query
;;

(deftest ^{:query true} test-basic-term-query
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)

  (let [result (doc/search index-name index-type :query (q/term :biography "avoid"))]
    (is (any-hits? result))
    (is (= fx/person-jack (:_source (first (hits-from result)))))))


(deftest ^{:query true} test-term-query-with-a-limit
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)
  (let [result (doc/search index-name index-type :query (q/term :planet "earth") :size 2)]
    (is (any-hits? result))
    (is (= 2 (count (hits-from result))))
    ;; but total # of hits is reported w/o respect to the limit. MK.
    (is (= 3 (total-hits result)))))
