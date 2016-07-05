;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.internal.rest-test
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojure.test :refer :all])
  (:import clojurewerkz.elastisch.rest.Connection))

(println (str "Using Clojure version " *clojure-version*))

(def es-url (or (System/getenv "ES_URL")
                (System/getenv "ELASTICSEARCH_URL")
                "http://localhost:9200"))

(deftest test-successful-connection
  (is (= es-url (.uri ^Connection rest/*endpoint*))))

(deftest test-mget-path
  (let [conn (rest/connect es-url)]
    (is (= (str es-url "/_mget")
         (rest/index-mget-url conn)))
    (is (= (str es-url "/index_name/_mget")
           (rest/index-mget-url conn "index_name")))
    (is (= (str es-url "/index_name/type_name/_mget")
           (rest/index-mget-url conn "index_name" "type_name")))))
