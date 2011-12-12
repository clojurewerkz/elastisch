(ns elastisch.index
  (:require [clojure.data.json     :as json]
            [elastisch.urls        :as urls]
            [clj-http.client       :as http]
            [elastisch.rest-client :as rest]))

(defn join-names
  [name-or-names]
  (clojure.string/join "," (flatten [name-or-names])))



;;
;; Index create, delete, exists
;;

;; TODO: FIXME: mappings and settings can't be not specified right now.
(defn create
  "Creates index"
  [index-name & { :keys [settings mappings]  }]
  (let [request-body { :settings settings :mappings mappings } ]
    (rest/json-post-req
     (urls/index index-name)
     :body request-body)))

(defn exists?
  "Checks wether the index exists or no"
  [index-name]
  (= 200 (:status (rest/head-req (urls/index index-name)))))

(defn delete
  "Delete index"
  [index-name]
  (rest/delete-req (urls/index index-name)))

;;
;; Mappings
;;

(defn get-mapping
  ([index-name-or-names]
     (rest/json-get-req
      (urls/index-mapping (join-names index-name-or-names))))
  ([^String index-name ^String type-name]
     (rest/json-get-req
      (urls/index-mapping index-name type-name))))

(defn update-mapping
  "Updates index mapping"
  [^String index-name-or-names ^String type-name & { :keys [mapping] }]
  (rest/json-put-req
   (urls/index-mapping (join-names index-name-or-names) type-name)
   :body mapping))

;; def delete-index-mapping

;;
;; Settings
;;

(defn settings
  "Returns index settings"
  [index-name]
  (rest/json-get-req (urls/index-settings index-name)))

;; defn open
;; defn close
