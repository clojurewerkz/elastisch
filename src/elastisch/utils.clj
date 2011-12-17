(ns elastisch.utils)

(defn join-names
  [name-or-names]
  (clojure.string/join "," (flatten [name-or-names])))

(defn ok?
  [response]
  (:ok response))

(defn acknowledged?
  [response]
  (:acknowledged response))
