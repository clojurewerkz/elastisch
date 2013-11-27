(ns clojurewerkz.elastisch.native-api.delete-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native          :as es]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojure.stacktrace :as s]
            [clojurewerkz.elastisch.native.response :refer [count-from ok?]]
            [clojure.test :refer :all]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

;;
;; delete
;;

(deftest ^{:native true} test-delete-when-a-document-exists
  (let [id           "1"
        index-name   "people"
        mapping-type "person"]
    (doc/put index-name mapping-type id fx/person-jack)
    (is (doc/present? index-name mapping-type id))
    (is (ok? (doc/delete index-name mapping-type id)))
    (is (not (doc/present? index-name mapping-type id)))))


;;
;; delete by query
;;

(deftest ^{:native true} test-delete-by-query-with-a-term-query-and-mapping
  (let [index-name   "people"
        mapping-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name mapping-type fx/person-jack)
    (doc/create index-name mapping-type fx/person-joe)
    (idx/refresh index-name)
    (doc/delete-by-query index-name mapping-type (q/term :username "esjoe"))
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count index-name mapping-type (q/term :username "esjack"))
         0 (doc/count index-name mapping-type (q/term :username "esjoe"))
         0 (doc/count index-name mapping-type (q/term :username "esmary")))))


(deftest ^{:native true} test-delete-by-query-with-a-term-query-across-all-mappings
  (let [index-name   "people"
        mapping-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name mapping-type fx/person-jack)
    (doc/create index-name mapping-type fx/person-joe)
    (doc/create index-name "lawyer" fx/person-joe)
    (idx/refresh index-name)
    (doc/delete-by-query-across-all-types index-name (q/match-all))
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         0 (doc/count index-name mapping-type (q/match-all)))
    (is (= 0 (count-from (doc/count index-name "lawyer" (q/match-all)))))))
