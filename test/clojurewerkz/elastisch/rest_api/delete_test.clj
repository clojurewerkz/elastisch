;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.delete-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")


(let [conn (rest/connect)]
  (deftest ^{:rest true} test-delete-when-a-document-exists
    (let [id "1"]
      (doc/put conn index-name mapping-type id fx/person-jack)
      (is (doc/present? conn index-name mapping-type id))
      (is (found? (doc/delete conn index-name mapping-type id)))
      (is (not (doc/present? conn index-name mapping-type id))))))
