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
               :children ::common/hiccup-vals))

(s/fdef list/group*
  :args (s/cat :opts ::group-opts
               :children ::common/hiccup-seq))

(s/fdef list/item
  :args (s/cat :opts ::item-opts
               :children ::common/hiccup-vals))
