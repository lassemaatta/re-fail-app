(ns re-fail-app.components.spec.button
  (:require [cljs.spec.alpha :as s]
            [re-fail-app.components.button :as button]
            [re-fail-app.components.spec.common :as common]))

(s/def ::class string?)
(s/def ::title string?)
(s/def ::disabled? boolean?)
(s/def ::outline? boolean?)

(s/def ::type #{:submit})

(s/def ::variant-opts (s/keys :req-un [::title]
                              :opt-un [::class
                                       ::type
                                       ::disabled?
                                       ::outline?]))

(s/def ::button-opts (s/merge ::variant-opts
                              (s/keys :req-un [::common/variant])))

(s/fdef button/button
  :args (s/cat :button-opts ::button-opts))

(s/fdef button/primary   :args (s/cat :opts ::variant-opts))
(s/fdef button/secondary :args (s/cat :opts ::variant-opts))
(s/fdef button/success   :args (s/cat :opts ::variant-opts))
(s/fdef button/danger    :args (s/cat :opts ::variant-opts))
(s/fdef button/warning   :args (s/cat :opts ::variant-opts))
(s/fdef button/info      :args (s/cat :opts ::variant-opts))
