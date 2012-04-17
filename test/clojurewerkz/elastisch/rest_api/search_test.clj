(ns clojurewerkz.elastisch.rest-api.search-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document :as doc]
            [clojurewerkz.elastisch.rest     :as rest]
            [clojurewerkz.elastisch.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response)
  (:import java.util.UUID))


(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")


;;
;; Versioning
;;

(deftest test-search-with-multiple-versions-of-a-document-matching-a-query
  (testing "that only one version is stored (versions are just for MVCC, that is, conflict resolution)"
    (idx/create index-name :mappings fx/people-mapping)
    (let [id (str (UUID/randomUUID))]
      (dotimes [n 5]
        (doc/put index-name index-type id fx/person-jack))
      (idx/refresh index-name)
      (let [result (doc/search index-name index-type :query (q/term :biography "avoid") :version true)]
        (is (= 1 (total-hits result)))))))
