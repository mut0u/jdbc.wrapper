(ns jdbc-wrapper.core-test
  (:require [clojure.test :refer :all]
            [jdbc-wrapper.core :as w]
            )
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(defn pool-c3p0
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(w/defdb testdb (pool-c3p0 {:subprotocol "mysql"
                            :subname "//127.0.0.1/rui_site?characterEncoding=UTF-8"
                            :delimiters "`"
                            :user "demo"
                            :password "demo"}))


(comment
  (w/defdb t1 ds)
  (t1 ["select * from users where id=1"])
  (t1 (fn [] (t1 ["select * from users where id=1"]) (t1 :set-rollback-only!)))
  )


(deftest defdb-test
  (testing "defdb test"
    (is (= (testdb ["select id from users where id=1"]) '({:id 1})))
    (is (= (testdb :row ["select id from users where id=1"]) {:id 1}))
    (is (= (testdb :scalar ["select id from users where id=1"]) 1))
    (is (= (testdb (fn []
                     (testdb :scalar ["select id from users where id=1"])))
           1))
    ))
