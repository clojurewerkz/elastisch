(ns clojurewerkz.elastisch.rest.index
  (:require [clojure.data.json           :as json]
            [clojurewerkz.elastisch.rest :as rest]
            [clj-http.client             :as http])
  (:use [clojurewerkz.elastisch.rest.utils :only [join-names]]))

;;
;; create, delete, exists?
;;

(defn create
  "The create index API allows to instantiate an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (clojurewerkz.elastisch.rest.index/create \"myapp_development\")
    (clojurewerkz.elastisch.rest.index/create \"myapp_development\" :settings {\"number_of_shards\" 1})

    (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                               :first-name {:type \"string\" :store \"yes\"}
                                               :last-name  {:type \"string\"}
                                               :age        {:type \"integer\"}
                                               :title      {:type \"string\" :analyzer \"snowball\"}
                                               :planet     {:type \"string\"}
                                               :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
      (clojurewerkz.elastisch.rest.index/create \"myapp_development\" :mappings mapping-types))



   Related ElasticSearch API Reference section:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [^String index-name & {:keys [settings mappings]}]
  (rest/post (rest/index-url index-name) :body (if mappings
                                                 {:settings settings :mappings mappings}
                                                 {:settings settings})))

(defn exists?
  "Used to check if the index (indices) exists or not.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-indices-exists.html"
  [^String index-name]
  (= 200 (:status (rest/head (rest/index-url index-name)))))

(defn delete
  "The delete index API allows to delete an existing index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-index.html"
  [^String index-name]
  (rest/delete (rest/index-url index-name)))

;;
;; Mappings
;;

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([^String index-name]
     (rest/get (rest/index-mapping-url (join-names index-name))))
  ([^String index-name ^String type-name]
     (rest/get
      (rest/index-mapping-url index-name type-name))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html"
  [^String index-name-or-names ^String type-name & { :keys [mapping ignore_conflicts] }]
  (rest/put (rest/index-mapping-url (join-names index-name-or-names) type-name)
            :body mapping
            :query-params { :ignore_conflicts ignore_conflicts }))

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