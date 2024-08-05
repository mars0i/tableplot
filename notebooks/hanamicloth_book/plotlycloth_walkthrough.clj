;; # Plotlycloth Walkthrough

;; Plotlycloth is a Clojure API for creating [Plotly.js](https://plotly.com/javascript/) plots through layered pipelines. It is part of the Hanamicloth library.

;; Here, we provide a walkthrough of the API.

;; Soon, we will provide more in-depth explanations in additional chapters.

;; * The plotlycloth API namepace

;; * [Tablecloth](https://scicloj.github.io/tablecloth/) for dataset processing

;; * the [datetime namespace](https://cnuernber.github.io/dtype-next/tech.v3.datatype.datetime.html) of [dtype-next](https://github.com/cnuernber/dtype-next)

;; * the [print namespace](https://techascent.github.io/tech.ml.dataset/tech.v3.dataset.print.html) of [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) for customized dataset printing

;; * [Kindly](https://scicloj.github.io/kindly-noted/) (to specify how certaiun values should be visualized)

;; * the datasets defined in the [Datasets chapter](./hanamicloth.datasets.html)

(ns hanamicloth-book.plotlycloth-walkthrough
  (:require [scicloj.hanamicloth.v1.plotlycloth :as ploclo]
            [tablecloth.api :as tc]
            [tech.v3.datatype.datetime :as datetime]
            [tech.v3.dataset.print :as print]
            [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]
            [scicloj.kindly.v4.api :as kindly]
            [hanamicloth-book.datasets :as datasets]))

(-> datasets/iris
    (ploclo/layer-point
     {:=x :sepal-width
      :=y :sepal-length
      :=color :species
      :=mark-size 20
      :=mark-opacity 0.6}))

(-> datasets/iris
    (ploclo/layer-point
     {:=x :sepal-width
      :=y :sepal-length
      :=group [:species]
      :=mark-size 20
      :=mark-opacity 0.6}))

(-> datasets/mtcars
    (ploclo/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=mark-size 20}))

(-> datasets/mtcars
    (ploclo/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=color-type :nominal
      :=mark-size 20}))

(-> datasets/mtcars
    (ploclo/layer-boxplot
     {:=x :cyl
      :=y :disp}))

(-> datasets/iris
    (ploclo/layer-segment
     {:=x0 :sepal-width
      :=y0 :sepal-length
      :=x1 :petal-width
      :=y1 :petal-length
      :=mark-opacity 0.4
      :=mark-size 3
      :=color :species}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/layer-line
     {:=x :date
      :=y :value
      :=mark-color "purple"}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/base {:=x :date
                  :=y :value
                  :=mark-color "purple"})
    ploclo/layer-line)

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/base {:=x :date
                  :=y :value})
    (ploclo/layer-line {:=mark-color "purple"}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/base {:=x :date
                  :=y :value})
    (ploclo/layer-point {:=mark-color "green"
                         :=mark-size 20
                         :=mark-opacity 0.5})
    (ploclo/layer-line {:=mark-color "purple"}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/layer-line {:=x :date
                        :=y :value
                        :=mark-color "purple"}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/base {:=x :date
                  :=y :value})
    (ploclo/layer-line {:=mark-color "purple"})
    (ploclo/update-data tc/random 5)
    (ploclo/layer-point {:=mark-color "green"
                         :=mark-size 15
                         :=mark-opacity 0.5}))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (ploclo/base {:=x :date
                  :=y :value})
    (ploclo/layer-line {:=mark-color "purple"})
    (ploclo/update-data tc/random 5)
    (ploclo/layer-point {:=mark-color "green"
                         :=mark-size 15
                         :=mark-opacity 0.5})
    ploclo/plot
    (assoc-in [:layout :plot_bgcolor] "#eeeedd"))

(-> datasets/iris
    (ploclo/base {:=title "dummy"
                  :=mark-color "green"
                  :=x :sepal-width
                  :=y :sepal-length})
    ploclo/layer-point
    (ploclo/layer-smooth {:=mark-color "orange"})
    ploclo/plot)

(-> datasets/iris
    (ploclo/base {:=x :sepal-width
                  :=y :sepal-length})
    ploclo/layer-point
    (ploclo/layer-smooth {:=predictors [:petal-width
                                        :petal-length]
                          :=mark-opacity 0.5})
    ploclo/plot)


(-> datasets/iris
    (ploclo/base {:=title "dummy"
                  :=color :species
                  :=x :sepal-width
                  :=y :sepal-length})
    ploclo/layer-point
    ploclo/layer-smooth)


(-> datasets/iris
    (ploclo/base {:=title "dummy"
                  :=mark-color "green"
                  :=color :species
                  :=group []
                  :=x :sepal-width
                  :=y :sepal-length})
    ploclo/layer-point
    ploclo/layer-smooth)

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :days))
                            :relative-time "Future"}))
    (print/print-range 6))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y))))
    (print/print-range 6))

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y))))
    (ploclo/base {:=x :date
                  :=y :value})
    (ploclo/layer-smooth {:=color :relative-time
                          :=mark-size 20
                          :=group []
                          :=predictors [:yearmonth]})
    ;; Keep only the past for the following layer:
    (ploclo/update-data (fn [dataset]
                          (-> dataset
                              (tc/select-rows (fn [row]
                                                (-> row :relative-time (= "Past")))))))
    (ploclo/layer-line {:=mark-color "purple"
                        :=mark-size 3}))

(-> datasets/iris
    (ploclo/layer-histogram {:=x :sepal-width}))


(-> datasets/iris
    (ploclo/layer-histogram {:=x :sepal-width
                             :=histogram-nbins 30}))



(-> {:ABCD (range 1 11)
     :EFGH [5 2.5 5 7.5 5 2.5 7.5 4.5 5.5 5]
     :IJKL [:A :A :A :A :A :B :B :B :B :B]
     :MNOP [:C :D :C :D :C :D :C :D :C :D]}
    tc/dataset
    (ploclo/base {:=title "IJKLMNOP"})
    (ploclo/layer-point {:=x :ABCD
                         :=y :EFGH
                         :=color :IJKL
                         :=size :MNOP
                         :=name "QRST1"})
    (ploclo/layer-line
     {:=title "IJKL MNOP"
      :=x :ABCD
      :=y :ABCD
      :=name "QRST2"
      :=mark-color "magenta"
      :=mark-size 20
      :=mark-opacity 0.2})
    ploclo/plot)
