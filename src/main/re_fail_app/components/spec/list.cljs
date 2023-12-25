(ns re-fail-app.components.spec.list
  (:require [cljs.spec.alpha :as s]
            [re-fail-app.components.list :as list]
            [re-fail-app.components.spec.common :as common]))

(s/def ::variant #{:flush})
(s/def ::on-click fn?)

(s/def ::group-opts (s/keys :opt-un [::variant]))

(s/def ::item-opts (s/keys :opt-un [::common/variant
                                    ::on-click]))

(s/fdef list/group
  :args (s/cat :opts ::group-opts
               :children (s/* ::common/hiccup)))

(s/fdef list/group*
  :args (s/cat :opts ::group-opts
               :children (s/coll-of ::common/hiccup :kind seq?)))

(s/fdef list/item
  :args (s/cat :opts ::item-opts
               :children (s/* ::common/hiccup)))
