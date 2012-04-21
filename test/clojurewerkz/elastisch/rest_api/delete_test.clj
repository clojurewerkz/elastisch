(ns clojurewerkz.elastisch.rest-api.delete-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")


;;
;; delete
;;

(deftest test-delete-when-a-document-exists
  (let [id "1"]
    (doc/put index-name index-type id fx/person-jack)
    (is (doc/present? index-name index-type id))
    (is (ok? (doc/delete index-name index-type id)))
    (is (not (doc/present? index-name index-type id)))))


;;
;; delete by query
;;

(deftest test-delete-by-query-with-a-term-query
  (idx/create index-name :mappings fx/people-mapping)
  (doc/create index-name index-type fx/person-jack)
  (doc/create index-name index-type fx/person-joe)
  (idx/refresh index-name)
  (doc/delete-by-query index-name index-type (q/term :username "esjoe"))
  (idx/refresh index-name)
  (are [c r] (is (= c (count-from r)))
       1 (doc/count index-name index-type (q/term :username "esjack"))
       0 (doc/count index-name index-type (q/term :username "esjoe"))
       0 (doc/count index-name index-type (q/term :username "esmary"))))
