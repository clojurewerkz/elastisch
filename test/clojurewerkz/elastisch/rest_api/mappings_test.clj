;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.mappings-test
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-getting-index-mapping
    (let [index    "people1"
          mappings fx/people-mapping
          response (idx/create conn index {:mappings mappings})]
      (is (created-or-acknowledged? response))
      (is (get-in (idx/get-mapping conn index) [:people1 :mappings :person :properties :username :store]))))

  (deftest ^{:rest true} test-updating-index-mapping
    (let [index    "people2"
          mapping2 {:person {:properties {:first-name {:type "string" :store "yes"}}}}
          mapping  fx/people-mapping
          _        (idx/create conn index {:mappings mapping2})
          response (idx/update-mapping conn index "person" {:mapping mapping})]
      (is (created-or-acknowledged? response))))

  (deftest ^{:rest true} test-updating-blank-index-mapping
    (let [index    "people4"
          mapping  fx/people-mapping
          _        (idx/create conn index {:mappings {}})
          response (idx/update-mapping conn index "person" {:mapping mapping})]
      (is (created-or-acknowledged? response))
      (is (get-in (idx/get-mapping conn index) [:people4 :mappings :person :properties :username :store])))))

