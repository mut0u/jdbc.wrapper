(ns jdbc-wrapper.core
  (:require [clojure.java.jdbc1 :as jdbc]
            [taoensso.timbre :as timbre]))

(defn- query-type [query]
  (or (when (keyword? query)
        :keyword)
      (when (sequential? query)
        (or (-> (first query)
                clojure.string/trim
                clojure.string/lower-case
                (clojure.string/split #" ")
                first
                keyword)
            (throw (ex-info "query is wrong " {:query query}))))
      (when (ifn? query)
        :transaction)
      (throw (ex-info "query is not sequential or fn " {:query query}))))

(defmulti jdbc-query (fn [db query-type query] query-type))

(defmethod jdbc-query :keyword [db _ query]
  (case query
    :is-rollback-only (jdbc/db-is-rollback-only db)
    :set-rollback-only! (jdbc/db-set-rollback-only! db)
    :connection db))

(defmethod jdbc-query :insert [db _ query]
  (-> (jdbc/db-do-prepared-return-keys db (first query) (rest query))
      vals
      first))

(defmethod jdbc-query :update [db _ query]
  (first (jdbc/execute! db query)))

(defmethod jdbc-query :delete [db _ query]
  (first (jdbc/execute! db query)))

(defmethod jdbc-query :select [db _ query]
  (jdbc/query db query))

(defmethod jdbc-query :transaction [db _ query]
  (jdbc/with-db-transaction [tdb db]
    (query tdb)))

(defmulti return-query (fn [query-type return-type result] query-type))

(defmethod return-query :keyword [_ _ result]
  result)

(defmethod return-query :insert [_ return-type result]
  result)

(defmethod return-query :update [_ return-type result]
  (case return-type
    :jdbc result
    :boolean (pos? result)))

(defmethod return-query :delete [_ return-type result]
  (case return-type
    :jdbc result
    :boolean (pos? result)))

(defmethod return-query :select [_ return-type result]
  (case return-type
    :jdbc result
    :row (first result)
    :scalar (-> result first vals first)
    :number (or (-> result first vals first)
                0)))

(defmethod return-query :transaction [_ _ result]
  result)

(defn query*
  ([db query]
   (timbre/info query)
   (query* db :jdbc query))
  ([db return-type query]
   (timbre/info query)
   (let [qt (query-type query)]
     (->> (jdbc-query db qt query)
          (return-query qt return-type)))))

(comment
  (w/query* *db* return-type (if (ifn? query) (fn [db] (binding [*db* db] (query))) query))
  (def sitedb (let []
                (def ^:dynamic *db* ds)
                (fn ths
                  ([query] (ths :jdbc query))
                  ([return-type query]
                   (w/query* *db* return-type
                             (if (fn? query)
                               (fn [db] (binding [*db* db] (query)))
                               query))))))
  (sitedb ["select * from users where id=1"])
  (sitedb (fn [] (sitedb ["select * from users where id=1"]) (sitedb :set-rollback-only!)))
  )

(defmacro defdb [name db]
  (let [dynamic-name (symbol (str \* (clojure.core/name name) \*))]
    `(let []
       (def ~(vary-meta dynamic-name assoc :dynamic true) ~db)
       (defn ~name
         ([~'query]
          (~name :jdbc ~'query))
         ([~'return-type ~'query]
          (query* ~dynamic-name ~'return-type
                  (if (fn? ~'query)
                    (fn [~'db] (binding [~dynamic-name ~'db] (~'query)))
                    ~'query)))))))
