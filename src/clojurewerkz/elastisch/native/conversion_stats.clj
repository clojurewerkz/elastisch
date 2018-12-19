(ns clojurewerkz.elastisch.native.conversion-stats
  (:import
    (org.elasticsearch.action.admin.indices.stats CommonStats IndicesStatsResponse)
    org.elasticsearch.index.shard.DocsStats
    org.elasticsearch.search.suggest.completion.CompletionStats
    org.elasticsearch.index.fielddata.FieldDataStats
    org.elasticsearch.index.flush.FlushStats
    org.elasticsearch.index.get.GetStats
    org.elasticsearch.index.merge.MergeStats
    org.elasticsearch.index.percolator.stats.PercolateStats
    org.elasticsearch.index.cache.query.QueryCacheStats
    org.elasticsearch.index.recovery.RecoveryStats
    org.elasticsearch.index.refresh.RefreshStats
    org.elasticsearch.index.cache.request.RequestCacheStats
    org.elasticsearch.index.search.stats.SearchStats
    org.elasticsearch.index.engine.SegmentsStats
    org.elasticsearch.index.suggest.stats.SuggestStats
    org.elasticsearch.index.store.StoreStats
    org.elasticsearch.index.translog.TranslogStats
    org.elasticsearch.index.warmer.WarmerStats))

(defprotocol IHashableStats
  (to-stats [this] "returns Clojure hash-map with ES metrics"))

