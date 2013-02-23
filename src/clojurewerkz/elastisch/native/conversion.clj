(ns clojurewerkz.elastisch.native.conversion
  (:refer-clojure :exclude [get merge flush])
  (:require [clojure.walk :as wlk])
  (:import [org.elasticsearch.common.settings Settings ImmutableSettings ImmutableSettings$Builder]
           [org.elasticsearch.common.transport
            TransportAddress InetSocketTransportAddress LocalTransportAddress]
           java.util.Map
           clojure.lang.IPersistentMap
           org.elasticsearch.common.xcontent.XContentType
           org.elasticsearch.index.VersionType
           ;; Actions
           org.elasticsearch.action.ShardOperationFailedException
           [org.elasticsearch.action.index IndexRequest IndexResponse]
           [org.elasticsearch.action.get GetRequest GetResponse MultiGetRequest MultiGetResponse MultiGetItemResponse]
           [org.elasticsearch.action.delete DeleteRequest DeleteResponse]
           [org.elasticsearch.action.count CountRequest CountResponse]
           [org.elasticsearch.action.search SearchRequest SearchResponse]
           [org.elasticsearch.search SearchHits SearchHit]
           ;; Administrative Actions
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequest
           org.elasticsearch.action.support.broadcast.BroadcastOperationResponse
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.optimize.OptimizeRequest
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.status.IndicesStatusRequest
           [org.elasticsearch.action.admin.indices.segments IndicesSegmentsRequest IndicesSegmentResponse IndexSegments]
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest))

;;
;; Implementation
;;

(defn- ^"[Ljava.lang.String;" ->string-array
  "Coerces argument to an array of strings"
  [index-name]
  (if (coll? index-name)
    (into-array String index-name)
    (into-array String [index-name])))

(def ^{:const true}
  default-content-type XContentType/JSON)

(defprotocol XContentTypeConversion
  (^XContentType to-content-type [input] "Picks a content type for given input"))

(extend-protocol XContentTypeConversion
  clojure.lang.Named
  (to-content-type [input]
    (to-content-type (name input)))

  String
  (to-content-type [^String s]
    (case (.toLowerCase s)
      "application/json" XContentType/JSON
      "text/json"        XContentType/JSON
      "json"             XContentType/JSON

      "application/smile" XContentType/SMILE
      "smile"             XContentType/SMILE
      XContentType/JSON))

  XContentType
  (to-content-type [input]
    input))

(defprotocol VersionTypeConversion
  (^VersionType to-version-type [input] "Picks a content type for given input"))

(extend-protocol VersionTypeConversion
  clojure.lang.Named
  (to-version-type [input]
    (to-version-type (name input)))

  String
  (to-version-type [^String s]
    (case (.toLowerCase s)
      "internal" VersionType/INTERNAL
      "external" VersionType/EXTERNAL
      VersionType/INTERNAL))

  VersionType
  (to-version-type [input]
    input))


;;
;; API
;;

;;
;; Settings
;;

(defn ^Settings ->settings
  "Converts a Clojure map into immutable ElasticSearch settings"
  [m]
  (if m
    (let [^ImmutableSettings$Builder sb (ImmutableSettings/settingsBuilder)]
      (doseq [[k v] m]
        (.put sb ^String (name k) ^String v))
      (.build sb))
    ImmutableSettings$Builder/EMPTY_SETTINGS))

;;
;; Transports
;;

(defn ^TransportAddress ->socket-transport-address
  [^String host ^long port]
  (InetSocketTransportAddress. host port))

(defn ^TransportAddress ->local-transport-address
  [^String id]
  (LocalTransportAddress. id))


;;
;; Indexing
;;

(defn ^IndexRequest ->index-request
  "Builds an index action request"
  ([index mapping-type ^Map doc]
     ;; default content type used by IndexRequest is JSON. MK.
     (-> (IndexRequest. (name index) (name mapping-type))
         (.setSource ^Map (wlk/stringify-keys doc))))
  ;; non-variadic because it is more convenient and efficient to
  ;; invoke this internal implementation fn this way. MK.
  ([index mapping-type ^Map doc ^String {:keys [id
                                                routing
                                                parent
                                                timestamp
                                                ttl
                                                op-type
                                                refresh
                                                version-type
                                                percolate
                                                content-type]}]
     (let [ir (-> (IndexRequest. (name index) (name mapping-type))
                  (.setSource ^Map (wlk/stringify-keys doc)))]
       (when id
         (.setId ir id))
       (when content-type
         (.setContentType ir (to-content-type content-type)))
       (when routing
         (.setRouting ir routing))
       (when parent
         (.setParent ir parent))
       (when timestamp
         (.setTimestamp ir timestamp))
       (when ttl
         (.setTtl ir ttl))
       (when op-type
         (.setOpType ir ^String (.toLowerCase (name op-type))))
       (when refresh
         (.setRefresh ir refresh))
       (when version-type
         (.setVersionType ir (to-version-type version-type)))
       (when percolate
         (.setPercolate ir percolate))
       ir)))

