(ns se.nyrell.addressbookapp.dummy-db)

(def contact-db
  (atom [{:name "Kalle"}
         {:name "Olle"}
         {:name "Maja"}]
   ))

(defn get-db []
  contact-db)

(defn add-contact [new-contact]
  (swap! contact-db conj {:name new-contact}))

(defn count-contacts []
  (count @contact-db))

(defn get-contact-name-list [contact-db]
  (into [] (for [contact contact-db] (:name contact))))

;;(get-contact-name-list @contact-db)
