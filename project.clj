(defproject clojurewerkz/elastisch "1.0.0-SNAPSHOT"
  :url "http://clojureelasticsearch.info"
  :description "Minimalistic fully featured well documented Clojure ElasticSearch client"
  :license {:name "Eclipse Public License"}
  :dependencies [[org.clojure/clojure   "1.4.0"]
                 [cheshire              "4.0.2"]
                 [clj-http              "0.5.1" :exclusions [org.clojure/clojure]]
                 [clojurewerkz/support  "0.7.0"]]
  :min-lein-version "2.0.0"
  :profiles     {:dev {:resource-paths ["test/resources"]
                       :dependencies [[clj-time            "0.4.4" :exclusions [org.clojure/clojure]]]}
                 :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
                 :1.5 {:dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]]}}
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
                   :all         (constantly true)})
