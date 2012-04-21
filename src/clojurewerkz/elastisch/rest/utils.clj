(ns clojurewerkz.elastisch.rest.utils
  (:require clojure.string))

;;
;; API
;;

(defn join-names
  [name-or-names]
  (clojure.string/join "," (flatten [name-or-names])))
