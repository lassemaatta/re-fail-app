(ns re-fail-app.components.spec.form
  (:require [cljs.spec.alpha :as s]
            [re-fail-app.components.form :as form]))

(s/def ::text string?)
(s/def ::for string?)
(s/def ::placeholder string?)
(s/def ::disabled? boolean?)
(s/def ::type #{:email
                :password})

(s/fdef form/label
  :args (s/cat :opts (s/keys :req-un [::text]
                             :opt-un [::for])))

(s/def ::control-opts (s/keys :opt-un [::type
                                       ::placeholder
                                       ::disabled?]))

(s/fdef form/control
  :args (s/cat :opts ::control-opts))
