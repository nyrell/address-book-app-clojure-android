(ns se.nyrell.addressbookapp.db)

(def dummy-contact-db
  (atom [{:name "Kalle"}
         {:name "Olle"}
         {:name "Maja"}]
   ))

(defn db-count-contacts []
  (count @dummy-contact-db))

(defn db-add-contact [new-contact]
  (swap! dummy-contact-db conj {:name new-contact}))
