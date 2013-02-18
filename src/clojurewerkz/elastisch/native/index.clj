(ns clojurewerkz.elastisch.native.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import [org.elasticsearch.action.admin.indices.exists.indices IndicesExistsResponse]
           [org.elasticsearch.action.admin.indices.create CreateIndexResponse]
           [org.elasticsearch.action.index IndexResponse]))

;;
;; API
;;

(defn create
  "The create index API allows to instantiate an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.native.index :as idx])

    (idx/create \"myapp_development\")
    (idx/create \"myapp_development\" :settings {\"number_of_shards\" 1})

    (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                               :first-name {:type \"string\" :store \"yes\"}
                                               :last-name  {:type \"string\"}
                                               :age        {:type \"integer\"}
                                               :title      {:type \"string\" :analyzer \"snowball\"}
                                               :planet     {:type \"string\"}
                                               :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
      (idx/create \"myapp_development\" :mappings mapping-types))

   Related ElasticSearch API Reference section:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [^String index-name & {:keys [settings mappings]}]
  (let [ft                       (es/admin-index-create (cnv/->create-index-request index-name settings mappings))
        ^CreateIndexResponse res (.get ft)]
    (.acknowledged res)))


(defn exists?
  "Returns true if given index (or indices) exists"
  [index-name]
  (let [ft                        (es/admin-index-exists (cnv/->index-exists-request index-name))
        ^IndicesExistsResponse res (.get ft)]
    (.exists res)))
