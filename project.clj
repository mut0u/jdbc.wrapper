(defproject jdbc-wrapper "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc1 "0.4.2"]]
  :main ^:skip-aot jdbc-wrapper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
