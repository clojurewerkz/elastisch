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
           [org.elasticsearch.action.search SearchRequest SearchResponse SearchScrollRequest]
           [org.elasticsearch.search.builder SearchSourceBuilder]
           [org.elasticsearch.search SearchHits SearchHit]
           [org.elasticsearch.search.facet Facets Facet]
           [org.elasticsearch.search.facet.terms TermsFacet TermsFacet$Entry]
           [org.elasticsearch.search.facet.range RangeFacet RangeFacet$Entry]
           [org.elasticsearch.search.facet.histogram HistogramFacet HistogramFacet$Entry]
           [org.elasticsearch.search.facet.datehistogram DateHistogramFacet DateHistogramFacet$Entry]
           org.elasticsearch.search.facet.statistical.StatisticalFacet
           [org.elasticsearch.search.facet.termsstats TermsStatsFacet TermsStatsFacet$Entry]
           [org.elasticsearch.search.facet.geodistance GeoDistanceFacet GeoDistanceFacet$Entry]
           org.elasticsearch.search.facet.query.QueryFacet
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
         (.source ^Map (wlk/stringify-keys doc))))
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
                  (.source ^Map (wlk/stringify-keys doc)))]
       (when id
         (.id ir ^String id))
       (when content-type
         (.contentType ir (to-content-type content-type)))
       (when routing
         (.routing ir ^String routing))
       (when parent
         (.parent ir ^String parent))
       (when timestamp
         (.timestamp ir timestamp))
       (when ttl
         (.ttl ir ttl))
       (when op-type
         (.opType ir ^String (.toLowerCase (name op-type))))
       (when refresh
         (.refresh ir refresh))
       (when version-type
         (.versionType ir (to-version-type version-type)))
       (when percolate
         (.percolate ir percolate))
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
         (.routing gr routing))
       (when parent
         (.parent gr parent))
       (when preference
         (.preference gr preference))
       (when fields
         (.fields gr (into-array String fields)))
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
         (.preference r preference))
       (when refresh
         (.refresh r refresh))
       (when realtime
         (.realtime r realtime))
       r)))

(defn ^CountRequest ->count-request
  ([index-name options]
     (->count-request index-name [] options))
  ([index-name mapping-type {:keys [query min-score routing]}]
     (let [r (CountRequest. (->string-array index-name))]
       (.types r (->string-array mapping-type))
       (when query
         (.query r ^Map (wlk/stringify-keys query)))
       (when min-score
         (.minScore r min-score))
       (when routing
         (.routing r (->string-array routing)))
       r)))

