;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.mappings-test
  (:require [clojurewerkz.elastisch.native.index :as idx]
            [clojurewerkz.elastisch.fixtures     :as fx]
            [clojurewerkz.elastisch.test.helpers :as th]
            [clojurewerkz.elastisch.native.response :as resp]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true} test-updating-index-mapping
    (let [index    "people1"
          mapping  fx/people-mapping
          orig-mapping {:person {:properties {:first-name {:type "string" :store "yes"}}}}
          _        (idx/create conn index {:mappings orig-mapping})
          response (idx/update-mapping conn index "person" {:mapping mapping})]
      (is (resp/created-or-acknowledged? response))
      (is (get-in (idx/get-mapping conn index) [:people1 :mappings :person :properties :username :store]))))


  (deftest ^{:native true} test-updating-blank-index-mapping
    (let [index    "people3"
          mapping  fx/people-mapping
          _        (idx/create conn index {:mappings {}})
          response (idx/update-mapping conn index "person" {:mapping mapping})]
      (is (resp/created-or-acknowledged? response)))))
