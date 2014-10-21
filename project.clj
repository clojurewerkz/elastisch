(defproject clojurewerkz/elastisch "2.1.0-beta9-SNAPSHOT"
  :url "http://clojureelasticsearch.info"
  :description "Minimalistic fully featured well documented Clojure ElasticSearch client"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure   "1.6.0"]
                 [cheshire              "5.3.1"]
                 [clj-http              "1.0.0" :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support  "1.1.0"]
                 ;; used by the native client
                 [org.elasticsearch/elasticsearch "1.3.4"]]
  :min-lein-version "2.5.0"
  :profiles     {:dev {:resource-paths ["test/resources"]
                       :dependencies [[clj-time "0.8.0" :exclusions [org.clojure/clojure]]]
                       :plugins [[codox "0.8.10"]]
                       :codox {:sources ["src"]
                               :output-dir "doc/api"}}
                 ;; this version of clj-http depends on HTTPCore 4.2.x which
                 ;; some projects (e.g. using Spring's RestTemplate) can rely on,
                 ;; so we test for compatibility with it. MK.
                 :cljhttp076 {:dependencies [[clj-http "0.7.6"]]}
                 :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
                 :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
                 :master {:dependencies [[org.clojure/clojure "1.7.0-master-SNAPSHOT"]]}}
  :aliases      {"all" ["with-profile" "dev:dev,1.4:dev,1.5:dev,cljhttp076:dev,1.5,cljhttp076"]}
  :repositories {"sonatype"         {:url "http://oss.sonatype.org/content/repositories/releases"
                                     :snapshots false
                                     :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :global-vars {*warn-on-reflection* true}
  :test-selectors {:focus       :focus
                   :indexing    :indexing
                   :query       :query
                   :facets      :facets
                   :percolation :percolation
                   :scroll      :scroll
                   :snapshots   :snapshots
                   :native      :native
                   :rest        :rest
                   :version-dependent :version-dependent
                   :all         (constantly true)
                   :default     (constantly true)
                   :ci          (fn [m] (and (not (:native m)) (not (:version-dependent m))))}
  :mailing-list {:name "clojure-elasticsearch"
                 :archive "https://groups.google.com/group/clojure-elasticsearch"
                 :post "clojure-elasticsearch@googlegroups.com"})