(defn ^DeleteRequest ->delete-request
  ([index-name mapping-type id]
     (DeleteRequest. index-name mapping-type id))
  ([index-name mapping-type id {:keys [routing refresh version version-type parent]}]
     (let [r (DeleteRequest. index-name mapping-type id)]
       (when routing
         (.routing r routing))
       (when refresh
         (.refresh r refresh))
       (when version
         (.version r version))
       (when version-type
         (.versionType r version-type))
       (when parent
         (.parent r parent))
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
  [index-name mapping-type {:keys [search-type search_type scroll routing
                                   preference
                                   query facets from size timeout filter
                                   min_score version fields sort stats] :as options}]
  (let [r                       (SearchRequest.)
        ^SearchSourceBuilder sb (SearchSourceBuilder.)]

    ;; source
    (when query
      (.query sb ^Map (wlk/stringify-keys query)))
    (when facets
      (.facets sb ^Map (wlk/stringify-keys facets)))
    (when from
      (.from sb from))
    (when size
      (.size sb size))
    (when timeout
      (.timeout sb ^String timeout))
    (when filter
      (.filter sb ^Map (wlk/stringify-keys filter)))
    (when fields
      (.fields sb ^java.util.List fields))
    (when version
      (.version sb version))
    ;; TODO: map support, asc/desc
    (when sort
      (.sort sb ^String sort))
    (when stats
      (.stats sb ->string-array stats))
    (.source r sb)

    ;; non-source
    (when index-name
      (.indices r (->string-array index-name)))
    (when mapping-type
      (.types r (->string-array mapping-type)))
    (when-let [s (or search-type search_type)]
      (.searchType r ^String s))
    (when routing
      (.routing r ^String routing))
    (when scroll
      (.scroll r ^String scroll))

    r))

(defn ^SearchScrollRequest ->search-scroll-request
  [^String scroll-id {:keys [scroll]}]
  (let [r (SearchScrollRequest. scroll-id)]
    (when scroll
      (.scroll r ^String scroll))
    r))

(defprotocol ToClojure
  "Auxilliary protocol that is used to recursively convert
   Java maps to Clojure maps"
  (as-clj [o]))

(extend-protocol ToClojure
  java.util.Map
  (as-clj [o] (reduce (fn [m [^String k v]]
                        (assoc m (keyword k) (as-clj v)))
                      {} (.entrySet o)))

  java.util.List
  (as-clj [o] (vec (map as-clj o)))

  java.lang.Object
  (as-clj [o] o)

  nil
  (as-clj [_] nil))




(defn- ^IPersistentMap search-hit->map
  [^SearchHit sh]
  {:_index    (.getIndex sh)
   :_type     (.getType sh)
   :_id       (.getId sh)
   :_score    (.getScore sh)
   :_version  (.getVersion sh)
   :_source   (wlk/keywordize-keys (as-clj (.getSource sh)))})

(defn- search-hits->seq
  [^SearchHits hits]
  {:total     (.getTotalHits hits)
   :max_score (.getMaxScore hits)
   :hits      (map search-hit->map (.getHits hits))})

(defprotocol FacetConversion
  (^IPersistentMap facet-to-map [facet] "Converts a facet into a Clojure map"))
(extend-protocol FacetConversion
  ;; {:tags {:_type terms,
  ;;         :missing 0,
  ;;         :total 26,
  ;;         :other 6,
  ;;         :terms [{:term text, :count 2}
  ;;                 {:term technology, :count 2}
  ;;                 {:term software, :count 2}
  ;;                 {:term search, :count 2}
  ;;                 {:term opensource, :count 2}
  ;;                 {:term norteamérica, :count 2}
  ;;                 {:term lucene, :count 2}
  ;;                 {:term historia, :count 2}
  ;;                 {:term geografía, :count 2}
  ;;                 {:term full, :count 2}]}}
  TermsFacet
  (facet-to-map [^TermsFacet ft]
    {:_type TermsFacet/TYPE
     :missing (.getMissingCount ft)
     :total   (.getTotalCount ft)
     :other   (.getOtherCount ft)
     :terms (map (fn [^TermsFacet$Entry et]
                   ;; TODO: terms may have bytes and not string representation. MK.
                   {:term (-> et .getTerm .string) :count (.getCount et)})
                 (.getEntries ft))})

  ;; {:ages {:_type range,
  ;;         :ranges [{:from 18.0, :to 20.0, :count 0, :total_count 0, :total 0.0, :mean 0.0}
  ;;                  {:from 21.0, :to 25.0, :count 1, :min 22.0, :max 22.0, :total_count 1, :total 22.0, :mean 22.0}
  ;;                  {:from 26.0, :to 30.0, :count 2, :min 28.0, :max 29.0, :total_count 2, :total 57.0, :mean 28.5}
  ;;                  {:from 30.0, :to 35.0, :count 0, :total_count 0, :total 0.0, :mean 0.0}
  ;;                  {:to 45.0, :count 4, :min 22.0, :max 37.0, :total_count 4, :total 116.0, :mean 29.0}]}}
  RangeFacet
  (facet-to-map [^RangeFacet ft]
    {:_type  RangeFacet/TYPE
     :ranges (map (fn [^RangeFacet$Entry et]
                    {:from (.getFrom et) :to (.getTo et) :count (.getCount et) :total_count (.getTotalCount et) :total (.getTotal et)
                     :mean (.getMean et) :min (.getMin et) :max (.getMax et)})
                  (.getEntries ft))})

  ;; {:ages {:_type histogram,
  ;;         :entries [{:key 20, :count 1}
  ;;                   {:key 25, :count 2}
  ;;                   {:key 35, :count 1}]}}
  HistogramFacet
  (facet-to-map [^HistogramFacet ft]
    {:_type   HistogramFacet/TYPE
     :entries (map (fn [^HistogramFacet$Entry et]
                     {:key (.getKey et) :count (.getCount et) :total_count (.getTotalCount et)
                      :mean (.getMean et) :min (.getMin et) :max (.getMax et)})
                   (.getEntries ft))})

  ;; {:dates {:_type date_histogram,
  ;;          :entries [{:time 1343685600000, :count 1}
  ;;                    {:time 1343761200000, :count 1}
  ;;                    {:time 1343804400000, :count 1}
  ;;                    {:time 1343836800000, :count 1}
  ;;                    {:time 1343898000000, :count 1}]}}
  DateHistogramFacet
  (facet-to-map [^DateHistogramFacet ft]
    {:_type   DateHistogramFacet/TYPE
     :entries (map (fn [^DateHistogramFacet$Entry et]
                     {:time (.getTime et) :count (.getCount et) :total_count (.getTotalCount et)
                      :mean (.getMean et) :min (.getMin et) :max (.getMax et)})
                   (.getEntries ft))})

  ;; {:comments {:total 68.0,
  ;;             :mean 22.666666666666668,
  ;;             :count 3,
  ;;             :max 44.0,
  ;;             :std_deviation 16.438437341250605,
  ;;             :sum_of_squares 2352.0,
  ;;             :min 4.0,
  ;;             :variance 270.22222222222223,
  ;;             :_type "statistical"}}
  StatisticalFacet
  (facet-to-map [^StatisticalFacet ft]
    {:_type   StatisticalFacet/TYPE
     :count (.getCount ft) :total (.getTotal ft) :sum_of_squares (.getSumOfSquares ft)
     :mean (.getMean ft) :min (.getMin ft) :max (.getMax ft) :variance (.getVariance ft)
     :std_deviation (.getStdDeviation ft)})

  ;; {:comments {:_type "terms_stats",
  ;;             :missing 0,
  ;;             :terms ({:term "boom", :count 2, :total_count 2, :min 100.0, :max 388.0, :total 488.0, :mean 244.0}
  ;;                     {:term "aha", :count 2, :total_count 2, :min 20.0, :max 120.0, :total 140.0, :mean 70.0}
  ;;                     {:term "wheeeeeha", :count 1, :total_count 1, :min 4.0, :max 4.0, :total 4.0, :mean 4.0}
  ;;                     {:term "booya", :count 1, :total_count 1, :min 44.0, :max 44.0, :total 44.0, :mean 44.0})}}
  TermsStatsFacet
  (facet-to-map [^TermsStatsFacet ft]
    {:_type TermsStatsFacet/TYPE
     :missing (.getMissingCount ft)
     :terms (map (fn [^TermsStatsFacet$Entry et]
                   ;; TODO: terms may have bytes and not string representation. MK.
                   {:term (-> et .getTerm .string) :count (.getCount et) :total_count (.getTotalCount et)
                    :min (.getMin et) :max (.getMax et) :total (.getTotal et) :mean (.getMean et)})
                 (.getEntries ft))})

  GeoDistanceFacet
  (facet-to-map [^GeoDistanceFacet ft]
    {:_type GeoDistanceFacet/TYPE
     :terms (map (fn [^GeoDistanceFacet$Entry et]
                   {:from (.getFrom et) :to (.getTo et) :count (.getCount et) :total_count (.getTotalCount et) :total (.getTotal et)
                     :mean (.getMean et) :min (.getMin et) :max (.getMax et)})
                 (.getEntries ft))})

  QueryFacet
  (facet-to-map [^QueryFacet ft]
    {:_type   QueryFacet/TYPE
     :count   (.getCount ft)}))




(defn- search-facets->seq
  [^Facets facets]
  (when facets
    (reduce (fn [acc [^String name ^Facet facet]]
              (assoc acc (keyword name) (facet-to-map facet)))
            {}
            (.facetsAsMap facets))))

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
  {:took       (.getTookInMillis r)
   :timed_out  (.isTimedOut r)
   :_scroll_id (.getScrollId r)
   :facets     (search-facets->seq (.getFacets r))
   ;; TODO: facets
   ;; TODO: suggestions
   :_shards    {:total      (.getTotalShards r)
                :successful (.getSuccessfulShards r)
                :failed     (.getFailedShards r)}
   :hits       (search-hits->seq (.getHits r))})

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
      (.settings r ^Map settings))
    (when mappings
      (doseq [[k v] m]
        (.mapping r ^String k ^Map v)))
    r))