(defn ^IPersistentMap index-response->map
  "Converts an index action response to a Clojure map"
  [^IndexResponse r]
  ;; underscored aliases are there to match REST API responses
  {:index    (.getIndex r)
   :_index   (.getIndex r)
   :id       (.getId r)
   :_id      (.getId r)
   :type     (.getType r)
   :_type    (.getType r)
   :version  (.getVersion r)
   :_version (.getVersion r)
   :matches  (.getMatches r)})


;;
;; Actions
;;

(defn ^GetRequest ->get-request
  "Builds a get action request"
  ([index mapping-type ^String id]
     (GetRequest. (name index) (name mapping-type) id))
  ([index mapping-type ^String id {:keys [parent preference
                                          routing fields]}]
     (let [gr (GetRequest. (name index) (name mapping-type) id)]
       (when routing
         (.setRouting gr routing))
       (when parent
         (.setParent gr parent))
       (when preference
         (.setPreference gr preference))
       (when fields
         (.setFields gr (into-array String fields)))
       gr)))

(defn ^IPersistentMap get-response->map
  [^GetResponse r]
  (let [s (wlk/keywordize-keys (into {} (.getSourceAsMap r)))]
    ;; underscored aliases are there to match REST API responses
    {:exists? (.isExists r)
     :exists  (.isExists r)
     :index   (.getIndex r)
     :_index  (.getIndex r)
     :type    (.getType r)
     :_type   (.getType r)
     :id      (.getId r)
     :_id     (.getId r)
     :version  (.getVersion r)
     :_version (.getVersion r)
     :empty?   (.isSourceEmpty r)
     :source   s
     :_source  s
     ;; TODO: convert GetFields to maps
     :fields   (into {} (.getFields r))}))

(defn ^IPersistentMap multi-get-item-response->map
  [^MultiGetItemResponse i]
  (let [r  (.getResponse i)
        s  (wlk/keywordize-keys (into {} (.getSourceAsMap r)))]
    {:exists   (.isExists r)
     :_index   (.getIndex r)
     :_type    (.getType r)
     :_id      (.getId r)
     :_version (.getVersion r)
     :_source  s}))

(defn multi-get-response->seq
  [^MultiGetResponse r]
  (let [items (.getResponses r)]
    (map multi-get-item-response->map items)))

(defn ^MultiGetRequest ->multi-get-request
  "Builds a multi-get action request"
  ([queries]
     (->multi-get-request queries {}))
  ([queries {:keys [preference refresh realtime]}]
     (let [r (MultiGetRequest.)]
       (doseq [q queries]
         (.add r (:_index q) (:_type q) (:_id q)))
       (when preference
         (.setPreference r preference))
       (when refresh
         (.setRefresh r refresh))
       (when realtime
         (.setRealtime r realtime))
       r)))

(defn ^CountRequest ->count-request
  ([index-name options]
     (->count-request index-name [] options))
  ([index-name mapping-type {:keys [query min-score routing]}]
     (let [r (CountRequest. (->string-array index-name))]
       (.setTypes r (->string-array mapping-type))
       (when query
         (.setQuery r ^Map (wlk/stringify-keys query)))
       (when min-score
         (.setMinScore r min-score))
       (when routing
         (.setRouting r (->string-array routing)))
       r)))

(defn ^DeleteRequest ->delete-request
  ([index-name mapping-type id]
     (DeleteRequest. index-name mapping-type id))
  ([index-name mapping-type id {:keys [routing refresh version version-type parent]}]
     (let [r (DeleteRequest. index-name mapping-type id)]
       (when routing
         (.setRouting r routing))
       (when refresh
         (.setRefresh r refresh))
       (when refresh
         (.setRefresh r refresh))
       (when version
         (.setVersion r version))
       (when version-type
         (.setVersionType r version-type))
       (when parent
         (.setParent r parent))
       r)))

(defn ^IPersistentMap delete-response->map
  [^DeleteResponse r]
  ;; matches REST API responses
  {:found    (not (.isNotFound r))
   :found?   (not (.isNotFound r))
   :_index   (.getIndex r)
   :_type    (.getType r)
   :_version (.getVersion r)
   :_id      (.getId r)
   :ok       true})

