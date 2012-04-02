(ns clojurewerkz.elastisch.utils
  (:require clojure.string))

;;
;; API
;;

(defn join-names
  [name-or-names]
  (clojure.string/join "," (flatten [name-or-names])))

(defn keyword-to-str
  [kwd]
  (clojure.string/replace kwd #"^\:+" ""))

(defn join-hash
  [hash]
  (let [key-value-joiner (fn [k v] (if (empty? k) v (clojure.string/join "&" [k v])))
        params-joiner    (fn [params] (clojure.string/join "=" params))
        colon-stripper   (fn [c] (keyword-to-str c))]
    (reduce (fn [a, b] (key-value-joiner a (params-joiner (map colon-stripper b)))) "" (vec hash))))

(defn dasherize
  [str]
  (clojure.string/replace str #"\-", "_"))

(defn clj-to-json-options
  [options]
  (zipmap
   (map #(keyword (dasherize (keyword-to-str %))) (keys options))
   (vals options)))