(ns clojurewerkz.elastisch.queries.prefix-query-test
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response))

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn- prepopulate-index
  [f]

  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)

;;
;; prefix query
;;

(deftest ^{:query true} test-basic-prefix-query
  (let [response (doc/search index-name index-type :query (q/prefix :username "esj"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "3"} (set (map :_id hits))))))
