;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.indexing-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer [created? acknowledged? conflict? hits-from any-hits? no-hits?]]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago]]
            [clojure.string :refer [join]]))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(let [conn (rest/connect)]
  (deftest ^{:rest true :indexing true} test-put-with-autocreated-index
    (let [id         "1"
          document   fx/person-jack
          response   (doc/put conn index-name index-type id document)
          get-result (doc/get conn index-name index-type id)]
      (is (created? response))
      (is (idx/exists? conn index-name))
      (are [expected actual] (= expected (actual get-result))
           document   :_source
           index-name :_index
           index-type :_type
           id         :_id)))

  (deftest ^{:rest true :indexing true} test-put-with-precreated-index-without-mapping-types
    (let [id       "1"
          _        (idx/create conn index-name)
          document   fx/person-jack
          response   (doc/put conn index-name index-type id document)
          get-result (doc/get conn index-name index-type id)]
      (is (created? response))
      (are [expected actual] (= expected (actual get-result))
           document   :_source
           index-name :_index
           index-type :_type
           id         :_id)))

  (deftest ^{:rest true :indexing true} test-put-with-precreated-index-with-mapping-types
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          document   fx/person-jack
          response   (doc/put conn index-name index-type id document)
          get-result (doc/get conn index-name index-type id)]
      (is (created? response))
      (are [expected actual] (= expected (actual get-result))
           document   :_source
           index-name :_index
           index-type :_type
           id         :_id)))

  (deftest ^{:rest true :indexing true} test-put-with-missing-document-versioning-type
    (let [id       "1"
          _        (doc/put conn index-name index-type id fx/person-jack)
          _        (doc/put conn index-name index-type id fx/person-mary)
          response (doc/put conn index-name index-type id fx/person-joe {:version 1})]
      (is (conflict? response))))

  (deftest ^{:rest true :indexing true} test-put-with-conflicting-document-version
    (let [id       "1"
          _        (doc/put conn index-name index-type id fx/person-jack)
          _        (doc/put conn index-name index-type id fx/person-mary)
          response (doc/put conn index-name index-type id fx/person-joe {:version 1 :version_type "external"})]
      (is (conflict? response))
      (is (not (created? response)))))

  (deftest ^{:rest true :indexing true} test-put-with-new-document-version
    (let [id       "1"
          _        (doc/put conn index-name index-type id fx/person-jack {:version 1 :version_type "external"})
          _        (doc/put conn index-name index-type id fx/person-mary {:version 2 :version_type "external"})
          response (doc/put conn index-name index-type id fx/person-joe  {:version 3 :version_type "external"})]
      (is (not (conflict? response)))
      (is (= 3 (:_version response)))))

  (deftest ^{:rest true :indexing true} create-when-already-created-test
    (let [id       "1"
          _        (doc/put conn index-name index-type id fx/person-jack)
          response (doc/put conn index-name index-type id fx/person-joe {:op_type "create"})]
      (is (conflict? response))))

  (deftest ^{:rest true :indexing true} test-put-with-a-timestamp
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          response (doc/put conn index-name index-type id fx/person-jack {:timestamp (-> 2 months ago)})]
      (is (created? response))))

  (deftest ^{:rest true :indexing true} test-put-with-a-1-day-ttl
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          response (doc/put conn index-name index-type id fx/person-jack {:ttl "1d"})]  ; TODO ttl is deprecated
      (is (created? response))))

  (deftest ^{:rest true :indexing true} test-put-with-a-10-seconds-ttl
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          response (doc/put conn index-name index-type id fx/person-jack {:ttl "10000ms"})]  ; TODO ttl is deprecated
      (is (created? response))))

  (deftest ^{:rest true :indexing true} test-put-with-a-timeout
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          response (doc/put conn index-name index-type id fx/person-jack {:timeout "1m"})]
      (is (created? response))))

  (deftest ^{:rest true :indexing true} test-put-with-refresh-set-to-true
    (let [id       "1"
          _        (idx/create conn index-name {:mappings fx/people-mapping})
          response (doc/put conn index-name index-type id fx/person-jack {:refresh true})]
      (is (created? response))))

  (deftest ^{:rest true :indexing true} test-put-create-autogenerate-id-test
    (let [response (doc/create conn index-name index-type fx/person-jack)]
      (is (created? response))
      (is (:_id response))
      (are [expected actual] (= expected (actual response))
           index-name :_index
           index-type :_type
           1          :_version)))

  (deftest ^{:rest true :indexing true} test-a-custom-analyzer-and-stop-word-list
    (is (acknowledged? (idx/create conn "alt-tweets"
                                   {:settings {:index {:analysis {:analyzer {:antiposers {:type      "standard"
                                                                                          :filter    ["standard" "lowercase" "stop"]
                                                                                          :stopwords ["lol" "rockstar" "ninja" "cloud" "event"]}}}}}
                                    :mappings {:tweet {:properties {:text {:type "string" :analyzer "antiposers"}}}}})))
    (is (created? (doc/create conn "alt-tweets" "tweet" {:text "I am a ninja rockstar brogrammer, yo. I like that event-driven thing."})))
    (idx/refresh conn "alt-tweets")
    (let [r1 (doc/search conn "alt-tweets" "tweet" {:query (q/query-string {:query "text:event-driven" :default_field :text})})
          r2 (doc/search conn "alt-tweets" "tweet" {:query (q/query-string {:query "text:(rockstar OR ninja)" :default_field :text})})]
      (is (any-hits? r1))
      (is (no-hits? r2)))))
