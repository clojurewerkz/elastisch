(ns elastisch.core
  (:refer-clojure :exclude [get])
  (:require [elastisch.utils         :as utils]
            [elastisch.rest-client   :as rest]
            [elastisch.urls          :as urls]))

(defn put
  [index type id document & {:keys [version op_type routing parent timestamp ttl prelocate timeout refresh replication consistency] :as all}]
  (rest/json-put-req
   (urls/record index type id all)
   :body document))

(defn create
  "Creates document, autogenerating ID"
  [index type document & {:keys [version op_type routing parent timestamp ttl prelocate timeout refresh replication consistency] :as all}]
  (rest/json-post-req
   (urls/index-type index type all)
   :body document))

(defn get
  "Gets Document by Id or returns nil if document is not found."
  [index type id & {:keys [realtime fields routing preference refresh] :as all}]
  (let [result (rest/json-get-req
                (urls/record index type id all))]
    (if (utils/not-found? result)
      nil
      result)))

(defn delete
  [index type id & {:keys [version routing parent replication consistency refresh] :as all}]
  (rest/delete-req
   (urls/record index type id all)))

(defn present?
  [index type id]
  (not (nil? (get index type id))))

;; defn multi-get
;; defn get-records
;; defn search
;; defn count
;; defn delete-by-query
;; defn more-like-this

