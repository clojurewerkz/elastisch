;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.get-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")

(let [conn (th/connect-native-client)]
  (deftest ^{:native true} test-present-with-non-existing-id
    (doc/put conn index-name mapping-type "10" fx/person-jack)
    (is (not (doc/present? conn index-name mapping-type "1"))))

  (deftest ^{:native true} test-present-with-existing-id
    (doc/put conn index-name mapping-type "1" fx/person-jack)
    (is (doc/present? conn index-name mapping-type "1")))

  (deftest ^{:native true} multi-get-test
    (doc/put conn index-name mapping-type "1" fx/person-jack)
    (doc/put conn index-name mapping-type "2" fx/person-mary)
    (doc/put conn index-name mapping-type "3" fx/person-joe)
    (let [mget-result (doc/multi-get conn
                       [{:_index index-name :_type mapping-type :_id "1"}
                        {:_index index-name :_type mapping-type :_id "2"}])]
      (is (= fx/person-jack (:_source (first mget-result))))
      (is (= fx/person-mary (:_source (second mget-result)))))
    (let [mget-result (doc/multi-get conn index-name
                                     [{:_type mapping-type :_id "1"}
                                      {:_type mapping-type :_id "2"}])]
      (is (= fx/person-jack (:_source (first mget-result))))
      (is (= fx/person-mary (:_source (second mget-result)))))
    (let [mget-result (doc/multi-get conn index-name mapping-type
                                     [{:_id "1"} {:_id "2"}])]
      (is (= fx/person-jack (:_source (first mget-result))))
      (is (= fx/person-mary (:_source (second mget-result)))))))
