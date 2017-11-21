# AddressBookApp - Clojure/Android

This is a Clojure/Android application. It is a simple addressbook app created for learning more about Clojure/Android and how to work with a sqlite database. It is not inteded for practical usage.

The main sources used to learn how to do this was:

* The Neko documentation and source code: http://clojure-android.github.io/neko/

* The source code for 4Clojure: https://github.com/alexander-yakushev/foreclojure-android

* Android App Development with Clojure: https://github.com/alexander-yakushev/events/blob/master/tutorial.md


## Usage

Setup your Clojure/Android environment according to information here: http://clojure-android.info/

If your environment is setup correctly, just build and run with:

lein droid doall



There are two possible backends used for the DB:

* A simple atom list used together with the ref-adapter.
* A sqlite db used together with the cursor-adapter.

You can switch between these just by selecting wich one to "require" from main.clj. 

Example: 
[dummy-db :as db]  ;; Use the atom list "db"
[sqlite-db :as db] ;; Use sqlite


## License

Distributed under the Eclipse Public License, the same as Clojure.
