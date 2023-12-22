(ns re-fail-app.components.card
  (:require ["react-bootstrap/Card" :as Card]
            [cljs.spec.alpha :as s]))

(s/def ::header string?)
(s/def ::footer string?)

(s/fdef card-header
  :args (s/cat :header ::header))

(defn- card-header
  [header]
  [:> Card/Header
   {}
   header])

(s/fdef card-footer
  :args (s/cat :footer ::footer))

(defn- card-footer
  [footer]
  [:> Card/Footer
   {}
   footer])

(defn- card-body
  [items]
  (into [:> Card/Body
         {}]
        items))

(s/def ::card (s/keys :opt-un [::header
                               ::footer]))

(s/fdef card
  :args (s/cat :args ::card
               :children (s/* vector?)))

(defn card
  [{:keys [header footer]} & children]
  [:> Card
   {}
   (when header
     [card-header header])
   (when (seq children)
     [card-body children])
   (when footer
     [card-footer footer])])

(defn text
  [content]
  [:> Card/Text {} content])
