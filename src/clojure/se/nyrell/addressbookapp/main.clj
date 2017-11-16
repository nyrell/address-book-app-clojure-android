(ns se.nyrell.addressbookapp.main
  (:require [neko.activity :refer [defactivity set-content-view!]]
            [neko.debug :refer [*a]]
            [neko.log :as log]
            [neko.notify :refer [toast]]
            [neko.data.sqlite :as ndb]
            [neko.resource :as res]
            [neko.find-view :refer [find-view]]
            [neko.threading :refer [on-ui]]
            [neko.ui.adapters :refer [ref-adapter update-cursor]]
            [neko.ui :refer [config]]
            [se.nyrell.addressbookapp
             ;; [dummy-db :as db]
             [sqlite-db :as db]
             ])
  (:import android.widget.EditText)
  (:import [android.widget TextView ListView]
           (android.app Activity)
           ))


;; We execute this function to import all subclasses of R class. This gives us
;; access to all application resources.
(res/import-all)




(defn gui-add-contact [activity]
  (let [^EditText name-input (.getText (find-view activity ::input-contact-name))        
        contact-name (if (empty? name-input)
                       (str "Contact " (inc (db/count-contacts)))
                       (res/get-string R$string/your_input_fmt name-input))
        
        ^EditText email-input (.getText (find-view activity ::input-contact-email))
        contact-email-csv (if (empty? email-input)
                            ""
                            (res/get-string R$string/your_input_fmt email-input))
        contact-emails (for [raw-email (clojure.string/split contact-email-csv #", ")]
                         (clojure.string/trim raw-email))
        
        contact-map {:name contact-name :emails contact-emails}
        ]
    (log/d "Add contact: " contact-map)
    (db/add-contact contact-map)
    (update-cursor (.getAdapter ^ListView (find-view activity ::contact-list-view)))
    ))

(defn gui-delete-all-contacts [activity]
  (db/delete-all-contacts))


(defactivity se.nyrell.addressbookapp.MainActivity
  :key :main

  (onCreate [this bundle]
            (.superOnCreate this bundle)
            (neko.debug/keep-screen-on this)
            (db/initialize)
            (log/d "fn: onCreate: 1")
            ;; (let [cursor         (db/contact-list-cursor-fn)
            ;;       row-count      (.getCount cursor)
            ;;       col-count      (.getColumnCount cursor)
            ;;       col-names-java (.getColumnNames cursor)
            ;;       col-names      (str (into [] (for [col-name col-names-java] (str col-name))))
            ;;       ]
            ;;   (log/d "Cursor test:")
            ;;   (log/d "Rows:" row-count)
            ;;   (log/d "Cols:" col-count)
            ;;   (log/d "Col names:" col-names)
            ;;   (.moveToFirst cursor)
            ;;   (log/d (str (ndb/entity-from-cursor cursor)))
            ;;   (.moveToNext cursor)
            ;;   (log/d (str (ndb/entity-from-cursor cursor)))
            ;;   (.moveToNext cursor)
            ;;   (log/d (str (ndb/entity-from-cursor cursor)))
            ;;   )
              
            (log/d "fn: onCreate: 2")
            (on-ui
             (set-content-view! (*a)
                                [:linear-layout {:orientation :vertical
                                                 :layout-width :fill
                                                 :layout-height :wrap}
                                 [:text-view {:text "Contacts in address book"}]
                                 [:list-view {:adapter (db/make-contact-list-adapter this)
                                              :on-item-click (fn [^ListView parent, view position id] (neko.notify/toast (str "Clicked! " id)))
                                              :id ::contact-list-view}]
                                 [:text-view {:text "New contact:"}]
                                 [:linear-layout {:orientation :horizontal}
                                  [:text-view {:text "  Name:"}]
                                  [:edit-text {:text "" :hint "Type name here." :id ::input-contact-name}]]
                                 [:linear-layout {:orientation :horizontal}
                                  [:text-view {:text "  E-mail:"}]
                                  [:edit-text {:text "" :hint "Type email here. (comma separated)!" :id ::input-contact-email}]]
                                 [:button {:text "Add contact!"
                                           :on-click (fn [^android.widget.Button b]
                                                       (gui-add-contact (*a))
                                                       )}]
                                 [:button {:text "Delete all!"
                                           :on-click (fn [^android.widget.Button b]
                                                       (gui-delete-all-contacts (*a))
                                                       )}]
                                 ]))))
