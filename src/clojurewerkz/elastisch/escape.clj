;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.escape
  (:require [clojure.string :as string]))

(defn escape-query-string-characters
  "Escapes Lucene special characters

  For more information, please refer to <http://lucene.apache.org/core/3_4_0/queryparsersyntax.html#Escaping%20Special%20Characters>"
  [s]
  (->
    (string/replace s #"[*\-+!(){}\[\]^\"~?:\\]" #(str "\\" %1))
    (string/replace #"(&&|\|\|)" #(str "\\" (%1 1)))))
