;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.response-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.response :as resp]
            [clojure.test :refer :all]))

(deftest ^{:rest true} test-created?
  (is (resp/created?
        {:_id "id"
         :_index "idx"
         :_type "type"
         :_version 1
         :status 201}))
  (is (false? (resp/created?
                {:_id "id"
                 :_index "idx"
                 :_type "type"
                 :_version 1
                 :status 200}))))


