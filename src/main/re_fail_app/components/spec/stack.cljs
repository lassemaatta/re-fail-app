(ns re-fail-app.components.spec.stack
  (:require [cljs.spec.alpha :as s]
            [re-fail-app.components.stack :as stack]
            [re-fail-app.components.spec.common :as common]))

(s/def ::direction #{:horizontal :vertical})
(s/def ::gap int?)

(s/def ::opts (s/keys :opt-un [::direction
                               ::gap]))

(s/fdef stack/stack
  :args (s/cat :opts ::opts
               :children (s/* ::common/hiccup)))
