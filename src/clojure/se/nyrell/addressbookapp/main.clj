(ns se.nyrell.addressbookapp.main
    (:require [neko.activity :refer [defactivity set-content-view!]]
              [neko.debug :refer [*a]]
              [neko.notify :refer [toast]]
              [neko.resource :as res]
              [neko.find-view :refer [find-view]]
              [neko.threading :refer [on-ui]]
              [neko.ui.adapters :refer [ref-adapter]]
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


(defn make-contact-list-adapter []
  (ref-adapter
   (fn [_] [:linear-layout {:id-holder true}
            [:text-view {:id ::caption-tv}]])
   (fn [position view _ data]
     (let [tv (find-view view ::caption-tv)]
       ;;(config tv :text (str position ". " data))
       (config tv :text (str data))
       ))
   (db/get-db-atom)
   db/get-contact-name-list))


(defn gui-add-contact [activity]
  (let [^EditText input (.getText (find-view activity ::input-contact-name))
        new-contact (if (empty? input)
                      (str "Contact " (inc (db/count-contacts)))
                      (res/get-string R$string/your_input_fmt input))
        ]
    (db/add-contact new-contact)
    ))

(defn gui-delete-all-contacts [activity]
  (db/delete-all-contacts))


(defactivity se.nyrell.addressbookapp.MainActivity
  :key :main

  (onCreate [this bundle]
            (.superOnCreate this bundle)
            (neko.debug/keep-screen-on this)
            (db/initialize)
            (on-ui
             (set-content-view! (*a)
                                [:linear-layout {:orientation :vertical
                                                 :layout-width :fill
                                                 :layout-height :wrap}
                                 [:text-view {:text "Contacts in address book"}]
                                 [:list-view {:adapter (make-contact-list-adapter)
                                              :on-item-click (fn [^ListView parent, view position id] (neko.notify/toast (str "Clicked! " id)))}]
                                 [:linear-layout {:orientation :horizontal}
                                  [:text-view {:text "New contact:"}]
                                  [:edit-text {:text "" :hint "Type text here" :id ::input-contact-name}]]
                                 [:button {:text "Add contact!"
                                           :on-click (fn [^android.widget.Button b]
                                                       (gui-add-contact (*a))
                                                       )}]
                                 [:button {:text "Delete all!"
                                           :on-click (fn [^android.widget.Button b]
                                                       (gui-delete-all-contacts (*a))
                                                       )}]
                                 ]))))
