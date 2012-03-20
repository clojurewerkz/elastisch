(ns clojurewerkz.elastisch.utils
  (:require clojure.string
            [clojurewerkz.support.http.statuses :as statuses]))

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

(defn ok?
  [response]
  (true? (:ok response)))

(defn conflict?
  [response]
  (statuses/conflict? (:status response)))

(defn not-found?
  [response]
  (let [s (:status response)]
    (or (false? (:exists response))
        (and s (statuses/missing? s)))))

(defn acknowledged?
  [response]
  (:acknowledged response))

(defn dasherize
  [str]
  (clojure.string/replace str #"\-", "_"))

(defn clj-to-json-options
  [options]
  (zipmap
   (map #(keyword (dasherize (keyword-to-str %))) (keys options))
   (vals options)))