(extend-protocol IHashableStats
  nil
  (to-stats [s] {})

  org.elasticsearch.index.shard.DocsStats
  (to-stats [s]
    {:count (.getCount s)
     :deleted (.getDeleted s)})

  org.elasticsearch.search.suggest.completion.CompletionStats
  (to-stats [s]
    {:size_in_bytes (.getSizeInBytes s)})
  
  org.elasticsearch.index.fielddata.FieldDataStats
  (to-stats [s]
    {:memory_size_in_bytes (.getMemorySizeInBytes s)
     :evictions (.getEvictions s)})

  org.elasticsearch.index.flush.FlushStats
  (to-stats [s]
    {:total (.getTotal s)
     :total_time_in_millis (.getTotalTimeInMillis s)})
  
  org.elasticsearch.index.get.GetStats
  (to-stats [s]
    {:total (.getCount s)
     :time_in_millis (.getTimeInMillis s)
     :exists_total (.getExistsCount s)
     :exists_time_in_millis (.getExistsTimeInMillis s)
     :missing_total (.getMissingCount s)
     :missing_time_in_millis (.getMissingTimeInMillis s)
     :current (.current s)})
 
  org.elasticsearch.index.indexing.IndexingStats 
  (to-stats [s]
    (let [totals (.getTotal s)]
      {:index_total (.getIndexCount totals)
       :index_time_in_millis (.getIndexTimeInMillis totals)
       :index_current (.getIndexCurrent totals)
       :index_failed (.getIndexFailedCount totals)
       :delete_total (.getDeleteCount totals)
       :delete_time_in_millis (.getDeleteTimeInMillis totals)
       :delete_current (.getDeleteCurrent totals)
       :noop_update_total (.getNoopUpdateCount totals)
       :is_throttled (.isThrottled totals)
       :throttle_time_in_millis (.getThrottleTimeInMillis totals)}))

  org.elasticsearch.index.merge.MergeStats
  (to-stats [s]
    {:current (.getCurrent s)
     :current_docs (.getCurrentNumDocs s)
     :current_size_in_bytes (.getCurrentSizeInBytes s)
     :total (.getTotal s)
     :total_time_in_millis (.getTotalTimeInMillis s)
     :total_docs (.getTotalNumDocs s)
     :total_size_in_bytes (.getTotalSizeInBytes s)
     :total_stopped_time_in_millis (.getTotalStoppedTimeInMillis s)
     :total_throttled_time_in_millis (.getTotalThrottledTimeInMillis s)
     :total_auto_throttle_in_bytes (.getTotalBytesPerSecAutoThrottle s)})

  org.elasticsearch.index.percolator.stats.PercolateStats
  (to-stats [s]
    {:total (.getCount s)
     :time_in_millis (.getTimeInMillis s)
     :current (.getCurrent s)
     :memory_size_in_bytes (.getMemorySizeInBytes s)
     :memory_size (-> s .getMemorySize .toString)
     :queries (.getNumQueries s)})

  org.elasticsearch.index.cache.query.QueryCacheStats
  (to-stats [s]
    {:memory_size_in_bytes (.getMemorySizeInBytes s)
     :total_count (.getTotalCount s)
     :hit_count (.getHitCount s)
     :miss_count (.getMissCount s)
     :cache_size (.getCacheSize s)
     :cache_count (.getCacheCount s)
     :evictions (.getEvictions s)}) 

  org.elasticsearch.index.recovery.RecoveryStats
  (to-stats [s]
    {:current_as_source (.currentAsSource s)
     :current_as_target (.currentAsTarget s)
     :throttle_time_in_millis (-> s .throttleTime .getMillis)})
  
  org.elasticsearch.index.refresh.RefreshStats
  (to-stats [s]
    {:total (.getTotal s)
     :total_time_in_millis (.getTotalTimeInMillis s)})

  org.elasticsearch.index.cache.request.RequestCacheStats
  (to-stats [s]
    {:memory_size_in_bytes (.getMemorySizeInBytes s)
     :evictions (.getEvictions s)
     :hit_count (.getHitCount s)
     :miss_count (.getMissCount s)})
  
  org.elasticsearch.index.search.stats.SearchStats
  (to-stats [s]
    (let [totals (-> s .getTotal)]
      {:open_contexts (.getOpenContexts s)
       :query_total (.getQueryCount totals)
       :query_time_in_millis (.getQueryTimeInMillis totals)
       :query_current (.getQueryCurrent totals)
       :fetch_total (.getFetchCount totals)
       :fetch_time_in_millis (.getFetchTimeInMillis totals)
       :fetch_current (.getFetchCurrent totals)
       :scroll_total (.getScrollCount totals)
       :scroll_time_in_millis (.getScrollTimeInMillis totals)
       :scroll_current (.getScrollCurrent totals)}))

  org.elasticsearch.index.engine.SegmentsStats
  (to-stats [s]
    {:count (.getCount s)
     :memory_in_bytes (.getMemoryInBytes s)
     :terms_memory_in_bytes (.getTermsMemoryInBytes s)
     :stored_fields_memory_in_bytes (.getStoredFieldsMemoryInBytes s)
     :term_vectors_memory_in_bytes (.getTermVectorsMemoryInBytes s)
     :norms_memory_in_bytes (.getNormsMemoryInBytes s)
     :doc_values_memory_in_bytes (.getDocValuesMemoryInBytes s)
     :index_writer_memory_in_bytes (.getIndexWriterMemoryInBytes s)
     :index_writer_max_memory_in_bytes (.getIndexWriterMaxMemoryInBytes s)
     :version_map_memory_in_bytes (.getVersionMapMemoryInBytes s)
     :fixed_bit_set_memory_in_bytes (.getBitsetMemoryInBytes s)})

  org.elasticsearch.index.suggest.stats.SuggestStats
  (to-stats [s]
    {:total (.getCount s)
     :time_in_millis (.getTimeInMillis s)
     :current (.getCurrent s)})
  
  org.elasticsearch.index.store.StoreStats
  (to-stats [s]
    {:size_in_bytes (.getSizeInBytes s)
     :throttle_time_in_millis (-> s .getThrottleTime .getMillis)})

  org.elasticsearch.index.translog.TranslogStats
  (to-stats [s]
    {:operations (.estimatedNumberOfOperations s)
     :size_in_bytes (.getTranslogSizeInBytes s)})
  
  org.elasticsearch.index.warmer.WarmerStats
  (to-stats [s]
    {:current (.current s)
     :total (.total s)
     :total_time_in_millis (.totalTimeInMillis s)})

  org.elasticsearch.action.admin.indices.stats.CommonStats
  (to-stats [s]
    {:completion    (-> s .getCompletion to-stats)
     :docs          (-> s .getDocs to-stats)
     :fielddata     (-> s .getFieldData to-stats)
     :flush         (-> s .getFlush to-stats)
     :get           (-> s .getGet to-stats)
     :indexing      (-> s .getIndexing to-stats)
     :merges        (-> s .getMerge to-stats)
     :percolate     (-> s .getPercolate to-stats)
     :query_cache   (-> s .getQueryCache to-stats)
     :recovery      (-> s .getRecoveryStats to-stats)
     :refresh       (-> s .getRefresh to-stats)
     :request_cache (-> s .getRequestCache to-stats)
     :search        (-> s .getSearch to-stats)
     :segments      (-> s .getSegments to-stats)
     :suggests      (-> s .getSuggest to-stats)
     :store         (-> s .getStore to-stats)
     :translog      (-> s .getTranslog to-stats)
     :warmer        (-> s .getWarmer to-stats)})

  ;ps: it replicates IndicesStatsResponse.toString logic and includes no Shards data
  org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse
  (to-stats [r]
    {:_all {:primaries (-> r .getPrimaries to-stats)
            :total (-> r .getTotal to-stats)}
     :indices (reduce
                (fn [acc [idx-name idx-stats]] (assoc acc idx-name idx-stats))
                {}
                (for [[idx-name idx-stats] (.getIndices r)]
                  [idx-name {:primaries (-> idx-stats .getPrimaries to-stats)
                             :total (-> idx-stats .getTotal to-stats)}]))}))

