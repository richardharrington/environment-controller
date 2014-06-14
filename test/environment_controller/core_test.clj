(ns environment-controller.core-test
  (:require [clojure.test :refer :all]
            [environment-controller.core :refer :all]
            [environment-controller.hvac]))

(def hot (+ perfect-temp tolerance 1))
(def cold (- perfect-temp tolerance 1))
(def moderate perfect-temp)



(defn fixtures [f]
  (reset! heater-countdown-store 0)
  (reset! cooler-countdown-store 0)
  (f))

(use-fixtures :each fixtures)

(defn make-hvac-stub []
  (let [hvac (environment-controller.hvac/make-hvac)]
    (swap! hvac merge {:set-states! (partial swap! hvac assoc :states)
                       :set-temp! (fn [temp]
                                    (swap! hvac assoc :get-temp (constantly temp)))
                       :states {:heater-on? false
                                :cooler-on? false
                                :blower-on? false}})
    hvac))

(defn execute-tics-with-temps! [hvac temp-sequence]
  (doseq [temp temp-sequence]
    (when temp
      ((:set-temp! @hvac) temp))
    (tic! hvac)))

(defn assert-states [hvac expected]
  (is (= (:states @hvac)
         expected)))

(defn assert-temp-sequence-leads-to-states [temp-sequence expected-states]
  (let [hvac (make-hvac-stub)]
    (execute-tics-with-temps! hvac temp-sequence)
    (assert-states hvac expected-states)))



(deftest test-tic-does-nothing-to-hvac-states-when-temp-starts-out-just-right
  (testing "tic does nothing to hvac states when :get-temp returns 70 degrees"
    (assert-temp-sequence-leads-to-states
     [moderate]
     {:heater-on? false
      :cooler-on? false
      :blower-on? false})))

(deftest test-tic-turns-on-cooler-and-blower-when-temp-starts-out-too-high
  (testing "tic turns on cooler and blower when :get-temp returns 76 degrees"
    (assert-temp-sequence-leads-to-states
     [hot]
     {:heater-on? false
      :cooler-on? true
      :blower-on? true})))

(deftest test-tic-turns-on-heater-and-blower-when-temp-starts-out-too-low
  (testing "tic turns on heater and blower when :get-temp returns 64 degrees"
    (assert-temp-sequence-leads-to-states
     [cold]
     {:heater-on? true
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic-keeps-blower-on-till-heater-cools-down
  (testing "blower stays on even under moderate conditions if heater has been off for less than 5 tics"
    (assert-temp-sequence-leads-to-states
     [cold moderate nil nil nil nil]
     {:heater-on? false
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic-turns-blower-off-after-heater-cools-down
  (testing "blower turns off under moderate conditions if heater has been off for at least 5 tics"
    (assert-temp-sequence-leads-to-states
     [cold moderate nil nil nil nil nil]
     {:heater-on? false
      :cooler-on? false
      :blower-on? false})))

(deftest test-tic-keeps-cooler-off-until-its-ready-to-start-up-again
  (testing "cooler stays off even under hot conditions if it's been off for less than 3 tics"
    (assert-temp-sequence-leads-to-states
     [hot moderate hot nil]
     {:heater-on? false
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic-turns-cooler-on-if-its-too-hot-and-cooler-has-been-off-long-enough
  (testing "cooler turns on under hot conditions if it's been off for at least 3 tics"
    (assert-temp-sequence-leads-to-states
     [hot moderate hot nil nil]
     {:heater-on? false
      :cooler-on? true
      :blower-on? true})))


(run-tests 'environment-controller.core-test)
