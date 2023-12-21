(ns re-fail-app.views.core
  (:require [re-fail-app.components.button :as button]))

(defn view
  []
  [:<>
   [button/primary {:title "I'm a button"}]
   [:span "Hello world"]])
