(ns clojurewerkz.elastisch.native-api.get-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response))


(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")


;;
;; get
;;

(deftest ^{:native true} test-get-with-non-existing-document
  (doc/create index-name mapping-type {:name "Michael"})
  (is (nil? (doc/get index-name mapping-type "1999999"))))

(deftest ^{:native true} test-get-with-existing-id-that-needs-url-encoding
  (let [id "http://www.faz.net/artikel/C31325/piratenabwehr-keine-kriegswaffen-fuer-private-dienste-30683040.html"]
    (doc/put index-name mapping-type id fx/person-jack)
    (is (doc/get index-name mapping-type id))))

;;
;; present?
;;

(deftest ^{:native true} test-present-with-non-existing-id
  (doc/put index-name mapping-type "10" fx/person-jack)
  (is (not (doc/present? index-name mapping-type "1"))))

(deftest ^{:native true} test-present-with-existing-id
  (doc/put index-name mapping-type "1" fx/person-jack)
  (is (doc/present? index-name mapping-type "1")))



;;
;; mget
;;

(deftest ^{:native true} multi-get-test
  (doc/put index-name mapping-type "1" fx/person-jack)
  (doc/put index-name mapping-type "2" fx/person-mary)
  (doc/put index-name mapping-type "3" fx/person-joe)
  (let [mget-result (doc/multi-get
                     [{:_index index-name :_type mapping-type :_id "1"}
                      {:_index index-name :_type mapping-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name
                                   [{:_type mapping-type :_id "1"}
                                    {:_type mapping-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name mapping-type
                                   [{:_id "1"} {:_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result))))))
