;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.indices-test
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (rest/connect)]
  (deftest ^{:rest true :indexing true} test-create-an-index-without-mappings-or-settings
    (let [response (idx/create conn "elastisch-index-without-mappings")]
      (is (acknowledged? response))
      (is (not (idx/type-exists? conn "elastisch-index-without-mappings" "person")))))

  (deftest ^{:rest true :indexing true} test-create-an-index-with-settings
    (let [response (idx/create conn "elastisch-index-without-mappings" {:settings {"index" {"number_of_shards" 1}}})]
      (is (acknowledged? response))))

  (deftest ^{:rest true :indexing true} test-successful-creation-of-index-with-mappings-and-without-settings
    (let [index    "people"
          response (idx/create conn index {:mappings fx/people-mapping})]
      (is (idx/exists? conn index))
      (is (idx/type-exists? conn index "person"))))

  (deftest ^{:rest true :indexing true} test-successful-deletion-of-index
    (let [index    "people"
          _        (idx/create conn index {:mappings fx/people-mapping})
          response (idx/delete conn index)]
      (is (not (idx/exists? conn index)))
      (is (not (idx/type-exists? conn index "person")))))

  (deftest ^{:rest true :indexing true} test-getting-index-settings
    (let [index     "people"
          settings  {:index {:refresh_interval "1s"}}
          response  (idx/create conn index {:settings settings :mappings fx/people-mapping})
          settings' (idx/get-settings conn "people")]
      (acknowledged? response)
      (is (= "1s" (get-in settings' [:people :settings :index :refresh_interval])))))

  (deftest ^{:rest true :indexing true} testing-updating-specific-index-settings
    (let [index     "people"
          settings  { :index { :refresh_interval "7s" } }
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/update-settings conn index settings)
          settings' (idx/get-settings conn "people")]
      (acknowledged? response)
      (is (= "7s" (get-in settings' [:people :settings :index :refresh_interval])))))

  (deftest ^{:rest true :indexing true} test-optimize-index
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/optimize conn index {:only_expunge_deletes 1})]
      (is (:_shards response))))

  (deftest ^{:rest true :indexing true} test-flush-index-with-refresh
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/flush conn index {:refresh true})]
      (is (:_shards response))))

  (deftest ^{:rest true :indexing true} test-clear-index-cache-with-refresh
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/clear-cache conn index {:filter true :field_data true})]
      (is (:_shards response))))

  (deftest ^{:rest true :indexing true} test-index-recovery-1
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/recovery conn index)]
      (is (get-in response [:people :shards]))))

  (deftest ^{:rest true :indexing true} test-index-recovery-for-multiple-indexes-1
    (is (acknowledged? (idx/create conn "group1")))
    (is (acknowledged? (idx/create conn "group2")))
    (idx/refresh conn "group2") ;; let's wait until ES has finished indexing
   
    (let [response (idx/recovery conn ["group1" "group2"])]
      (is (get-in response [:group1 :shards]))
      (is (get-in response [:group2 :shards]))))

  (deftest ^{:rest true :indexing true} test-index-status-2
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          response  (idx/segments conn index)]
      (is (:_shards response))))

  (deftest ^{:rest true :indexing true} test-index-status-for-multiple-indexes-2
    (idx/create conn "group1")
    (idx/create conn "group2")
    (let [response (idx/segments conn ["group1" "group2"])]
      (is (:_shards response))))

  (deftest ^{:rest true :indexing true} test-index-stats
    (let [index     "people"
          _         (idx/create conn index {:mappings fx/people-mapping})
          _         (idx/refresh conn index) ;; let's wait until ES has finished indexing
          response  (idx/stats conn index {:stats ["docs" "store" "indexing"] :types "person"})
          stats     (-> response :_all :primaries)]
      (acknowledged? response)
      (is (every? #(contains? stats %) [:docs :store :indexing]))
      (is (not-any? #(contains? stats %) [:get :search :completion :fielddata :flush :merge :query_cache :refresh :suggest :warmer :translog]))))

  (deftest ^{:rest true :indexing true} test-index-stats-for-multiple-indexes
    (idx/create conn "group1")
    (idx/create conn "group2")
    (let [response (idx/stats conn ["group1" "group2"] {:stats ["docs" "store" "indexing"]})
          stats (-> response :_all :primaries)]
      (is (every? #(contains? stats %) [:docs :store :indexing]))
      (is (not-any? #(contains? stats %) [:get :search :completion :fielddata :flush :merge :query_cache :refresh :suggest :warmer :translog]))))

  (deftest ^{:rest true :indexing true} test-create-an-index-with-two-aliases
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (let [response (idx/update-aliases conn [{:add {:index "aliased-index" :alias "alias1"}}
                                             {:add {:index "aliased-index" :alias "alias2"}}])]
      (acknowledged? response))
    (is (= "42s" (get-in (idx/get-settings conn "alias2")
                         [:aliased-index :settings :index :refresh_interval]))))


  (deftest ^{:rest true :indexing true} test-getting-aliases
    (idx/create conn "aliased-index" {:settings {"index" {"refresh_interval" "42s"}}})
    (let [res1 (idx/update-aliases conn [{:add {:index "aliased-index" :alias "alias1"}}
                                         {:add {:index "aliased-index" :alias "alias2" :routing 1}}])
          res2 (idx/get-aliases conn "aliased-index")]
      (acknowledged? res1)
      (is (get-in res2 [:aliased-index :aliases]))))

  (deftest ^{:rest true :indexing true} test-create-an-index-template-and-fetch-it
    (idx/create-template conn "accounts" {:template "account*" :settings {:index {:refresh_interval "60s"}}})
    (let [res (idx/get-template conn "accounts")]
      (is (= "account*" (get-in res [:accounts :template])))
      (is (get-in res [:accounts :settings]))))

  (deftest ^{:rest true :indexing true} test-create-an-index-template-with-alias-and-fetch-it
    (idx/create-template conn "accounts" {:template "account*" :settings {:index {:refresh_interval "60s"}} :aliases { :account-alias {} }})
    (let [res (idx/get-template conn "accounts")]
      (is (= "account*" (get-in res [:accounts :template])))
      (is (get-in res [:accounts :settings]))
      (is (get-in res [:accounts :aliases :account-alias]))))

  (deftest ^{:rest true :indexing true} test-create-an-index-template-and-delete-it
    (idx/create-template conn "accounts" {:template "account*" :settings {:index {:refresh_interval "60s"}}})
    (acknowledged? (idx/delete-template conn "accounts"))))
