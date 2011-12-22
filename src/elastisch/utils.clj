(ns elastisch.utils)

(defn join-names
  [name-or-names]
  (clojure.string/join "," (flatten [name-or-names])))

(defn join-hash
  [hash]
  (let [key-value-joiner (fn [k v] (if (empty? k) v (clojure.string/join "&" [k v])))
        params-joiner    (fn [params] (clojure.string/join "=" params))
        colon-stripper   (fn [c] (clojure.string/replace c #"^\:+" ""))]
    (reduce (fn [a, b] (key-value-joiner a (params-joiner (map colon-stripper b)))) "" (vec hash))))

(defn ok?
  [response]
  (= true (:ok response)))

(defn conflict?
  [response]
  (= 409 (:status response)))

(defn acknowledged?
  [response]
  (:acknowledged response))
