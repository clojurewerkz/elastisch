;; Copyright 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.native.admin
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse
           org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse
           org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotResponse))

(defn register-snapshot-repository
  ([^Client conn ^String name]
   (register-snapshot-repository conn name nil))
  ([^Client conn ^String name opts]
   (let [ft                         (es/admin-put-repository conn (cnv/->put-repository-request name opts))
         ^PutRepositoryResponse res (.actionGet ft)]
     (cnv/acknowledged-response->map res))))

(defn take-snapshot
  "Takes a snapshot"
  ([^Client conn ^String repository ^String snapshot]
   (take-snapshot conn repository snapshot nil))
  ([^Client conn ^String repository ^String snapshot opts]
   (let [ft                           (es/admin-create-snapshot conn (cnv/->create-snapshot-request repository snapshot opts))
         ^CreateSnapshotResponse res (.actionGet ft)]
     ;; TODO: actually calculate this using RestStatus
     {:accepted true})))

(defn delete-snapshot
  "Deletes a snapshot"
  [^Client conn ^String repository ^String snapshot]
  (let [ft                           (es/admin-delete-snapshot conn (cnv/->delete-snapshot-request repository snapshot))
        ^DeleteSnapshotResponse res (.actionGet ft)]
    (cnv/acknowledged-response->map res)))
