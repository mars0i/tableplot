;; # Plotly API reference

;; This chapter is a detailed refernce of Tableplot's Plotly API.
;; For diverse examples, see the
;; [Plotly API Walkthrough](./tableplot_book.plotly_walkthrough.html).

(ns tableplot-book.plotly-reference
  (:require [scicloj.tableplot.v1.plotly :as plotly]
            [scicloj.tableplot.v1.dag :as dag]
            [scicloj.kindly.v4.kind :as kind]
            [aerial.hanami.common :as hc]
            [clojure.string :as str]
            [tablecloth.api :as tc]
            [tableplot-book.datasets :as datasets]))

;; ## Overview

;; The Tableplot Plotly API allows the user to write functional pipelines
;; to create and process *templates* of plots, that can eventually be realized
;; as [Plotly.js](https://plotly.com/javascript/) specifications.

;; The data is assumed to be held in datasets defined by [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset)
;; (which can conveniently be used using [Tablecloth](https://scicloj.github.io/tablecloth)).

;; The templates are an adapted version of
;; [Hanami Templates](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#templates-substitution-keys-and-transformations).
;; Hanami transforms templates by recursively applying a simple set of rules.
;; The rules are based on looking up *substitution keys* according to standard defaults
;; as well as user substitutions overriding those defaults.
;; Tableplot uses a slighly adapted version of Hanami's template transformations,
;; which make sure not to recurse into datasets.

;; For example, the `layer-point` function generates a template with some specified
;; substitutions. Let us apply this function to a dataset with some user substitutions.
;; As you can see be low, all the substitution keys are keywords beginning with `=`.
;; This is just a convention that helps distinguish their role from other k

;; By default, this template is displayed by realizing it as  an actual Plotly.js
;; specification.     

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 10}))

;; We will use [Kindly](https://scicloj.github.io/kindly-noted/) to specify
;; that this template should rather be pretty-printed as a data structure.
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 10})
    kind/pprint)

;; For now, you are not supposed to make sense of this data representation.
;; As a user, you usually do not need to think about it.

;; If you wish to see the actual Plotly.js specification, you can use
;; the `plot` function:

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 10})
    plotly/plot
    kind/pprint)

;; This is useful for debugging, and also when one wishes to edit the Plotly.js
;; spec directly.

;; In the remainder of this chapter, we will offer a detailed reference to the API
;; functions, the way layers are defined, the substitution keys, and the relationships
;; among them.

;; ## Debugging

;; Throughout this notebook, we will sometimes use the `debug` function that
;; allows one to look into the value of a given substitution key in a given
;; context. For example, here we learn about the `:=background` key for background
;; color, which is a grey colour by default.

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 10})
    (plotly/debug :=background))

;; ## Raw Plotly specifications

;; Before beginning the exploration of Tableplot's Plotly API, let us remember we may
;; also use the raw format of specifying plots to Plotly.js.
;; We simply use JSON data structures to represent the JSON format expected by Plotly.js
;; and annotate it as `kind/plotly`, so that our Clojure tooling knows to treat it as such
;; when displaying it.

