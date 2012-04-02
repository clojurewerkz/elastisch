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
