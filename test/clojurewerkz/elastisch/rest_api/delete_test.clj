;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
      (is (not (doc/present? conn index-name mapping-type id)))))

  (deftest ^{:rest true} test-delete-by-query-with-a-term-query-and-mapping
    (idx/create conn index-name :mappings fx/people-mapping)
    (doc/create conn index-name mapping-type fx/person-jack)
    (doc/create conn index-name mapping-type fx/person-joe)
    (idx/refresh conn index-name)
    (doc/delete-by-query conn index-name mapping-type (q/term :username "esjoe"))
    (idx/refresh conn index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count conn index-name mapping-type (q/term :username "esjack"))
         0 (doc/count conn index-name mapping-type (q/term :username "esjoe"))
         0 (doc/count conn index-name mapping-type (q/term :username "esmary"))))

  (deftest ^{:rest true} test-delete-by-query-with-a-term-query-across-all-mappings
    (idx/create conn index-name :mappings fx/people-mapping)
    (doc/create conn index-name mapping-type fx/person-jack)
    (doc/create conn index-name mapping-type fx/person-joe)
    (idx/refresh conn index-name)
    (doc/delete-by-query-across-all-types conn index-name (q/term :username "esjoe"))
    (idx/refresh conn index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count conn index-name mapping-type (q/term :username "esjack"))
         0 (doc/count conn index-name mapping-type (q/term :username "esjoe"))
         0 (doc/count conn index-name mapping-type (q/term :username "esmary")))))