;; For example, let us reproduce one of its [Bubble Chart Examples](https://plotly.com/javascript/bubble-charts/).

;; The original Javascript code looks like this:
;; ```js
;; var trace1 = {
;;               x: [1, 2, 3, 4],
;;               y: [10, 11, 12, 13],
;;               text: ['A<br>size: 40', 'B<br>size: 60', 'C<br>size: 80', 'D<br>size: 100'],
;;               mode: 'markers',
;;               marker: {
;;                        color: ['rgb(93, 164, 214)', 'rgb(255, 144, 14)',  'rgb(44, 160, 101)', 'rgb(255, 65, 54)'],
;;                        size: [40, 60, 80, 100]
;;                        }
;;               };

;; var data = [trace1];

;; var layout = {
;;               title: {
;;                       text: 'Bubble Chart Hover Text'
;;                       },
;;               showlegend: false,
;;               height: 600,
;;               width: 600
;;               };

;; Plotly.newPlot('myDiv', data, layout);

;; ```

;; Here is how we represent that in Clojure:

(kind/plotly
 {:data [{:x [1 2 3 4]
          :y [10 11 12 13]
          :text ["A<br>size: 40" "B<br>size: 60" "C<br>size: 80" "D<br>size: 100"]
          :mode :markers
          :marker {:color ["rgb(93, 164, 214)", "rgb(255, 144, 14)",  "rgb(44, 160, 101)", "rgb(255, 65, 54)"]
                   :size [40 60 80 100]}}]
  :layout {:title {:text "Bubble Chart Hover Text"}
           :showlegend false
           :height 600
           :width 600}}
 ;; Style the wrapping div:
 {:style {:height :auto}})

;; Sometimes, this raw way is all we need; but in common situations, Tableplot make things easier.

;; ## Concepts

;; ### Traces

;; Traces are a core concept in Plotly.js.
;; They specify separate parts of the plots which can be drawn on the same canvas
;; but may vary in their visual nature.

;; For example, here is a raw Plotly.js spec with two traces.

(kind/plotly
 {:data [{:x [1 2 3 4]
          :y [10 15 12 13]
          :color "blue"
          :mode :markers
          :marker {:size [40 60 80 100]
                   :color ["blue" "brown" "red" "green"]}}
         {:x [1 2 3 4]
          :y [15 21 17 18]
          :mode :markers
          :color "grey"
          :marker {:size 50
                   :color "grey"}}]})

;; In Tableplot, we often do not need to think about traces, as they are drawn for us.
;; But it is helpful to know about them if we wish to understand the Plotly specs
;; generated by Tableplot.

;; ### Layers

;; Layers are a more high-level concept. We introduce them in Tableplot following
;; [ggplot2](https://ggplot2.tidyverse.org/)'s
;; [layered grammar of graphics](https://vita.had.co.nz/papers/layered-grammar.html).
;; Plotly bindings in other languages have similar concepts.

;; Like traces, layers are also parts of the plot that can be drawn on the same canvas,
;; but they are a slightly higher-level concept, that makes it easier to bind our data to
;; what we receive as the plot.

;; For example:
(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species}))

;; This plot has **two layers**: one for points, and one for text (which is visible on hover).

;; Let us see that using `debug`:

(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species})
    (plotly/debug :=layers)
    kind/pprint)

;; You see, a layer is an intermediate data representation of Tableplot
;; that takes care of the details necessary to generate traces.

;; In our example, the two layers are realied as **four traces**: since the point layer is colored
;; by species, it is realized as three traces.

;; Let us see that using `debug`:

(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-height
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species})
    (plotly/debug :=traces)
    kind/pprint)

;; ## API functions

;; ### Simple layers

;; ### Statistical layers

;; ### Base

;; ### Realizing the plot

;; ## Keys 

^:kindly/hide-code
(defn include-form [form]
  (format "`%s`" (pr-str form)))

^:kindly/hide-code
(defn include-key [k]
  (format "[`%s`](#%s)"
          (pr-str k)
          (-> k name (str/replace #"^=" ""))))

^:kindly/hide-code
(defn include-fn [f]
  (format "%s\n\n **depends on**: %s"
          (-> f
              meta
              :doc)
          (->> f
               meta
               ::dag/dep-ks
               (map include-key)
               (str/join " "))))

^:kindly/hide-code
(->> plotly/standard-defaults
     (map (fn [[k v doc]]
            (kind/md
             [(format "### %s" (include-key k))
              (some->> doc
                       (format "**role:** %s\n"))
              (format "**default:** %s\n"
                      (cond (fn? v) (include-fn v)
                            (= v hc/RMV) "`NONE`"
                            (keyword? v) (include-key v)
                            :else (include-form v)))])))
     kind/fragment)
