(ns clojurewerkz.elastisch.index
  (:require [clojure.data.json     :as json]
            [clojurewerkz.elastisch.rest        :as rest]
            [clj-http.client       :as http])
  (:use [clojurewerkz.elastisch.utils :only [join-names]]))

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
     (rest/index-url index-name)
     :body request-body)))

(defn exists?
  "Used to check if the index (indices) exists or not.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-indices-exists.html"
  [index-name]
  (= 200 (:status (rest/head (rest/index-url index-name)))))

(defn delete
  "The delete index API allows to delete an existing index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-index.html"
  [index-name]
  (rest/delete (rest/index-url index-name)))

;;
;; Mappings
;;

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([index-name]
     (rest/get (rest/index-mapping-url (join-names index-name))))
  ([^String index-name ^String type-name]
     (rest/get
      (rest/index-mapping-url index-name type-name))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html"
  [^String index-name-or-names ^String type-name & { :keys [mapping ignore-conflicts] }]
  (rest/put (rest/index-mapping-url (join-names index-name-or-names) type-name)
            :body mapping
            :query-params { :ignore-conflicts true }))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data. The REST endpoint is /{index}/{type} with DELETE method.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-mapping.html"
  [^String index-name ^String type-name]
  (rest/delete (rest/index-mapping-url index-name type-name)))

;;
;; Settings
;;

(defn update-settings
  "Change specific index level settings in real time.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-update-settings.html"
  ([settings]
     (rest/put (rest/index-settings-url)
               :body settings))
  ([^String index-name settings]
     (rest/put (rest/index-settings-url index-name)
               :body settings)))


(defn get-settings
  "The get settings API allows to retrieve settings of index/indices

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-settings.html
  "
  ([]
     (rest/get (rest/index-settings-url)))
  ([^String index-name]
     (rest/get (rest/index-settings-url index-name))))

;;
;; Open/close
;;

(defn open
  "Open index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html
  "
  [index-name]
  (rest/post (rest/index-open-url index-name)))

(defn close
  "Close index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html
  "
  [index-name]
  (rest/post (rest/index-close-url index-name)))

(defn refresh
  ([]
     (rest/post (rest/index-refresh-url)))
  ([index-name]
     (rest/post (rest/index-refresh-url (join-names index-name)))))

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