(defn ^SearchRequest ->search-request
  [index-name mapping-type {:keys [search-type scroll routing
                                   preference] :as options}]
  (let [r  (doto (SearchRequest. (->string-array index-name))
             (.setTypes (->string-array mapping-type)))
        excludes [:search_type :scroll :routing :preference]
        source   (apply dissoc (concat [options] excludes))
        m        (wlk/stringify-keys source)]
    (.setSource r ^Map m)
    (when search-type
      (.setSearchType r ^String search-type))
    (when routing
      (.setRouting r ^String routing))
    (when scroll
      (.setScroll r ^String scroll))
    r))

(defn- ^IPersistentMap search-hit->map
  [^SearchHit sh]
  {:_index    (.getIndex sh)
   :_type     (.getType sh)
   :_id       (.getId sh)
   :_score    (.getScore sh)
   :_version  (.getVersion sh)
   :_source   (wlk/keywordize-keys (into {} (.getSource sh)))})

(defn- search-hits->seq
  [^SearchHits hits]
  {:total     (.getTotalHits hits)
   :max_score (.getMaxScore hits)
   :hits      (map search-hit->map (.getHits hits))})

(defn search-response->seq
  [^SearchResponse r]
  ;; Example REST API response:
  ;; 
  ;; {:took 18,
  ;;  :timed_out false,
  ;;  :_shards {:total 5, :successful 5, :failed 0},
  ;;  :hits {
  ;;    :total 4,
  ;;    :max_score 1.0,
  ;;    :hits [{:_index "articles",
  ;;       :_type "article",
  ;;       :_id 1,
  ;;       :_score 1.0,
  ;;       :_source {:latest-edit {:date "2012-03-26T06:07:00", :author nil},
  ;;                 :number-of-edits 10,
  ;;                 :language "English",
  ;;                 :title "ElasticSearch",
  ;;                 :url "http://en.wikipedia.org/wiki/ElasticSearch",
  ;;                 :summary "...",
  ;;                 :tags "..." }}
  ;;       {:_index "articles",
  ;;       :_type "article",
  ;;       :_id 2,
  ;;       :_score 1.0,
  ;;       :_source {:latest-edit {:date "2012-03-11T02:19:00", :author "..." },
  ;;                 :number-of-edits 48,
  ;;                 :language "English",
  ;;                 :title "Apache Lucene",
  ;;                 :url "http://en.wikipedia.org/wiki/Apache_Lucene",
  ;;                 :summary "..." }}]
  ;;  }
  ;; }
  {:took      (.getTookInMillis r)
   :timed_out (.isTimedOut r)
   :_shards   {:total      (.getTotalShards r)
               :successful (.getSuccessfulShards r)
               :failed     (.getFailedShards r)}
   :hits      (search-hits->seq (.getHits r))})

;;
;; Administrative Actions
;;

(defn ^IndicesExistsRequest ->index-exists-request
  [index-name]
  (IndicesExistsRequest. (->string-array index-name)))

(defn ^CreateIndexRequest ->create-index-request
  [index-name settings mappings]
  (let [r (CreateIndexRequest. index-name)
        m (wlk/stringify-keys mappings)]
    (when settings
      (.setSettings r ^Map settings))
    (when mappings
      (doseq [[k v] m]
        (.addMapping r ^String k ^Map v)))
    r))

(defn ^DeleteIndexRequest ->delete-index-request
  [index-name]
  (DeleteIndexRequest. (->string-array index-name)))

(defn ^UpdateSettingsRequest ->update-settings-request
  [index-name settings]
  (let [ary (->string-array index-name)
        m   (wlk/stringify-keys settings)]
    (doto (UpdateSettingsRequest. ary)
      (.setSettings ^Map m))))

(defn ^OpenIndexRequest ->open-index-request
  [index-name]
  (OpenIndexRequest. index-name))

(defn ^CloseIndexRequest ->close-index-request
  [index-name]
  (CloseIndexRequest. index-name))

(defn ^OptimizeRequest ->optimize-index-request
  [index-name {:keys [wait-for-merge max-num-segments only-expunge-deletes flush refresh]}]
  (let [ary (->string-array index-name)
        r   (OptimizeRequest. ary)]
    (when wait-for-merge
      (.setWaitForMerge r wait-for-merge))
    (when max-num-segments
      (.setMaxNumSegments r max-num-segments))
    (when only-expunge-deletes
      (.setOnlyExpungeDeletes r only-expunge-deletes))
    (when flush
      (.setFlush r flush))
    (when refresh
      (.setRefresh r refresh))
    r))

(defn ^FlushRequest ->flush-index-request
  [index-name {:keys [refresh force full]}]
  (let [ary (->string-array index-name)
        r   (FlushRequest. ary)]
    (when force
      (.setForce r force))
    (when full
      (.setFull r full))
    (when refresh
      (.setRefresh r refresh))
    r))

