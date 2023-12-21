(ns re-fail-app.components.button
  (:require ["react-bootstrap/Button" :as Button]))

(defn primary
  [{:keys [title]}]
  [:> Button
   {:variant "primary"}
   title])
