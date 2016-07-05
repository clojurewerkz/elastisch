;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.count-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer [count-from created?]]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

;;
;; count
;;

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-count-with-the-default-query
  (let [index-name "people"
        index-type "person"]
    (idx/create conn index-name {:mappings fx/people-mapping})
    (doc/create conn index-name index-type fx/person-jack)
    (doc/create conn index-name index-type fx/person-joe)
    (idx/refresh conn index-name)
    (are [c r] (= c (count-from r))
         2 (doc/count conn index-name index-type))))

  (deftest ^{:rest true} test-count-with-a-term-query
    (let [index-name "people"
          index-type "person"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type fx/person-jack)
      (doc/create conn index-name index-type fx/person-joe)
      (idx/refresh conn index-name)
      (are [c r] (= c (count-from r))
           1 (doc/count conn index-name index-type (q/term :username "esjack"))
           1 (doc/count conn index-name index-type (q/term :username "esjoe"))
           0 (doc/count conn index-name index-type (q/term :username "esmary")))))


  (deftest ^{:rest true} test-count-with-mixed-mappings
    (let [index-name "people"
          index-type "person"
          person-properties (:person fx/people-mapping)]
      (idx/create conn index-name {:mappings {:person person-properties
                                              :altperson person-properties}})
      (doc/create conn index-name index-type fx/person-jack)
      (doc/create conn index-name index-type fx/person-joe)
      (doc/create conn index-name "altperson" fx/person-jack)
      (idx/refresh conn index-name)
      (are [c r] (= c (count-from r))
           1 (doc/count conn index-name index-type  (q/term :username "esjack"))
           1 (doc/count conn index-name "altperson" (q/term :username "esjack"))
           0 (doc/count conn index-name "altperson" (q/term :username "esjoe"))))))