(defn ^DeleteIndexRequest ->delete-index-request
  ([]
     (DeleteIndexRequest. (->string-array [])))
  ([index-name]
     (DeleteIndexRequest. (->string-array index-name))))

(defn ^UpdateSettingsRequest ->update-settings-request
  [index-name settings]
  (let [ary (->string-array index-name)
        m   (wlk/stringify-keys settings)]
    (doto (UpdateSettingsRequest. ary)
      (.settings ^Map m))))

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
      (.waitForMerge r wait-for-merge))
    (when max-num-segments
      (.maxNumSegments r max-num-segments))
    (when only-expunge-deletes
      (.onlyExpungeDeletes r only-expunge-deletes))
    (when flush
      (.flush r flush))
    (when refresh
      (.refresh r refresh))
    r))

(defn ^FlushRequest ->flush-index-request
  [index-name {:keys [refresh force full]}]
  (let [ary (->string-array index-name)
        r   (FlushRequest. ary)]
    (when force
      (.force r force))
    (when full
      (.full r full))
    (when refresh
      (.refresh r refresh))
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
      (.filterCache r filter-cache))
    (when field-data-cache
      (.fieldDataCache r field-data-cache))
    (when id-cache
      (.idCache r id-cache))
    (when fields
      (.fields r (->string-array fields)))
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
         (.docs r docs))
       (when store
         (.store r store))
       (when indexing
         (.indexing r indexing))
       (when types
         (.types r (into-array String types)))
       (when groups
         (.groups r (into-array String groups)))
       (when get
         (.get r get))
       (when search
         (.search r search))
       (when merge
         (.merge r merge))
       (when flush
         (.flush r flush))
       (when refresh
         (.refresh r refresh))
       r)))

(defn ^IndicesStatusRequest ->indices-status-request
  [index-name {:keys [recovery snapshot]}]
  (let [ary (->string-array index-name)
        r   (IndicesStatusRequest. ary)]
    (when recovery
      (.recovery r recovery))
    (when snapshot
      (.snapshot r snapshot))
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
      (.timeout r ^String timeout))
    r))

(defn ^PutIndexTemplateRequest ->put-index-template-request
  [template-name {:keys [template settings mappings order]}]
  (let [r (doto (PutIndexTemplateRequest. template-name)
            (.template template))]
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    (when mappings
      (doseq [[k v] (wlk/stringify-keys mappings)]
        (.mapping r ^String k ^Map v)))
    (when order
      (.order r order))
    r))

(defn ^PutIndexTemplateRequest ->create-index-template-request
  [template-name {:as options}]
  (doto (->put-index-template-request template-name options)
    (.create)))

(defn ^DeleteIndexTemplateRequest ->delete-index-template-request
  [template-name]
  (DeleteIndexTemplateRequest. template-name))
