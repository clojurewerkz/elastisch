(ns clojurewerkz.elastisch.native-api.update-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all])
  (:import [org.elasticsearch.index.engine VersionConflictEngineException]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)


;;
;; replace
;;

(deftest test-replacing-documents
  (let [index-name "people"
        index-type "person"
        id         "3"
        new-bio    "Such a brilliant person"]
    (idx/create index-name :mappings fx/people-mapping)

    (doc/put index-name index-type "1" fx/person-jack)
    (doc/put index-name index-type "2" fx/person-mary)
    (doc/put index-name index-type "3" fx/person-joe)

    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))
    (is (no-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    (doc/replace index-name index-type id (assoc fx/person-joe :biography new-bio))
    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    ;; TODO: investigate this. MK.
    (is (no-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))))

(deftest test-versioning
  (let [index-name "people"
        index-type "person"
        id         "1"]
    (idx/create index-name :mappings fx/people-mapping)

    (doc/create index-name index-type (assoc fx/person-jack :biography "brilliant1") :id id)
    (idx/refresh index-name)

    (let [original-document (doc/get index-name index-type id)
          original-version (:_version original-document)]

      ; Can perform a write when we say the correct version
      (doc/put index-name index-type id (assoc fx/person-jack :biography "brilliant2") :version original-version)

      ; Now should have the new data
      (is (= "brilliant2" (get-in (doc/get index-name index-type id) [:source :biography])))

      ; Can't perform a write when we pass the wrong version
      (is (thrown? VersionConflictEngineException (doc/put index-name index-type id (assoc fx/person-jack :biography "brilliant3") :version original-version)))

      ; Still should have the new data
      (is (= "brilliant2" (get-in (doc/get index-name index-type id) [:source :biography]))))))

;;
;; scripts
;;

(deftest test-assigning-with-a-script-1
  (let [index-name "people"
        mapping-type "person"
        id         "1"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/put index-name mapping-type "1" fx/person-jack)
    (idx/refresh index-name)
    (doc/update-with-script index-name mapping-type "1"
      "ctx._source.counter = 1")
    (is (= 1
           (get-in (doc/get index-name mapping-type "1") [:_source :counter])))))

(deftest test-assigning-with-a-script-2
  (let [index-name "people"
        mapping-type "person"
        id         "1"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/put index-name mapping-type "1" (assoc fx/person-jack :counter 1))
    (idx/refresh index-name)
    (doc/update-with-script index-name mapping-type "1"
      "ctx._source.counter += inc"
      {"inc" 4})
    (is (= 5
           (get-in (doc/get index-name mapping-type "1") [:_source :counter])))))
