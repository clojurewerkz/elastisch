;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.common.bulk)

(def ^:private special-operation-keys [:_doc_as_upsert
                                       :_index
                                       :_type
                                       :_id
                                       :_retry_on_conflict
                                       :_routing
                                       :_percolate
                                       :_parent
                                       :_script
                                       :_script_params
                                       :_scripted_upsert
                                       :_timestamp
                                       :_ttl])

(defn index-operation
  [doc]
  {"index" (select-keys doc special-operation-keys)})

(defn create-operation
  [doc]
  {"create" (select-keys doc special-operation-keys)})

(defn update-operation
  [doc]
  {"update" (select-keys doc special-operation-keys)})

(defn delete-operation
  [doc]
  {"delete" (select-keys doc special-operation-keys)})

(defn bulk-index
  "generates the content for a bulk insert operation"
  ([documents]
     (let [operations (map index-operation documents)
           documents  (map #(apply dissoc % special-operation-keys) documents)]
       (interleave operations documents))))

(defn bulk-create
  "generates the content for a bulk create operation"
  ([documents]
     (let [operations (map create-operation documents)
           documents  (map #(apply dissoc % special-operation-keys) documents)]
       (interleave operations documents))))

(defn bulk-update
  "generates the content for a bulk update operation"
  ([documents]
     (let [operations (map update-operation documents)
           documents  (map #(apply dissoc % special-operation-keys) documents)]
       (interleave operations documents))))

(defn bulk-delete
  "generates the content for a bulk delete operation"
  ([documents]
   (map delete-operation documents)))
