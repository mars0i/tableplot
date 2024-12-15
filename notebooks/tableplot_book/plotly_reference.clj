;; # Plotly API reference 📖

;; This chapter is a detailed refernce of Tableplot's Plotly API.
;; For diverse examples, see the
;; [Plotly API Walkthrough](./tableplot_book.plotly_walkthrough.html).

;; ## Setup

;; In this tutorial, we use:

;; * The Tableplot plotly API namepace
;; * [Tablecloth](https://scicloj.github.io/tablecloth/) for dataset processing and column processing
;; * [dtype-next](https://github.com/cnuernber/dtype-next)'s tensor namespace for tensor examples
;; * dtype-next's buffered image namespace for image representation
;; * [Kindly](https://scicloj.github.io/kindly-noted/) (to specify how certain values should be visualized)
;; * the datasets defined in the [Datasets chapter](./tableplot_book.datasets.html)

;; We require a few aditional namespaces which are used internally to generate this reference.

(ns tableplot-book.plotly-reference
  (:require [scicloj.tableplot.v1.plotly :as plotly]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.tensor :as tensor]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.tableplot.v1.dag :as dag]
            [tableplot-book.datasets :as datasets]
            [clojure.string :as str]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [clojure.math :as math]))

^:kindly/hide-code
(defn include-form [form]
  (format "`%s`" (pr-str form)))

^:kindly/hide-code
(defn include-key-or-symbol-name [s]
  (format "[`%s`](#%s)"
          s
          (-> s (str/replace #"^\:\=" ""))))

^:kindly/hide-code
(def symbol-or-key-pattern
  #"`[a-z|\-|\:|\=]+`")

^:kindly/hide-code
(def subkey-pattern
  #"`\:\=[a-z|\-]+`")

^:kindly/hide-code
(defn known-symbol? [s]
  (-> s
      (str/replace #"`" "")
      symbol
      ('#{base layer
          layer-point layer-line layer-bar layer-boxplot layer-violin layer-segment layer-text
          layer-histogram layer-density layer-smooth
          plot
          debug
          smooth-stat histogram-stat density-stat
          imshow surface splom})))

^:kindly/hide-code
(defn enrich-text-with-links [text]
  (or (some->> text
               (re-seq symbol-or-key-pattern)
               distinct
               (reduce (fn [current-text s]
                         (if (or (re-matches subkey-pattern s)
                                 (known-symbol? s))
                           (str/replace current-text
                                        s
                                        (-> s
                                            (str/replace #"[`]" "")
                                            include-key-or-symbol-name))
                           current-text))
                       text))
      text))


(defn f->deps [f]
  (->> f
       meta
       :scicloj.tableplot.v1.dag/dep-ks
       (map include-key-or-symbol-name)
       (str/join " ")))

^:kindly/hide-code
(defn include-dag-fn [f]
  (format "%s\n\n **by default depends on**: %s"
          (-> f
              meta
              :doc
              enrich-text-with-links)
          (f->deps f)))


^:kindly/hide-code
(defn include-dag-fn-as-section [fnsymbol f]
  (-> (format "### `%s`\n%s"
              (pr-str fnsymbol)
              (include-dag-fn f))
      kind/md
      kindly/hide-code))

^:kindly/hide-code
(defn include-fnvar-as-section [fnvar]
  (-> (let [{:keys [name arglists doc]} (meta fnvar)]
        (str (format "### `%s`\n" name)
             (->> arglists
                  (map (fn [l]
                         (->> l
                              pr-str
                              (format "`%s`\n\n"))))
                  (str/join ""))
             (enrich-text-with-links doc)))
      kind/md
      kindly/hide-code))

^:kindly/hide-code
(defn include-all-keys []
  (->> plotly/standard-defaults
       (map (fn [[k v doc]]
              (kind/md
               [(format "### %s" (include-key-or-symbol-name k))
                (some->> doc
                         (format "**role:** %s\n"))
                (format "**default:** %s\n"
                        (cond (fn? v) (include-dag-fn v)
                              (= v hc/RMV) "`NONE`"
                              (keyword? v) (include-key-or-symbol-name v)
                              :else (include-form v)))])))
       kind/fragment))

^:kindly/hide-code
(defn md [& strings]
  (->> strings
       (str/join " ")
       enrich-text-with-links
       kind/md
       kindly/hide-code))

(md
 
 "## Overview
The Tableplot Plotly API allows the user to write functional pipelines
that create and process *templates* of plots, that can eventually be realized
as [Plotly.js](https://plotly.com/javascript/) specifications.

The data is assumed to be held in datasets defined by [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset)
(which can conveniently be used using [Tablecloth](https://scicloj.github.io/tablecloth)).

The templates are an adapted version of
[Hanami Templates](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#templates-substitution-keys-and-transformations).
Hanami transforms templates by recursively applying a simple set of rules.
The rules are based on looking up *substitution keys* according to standard defaults
as well as user substitutions overriding those defaults.
Tableplot uses a slighly adapted version of Hanami's template transformations,
which makes sure not to recurse into datasets.

For example, the `layer-point` function generates a template with some specified
substitutions. Let us apply this function to a dataset with some user substitutions.
As you can see below, all the substitution keys are keywords beginning with `=`. E.g., `:=color-type`.
This is just a convention that helps distinguish their role from other keys.

By default, this template is annotated by the [Kindly](https://scicloj.github.io/kindly/)
standared so that tools supporting Kindly (such as [Clay](https://scicloj.github.io/clay/))
will display by realizing it and using it as a Plotly.js specification.     
")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 10}))

(md "To inspect it, let us use [Kindly](https://scicloj.github.io/kindly-noted/) to request
that this template would rather be pretty-printed as a data structure.")
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 10})
    kind/pprint)

(md "
For now, you are not supposed to make sense of this data representation.
As a user, you usually do not need to think about it.

If you wish to see the actual Plotly.js specification, you can use
the `plot` function:
")
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 10})
    plotly/plot
    kind/pprint)

(md "We may also inspect it with [Portal](https://github.com/djblue/portal):")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 10})
    plotly/plot
    kind/portal)


(md "
This is useful for debugging, and also when one wishes to edit the Plotly.js
spec directly.

In the remainder of this chapter, we will offer a detailed reference to the API
functions, the way layers are defined, the substitution keys, and the relationships
among them.

## Debugging

Throughout this notebook, we will sometimes use the `debug` function that
allows one to look into the value of a given substitution key in a given
context.

For example, here we learn about the `:=background`
key for background color. In this example,
we kept it grey, which is its default.")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 10})
    (plotly/debug :=background))

(md "
## Raw Plotly specifications

Before beginning the exploration of Tableplot's Plotly API, let us remember we may
also use the raw format supported by Plotly.js.
We simply use plain Clojuree data structures to represent the JSON format expected by Plotly.js
and annotate it as `kind/plotly`, so that our Clojure tooling knows to treat it as such
when displaying it.

For example, let us reproduce one of its [Bubble Chart Examples](https://plotly.com/javascript/bubble-charts/).

The original Javascript code looks like this:
```js
var trace1 = {
              x: [1, 2, 3, 4],
              y: [10, 11, 12, 13],
              text: ['A<br>size: 40', 'B<br>size: 60', 'C<br>size: 80', 'D<br>size: 100'],
              mode: 'markers',
              marker: {
                       color: ['rgb(93, 164, 214)', 'rgb(255, 144, 14)',  'rgb(44, 160, 101)', 'rgb(255, 65, 54)'],
                       size: [40, 60, 80, 100]
                       }
              };

var data = [trace1];

var layout = {
              title: {
                      text: 'Bubble Chart Hover Text'
                      },
              showlegend: false,
              height: 600,
              width: 600
              };

Plotly.newPlot('myDiv', data, layout);

```

Here is how we represent that in Clojure:
")
(plotly/plot
 {:data [{:x [1 2 3 4]
          :y [10 11 12 13]
          :text ["A<br>size: 40" "B<br>size: 60" "C<br>size: 80" "D<br>size: 100"]
          :mode :markers
          :marker {:color ["rgb(93, 164, 214)", "rgb(255, 144, 14)",  "rgb(44, 160, 101)", "rgb(255, 65, 54)"]
                   :size [40 60 80 100]}}]
  :layout {:title {:text "Bubble Chart Hover Text"}
           :showlegend false
           :height 600
           :width 600}})

(md "
Sometimes, this raw way is all we need; but in common situations, Tableplot makes things easier.

## Concepts

### Plotly.js traces

Traces are a core concept in Plotly.js.
They specify separate parts of the plots which can be drawn on the same canvas
but may vary in their visual nature.

For example, here is a raw Plotly.js spec with two traces.
")
(plotly/plot
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

(md "
In Tableplot, we often do not need to think about traces, as they are drawn for us.
But it is helpful to know about them if we wish to understand the specs generated by Tableplot.

### Layers

Layers are a higher-level concept. We introduce them in Tableplot following
[ggplot2](https://ggplot2.tidyverse.org/)'s
[layered grammar of graphics](https://vita.had.co.nz/papers/layered-grammar.html).
Plotly bindings in other languages have similar concepts.

Like traces, layers are also parts of the plot that can be drawn on the same canvas,
but they are a slightly higher-level concept, that makes it easier to bind our data to parts of the plot.

Layers are themselves templates, so they can have their own substitutions.

For example:")
(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species}))

(md "
This plot has **two layers**: one for points, and one for text (which is visible on hover).

Let us see that using `debug`:
")
(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species})
    (plotly/debug :=layers)
    kind/pprint)

(md "
You see, a layer is an intermediate data representation of Tableplot
that takes care of the details necessary to generate traces.

In our example, the two layers are realized as **four traces**: since the point layer is colored
by species, it is realized as three traces.

Let us see that using `debug`:
")
(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species
                         :=mark-size 20})
    (plotly/layer-text {:=text :species})
    (plotly/debug :=traces)
    kind/pprint)

(md "
### Mark

Mark is a Tableplot notion that is used to distinguish different types of layers,
e.g. `point-layer` vs `line-layer`.
It is similar to the ggplot notion of 'geom'.

Its possible values are:")
^:kindly/hide-code
[:point :text :line :box :violin :bar :segment]

(md "
Here, `:box` means [boxplot](https://en.wikipedia.org/wiki/Box_plot),
and `:violin` means [Violin plot](https://en.wikipedia.org/wiki/Violin_plot).

### Coordinates

Coordinates are a Tableplot notion that defines
how marks are eventually mapped over to the cranvas,
similar to ggplot's notion.

We currently support the following:")
^:kindly/hide-code
[:2d :3d :polar :geo]

(md "
Here, 2d and 3d mean Eucledian coordinates of the corresponding dimensions,
and `:geo` means latitude and longitude.

### Plotly.js mode and type  

Mode and type are Plotly.js notions that are used to distinguish
diffetent types of traces.

Combinations of Tableplot's mark and coordinates
are mapped onto combinations of Plotly.js mode and type,
but currently we do not use all the meaningful combinations
supported by Plotly.js.

Mode is derived from mark as follows:
")
^:kindly/hide-code
(-> {:mark [:point :text :line :box :bar :segment]}
    tc/dataset
    (tc/map-columns :mode [:mark] plotly/mark->mode))

(md "
Type is defined as the concatenation of a mark-based string:
(`\"box\"` or `\"bar\"` if that is the mark, and `\"scatter\"` otherwise)
with a coordinates-based string
(`\"3d\"`, `\"polar\"`, or `\"geo\"` if we have such coordinates, `nil` otherwise).

Thus, for example, if the mark is `:point` and the coordinates are `:polar`,
then the type is `\"scatterpolar\"`.

### Field types

Looking into the data in the columns, we may classify them into the following types:

- `:quantitative` - numerical columns
- `:temporal` - date-time columns
- `:nominal` - all other column types (e.g., Strings, keywords)

In certain situations, the types of data in relevant columns determines the way things
should be plotted.

For example, when a column is used for coloring a plot, we should use gradient colors
when it is quantitative but more distinct colors when it is nominal.

Colorin gby a nominal column:
")
(-> datasets/iris
    (plotly/layer-point
     {:=x :sepal-width
      :=y :sepal-length
      :=color :species
      :=mark-size 20}))

(md "
Coloring by a Quantitative column:
")
(-> datasets/mtcars
    (plotly/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=mark-size 20}))

(md "
Overriding a quantitative column to be considered nominal by the `:=color-type` key:
")
(-> datasets/mtcars
    (plotly/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=color-type :nominal
      :=mark-size 20}))

(md "
### Stat

A stat is a statistical transformation that takes the substitution context
and returns a new dataset.
Stats such as `smooth-stat`, `histogram-stat`, and `density-stat` are used in a few of the layer functions, such as
`layer-smooth`, `layer-histogram`, and `layer-density`.

The user will typically not need to think about them, but they
are a useful concept in extending Tableplot.

## API functions
")

(include-fnvar-as-section #'plotly/base)

(md "#### For example:")

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length
                  :=mark-size 10})
    (plotly/layer-point {:=mark-color "grey"
                         :=mark-size 20
                         :=mark-opacity 0.3})
    (plotly/layer-point {:=color :species}))

(include-fnvar-as-section #'plotly/layer)

(md "#### For example:
We could write someting like:")

(-> datasets/iris
    (plotly/layer plotly/layer-base
                  {:=mark :point
                   :=x :sepal-width
                   :=y :sepal-length}))

(md "
Of course, this can also be expressed more succinctly using `layer-point`.
")
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length}))

(include-fnvar-as-section #'plotly/layer-point)

(md "#### Example:")

(-> datasets/mtcars
    (plotly/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=color-type :nominal
      :=mark-size 20}))

(include-fnvar-as-section #'plotly/layer-line)

(md "#### Example:")

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/layer-line
     {:=x :date
      :=y :value
      :=mark-color "purple"}))

(include-fnvar-as-section #'plotly/layer-bar)

(md "#### Example:")

(-> datasets/mtcars
    (tc/group-by [:cyl])
    (tc/aggregate {:total-disp #(-> % :disp tcc/sum)})
    (tc/add-column :bar-width 0.5)
    (plotly/layer-bar
     {:=x :cyl
      :=bar-width :bar-width
      :=y :total-disp}))

(include-fnvar-as-section #'plotly/layer-boxplot)

(md "#### Example:")

(-> datasets/mtcars
    (plotly/layer-boxplot
     {:=x :cyl
      :=y :disp}))

(-> datasets/mtcars
    (plotly/layer-boxplot
     {:=x :cyl
      :=y :disp
      :=color :am
      :=color-type :nominal}))

(-> datasets/mtcars
    (plotly/layer-boxplot
     {:=x :cyl
      :=y :disp
      :=color :am
      :=color-type :nominal
      :=boxmode :group}))

(include-fnvar-as-section #'plotly/layer-violin)

(md "#### Example:")

(-> datasets/mtcars
    (plotly/layer-violin
     {:=x :cyl
      :=y :disp}))

(-> datasets/mtcars
    (plotly/layer-violin
     {:=x :cyl
      :=y :disp
      :=box-visible true}))

(-> datasets/mtcars
    (plotly/layer-violin
     {:=x :cyl
      :=y :disp
      :=meanline-visible true}))

(-> datasets/mtcars
    (plotly/layer-violin
     {:=x :cyl
      :=y :disp
      :=color :am
      :=color-type :nominal}))

(-> datasets/mtcars
    (plotly/layer-violin
     {:=x :cyl
      :=y :disp
      :=color :am
      :=color-type :nominal
      :=violinmode :group}))

(include-fnvar-as-section #'plotly/layer-segment)

(md "#### Example:")

(-> datasets/iris
    (plotly/layer-segment
     {:=x0 :sepal-width
      :=y0 :sepal-length
      :=x1 :petal-width
      :=y1 :petal-length
      :=mark-opacity 0.4
      :=mark-size 3
      :=color :species}))

(include-fnvar-as-section #'plotly/layer-text)

(md "#### Example:")

(-> datasets/mtcars
    (plotly/layer-text
     {:=x :mpg
      :=y :disp
      :=text :cyl
      :=textfont {:family "Courier New, monospace"
                  :size 16
                  :color :purple}
      :=mark-size 20}))

(include-fnvar-as-section #'plotly/layer-histogram)

(md "#### Examples:")

(-> datasets/iris
    (plotly/layer-histogram {:=x :sepal-width}))

(-> datasets/iris
    (plotly/layer-histogram {:=x :sepal-width
                             :=histogram-nbins 30}))

(-> datasets/iris
    (plotly/layer-histogram {:=x :sepal-width
                             :=color :species
                             :=mark-opacity 0.5}))

(include-fnvar-as-section #'plotly/layer-density)

(md "#### Examples:")

(-> datasets/iris
    (plotly/layer-density {:=x :sepal-width}))

(-> datasets/iris
    (plotly/layer-density {:=x :sepal-width
                           :=density-bandwidth 0.05}))

(-> datasets/iris
    (plotly/layer-density {:=x :sepal-width
                           :=density-bandwidth 1}))

(-> datasets/iris
    (plotly/layer-density {:=x :sepal-width
                           :=color :species}))

(include-fnvar-as-section #'plotly/layer-smooth)

(md "#### Examples:")

(md "Simple linear regression of `:=y` by `:=x`:")

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=mark-color "orange"
                          :=name "Predicted"}))

(md "Multiple linear regression of `:=y` by `:=predictors`:")

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=predictors [:petal-width
                                        :petal-length]
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

(md "Polynomial regression of `:=y` by `:=design-matrix`:")

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=design-matrix [[:sepal-width '(identity sepal-width)]
                                           [:sepal-width-2 '(* sepal-width
                                                               sepal-width)]]
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

(md "Custom regression defined by `:=model-options`:")

(require 'scicloj.ml.tribuo)

(def regression-tree-options
  {:model-type :scicloj.ml.tribuo/regression
   :tribuo-components [{:name "cart"
                        :type "org.tribuo.regression.rtree.CARTRegressionTrainer"
                        :properties {:maxDepth "8"
                                     :fractionFeaturesInSplit "1.0"
                                     :seed "12345"
                                     :impurity "mse"}}
                       {:name "mse"
                        :type "org.tribuo.regression.rtree.impurity.MeanSquaredError"}]
   :tribuo-trainer-name "cart"})

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=model-options regression-tree-options
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

(md "Grouped regression where `:=inferred-group` is influenced by `:color`
since `:=color-type` is `:nominal`:")

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=color :species
                  :=x :sepal-width
                  :=y :sepal-length})
    plotly/layer-point
    plotly/layer-smooth)

(md "Regression where grouping is avoiding using through `:=group`:")

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=color :species
                  :=group []
                  :=x :sepal-width
                  :=y :sepal-length})
    plotly/layer-point
    (plotly/layer-smooth {:=mark-color "red"}))

(md "An simpler way to achieve this -- the color is only defined for the point layer:")

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=color :species})
    plotly/layer-smooth)

(include-fnvar-as-section #'plotly/plot)

(md "#### For example:")
(-> datasets/iris
    tc/head
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length})
    plotly/plot
    kind/pprint)

(md "
This can be useful for editing the plot as a raw Plotly.js specification.
For example:
")
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length})
    plotly/plot
    (assoc-in [:layout :plot_bgcolor] "floralwhite"))

(include-fnvar-as-section #'plotly/debug)

(md "#### For example:")
(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species}))

(md "Let us verify that `:=background` is deterimined to be grey.")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species})
    (plotly/debug :=background))

(md "Here, let us verify `:=color-type` for the 0th layer is deterimined to be `:nominal`.")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species})
    (plotly/debug 0 :=color-type))

(md "Here, let us check both `:=color` and `:=color-type` for the 0th layer.")

(-> datasets/iris
    (plotly/layer-point {:=x :sepal-width
                         :=y :sepal-length
                         :=color :species})
    (plotly/debug 0 {:color :=color
                     :color-type :=color-type}))

(include-fnvar-as-section #'plotly/imshow)

(md 
 "Imshow uses dtype-next's [BufferedImage support](https://cnuernber.github.io/dtype-next/buffered-image.html) to figure out the right order of color channels, etc.

So, it can handle plain vectors of vectors, dtype next tensors, and actual Java BufferedImage objects.")
(md "#### For example:")

(plotly/imshow
 (for [i (range 10)]
   (for [j (range 10)]
     [(* 10 i) ; Red
      (* 10 j) ; Green
      (* 10 (+ i j)) ; Blue
      ])))

(plotly/imshow
 (tensor/compute-tensor
  [100 100 3]
  (fn [i j k]
    (case k
      0 i ; Red
      1 j ; Green
      2 (+ i j) ; Blue
      ))
  :uint8))

(defonce Crab-Nebula-image
  (-> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/Crab_Nebula.jpg/240px-Crab_Nebula.jpg"
      (java.net.URL.)
      (javax.imageio.ImageIO/read)))

(plotly/imshow Crab-Nebula-image)

(include-fnvar-as-section #'plotly/surface)

(md "#### For example:")

(plotly/surface
 (for [i (range 100)]
   (for [j (range 100)]
     (-> (tcc/- [i j]
                [30 60])
         (tcc// [20 50])
         tcc/sq
         tcc/sum
         -
         math/exp))))

(md "
## Stats
")

(include-dag-fn-as-section 'histogram-stat plotly/histogram-stat)
(include-dag-fn-as-section 'density-stat plotly/density-stat)
(include-dag-fn-as-section 'smooth-stat plotly/smooth-stat)

(md "
## Substitution Keys 
")

^:kindly/hide-code
(include-all-keys)


