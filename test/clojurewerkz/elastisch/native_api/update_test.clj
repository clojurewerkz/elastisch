;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
      (idx/create conn index-name :mappings fx/people-mapping)

      (doc/put conn index-name index-type "1" fx/person-jack)
      (doc/put conn index-name index-type "2" fx/person-mary)
      (doc/put conn index-name index-type "3" fx/person-joe)

      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type :query (q/term :biography "nice"))))
      (is (no-hits?  (doc/search conn index-name index-type :query (q/term :biography "brilliant"))))
      (doc/replace conn index-name index-type id (assoc fx/person-joe :biography new-bio))
      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type :query (q/term :biography "brilliant"))))
      ;; TODO: investigate this. MK.
      (is (no-hits? (doc/search conn index-name index-type :query (q/term :biography "nice"))))))

  (deftest test-versioning
    (let [index-name "people"
          index-type "person"
          id         "1"]
      (idx/create conn index-name :mappings fx/people-mapping)
      (doc/create conn index-name index-type (assoc fx/person-jack :biography "brilliant1") :id id)
      (idx/refresh conn index-name)
      (let [original-document (doc/get conn index-name index-type id)
            original-version (:_version original-document)]
        (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant2") :version original-version)
        (is (= "brilliant2" (get-in (doc/get conn index-name index-type id) [:_source :biography])))
                                        ; Can't perform a write when we pass the wrong version
        (is (thrown? VersionConflictEngineException (doc/put conn index-name index-type id (assoc fx/person-jack :biography "brilliant3") :version original-version)))
        (is (= "brilliant2" (get-in (doc/get conn index-name index-type id) [:_source :biography]))))))

  (deftest test-assigning-with-a-script-1
    (let [index-name "people"
          mapping-type "person"
          id         "1"]
      (idx/create conn index-name :mappings fx/people-mapping)
      (doc/put conn index-name mapping-type "1" fx/person-jack)
      (idx/refresh conn index-name)
      (doc/update-with-script conn index-name mapping-type "1"
                              "ctx._source.counter = 1")
      (is (= 1
             (get-in (doc/get conn index-name mapping-type "1") [:_source :counter])))))

  (deftest test-assigning-with-a-script-2
    (let [index-name "people"
          mapping-type "person"
          id         "1"]
      (idx/create conn index-name :mappings fx/people-mapping)
      (doc/put conn index-name mapping-type "1" (assoc fx/person-jack :counter 1))
      (idx/refresh conn index-name)
      (doc/update-with-script conn index-name mapping-type "1"
                              "ctx._source.counter += inc"
                              {"inc" 4})
      (is (= 5
             (get-in (doc/get conn index-name mapping-type "1") [:_source :counter]))))))
