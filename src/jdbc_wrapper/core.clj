(ns jdbc-wrapper.core
  (:require [clojure.java.jdbc1 :as jdbc]))

(defn- query-type [query]
  (or (when (sequential? query)
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

(defmulti jdbc-query
  (fn [db query]
    (query-type query)))

(defmethod jdbc-query :insert [db query]
  (-> (jdbc/db-do-prepared-return-keys db (first query) (rest query))
      vals
      first))

(defmethod jdbc-query :update [db query]
  (first (jdbc/execute! db query)))

(defmethod jdbc-query :delete [db query]
  (first (jdbc/execute! db query)))

(defmethod jdbc-query :select [db query]
  (jdbc/query db query))

(defmulti q (fn qd
              ([db query]
               (qd db :simple query))
              ([db return-type query]
               )))
