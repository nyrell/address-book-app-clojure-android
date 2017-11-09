(ns se.nyrell.addressbookapp.sqlite-db
  (:require [neko.data.sqlite :as ndb]
            [neko.debug :refer [*a]]
            [neko.log :as log]
            [neko.find-view :refer [find-view]]
            [neko.ui :refer [config]]
            [neko.ui.adapters :refer [ref-adapter]]
            )
  (:import [android.database.sqlite SQLiteDatabase])
  (:import android.content.Context)
  (:import neko.data.sqlite.TaggedDatabase))

;; sqlite tutorial: https://www.tutorialspoint.com/sqlite/

;; SQL delete function implemented like the other functions in neko.data.sqlite
;;
;; Java API: delete(String table, String whereClause, String[] whereArgs)
(defn delete
  [^TaggedDatabase tagged-db table-name where]
  (.delete ^SQLiteDatabase (.db tagged-db)
           (name table-name)
           (@#'ndb/where-clause where)
           nil))
(def db-delete delete)


;; Dummy "db" variable just to get the ref-wrapper to work.
;; TODO: Investigate how to do this in a better way.
(def contact-db
  (atom 0)
  )

(defn get-db-atom []
  contact-db)

(defn touch-db-atom
  "Do something to the dummy db variable just to force an update of the GUI."
  []
  (swap! (get-db-atom) + 1))



;; SQLite functionality below. Mostly inspired by foreclojure.
;; -----------------------------------------------------------

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
   (touch-db-atom) ;; Force GUI update
   ))

(defn count-contacts
  ([]
   (count-contacts (get-db)))
  ([db]
   (ndb/query-scalar db ["count" :_id] :contacts nil)))

(defn populate-database [db]
  (ndb/transact db (add-contact "first-contact")))

(defn delete-all-contacts []
  (db-delete (get-db) :contacts nil)
  (touch-db-atom))

(defn initialize
  "Initialize the database, populates if necessary"
  []
  (let [db (get-db)]
    ;;(delete-all-contacts)
    (when (db-empty? db)
      (populate-database db))
    ))

(defn get-contact-name-list [contact-db]
  (let [real-contact-db (get-db)
        contact-seq (ndb/query-seq real-contact-db [:contacts/name] [:contacts] nil)]
    (into [] (for [contact contact-seq] (:contacts/name contact)))
    ;;(list (str (into [] contact-seq))) ;; Just display the db response as a string on a single line
    ))




(defn make-contact-list-adapter []
  (ref-adapter
   (fn [_] [:linear-layout {:id-holder true}
            [:text-view {:id ::caption-tv}]])
   (fn [position view _ data]
     (let [tv (find-view view ::caption-tv)]
       ;;(config tv :text (str position ". " data))
       (config tv :text (str data))
       ))
   (get-db-atom)
   get-contact-name-list))
