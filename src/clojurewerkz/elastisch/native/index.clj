(ns clojurewerkz.elastisch.native.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import [org.elasticsearch.action.admin.indices.exists.indices IndicesExistsRequest]))

;;
;; API
;;

(defn exists?
  "Returns true if given index (or indices) exists"
  [index-name]
  (let [ft                        (es/admin-index-exists (cnv/->index-exists-request index-name))
        ^IndicesExistsRequest res (.get ft)]
    (.exists res)))