(defn ^RefreshRequest ->refresh-index-request
  [index-name]
  (RefreshRequest. (->string-array index-name)))

(defn ^IPersistentMap shard-operation-failed-exception->map
  [^ShardOperationFailedException e]
  {:index    (.index e)
   :shard-id (.shardId e)
   :reason   (.reason e)})

(defn ^IPersistentMap broadcast-operation-response->map
  [^BroadcastOperationResponse res]
  ;; matches REST API responses
  {:_shards {:total      (.getTotalShards res)
             :successful (.getSuccessfulShards res)
             :failed     (.getFailedShards res)
             :failures   (map shard-operation-failed-exception->map (.getShardFailures res))}})


(defn ^GatewaySnapshotRequest ->gateway-snapshot-request
  [index-name]
  (GatewaySnapshotRequest. (->string-array index-name)))

(defn ^ClearIndicesCacheRequest ->clear-indices-cache-request
  [index-name {:keys [filter-cache field-data-cache id-cache fields]}]
  (let [ary (->string-array index-name)
        r   (ClearIndicesCacheRequest. ary)]
    (when filter-cache
      (.setFilterCache r filter-cache))
    (when field-data-cache
      (.setFieldDataCache r field-data-cache))
    (when id-cache
      (.setIdCache r id-cache))
    (when fields
      (.setFields r (->string-array fields)))
    r))


(defn ^IndicesStatsRequest ->index-stats-request
  ([]
     (let [r (IndicesStatsRequest.)]
       (.all r)
       r))
  ([{:keys [docs store indexing types groups get
            search merge flush refresh]}]
     (let [r   (IndicesStatsRequest.)]
       (when docs
         (.setDocs r docs))
       (when store
         (.setStore r store))
       (when indexing
         (.setIndexing r indexing))
       (when types
         (.setTypes r (into-array String types)))
       (when groups
         (.setGroups r (into-array String groups)))
       (when get
         (.setGet r get))
       (when search
         (.setSearch r search))
       (when merge
         (.setMerge r merge))
       (when flush
         (.setFlush r flush))
       (when refresh
         (.setRefresh r refresh))
       r)))

(defn ^IndicesStatusRequest ->indices-status-request
  [index-name {:keys [recovery snapshot]}]
  (let [ary (->string-array index-name)
        r   (IndicesStatusRequest. ary)]
    (when recovery
      (.setRecovery r recovery))
    (when snapshot
      (.setSnapshot r snapshot))
    r))

(defn ^IndicesSegmentsRequest ->indices-segments-request
  [index-name]
  (IndicesSegmentsRequest. (->string-array index-name)))

(defn ^IPersistentMap index-segments->map
  [^IndexSegments segs]
  ;; TODO
  segs)

(defn ^IPersistentMap indices-segments-response->map
  [^IndicesSegmentResponse r]
  (reduce (fn [m [^String idx ^IndexSegments segs]]
            (assoc m idx (index-segments->map segs)))
          {}
          (.getIndices r)))


(defn- apply-add-alias
  [^IndicesAliasesRequest req {:keys [index alias filter]}]
  (if filter
    (.addAlias req ^String index ^String alias ^Map filter)
    (.addAlias req ^String index ^String alias))
  req)

(defn- apply-remove-alias
  [^IndicesAliasesRequest req {:keys {index alias}}]
  (.removeAlias req ^String index ^String alias)
  req)

(defn ^IndicesAliasesRequest ->indices-aliases-request
  [ops {:keys [timeout]}]
  (let [r (IndicesAliasesRequest.)]
    (doseq [{:keys [add remove timeout]} ops]
      (when add
        (apply-add-alias r add))
      (when remove
        (apply-remove-alias r remove)))
    (when timeout
      (.setTimeout r ^String timeout))
    r))

(defn ^PutIndexTemplateRequest ->put-index-template-request
  [template-name {:keys [template settings mappings order cause]}]
  (let [r (doto (PutIndexTemplateRequest. template-name)
            (.template template))]
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    (when mappings
      (doseq [[k v] (wlk/stringify-keys mappings)]
        (.mapping r ^String k ^Map v)))
    (when order
      (.order r order))
    (when cause
      (.cause r cause))
    r))

(defn ^PutIndexTemplateRequest ->create-index-template-request
  [template-name {:as options}]
  (doto (->put-index-template-request template-name options)
    (.create)))

(defn ^DeleteIndexTemplateRequest ->delete-index-template-request
  [template-name]
  (DeleteIndexTemplateRequest. template-name))
