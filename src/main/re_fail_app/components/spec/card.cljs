(ns re-fail-app.components.spec.card
  (:require [cljs.spec.alpha :as s]
            [re-fail-app.components.card :as card]
            [re-fail-app.components.spec.common :as common]))

(s/def ::header string?)
(s/def ::footer string?)

(s/fdef card/card-header
  :args (s/cat :header ::header))

(s/fdef card/card-footer
  :args (s/cat :footer ::footer))

(s/def ::card (s/keys :opt-un [::header
                               ::footer]))

(s/fdef card/card
  :args (s/cat :opts ::card
               :children (s/* ::common/hiccup)))
