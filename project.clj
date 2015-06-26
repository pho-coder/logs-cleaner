(defproject logs-cleaner "0.1.0-SNAPSHOT"
  :description "magpie logs cleaner"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [com.taoensso/timbre "4.0.1"]
                 [org.yaml/snakeyaml "1.15"]]
  :main ^:skip-aot logs-cleaner.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-environ "1.0.0"]]}})
