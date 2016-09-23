;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

  (deftest ^{:native true} test-source-property
    (let [index-name "tweets"
          mapping-type "tweet"
          id "1"
          tweet fx/tweet1]
        (doc/put conn index-name mapping-type id tweet)
        (is (doc/present? conn index-name mapping-type id))
        (testing "get without specifying source property"
          (is (= tweet (source-from (doc/get conn index-name mapping-type id)))))

        (testing "get with specifying unnested _source properties"
          (is (= (select-keys tweet [:username])
                 (source-from (doc/get conn index-name mapping-type id {:_source ["username"]}))))
          (is (= (select-keys tweet [:username :timestamp])
                 (source-from (doc/get conn index-name mapping-type id {:_source ["username" "timestamp"]}))))
          (is (= (select-keys tweet [:username :timestamp])
                 (source-from (doc/get conn index-name mapping-type id {:_source ["username" "timestamp"]})))))

        (testing "get with specifying nested _source properties"
          (is (= {:username (:username tweet) :location (select-keys (:location tweet) [:country])}
                 (source-from (doc/get conn index-name mapping-type id {:_source ["username" "location.country"]}))))
          (is (= {:username (:username tweet) :location (select-keys (:location tweet) [:country :state])}
                 (source-from (doc/get conn index-name mapping-type id {:_source ["username" "location.country" "location.state"]})))))

        (testing "get with specifying exclude _source"
          (is (= (dissoc tweet :username :timestamp)
                 (source-from (doc/get conn index-name mapping-type id {:_source {:exclude ["username" "timestamp"]}}))))
          (is (= (-> tweet
                     (dissoc :username)
                     (update-in [:location] #(dissoc % :country)))
                 (source-from (doc/get conn index-name mapping-type id {:_source {:exclude ["username" "location.country"]}})))))))

  (deftest ^{:native true} test-get-search-template
   (doc/put-search-template conn "test-template1" fx/test-template1)
   (let [{:keys [exists _id empty? index _type source]} (doc/get-search-template conn "test-template1")]
      (is exists)
      (is (= _id "test-template1"))
      (is (= empty? false))
      (is (= _type "mustache" ))
      (is (= source  fx/test-template1))))

  (deftest ^{:native true} multi-get-test
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
