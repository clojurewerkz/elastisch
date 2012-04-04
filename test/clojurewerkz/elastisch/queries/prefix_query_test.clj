(ns clojurewerkz.elastisch.queries.prefix-query-test
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response))


(use-fixtures :each fx/reset-indexes)
(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

;;
;; prefix query
;;

(deftest test-basic-prefix-query
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)
  (let [response (doc/search index-name index-type :query (q/prefix :username "esj"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "3"} (set (map :_id hits))))))
