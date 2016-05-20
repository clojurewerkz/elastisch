;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.count-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.native.response :refer [count-from]]))

(use-fixtures :each fx/reset-indexes)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true} test-count-with-the-default-query
    (let [index-name "people"
          index-type "person"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type fx/person-jack)
      (doc/create conn index-name index-type fx/person-joe)
      (idx/refresh conn index-name)
      (are [c r] (= c (count-from r))
           2 (doc/count conn index-name index-type))))

  (deftest ^{:native true} test-count-with-a-term-query
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

  (deftest ^{:native true} test-count-with-mixed-mappings
    (let [index-name "people"
          index-type "person"
          alt-index-type "altperson"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/create conn index-name index-type fx/person-jack)
      (doc/create conn index-name index-type fx/person-joe)
      (doc/create conn index-name "altperson" fx/person-jack)
      (idx/refresh conn index-name)
      (are [c r] (= c (count-from r))
        1 (doc/count conn index-name index-type  (q/term :username "esjack"))
        1 (doc/count conn index-name alt-index-type (q/term :username "esjack"))
        0 (doc/count conn index-name alt-index-type (q/term :username "esjoe"))))))
