(defproject jdbc-wrapper "0.0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [org.clojure/java.jdbc "0.6.2-alpha3"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[mysql/mysql-connector-java "5.1.18"]]}})
