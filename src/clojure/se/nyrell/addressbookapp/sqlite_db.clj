(ns se.nyrell.addressbookapp.sqlite-db
  (:require ;; [clojure.data.json :as json]
            ;; [clojure.java.io :as jio]
            ;; [clojure.string :as str]
            [neko.data.sqlite :as ndb]
            [neko.debug :refer [*a]]
            [neko.log :as log])
  (:import [android.database.sqlite SQLiteDatabase])
  (:import android.content.Context)
  (:import neko.data.sqlite.TaggedDatabase))

;;(in-ns 'neko.data.sqlite)

;; sqlite tutorial: https://www.tutorialspoint.com/sqlite/

;; delete(String table, String whereClause, String[] whereArgs)
(defn delete
  [^TaggedDatabase tagged-db table-name where]
  (.delete ^SQLiteDatabase (.db tagged-db)
           (name table-name)
           (@#'ndb/where-clause where)
           nil))
(def db-delete delete)

;;(in-ns 'se.nyrell.addressbookapp.sqlite-db)

;;insert(String table, String nullColumnHack, ContentValues values)

;;update(String table, ContentValues values, String whereClause, String[] whereArgs)
;; (defn update
;;   [^TaggedDatabase tagged-db table-name set where]
;;   (.update ^SQLiteDatabase (.db tagged-db)
;;            (name table-name)
;;            (map-to-content tagged-db table-name set)
;;            (where-clause where)
;;            nil))
;; (def db-update update)



(def contact-db
  (atom 0)
  )

(defn get-db-atom []
  contact-db)


(defn touch-db-atom
  "This is just to force an update of the GUI"
  []
  (swap! (get-db-atom) + 1))

;; (defn get-db []
;;   contact-db)

;; (defn count-contacts []
;;   1)

;; (defn add-contact [new-contact]
;;   contact-db)

;; (defn get-contact-name-list [contact-db]
;;   ["sqlite"])


(def ^:private db-schema
  (let [nntext "text not null"]
    (ndb/make-schema
     :name "addressbookapp.db"
     :version 1
     :tables {:contacts {:columns
                         {:_id          "integer primary key"
                          :name        nntext}}
              })))

(def ^:private get-db-helper
  "Singleton of the SQLite helper."
  (memoize (fn [] (ndb/create-helper db-schema))))

(defn get-db
  "Returns a new writeable database each time it is called."
  []
  (ndb/get-database (get-db-helper) :write))

(defn db-empty?
  "Returns true if database hasn't been yet populated with any problems."
  [db]
  (zero? (ndb/query-scalar db ["count" :_id] :contacts nil)))

(defn add-contact
  ([new-contact]
   (add-contact (get-db) new-contact))
  ([db new-contact]
   (log/d "add-contact()" "new-contact:" new-contact)
   (ndb/insert db :contacts {:name new-contact})
   (touch-db-atom)
   ))

(defn count-contacts
  ([]
   (count-contacts (get-db)))
  ([db]
   (ndb/query-scalar db ["count" :_id] :contacts nil)))

(defn populate-database [db]
  (ndb/transact db
    (add-contact "first-contact")
    ))

(defn delete-all-contacts []
  (db-delete (get-db) :contacts nil)
  )

(defn initialize
  "Spins up the database, populates if necessary"
  []
  (let [db (get-db)]
    ;;(delete-all-contacts)
    (when (db-empty? db)
      (populate-database db))
    ))

(defn get-contact-name-list [contact-db]
  (let [real-contact-db (get-db)
        contact-seq (ndb/query-seq real-contact-db [:contacts/name] [:contacts] nil)]
    ;;(into [] (for [contact contact-db] (:name contact)))
    (into [] (for [contact contact-seq] (:contacts/name contact)))
    ;;(list (str (into [] contact-seq)))
    ))


