(ns elastisch.index
  (:require [clojure.data.json     :as json]
            [elastisch.urls        :as urls]
            [elastisch.utils       :as utils]
            [elastisch.rest-client :as rest]
            [clj-http.client       :as http]))

;;
;; create, delete, exists?
;;

;; TODO: FIXME: mappings and settings can't be not specified right now.
(defn create
  "The create index API allows to instantiate an index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [index-name & { :keys [settings mappings]  }]
  (let [request-body { :settings settings :mappings mappings } ]
    (rest/post
     (urls/index index-name)
     :body request-body)))

(defn exists?
  "Used to check if the index (indices) exists or not.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-indices-exists.html"
  [index-name]
  (= 200 (:status (rest/head (urls/index index-name)))))

(defn delete
  "The delete index API allows to delete an existing index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-index.html"
  [index-name]
  (rest/delete (urls/index index-name)))

;;
;; Mappings
;;

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([index-name-or-names]
     (rest/get
      (urls/index-mapping (utils/join-names index-name-or-names))))
  ([^String index-name ^String type-name]
     (rest/get
      (urls/index-mapping index-name type-name))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html"
  [^String index-name-or-names ^String type-name & { :keys [mapping ignore-conflicts] }]
  (rest/put
   (urls/index-mapping (utils/join-names index-name-or-names) type-name ignore-conflicts)
   :body mapping))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data. The REST endpoint is /{index}/{type} with DELETE method.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-mapping.html"
  [^String index-name ^String type-name]
  (rest/delete
   (urls/index-mapping index-name type-name)))

;;
;; Settings
;;

(defn update-settings
  "Change specific index level settings in real time.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-update-settings.html"
  ([settings]
     (rest/put
      (urls/index-settings)
      :body settings))
  ([^String index-name settings]
     (rest/put
      (urls/index-settings index-name)
      :body settings)))


(defn get-settings
  "The get settings API allows to retrieve settings of index/indices

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-settings.html
  "
  ([]
     (rest/get
      (urls/index-settings)))
  ([^String index-name]
     (rest/get
      (urls/index-settings index-name))))

;;
;; Open/close
;;

(defn open
  "Open index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html
  "
  [index-name]
  (rest/post (urls/index-open index-name)))

(defn close
  "Close index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html
  "
  [index-name]
  (rest/post (urls/index-close index-name)))

(defn refresh
  ([]
     (rest/post (urls/index-refresh)))
  ([index-name-or-names]
     (rest/post (urls/index-refresh (utils/join-names index-name-or-names)))))

;;
;; Aliases
;;

;; defn get-aliases
;; defn add-alias (+ with filter + with routing)
;; defn add-aliases
;; defn remove-alias
;; defn rename-alias

;;
;; Analyze
;;

;; defn analyze

;;
;; Templates
;;

;; defn get-template
;;
;; defn delete-template
;; defn optimize
;; defn flush
;; defn snapshot
;; defn stats
;; defn status
;; defn segments
;; defn clear-cache