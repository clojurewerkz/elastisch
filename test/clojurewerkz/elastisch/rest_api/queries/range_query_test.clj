(ns clojurewerkz.elastisch.rest-api.queries.range-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


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
;; flt query
;;

(deftest ^{:query true} test-range-query
  (let [response (doc/search index-name mapping-type :query (q/range :age :from 27 :to 29))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= #{"2"} (set (map :_id hits))))))
