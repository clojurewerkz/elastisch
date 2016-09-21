;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.get-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-get-with-non-existing-document
    (is (nil? (doc/get conn "pages" "page" "as8d8as882jk2jk9#d77$%88s7"))))

  (deftest ^{:rest true} test-get-with-existing-id-that-needs-url-encoding
    (let [id "http://www.faz.net/artikel/C31325/piratenabwehr-keine-kriegswaffen-fuer-private-dienste-30683040.html"]
      (doc/put conn "pages" "page" id {:url id})
      (is (doc/get conn "pages" "page" id))))

  (deftest ^{:rest true} test-present-with-non-existing-id
    (is (not (doc/present? conn index-name mapping-type "1"))))

  (deftest ^{:rest true} test-present-with-existing-id
    (doc/put conn index-name mapping-type "1" fx/person-jack)
    (is (doc/present? conn index-name mapping-type "1")))

  (deftest ^{:rest true} multi-get-test
    (doc/put conn index-name mapping-type "1" fx/person-jack)
    (doc/put conn index-name mapping-type "2" fx/person-mary)
    (doc/put conn index-name mapping-type "3" fx/person-joe)
    (let [mget-result (doc/multi-get conn
                                     [{:_index index-name :_type mapping-type :_id "1"}
                                      {:_index index-name :_type mapping-type :_id "2"}])]
      (is (= fx/person-jack (source-from (first mget-result))))
      (is (= fx/person-mary (source-from (second mget-result)))))
    (let [mget-result (doc/multi-get conn index-name
                                     [{:_type mapping-type :_id "1"}
                                      {:_type mapping-type :_id "2"}])]
      (is (= fx/person-jack (source-from (first mget-result))))
      (is (= fx/person-mary (source-from (second mget-result)))))
    (let [mget-result (doc/multi-get conn index-name mapping-type
                                     [{:_id "1"} {:_id "2"}])]
      (is (= fx/person-jack (source-from (first mget-result))))
      (is (= fx/person-mary (source-from (second mget-result)))))))
