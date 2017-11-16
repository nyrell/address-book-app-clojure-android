(ns se.nyrell.addressbookapp.sqlite-db
  (:require [neko.data.sqlite :as ndb]
            [neko.debug :refer [*a]]
            [neko.log :as log]
            [neko.find-view :refer [find-view]]
            [neko.ui :refer [config]]
            [neko.ui.adapters :refer [cursor-adapter]]
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




(def ^:private db-schema
  (let [nntext "text not null"]
    (ndb/make-schema
     :name "addressbookapp.db"
     :version 3
     :tables {:contacts {:columns
                         {:_id    "integer primary key"
                          :name   nntext}}
              :emails   {:columns
                         {:_id         "integer primary key"
                          :contact_id  "integer key"
                          :email       nntext}}
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
  (log/d "fn: db-empty?")
  (zero? (ndb/query-scalar db ["count" :_id] :contacts nil)))

(defn add-contact
  ([contact]
   (add-contact (get-db) contact))
  ([db contact]
   (log/d "add-contact()" "contact:" contact)
   (let [contact-id (ndb/insert db :contacts {:name (:name contact)})]
     (doseq [email (:emails contact)]
       (when (not (empty? email))
         (log/d "add-contact(): email: " email "contact-id: " contact-id)
         (ndb/insert db :emails {:contact_id contact-id :email email})))
   )))

(defn count-contacts
  ([]
   (count-contacts (get-db)))
  ([db]
   (ndb/query-scalar db ["count" :_id] :contacts nil)))

(defn populate-database [db]
;;  (ndb/transact db
  (add-contact {:name "Kalle Karlsson" :emails ["kalle@karlsson.se"]})
  (add-contact {:name "Sven Svensson"  :emails ["sven@svensson.se" "sven@hotmail.com"]})
  (add-contact {:name "Olle Olsson"    :emails []})
  )

(defn delete-all-contacts []
  (db-delete (get-db) :contacts nil)
  (db-delete (get-db) :emails nil)
  )

(defn initialize
  "Initialize the database, populates if necessary"
  []
  (log/d "fn: initialize")
  (let [db (get-db)]
    ;;(delete-all-contacts)
    (when (db-empty? db)
      (log/d "fn: initialize: Populate")
      (populate-database db))
    ))

;; (defn get-contact-name-list [contact-db]
;;   (let [real-contact-db (get-db)
;;         contact-seq (ndb/query-seq real-contact-db [:contacts/name] [:contacts] nil)]
;;     (into [] (for [contact contact-seq] (:contacts/name contact)))
;;     ;;(list (str (into [] contact-seq))) ;; Just display the db response as a string on a single line
;;     ))




;; (defn make-contact-list-adapter []
;;   (ref-adapter
;;    (fn [_] [:linear-layout {:id-holder true}
;;             [:text-view {:id ::caption-tv}]])
;;    (fn [position view _ data]
;;      (let [tv (find-view view ::caption-tv)]
;;        ;;(config tv :text (str position ". " data))
;;        (config tv :text (str data))
;;        ))
;;    (get-db-atom)
;;    get-contact-name-list))

(defn get-contact-emails [contact-id]
  (let [emails-seq (ndb/query-seq (get-db) :emails {:contact_id contact-id})]
    (log/d (str "Contact ID: " contact-id " - Email table: " (into [] emails-seq)))
    (into [] (for [email emails-seq] (:email email)))
    ;; (list (str (into [] emails-seq))) ;; Just display the db response as a string on a single line
    ))

(defn contact-list-cursor-fn []
  (log/d "fn: contact-list-cursor-fn")
  (let [db (get-db)]
    (ndb/query db :contacts nil)
    ))

(defn make-contact-list-adapter [context]
  (log/d "fn: make-contact-list-adapter")
  (cursor-adapter
   context
   (fn []
     (log/d "fn: cursor-adapter: create-view-fn")
     [:linear-layout {:id-holder true}
      [:text-view {:id ::caption-tv}]])
   (fn [view _ data]
     (log/d "fn: cursor-adapter: update-view-fn")
     (let [tv (find-view view ::caption-tv)
           email-list (get-contact-emails (:_id data))]
       (log/d "Contact ID:" (:_id data) ", email-list: " email-list ", Empty: " (empty? email-list))
       (config tv :text
               (if (empty? email-list)
                 (:name data)                 
                 (str (:name data) " (" (clojure.string/join ", " email-list) ")")))
       ))
   (fn []
     (log/d "fn: cursor-fn")
     (contact-list-cursor-fn))))


