(ns elastisch.core
  (:require [elastisch.rest-client   :as rest]
            [elastisch.urls          :as urls]))

(defn put-record
  [index type id document & {:keys [version op_type routing parent timestamp ttl prelocate timeout refresh replication consistency] :as all}]
  (rest/json-put-req
   (urls/record index type id all)
   :body document))
  
;; defn delete-record
(defn get-record
  [index type id & {:keys [realtime fields routing preference refresh] :as all}]
  (rest/json-get-req
   (urls/record index type id all)))

;; defn get-records
;; defn search
;; defn count
;; defn delete-by-query
;; defn more-like-this

