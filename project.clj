(defproject elastisch "0.0.1-SNAPSHOT"
  :description "Clojure ElasticSearch Client"
  :repositories {
                 "clojure-releases" "http://build.clojure.org/releases"
                 "sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}
                             }
                 }
  :dependencies [[org.clojure/clojure   "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-http              "0.2.4"]]
  :dev-resources-path "test/resources"
  :warn-on-reflection true)