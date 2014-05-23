;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native.admin
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse
           org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse
           org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotResponse))

(defn register-snapshot-repository
  [^Client conn ^String name & args]
  (let [opts                       (ar/->opts args)
        ft                         (es/admin-put-repository conn (cnv/->put-repository-request name opts))
        ^PutRepositoryResponse res (.actionGet ft)]
    (cnv/acknowledged-response->map res)))

(defn take-snapshot
  "Takes a snapshot"
  [^Client conn ^String repository ^String snapshot & args]
  (let [opts                         (ar/->opts args)
        ft                           (es/admin-create-snapshot conn (cnv/->create-snapshot-request repository snapshot opts))
        ^CreateSnapshotResponse res (.actionGet ft)]
    ;; TODO: actually calculate this using RestStatus
    {:accepted true}))

(defn delete-snapshot
  "Deletes a snapshot"
  [^Client conn ^String repository ^String snapshot]
  (let [ft                           (es/admin-delete-snapshot conn (cnv/->delete-snapshot-request repository snapshot))
        ^DeleteSnapshotResponse res (.actionGet ft)]
    (cnv/acknowledged-response->map res)))
