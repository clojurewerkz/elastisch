;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.escape
  (:require [clojure.string :as string]))

(defn escape-query-string-characters
  "Escapes Lucene special characters

   For more information, please refer to http://lucene.apache.org/core/3_4_0/queryparsersyntax.html#Escaping%20Special%20Characters"
  [s]
  (->
    (string/replace s #"[*\-+!(){}\[\]^\"~?:\\]" #(str "\\" %1))
    (string/replace #"(&&|\|\|)" #(str "\\" (%1 1)))))
