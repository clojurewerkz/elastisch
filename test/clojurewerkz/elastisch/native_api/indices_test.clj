;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.indices-test
  (:require [clojurewerkz.elastisch.native        :as es]
            [clojurewerkz.elastisch.native
              [document :as doc]
              [index :as idx]
              [response :refer [acknowledged?]]]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(defn- broadcast-operation-response?
  [m]
  (and (get-in m [:_shards :total])
       (get-in m [:_shards :successful])
       (get-in m [:_shards :failed])))

(let [conn (th/connect-native-client)]
  (deftest ^{:indexing true :native true} test-create-an-index-without-mappings-or-settings
    (let [response (idx/create conn "elastisch-index-without-mappings")]
      (is (acknowledged? response))
      (is (not (idx/type-exists? conn "elastisch-index-without-mappings" "person")))))

  (deftest ^{:indexing true :native true} test-create-an-index-with-settings
    (let [response (idx/create conn "elastisch-index-without-mappings" {:settings {"index" {"number_of_shards" 1}}})]
      (is (acknowledged? response))))

  (deftest ^{:indexing true :native true} test-create-index-accepts-keywordized-keys-in-settings
    (let [response (idx/create conn "elastisch-index-with-settings" {:settings {:index {:refresh_interval "42s"}}})]
      (is (acknowledged? response))))

  (deftest ^{:indexing true :native true} test-successful-creation-of-index-with-mappings-and-without-settings
    (let [index    "people"
          response (idx/create conn index {:mappings fx/people-mapping})]
      (is (idx/exists? conn index))
      (is (idx/type-exists? conn index "person"))))

  (deftest ^{:indexing true :native true} test-successful-deletion-of-index
    (let [index    "people"
          _        (idx/create conn index {:mappings fx/people-mapping})
          response (idx/delete conn index)]
      (is (not (idx/exists? conn index)))))

  (deftest ^{:native true :indexing true} test-getting-index-settings
      (let [index     "people"
            settings  {"index" {"refresh_interval" "1s"}}
            response  (idx/create conn index {:settings settings :mappings fx/people-mapping})
            settings' (idx/get-settings conn "people")]
        (acknowledged? response)
        (is (= "1s" (get-in settings' [:people :settings :index :refresh_interval])))))

  (deftest ^{:indexing true :native true} testing-updating-specific-index-settings
    (let [index     "people"
          settings  {:index {:refresh_interval "1s"}}
          _         (idx/create conn index {:mappings fx/people-mapping})]
      (is (idx/update-settings conn index settings))))

  (deftest ^{:indexing true :native true} test-force-merge-index
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/force-merge conn index {:only_expunge_deletes 1})]
      (is (broadcast-operation-response? response))))

  (deftest ^{:indexing true :native true} test-flush-index
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/flush conn index)]
      (is (broadcast-operation-response? response))))

  (deftest ^{:indexing true :native true} test-refresh-index
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/refresh conn index)]
      (is (broadcast-operation-response? response))))

  (deftest ^{:indexing true :native true} test-clear-index-cache-with-refresh
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/clear-cache conn index {:filter true :field_data true})]
      (is (broadcast-operation-response? response))))

  (deftest ^{:indexing true :native true} test-index-stats-for-all-indexes
    (idx/create conn "group1")
    (idx/create conn "group2")
    (let [res (idx/stats conn {:docs true :store true :indexing true})]
      (is (contains? res :_all))
      (is (contains? res :indices))

      (is (false? (empty? (get-in res [:indices "group1"]))))
      (is (false? (empty? (get-in res [:indices "group2"]))))))

  (deftest ^{:indexing true :native true} test-indices-with-aliases
    (idx/create conn "aliased-index1" {:settings {"index" {"refresh_interval" "42s"}}})
    (idx/create conn "aliased-index2" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged?
          (idx/update-aliases conn [{:add {:alias "alias1" :index "aliased-index1"}}
                                    {:add {:alias "alias2" :index "aliased-index2"}}])))
    (is (doc/put conn "aliased-index1" "type" "id1" {}))
    (is (doc/put conn "aliased-index2" "type" "id2" {}))
    (is (doc/get conn "alias1" "type" "id1"))
    (is (doc/get conn "alias2" "type" "id2"))
    (is (not (doc/get conn "alias1" "type" "id2")))
    (is (not (doc/get conn "alias2" "type" "id1"))))

  (deftest ^{:indexing true :native true} test-create-an-index-with-two-aliases
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged? (idx/update-aliases conn [{:add {:alias "alias1" :indices ["aliased-index"]}}
                                                 {:add {:alias "alias2" :indices ["aliased-index"]}}]))))

  (deftest ^{:indexing true :native true} test-create-an-index-with-two-aliases-using-plural-aliases-syntax 
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged? (idx/update-aliases conn [{:add {:aliases ["alias1" "alias2"] :index "aliased-index"}}])))
    (is (doc/put conn "aliased-index" "type" "id1" {}))
    (is (doc/get conn "alias1" "type" "id1"))
    (is (doc/get conn "alias2" "type" "id1")))

  (deftest ^{:indexing true :native true} test-create-an-index-with-an-alias-and-delete-it
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged? (idx/update-aliases conn [{:add {:alias "alias1" :indices "aliased-index"}}])))
    (is (acknowledged? (idx/update-aliases conn [{:remove {:aliases "alias1" :index "aliased-index"}}]))))

  (deftest ^{:indexing true :native true} test-remove-alias-allows-singular-alias
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged? (idx/update-aliases conn [{:add {:index "aliased-index" :alias "alias1"}}])))
    (is (acknowledged? (idx/update-aliases conn [{:remove {:index "aliased-index" :alias "alias1"}}]))))

  (deftest ^{:indexing true :native true} test-remove-alias-allows-multiple-indices
    (idx/create conn "aliased-index1" {:settings {"index" {"refresh_interval" "42s"}}})
    (idx/create conn "aliased-index2" {:settings {"index" {"refresh_interval" "42s"}}})
    (is (acknowledged? (idx/update-aliases conn [{:add {:indices ["aliased-index1" "aliased-index2"] :alias "alias"}}])))
    (is (acknowledged? (idx/update-aliases conn [{:remove {:alias "alias" :indices ["aliased-index1" "aliased-index2"]}}]))))

  (deftest ^{:indexing true :native true} test-create-an-index-template-and-fetch-it
    (let [response (idx/create-template conn "accounts" {:template "account*" :settings {:index {:refresh_interval "60s"}}})]
      (is (acknowledged? response))))

  (deftest ^{:indexing true :native true} test-create-an-index-template-and-delete-it
     (idx/create-template conn "accounts" {:template "account*" :settings {:index {:refresh_interval "60s"}}})
     (is (acknowledged? (idx/delete-template conn "accounts")))))

