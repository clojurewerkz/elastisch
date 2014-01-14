(ns clojurewerkz.elastisch.escape
  (:require [clojure.string :as string]))

(defn escape-query-string-characters
  "Escapes Lucene special characters

   For more information, please refer to http://lucene.apache.org/core/3_4_0/queryparsersyntax.html#Escaping%20Special%20Characters"
  [s]
  (->
    (string/replace s #"[*\-+!(){}\[\]^\"~?:\\]" #(str "\\" %1))
    (string/replace #"(&&|\|\|)" #(str "\\" (%1 1)))))
