(defproject clojurewerkz/elastisch "1.1.0-SNAPSHOT"
  :url "http://clojureelasticsearch.info"
  :description "Minimalistic fully featured well documented Clojure ElasticSearch client"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure   "1.4.0"]
                 [cheshire              "4.0.3"]
                 [clj-http              "0.5.5" :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support  "0.12.0"]
                 ;; to get the Java client. MK.
                 [org.elasticsearch/elasticsearch "0.20.5"]]
  :min-lein-version "2.0.0"
  :profiles     {:dev {:resource-paths ["test/resources"]
                       :dependencies [[clj-time            "0.4.4" :exclusions [org.clojure/clojure]]]
                       :plugins [[codox "0.6.1"]]}
                 :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
                 :1.5 {:dependencies [[org.clojure/clojure "1.5.0-RC16"]]}}
  :aliases      {"all" ["with-profile" "dev:dev,1.3:dev,1.5"]}
  :repositories {"sonatype"         {:url "http://oss.sonatype.org/content/repositories/releases"
                                     :snapshots false
                                     :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :warn-on-reflection true
  :test-selectors {:focus       :focus
                   :indexing    :indexing
                   :query       :query
                   :facets      :facets
                   :percolation :percolation
                   :all         (constantly true)
                   :default     (constantly true)}
  :mailing-list {:name "clojure-elasticsearch"
                 :archive "https://groups.google.com/group/clojure-elasticsearch"
                 :post "clojure-elasticsearch@googlegroups.com"}
  :plugins [[codox "0.6.1"]]
  :codox {:sources ["src"]
          :output-dir "doc/api"})
