;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native.conversion
  (:refer-clojure :exclude [get merge flush])
  (:require [clojure.walk :as wlk])
  (:import [org.elasticsearch.common.settings Settings ImmutableSettings ImmutableSettings$Builder]
           [org.elasticsearch.common.transport
            TransportAddress InetSocketTransportAddress LocalTransportAddress]
           java.util.Map
           clojure.lang.IPersistentMap
           org.elasticsearch.client.Client
           org.elasticsearch.common.xcontent.XContentType
           org.elasticsearch.index.VersionType
           [org.elasticsearch.search.highlight HighlightBuilder HighlightBuilder$Field
            HighlightField]
           org.elasticsearch.common.text.Text
           ;; Actions
           org.elasticsearch.action.ShardOperationFailedException
           [org.elasticsearch.action.index IndexRequest IndexResponse]
           [org.elasticsearch.index.get GetResult]
           [org.elasticsearch.action.get GetRequest GetResponse MultiGetRequest MultiGetResponse MultiGetItemResponse]
           [org.elasticsearch.action.delete DeleteRequest DeleteResponse]
           [org.elasticsearch.action.update UpdateRequest UpdateResponse]
           [org.elasticsearch.action.deletebyquery DeleteByQueryRequest DeleteByQueryResponse IndexDeleteByQueryResponse]
           [org.elasticsearch.action.count CountRequest CountResponse]
           [org.elasticsearch.action.search SearchRequest SearchResponse SearchScrollRequest
            MultiSearchRequestBuilder MultiSearchRequest MultiSearchResponse MultiSearchResponse$Item]
           [org.elasticsearch.search.builder SearchSourceBuilder]
           [org.elasticsearch.search.sort SortBuilder SortOrder]
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
           org.elasticsearch.action.mlt.MoreLikeThisRequest
           [org.elasticsearch.action.percolate PercolateRequestBuilder PercolateResponse PercolateResponse$Match]
           ;; Aggregations
           org.elasticsearch.search.aggregations.metrics.avg.Avg
           org.elasticsearch.search.aggregations.metrics.max.Max
           org.elasticsearch.search.aggregations.metrics.min.Min
           org.elasticsearch.search.aggregations.metrics.sum.Sum
           [org.elasticsearch.search.aggregations.metrics.percentiles Percentiles Percentile]
           org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality
           org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount
           org.elasticsearch.search.aggregations.metrics.stats.Stats
           org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats
           [org.elasticsearch.search.aggregations.bucket.histogram  Histogram Histogram$Bucket
            DateHistogram DateHistogram$Bucket]
           [org.elasticsearch.search.aggregations.bucket.range      Range     Range$Bucket]
           [org.elasticsearch.search.aggregations.bucket.range.date DateRange DateRange$Bucket]
           [org.elasticsearch.search.aggregations.bucket.terms      Terms     Terms$Bucket]
           org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation
           ;; Administrative Actions
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           [org.elasticsearch.action.admin.indices.mapping.get GetMappingsRequest GetMappingsResponse]
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
           org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.optimize.OptimizeRequest
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.status.IndicesStatusRequest
           [org.elasticsearch.action.admin.indices.segments IndicesSegmentsRequest IndicesSegmentResponse IndexSegments]
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest
           org.elasticsearch.common.hppc.cursors.ObjectObjectCursor
           org.elasticsearch.common.collect.ImmutableOpenMap
           org.elasticsearch.cluster.metadata.MappingMetaData
           org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest
           org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest
           org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest
           org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest
           org.elasticsearch.action.support.broadcast.BroadcastOperationResponse
           org.elasticsearch.action.support.master.AcknowledgedResponse))

;;
;; Implementation
;;

(defn ^"[Ljava.lang.String;" ->string-array
  "Coerces argument to an array of strings"
  [index-name]
  (if (coll? index-name)
    (into-array String index-name)
    (into-array String [index-name])))

(defprotocol DeepMapConversion
  (deep-java-map->map [o]))

