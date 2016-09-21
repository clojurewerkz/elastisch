;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.update-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all])
  (:import [org.elasticsearch.index.engine VersionConflictEngineException]))

(use-fixtures :each fx/reset-indexes)

(let [conn (th/connect-native-client)]
  (deftest test-replacing-documents
    (let [index-name "people"
          index-type "person"
          id         "3"
          new-bio    "Such a brilliant person"]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/put conn index-name index-type "1" fx/person-jack)
      (doc/put conn index-name index-type "2" fx/person-mary)
      (doc/put conn index-name index-type "3" fx/person-joe)

      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type {:query (q/term :biography "nice")})))
      (is (no-hits?  (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})))
      (doc/replace conn index-name index-type id (assoc fx/person-joe :biography new-bio))
      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})))
      (is (no-hits? (doc/search conn index-name index-type {:query (q/term :biography "nice")})))))

  (deftest test-versioning
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") {:id id})
      (idx/refresh conn index-name)
      (let [original-document (doc/get conn index-name index-type id)
            original-version (:_version original-document)]
        (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant2") {:version original-version})
        (is (= "brilliant2" (-> (doc/get conn index-name index-type id) source-from :biography)))
                                        ; Can't perform a write when we pass the wrong version
        (is (thrown? VersionConflictEngineException (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant3") {:version original-version})))
        (is (= "brilliant2" (-> (doc/get conn index-name index-type id) source-from :biography))))))

  (deftest test-retry-on-conflict
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") {:id id})
      (idx/refresh conn index-name)
      (let [original-document (doc/get conn index-name index-type id)
            original-version (:_version original-document)]
        (doall
          (pmap
            (fn [i]
              (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant2")))
              (repeat 10 10)))
        (is (= "brilliant2" (-> (doc/get conn index-name index-type id) source-from :biography))))))

  (deftest update-with-partial-doc
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") {:id id})
      (idx/refresh conn index-name)
      (let [original-document (doc/get conn index-name index-type id)]
        (doc/update-with-partial-doc conn index-name index-type id {:country "Sweden"})
        (idx/refresh conn index-name)
        (let [updated (doc/get conn index-name index-type id)]
          (is (= "brilliant1" (-> updated source-from :biography)))
          (is (= "Sweden" (-> updated source-from :country)))))))

  (deftest ^{:native true} create-search-template
    (let [{:keys [index id _type]}
          (doc/create-search-template conn "test-template1" fx/test-template1)]
          (is (= index ".scripts"))
          (is (= id "test-template1"))
          (is (= _type "mustache"))))

  (deftest ^{:native true} update-search-template
      (doc/create-search-template conn "test-template1" fx/test-template1)
      (doc/put-search-template conn "test-template1" fx/test-template2)
    (let [{:keys [index id _type source]}
          (doc/get-search-template conn "test-template1")]
          (is (= index ".scripts"))
          (is (= id "test-template1"))
          (is (= _type "mustache"))
          (is (= source fx/test-template2))))

  (deftest ^{:version-dependent true} update-with-script
    (let [index-name "people"
          index-type "person"
          id "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/create conn index-name index-type fx/person-jack {:id id})

      (idx/refresh conn index-name)

      ;; this works in ES<1.2, later dynamic scripting is disabled by default, so no mvel
      (testing "update with mvel script no params"
        (let [orig (doc/get conn index-name index-type id)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age += 1" nil {:lang "mvel"})
          (idx/refresh conn index-name)
          (is (= (-> orig source-from :age inc)
                 (-> (doc/get conn index-name index-type id) source-from :age)))))

      ;; this works in ES<1.2, later dynamic scripting is disabled by default, so no mvel
      (testing "update with mvel script and params"
        (let [orig (doc/get conn index-name index-type id)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age = ctx._source.age + timespan" {:timespan 1} {:lang "mvel"})
          (idx/refresh conn index-name)
          (is (= (-> orig source-from :age inc)
                 (-> (doc/get conn index-name index-type id) source-from :age)))))

      ;; this works in ES>=1.3 or with lang-groovy plugin, or with ES>=1.4 with dynamic scripting enabled
      (testing "update with groovy script no params"
        (let [orig (doc/get conn index-name index-type id)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age += 1" {} {:lang "groovy"})
          (idx/refresh conn index-name)
          (is (= (-> orig source-from :age inc)
                 (-> (doc/get conn index-name index-type id) source-from :age)))))

      ;; this works in ES>=1.3 or with lang-groovy plugin, or with ES>=1.4 with dynamic scripting enabled
      (testing "update with groovy script no params and upsert doc"
        (let [id "2"
              upsert (assoc fx/person-jack :age 10)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age += 1" {} {:lang "groovy"
                                                             :upsert upsert})
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age += 1" {} {:lang "groovy"
                                                             :upsert upsert})
          (idx/refresh conn index-name)
          (is (= 11 (-> (doc/get conn index-name index-type id) source-from :age))))))))
