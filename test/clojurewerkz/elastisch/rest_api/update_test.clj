;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.update-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojure.stacktrace :as s]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-replacing-documents
    (let [index-name   "people"
          mapping-type "person"
          id           "3"
          new-bio      "Such a brilliant person"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/put conn index-name mapping-type "1" fx/person-jack)
      (doc/put conn index-name mapping-type "2" fx/person-mary)
      (doc/put conn index-name mapping-type "3" fx/person-joe)
      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name mapping-type {:query (q/term :biography "nice")})))
      (is (no-hits? (doc/search conn index-name mapping-type {:query (q/term :biography "brilliant")})))
      (doc/replace conn index-name mapping-type id (assoc fx/person-joe :biography new-bio))
      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name mapping-type {:query (q/term :biography "brilliant")})))
      (is (no-hits? (doc/search conn index-name mapping-type {:query (q/term :biography "nice")})))))

  (deftest ^{:rest true} test-versioning
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") {:id id})
      (idx/refresh conn index-name)

      (let [original-document (doc/get conn index-name index-type id)
            original-version (:_version original-document)]
        ;; Can perform a write when we say the correct version
        (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant2") {:version original-version})
        ;; Now should have the new data
        (is (= "brilliant2"
               (-> (doc/get conn index-name index-type id) source-from :biography)))
        ;; Can't perform a write when we pass the wrong version
        (let [e (:error (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant3") {:version original-version}))]
          (is (:type e "version_conflict_engine_exception")))

        ;; Still should have the new data
        (is (= "brilliant2" (-> (doc/get conn index-name index-type id) source-from :biography))))))

  (deftest ^{:rest true} upsert-existing
    (let [index-name "people"
          index-type "person"
          id         "1"
          person (assoc fx/person-jack :biography "brilliant1")]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/create conn index-name index-type person {:id id})
      (idx/refresh conn index-name)

      (let [original-document (doc/get conn index-name index-type id)]
        ;; http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-update.html#upserts
        (doc/upsert conn index-name index-type id {:country "India"})
        (idx/refresh conn index-name)
        ;; Now should have merged document data
        (is (= "brilliant1"
               (-> (doc/get conn index-name index-type id) source-from :biography)))
        (is (= "India"
               (-> (doc/get conn index-name index-type id) source-from :country))))))

  (deftest ^{:rest true} upsert-new
    (let [index-name "people"
          index-type "person"
          id         "1"
          person (assoc fx/person-jack :biography "brilliant1" :country "India")]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (idx/refresh conn index-name)
      ;; http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-update.html#upserts
      (doc/upsert conn index-name index-type id person)
      (idx/refresh conn index-name)
      ;; Now should have new document data
      (is (= "brilliant1"
             (-> (doc/get conn index-name index-type id) source-from :biography)))
      (is (= "India"
             (-> (doc/get conn index-name index-type id) source-from :country)))))

  (deftest ^{:rest true} update-with-partial-doc
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") {:id id})
      (idx/refresh conn index-name)

      (let [original-document (doc/get conn index-name index-type id)]
        ;; Udpate with partial document. doc key is needed by update partial api of elastic search
        ;; http://www.elasticsearch.org/guide/en/elasticsearch/guide/current/partial-updates.html
        (doc/update-with-partial-doc conn index-name index-type id {:country "India"})
        (idx/refresh conn index-name)
        ;; Now should have merged document data
        (is (= "brilliant1"
               (-> (doc/get conn index-name index-type id) source-from :biography)))
        (is (= "India"
               (-> (doc/get conn index-name index-type id) source-from :country))))))

  (deftest ^{:rest true :version-dependent true} update-with-script
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
                                    "ctx._source.age += 1")
            (idx/refresh conn index-name)
            (is (= (-> orig source-from :age inc)
                   (-> (doc/get conn index-name index-type id) source-from :age)))))

      ;; this works in ES<1.2, later dynamic scripting is disabled by default, so no mvel
      (testing "update with mvel script and params"
        (let [orig (doc/get conn index-name index-type id)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age = ctx._source.age + timespan" {:timespan 1})
          (idx/refresh conn index-name)
          (is (= (-> orig source-from :age inc)
                 (-> (doc/get conn index-name index-type id) source-from :age)))))

      ;; this works in ES>=1.3 or with lang-groovy plugin
      (testing "update with groovy script no params"
        (let [orig (doc/get conn index-name index-type id)]
          (doc/update-with-script conn index-name index-type id
                                  "ctx._source.age = ctx._source.age += 1" {} {:lang "groovy"})
          (idx/refresh conn index-name)
          (is (= (-> orig source-from :age inc)
                 (-> (doc/get conn index-name index-type id) source-from :age))))))))
