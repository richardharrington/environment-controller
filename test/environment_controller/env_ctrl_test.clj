(ns environment-controller.env-ctrl-test
  (:require [clojure.test :refer :all]
            [environment-controller.env-ctrl :refer :all]
            [environment-controller.hvac :as hvac]))

(def hot (+ perfect-temp tolerance 1))
(def cold (- perfect-temp tolerance 1))
(def moderate perfect-temp)



(defn fixtures [f]
  (reset! heater-countdown-store 0)
  (reset! cooler-countdown-store 0)
  (reset! last-states-store initial-devices-state)
  (f))

(use-fixtures :each fixtures)



(defprotocol IHvacDeviceStatesSupplementalStub
  (get-device-states [this]))

(defn hvac-stub [temp-sequence]
  (let [temps (atom temp-sequence)
        device-states (atom {:heater-on? false, :cooler-on? false, :blower-on? false})]

    (reify

      hvac/ITemp
      (get-temp [_]
        (let [temp (first @temps)]
          (swap! temps rest)
          temp))

      hvac/IDeviceStates
      (set-device-states! [_ states]
        (reset! device-states states))

      IHvacDeviceStatesSupplementalStub
      (get-device-states [_]
        @device-states))))


(defn execute-tics! [hvac num-tics]
  (dorun (dec num-tics)
         (repeatedly #(tic! hvac))))

(defn assert-states [hvac expected]
  (is (= (get-device-states hvac)
         expected)))

(defn assert-temp-sequence-leads-to-states [temp-sequence expected-states]
  (let [hvac (hvac-stub temp-sequence)]
    (execute-tics! hvac (count temp-sequence))
    (assert-states hvac expected-states)))



(deftest test-tic!-temp-moderate
  (testing "tic! does nothing when temp is moderate"
    (assert-temp-sequence-leads-to-states
     [moderate]
     {:heater-on? false
      :cooler-on? false
      :blower-on? false})))

(deftest test-tic!-temp-too-hot
  (testing "tic! turns on cooler and blower when temp is too hot"
    (assert-temp-sequence-leads-to-states
     [hot]
     {:heater-on? false
      :cooler-on? true
      :blower-on? true})))

(deftest test-tic!-temp-too-cold
  (testing "tic! turns on heater and blower when temp is too cold"
    (assert-temp-sequence-leads-to-states
     [cold]
     {:heater-on? true
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic!-not-too-hot-but-blower-stays-on
  (testing "blower stays on even under moderate conditions if heater has been off for less than 5 tics"
    (assert-temp-sequence-leads-to-states
     [cold moderate moderate moderate moderate]
     {:heater-on? false
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic!-blower-turns-off-after-heater-cools-down
  (testing "blower turns off under moderate conditions if heater has been off for at least 5 tics"
    (assert-temp-sequence-leads-to-states
     [cold moderate moderate moderate moderate moderate]
     {:heater-on? false
      :cooler-on? false
      :blower-on? false})))

(deftest test-tic!-too-cold-but-cooler-stays-off
  (testing "cooler stays off even under hot conditions if it's been off for less than 3 tics"
    (assert-temp-sequence-leads-to-states
     [hot moderate hot hot]
     {:heater-on? false
      :cooler-on? false
      :blower-on? true})))

(deftest test-tic!-cooler-turns-on-after-resting
  (testing "cooler turns on under hot conditions if it's been off for at least 3 tics"
    (assert-temp-sequence-leads-to-states
     [hot moderate hot hot hot]
     {:heater-on? false
      :cooler-on? true
      :blower-on? true})))