(extend-protocol DeepMapConversion
  java.util.Map
  (deep-java-map->map [o]
    (reduce (fn [m [^String k v]]
              (assoc m (keyword k) (deep-java-map->map v)))
            {}
            (.entrySet o)))

  java.util.List
  (deep-java-map->map [o]
    (vec (map deep-java-map->map o)))

  java.lang.Object
  (deep-java-map->map [o] o)

  nil
  (deep-java-map->map [_] nil))

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
        (.put sb ^String (name k) v))
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
  ([index mapping-type ^Map doc {:keys [id
                                        routing
                                        parent
                                        timestamp
                                        ttl
                                        op-type
                                        refresh
                                        version
                                        version-type
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
       (when version
         (.version ir version))
       (when version-type
         (.versionType ir (to-version-type version-type)))
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
   :_version (.getVersion r)})


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

(defn- convert-source-result
  "Copied from clj-elasticsearch. More performant than doing wlk/keywordize-keys."
  [src]
  (cond
   (instance? java.util.HashMap src) (into {}
                                           (map (fn [^java.util.Map$Entry e]
                                                  [(keyword (.getKey e))
                                                   (convert-source-result (.getValue e))]) src))
   (instance? java.util.ArrayList src) (into [] (map convert-source-result src))
   :else src))

(defn- convert-fields-result
  "Get fields from search result, i.e. when filtering returned fields."
  [fields]
  (cond
   (instance? java.util.Map fields) (into {} (map (fn [^java.util.Map$Entry e]
                                                    [(keyword (.getKey e))
                                                     (.. e getValue getValue)]) fields))
   :else fields))

(defn ^IPersistentMap get-response->map
  [^GetResponse r]
  (let [s (convert-source-result (.getSourceAsMap r))]
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

(defn ^IPersistentMap get-result->map
  [^GetResult r]
  (let [s (convert-source-result (.sourceAsMap r))]
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
        s  (convert-source-result (.getSourceAsMap r))]
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
  ([index-name mapping-type {:keys [^Map source min-score routing]}]
     (let [^CountRequest r (CountRequest. (->string-array index-name))]
       (.types r (->string-array mapping-type))
       (when source
         (let [^Map m {"query" (wlk/stringify-keys source)}]
           (.source r m)))
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
  {:found    (.isFound r)
   :found?   (.isFound r)
   :_index   (.getIndex r)
   :_type    (.getType r)
   :_version (.getVersion r)
   :_id      (.getId r)})

(defn ^UpdateRequest ->update-request
  ([index-name mapping-type ^String id ^String script]
     (doto (UpdateRequest. index-name mapping-type id)
       (.script script)))
  ([index-name mapping-type ^String id ^String script ^Map params]
     (let [r (UpdateRequest. index-name mapping-type id)]
       (.script r script)
       (.scriptParams r ^Map params)
       r))
  ([index-name mapping-type ^String id ^String script ^Map params {:keys [script routing refresh fields parent]}]
     (let [r (UpdateRequest. index-name mapping-type id)]
       (.script r script)
       (.scriptParams r ^Map params)
       (when refresh
         (.refresh r))
       (when routing
         (.routing r routing))
       (when parent
         (.parent r parent))
       (when fields
         (.fields r (->string-array fields)))
       r)))

(defn ^UpdateRequest ->upsert-request
  ([index-name mapping-type ^String id ^Map doc {:keys [script routing refresh fields parent]}]
     (let [doc (wlk/stringify-keys doc)
           r   (UpdateRequest. index-name mapping-type id)]
       (.doc r ^Map doc)
       (.upsert r ^Map doc)
       (when refresh
         (.refresh r))
       (when routing
         (.routing r routing))
       (when parent
         (.parent r parent))
       (when fields
         (.fields r (->string-array fields)))
       r)))

(defn ^IPersistentMap update-response->map
  [^UpdateResponse r]
  ;; matches REST API responses
  ;; example: {:ok true, :_index people, :_type person, :_id 1, :_version 2}
  {:_index (.getIndex r) :type (.getType r) :_id (.getId r)
   :get-result (when-let [gr (.getGetResult r)]
                 (get-result->map gr))})

(defn ^DeleteByQueryRequest ->delete-by-query-request
  ([index mapping-type ^Map source]
     (let [^Map m {"query" (wlk/stringify-keys source)}]
       (doto (DeleteByQueryRequest. (->string-array index))
         (.source m)
         (.types (->string-array mapping-type)))))
  ([index mapping-type source {:keys [routing]}]
     (let [^Map m {"query" (wlk/stringify-keys source)}
           r      (doto (DeleteByQueryRequest. (->string-array index))
                    (.source m)
                    (.types (->string-array mapping-type)))]
       (when routing
         (.routing r (->string-array routing)))
       r)))

(defn ^DeleteByQueryRequest ->delete-by-query-request-across-all-types
  ([index ^Map source]
     (let [^Map m {"query" (wlk/stringify-keys source)}]
       (doto (DeleteByQueryRequest. (->string-array index))
         (.source m))))
  ([index source {:keys [routing]}]
     (let [^Map m {"query" (wlk/stringify-keys source)}
           r      (doto (DeleteByQueryRequest. (->string-array index))
                    (.source m))]
       (when routing
         (.routing r (->string-array routing)))
       r)))

(defn ^DeleteByQueryRequest ->delete-by-query-request-across-all-indices-and-types
  ([^Map source]
     (let [^Map m {"query" (wlk/stringify-keys source)}]
       (doto (DeleteByQueryRequest.)
         (.source m))))
  ([source {:keys [routing]}]
     (let [^Map m {"query" (wlk/stringify-keys source)}
           r      (doto (DeleteByQueryRequest.)
                    (.source m))]
       (when routing
         (.routing r (->string-array routing)))
       r)))

(defn- ^IPersistentMap index-delete-by-query-response->map
  [m [^String k ^IndexDeleteByQueryResponse v]]
  (assoc m (keyword k) {:_shards {:total      (.getTotalShards v)
                                  :successful (.getSuccessfulShards v)
                                  :failed     (.getFailedShards v)}}))

(defn ^IPersistentMap delete-by-query-response->map
  [^DeleteByQueryResponse r]
  ;; Example REST API response:
  ;; {:ok true, :_indices {:people {:_shards {:total 5, :successful 5, :failed 0}}}}
  {:ok       true
   :_indices (reduce index-delete-by-query-response->map {} (.getIndices r))})

(defn ^SortOrder ^:private ->sort-order
  [s]
  (SortOrder/valueOf (.toUpperCase (name s))))

(defn ^SearchSourceBuilder ^:private set-sort
  [^SearchSourceBuilder sb sort]
  (cond
   (instance? String sort)       (.sort sb ^String sort)
   ;; Allow 'sort' to be a SortBuilder, such as a GeoDistanceSortBuilder.
   (instance? SortBuilder sort)  (.sort sb ^SortBuilder sort)
   ;; map
   :else  (doseq [[k v] sort]
            (.sort sb (name k) (->sort-order (name v)))))
  sb)

(defn ^:private add-partial-fields-to-builder
  [^SearchSourceBuilder sb _source]
  (cond
   (nil? _source)        sb
   (false? _source)      (.fetchSource sb false)
   (map? _source)        (let [m  (wlk/stringify-keys _source)
                               in (->string-array (m "include" []))
                               ex (->string-array (m "exclude" []))]
                           (.fetchSource sb in ex))
   (sequential? _source) (.fetchSource sb (->string-array _source)
                                       (->string-array []))
   :else sb))

(defn ^HighlightBuilder$Field make-field
  [field-name {:keys [type pre_tags post_tags order
                      highlight_filter fragment_size number_of_fragments
                      encoder require_field_match boundary_max_scan
                      boundary_chars fragmenter highlight_query no_match_size
                      phrase_limit force_source] :as opts}]
  (let [fd (HighlightBuilder$Field. (name field-name))]
    (when type
      (.highlighterType fd type))
    (when pre_tags
      (.preTags fd (->string-array pre_tags)))
    (when post_tags
      (.postTags fd (->string-array post_tags)))
    (when order
      (.order fd order))
    (when highlight_filter
      (.highlightFilter fd highlight_filter))
    (when fragment_size
      (.fragmentSize fd (Integer/valueOf ^long fragment_size)))
    (when number_of_fragments
      (.numOfFragments fd (Integer/valueOf ^long number_of_fragments)))
    (when require_field_match
      (.requireFieldMatch fd require_field_match))
    (when boundary_max_scan
      (.boundaryMaxScan fd boundary_max_scan))
    ;; TODO: boundary_chars
    (when fragmenter
      (.fragmenter fd fragmenter))
    (when force_source
      (.forceSource fd force_source))
    ;; TODO: highlight_query
    ;; TODO: no_match_size
    ;; TODO: phrase_limit
    ;; TODO: custom highlighter options
    fd))

(defn ^:private add-highlight-to-builder
  [^SearchSourceBuilder sb {:keys [fields type tags_schema pre_tags post_tags order
                                   highlight_filter fragment_size number_of_fragments
                                   encoder require_field_match boundary_max_scan
                                   boundary_chars fragmenter highlight_query no_match_size
                                   phrase_limit force_source] :as opts}]
  (let [^HighlightBuilder hb (.highlighter sb)]
    (when type
      (.highlighterType hb type))
    (when tags_schema
      (.tagsSchema hb tags_schema))
    (when pre_tags
      (.preTags hb (->string-array pre_tags)))
    (when post_tags
      (.postTags hb (->string-array post_tags)))
    (when order
      (.order hb order))
    (when highlight_filter
      (.highlightFilter hb highlight_filter))
    (when fragment_size
      (.fragmentSize hb (Integer/valueOf ^long fragment_size)))
    (when number_of_fragments
      (.numOfFragments hb (Integer/valueOf ^long number_of_fragments)))
    (when encoder
      (.encoder hb encoder))
    (when require_field_match
      (.requireFieldMatch hb require_field_match))
    (when boundary_max_scan
      (.boundaryMaxScan hb boundary_max_scan))
    ;; TODO: boundary_chars
    (when fragmenter
      (.fragmenter hb fragmenter))
    (when force_source
      (.forceSource hb force_source))
    ;; TODO: highlight_query
    ;; TODO: no_match_size
    ;; TODO: phrase_limit
    ;; TODO: custom highlighter options
    (doseq [[k v] fields]
      (.field hb (make-field k v)))
    ))

(defn ^SearchRequest ->search-request
  [index-name mapping-type {:keys [search-type search_type scroll routing
                                   preference query facets aggregations from size timeout
                                   post-filter min_score version fields sort stats _source
                                   highlight] :as options}]
  (let [r                       (SearchRequest.)
        ^SearchSourceBuilder sb (SearchSourceBuilder.)]

    ;; source
    (when query
      (.query sb ^Map (wlk/stringify-keys query)))
    (when facets
      (.facets sb ^Map (wlk/stringify-keys facets)))
    (when aggregations
      (.aggregations sb ^Map (wlk/stringify-keys aggregations)))
    (when from
      (.from sb from))
    (when size
      (.size sb size))
    (when timeout
      (.timeout sb ^String timeout))
    (when post-filter
      (.postFilter sb ^Map (wlk/stringify-keys post-filter)))
    (when fields
      (.fields sb ^java.util.List fields))
    (when _source
      (add-partial-fields-to-builder sb _source))
    (when version
      (.version sb version))
    (when sort
      (set-sort sb sort))
    (when stats
      (.stats sb (->string-array stats)))
    (when highlight
      (add-highlight-to-builder sb highlight))

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

(defn ^MultiSearchRequest ->multi-search-request
  ([^Client conn queries opts]
     (let [sb (MultiSearchRequestBuilder. conn)]
       ;; pairs of [{:index "index name" :type "mapping type"}, search-options]
       (doseq [[{:keys [index type]} search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb)))
  ([^Client conn ^String index queries opts]
     (let [sb (MultiSearchRequestBuilder. conn)]
       (doseq [[{:keys [type]} search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb)))
  ([^Client conn ^String index ^String type queries opts]
     (let [sb (MultiSearchRequestBuilder. conn)]
       (doseq [[_ search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb))))

(defn ^SearchScrollRequest ->search-scroll-request
  [^String scroll-id {:keys [scroll]}]
  (let [r (SearchScrollRequest. scroll-id)]
    (when scroll
      (.scroll r ^String scroll))
    r))

(defn ^MoreLikeThisRequest ->more-like-this-request
  [^String index ^String mapping-type ^String id {:keys [routing fields mlt_fields
                                                         percent-terms-to-match percent_terms_to_match
                                                         max-query-terms max_query_terms
                                                         stop-words stop_words
                                                         min-doc-freq min_doc_freq
                                                         min-word-len min_word_len
                                                         max-word-len max_word_len
                                                         boost-terms boost_terms
                                                         query source
                                                         search-type search_type
                                                         size from]}]
  (let [r (doto (MoreLikeThisRequest. index)
            (.type mapping-type)
            (.id id))]
    (when routing
      (.routing r routing))
    (when-let [xs (or mlt_fields fields)]
      (.fields r (->string-array xs)))
    (when-let [v (or percent-terms-to-match percent_terms_to_match)]
      (.percentTermsToMatch r (Float/valueOf ^double v)))
    (when-let [v (or max-query-terms max_query_terms)]
      (.maxQueryTerms r (Integer/valueOf ^long v)))
    (when-let [v (or stop-words stop_words)]
      (.stopWords r (->string-array v)))
    (when-let [v (or min-doc-freq min_doc_freq)]
      (.minDocFreq r (Integer/valueOf ^long v)))
    (when-let [v (or min-word-len min_word_len)]
      (.minWordLen r (Integer/valueOf ^long v)))
    (when-let [v (or max-word-len max_word_len)]
      (.maxWordLen r (Integer/valueOf ^long v)))
    (when-let [v (or boost-terms boost_terms)]
      (.boostTerms r (Float/valueOf ^double v)))
    (when-let [q (or query source)]
      (.searchSource r ^Map (wlk/stringify-keys q)))
    (when-let [v (or search-type search_type)]
      (.searchType r ^String v))
    (when size
      (.searchSize r (Integer/valueOf ^long size)))
    (when from
      (.searchFrom r (Integer/valueOf ^long from)))
    r))

(defn ^:private highlight-field-to-map
  [^HighlightField hlf]
  {})

(defn ^:private add-highlight-from
  [^SearchHit sh m]
  (let [hls (.highlightFields sh)
        hlm (reduce (fn [acc [^String k ^HighlightField hlf]]
                      (assoc acc (keyword k) (vec (map (fn [^Text t]
                                                         (.string t)) (.getFragments hlf)))))
                    {}
                    hls)]
    (assoc m :highlight hlm)))

(defn- ^IPersistentMap search-hit->map
  [^SearchHit sh]
  (let [source (.getSource sh)
        fs (dissoc (convert-fields-result (.getFields sh)) :_source)
        result (add-highlight-from sh {:_index    (.getIndex sh)
                                       :_type     (.getType sh)
                                       :_id       (.getId sh)
                                       :_score    (.getScore sh)
                                       :_version  (.getVersion sh)})
        result-with-source (if source
                             (assoc result :_source (convert-source-result source))
                             result)]
    (if-not (or (nil? fs)
                (empty? fs))
      (assoc result-with-source
        :_fields fs
        :fields fs)
      result-with-source)))

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
                      :mean (.getMean et) :min (.getMin et) :max (.getMax et) :total (.getTotal et)})
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

  ;; TODO: filter facets

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

(defn histogram-bucket->map
  [^Histogram$Bucket b]
  {:key_as_string (.getKey b) :doc_count (.getDocCount b) :key (.. b getKeyAsNumber longValue)})

(defn range-bucket->map
  [^Range$Bucket b]
  {:doc_count (.getDocCount b)
   :from_as_string (String/valueOf ^long (.. b getFrom longValue))
   :from (.. b getFrom longValue)
   :to_as_string (String/valueOf ^long (.. b getTo longValue))
   :to (.. b getTo longValue)})

(defn date-range-bucket->map
  [^DateRange$Bucket b]
  {:doc_count (.getDocCount b)
   ;; :from_as_string, :to_as_string requires knowing what format the values
   ;; are in. We can format them using org.elasticsearch.common.joda.FormatDateTimeFormatter
   ;; but since aggregations can be arbitrarily nested, this is much trickier
   ;; than simply passing the formatter from native.document/search. MK.
   :from (.getFromAsDate b)
   :to (.getToAsDate b)})

(defn date-histogram-bucket->map
  [^DateHistogram$Bucket b]
  {:doc_count (.getDocCount b) :key (.getKeyAsDate b)})

(defn terms-bucket->map
  [^Terms$Bucket b]
  {:doc_count (.getDocCount b)
   :key (.getKey b)})

(defprotocol AggregatorPresenter
  (aggregation-value [agg] "Presents an aggregation as immutable Clojure map"))

(extend-protocol AggregatorPresenter
  Avg
  (aggregation-value [^Avg agg]
    {:value (.getValue agg)})

  Max
  (aggregation-value [^Max agg]
    {:value (.getValue agg)})

  Min
  (aggregation-value [^Min agg]
    {:value (.getValue agg)})

  Sum
  (aggregation-value [^Sum agg]
    {:value (.getValue agg)})

  Percentiles
  (aggregation-value [^Percentiles agg]
    {:values (reduce (fn [acc ^Percentile p]
                       (assoc acc (keyword (str (.getPercent p))) (.getValue p)))
                     {}
                     agg)})

  Cardinality
  (aggregation-value [^Cardinality agg]
    {:value (.getValue agg)})

  ValueCount
  (aggregation-value [^ValueCount agg]
    {:value (.getValue agg)})

  ;; Missing, Global, etc
  SingleBucketAggregation
  (aggregation-value [^SingleBucketAggregation agg]
    {:doc_count (.getDocCount agg)})

  Stats
  (aggregation-value [^Stats agg]
    {:count (.getCount agg)
     :min   (.getMin agg)
     :max   (.getMax agg)
     :avg   (.getAvg agg)
     :sum   (.getSum agg)})

  ExtendedStats
  (aggregation-value [^ExtendedStats agg]
    {:count (.getCount agg)
     :min   (.getMin agg)
     :max   (.getMax agg)
     :avg   (.getAvg agg)
     :sum   (.getSum agg)
     :sum_of_squares (.getSumOfSquares agg)
     :variance       (.getVariance agg)
     :std_deviation  (.getStdDeviation agg)})

  Histogram
  (aggregation-value [^Histogram agg]
    {:buckets (vec (map histogram-bucket->map (.getBuckets agg)))})

  DateHistogram
  (aggregation-value [^DateHistogram agg]
    {:buckets (vec (map date-histogram-bucket->map (.getBuckets agg)))})

  Range
  (aggregation-value [^Range agg]
    {:buckets (vec (map range-bucket->map (.getBuckets agg)))})

  DateRange
  (aggregation-value [^DateRange agg]
    {:buckets (vec (map date-range-bucket->map (.getBuckets agg)))})

  Terms
  (aggregation-value [^Terms agg]
    {:buckets (vec (map terms-bucket->map (.getBuckets agg)))}))

(defn aggregations-to-map
  [acc [^String name agg]]
  ;; <String, Aggregation>
  (assoc acc (keyword name) (aggregation-value agg)))

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
  ;;  :aggregations {:avg_age {:value 29.0}}
  ;; }
  (let [m {:took       (.getTookInMillis r)
           :timed_out  (.isTimedOut r)
           :_scroll_id (.getScrollId r)
           :facets     (search-facets->seq (.getFacets r))
           ;; TODO: suggestions
           :_shards    {:total      (.getTotalShards r)
                        :successful (.getSuccessfulShards r)
                        :failed     (.getFailedShards r)}
           :hits       (search-hits->seq (.getHits r))}]
    (if (seq (.getAggregations r))
      (clojure.core/merge m {:aggregations (reduce aggregations-to-map {} (.. r getAggregations asMap))})
      m)))

(defn multi-search-response->seq
  [^MultiSearchResponse r]
  ;; Example REST API response:
  ;;
  ;; [{:took 1, :timed_out false, :_shards {:total 5, :successful 5, :failed 0},
  ;;   :hits {:total 4, :max_score 1.0,
  ;;          :hits [{:_index people, :_type person, :_id 4, :_score 1.0,
  ;;                  :_source {:last-name Hall, :age 29, :username estony, :first-name Tony,
  ;;                  :title Yak Shaver, :planet Earth, :biography yak/reduce all day long, :country Uruguay}}]}}
  ;;  {:took 1, :timed_out false, :_shards {:total 5, :successful 5, :failed 0},
  ;;   :hits {:total 4, :max_score 1.0,
  ;;          :hits [{:_index articles, :_type article, :_id 4, :_score 1.0,
  ;;                  :_source {:tags geografía, EEUU, historia, ciudades, Norteamérica, :title Austin, :summary "...",
  ;;                  :language Spanish, :url http://es.wikipedia.org/wiki/Austin, :number-of-edits 13002}}]}}]
  (let [xs (map (fn [^MultiSearchResponse$Item item]
                  (.getResponse item))
                r)]
    (map search-response->seq xs)))


(defn ^IPersistentMap percolate-response->map
  [^PercolateResponse r]
  {:count (.getCount r) :matches (map (fn [^PercolateResponse$Match m]
                                        (.. m getId string))
                                      (into [] (.getMatches r)))})

;;
;; Administrative Actions
;;

(defn ^IndicesExistsRequest ->index-exists-request
  [index-name]
  (IndicesExistsRequest. (->string-array index-name)))

(defn ^TypesExistsRequest ->types-exists-request
  [index-name type-name]
  (TypesExistsRequest. (->string-array index-name) (->string-array type-name)))

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
  (doto (UpdateSettingsRequest. (->string-array index-name))
    (.settings ^Map (wlk/stringify-keys settings))))

(defn ^GetMappingsRequest ->get-mappings-request
  ([]
     (GetMappingsRequest.))
  ([index-name ^String mapping-type]
     (doto (GetMappingsRequest.)
       (.indices (->string-array index-name))
       (.types (->string-array mapping-type)))))

(defn ^IPersistentMap get-mappings-response->map
  [^GetMappingsResponse res]
  ;; TODO: a sane way of converting ImmutableOpenMaps to Clojure maps. MK.
  (reduce (fn [acc ^ObjectObjectCursor el]
            (let [^String           k (.key el)
                  ^ImmutableOpenMap v (.value el)]
              (assoc acc (keyword k)
                     ;; to match HTTP API responses. MK.
                     {:mappings (reduce (fn [acc2 ^ObjectObjectCursor el]
                                          (let [^String          k (.key el)
                                                ^MappingMetaData v (.value el)]
                                            (assoc acc (keyword k) (deep-java-map->map (.sourceAsMap v)))))
                                        {}
                                        v)})))
          {}
          (.mappings res)))


(defn ^PutMappingRequest ->put-mapping-request
  [index-name ^String mapping-type {:keys [mapping mappings ignore_conflicts ignore-conflicts]}]
  (let [r (doto (PutMappingRequest. (->string-array index-name))
            (.type mapping-type)
            (.source ^Map (wlk/stringify-keys (or mapping mappings))))]
    (when-let [v (or ignore_conflicts ignore-conflicts)]
      (.ignoreConflicts r v))
    r))

(defn ^DeleteMappingRequest ->delete-mapping-request
  [index-name mapping-types]
  (doto (DeleteMappingRequest. (->string-array index-name))
    (.types (->string-array mapping-types))))

(defn ^OpenIndexRequest ->open-index-request
  [index-name]
  (OpenIndexRequest. index-name))

(defn ^CloseIndexRequest ->close-index-request
  [index-name]
  (CloseIndexRequest. index-name))

(defn ^OptimizeRequest ->optimize-index-request
  [index-name {:keys [wait-for-merge max-num-segments only-expunge-deletes flush]}]
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
    r))

(defn ^FlushRequest ->flush-index-request
  [index-name {:keys [force full]}]
  (let [ary (->string-array index-name)
        r   (FlushRequest. ary)]
    (when force
      (.force r force))
    (when full
      (.full r full))
    r))

(defn ^RefreshRequest ->refresh-index-request
  [index-name]
  (RefreshRequest. (->string-array index-name)))

(defn ^IPersistentMap shard-operation-failed-exception->map
  [^ShardOperationFailedException e]
  {:index    (.index e)
   :shard-id (.shardId e)
   :reason   (.reason e)})

(defn ^IPersistentMap acknowledged-response->map
  [^AcknowledgedResponse res]
  ;; matches REST API responses
  {:acknowledged (.isAcknowledged res)})

(defn ^IPersistentMap broadcast-operation-response->map
  [^BroadcastOperationResponse res]
  ;; matches REST API responses
  {:_shards {:total      (.getTotalShards res)
             :successful (.getSuccessfulShards res)
             :failed     (.getFailedShards res)
             :failures   (map shard-operation-failed-exception->map (.getShardFailures res))}})


(defn ^PutRepositoryRequest ->put-repository-request
  [^String name {:keys [type settings]}]
  (let [r (PutRepositoryRequest. name)]
    (when type
      (.type r type))
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    r))

(defn ^CreateSnapshotRequest ->create-snapshot-request
  [^String repository ^String snapshot {:keys [wait-for-completion? partial settings indices]}]
  (let [r (CreateSnapshotRequest. repository snapshot)]
    (when wait-for-completion?
      (.waitForCompletion r wait-for-completion?))
    (when partial
      (.partial r partial))
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    (when indices
      (.indices r indices))
    r))

(defn ^DeleteSnapshotRequest ->delete-snapshot-request
  [^String repository ^String snapshot]
  (DeleteSnapshotRequest. repository snapshot))


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
  [^IndicesAliasesRequest req {:keys [indices alias filter]}]
  (if filter
    (.addAlias req ^String alias ^Map filter (->string-array indices))
    (.addAlias req ^String alias (->string-array indices)))
  req)

(defn- apply-remove-alias
  [^IndicesAliasesRequest req {:keys [index aliases]}]
  (.removeAlias req ^String index (->string-array aliases))
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
