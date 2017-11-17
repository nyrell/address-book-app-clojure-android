(ns se.nyrell.addressbookapp.dummy-db
  (:require [neko.ui.adapters :refer [ref-adapter]]
            [neko.find-view :refer [find-view]]
            [neko.ui :refer [config]]
            ))

(def contact-db
  (atom [{:name "Kalle"}
         {:name "Olle"}
         {:name "Maja"}]
   ))

(defn get-db []
  contact-db)

(defn add-contact [new-contact]
  (swap! contact-db conj {:name (:name new-contact)}))

(defn count-contacts []
  (count @contact-db))

(defn get-contact-name-list [contact-db]
  (into [] (for [contact contact-db] (:name contact))))

;; Only needed here to match the interface of sqlite_db.clj
(defn initialize []) 

(defn delete-all-contacts []
  (reset! contact-db []))

(defn make-contact-list-adapter [context]
  (ref-adapter
   (fn [_] [:linear-layout {:id-holder true}
            [:text-view {:id ::caption-tv}]])
   (fn [position view _ data]
     (let [tv (find-view view ::caption-tv)]
       ;;(config tv :text (str position ". " data))
       (config tv :text (str data))
       ))
   (get-db)
   get-contact-name-list))
