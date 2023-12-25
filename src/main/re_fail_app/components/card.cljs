(ns re-fail-app.components.card
  (:require ["react-bootstrap/Card" :as Card]))

(defn card-header
  [header]
  [:> Card/Header {} header])

(defn card-footer
  [footer]
  [:> Card/Footer {} footer])

(defn card-body
  [items]
  (into [:> Card/Body {}]
        items))

(defn card
  [{:keys [header footer]} & children]
  [:> Card {}
   (when header
     [card-header header])
   (when (seq children)
     [card-body children])
   (when footer
     [card-footer footer])])

(defn text
  [content]
  [:> Card/Text {} content])
