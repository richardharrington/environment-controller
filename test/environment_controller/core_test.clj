(ns environment-controller.core-test
  (:require [clojure.test :refer :all]
            [environment-controller.core :refer :all]))

; TODO: find out whether we're supposed to be querying
; the hvac device for its device states, or whether we
; keep track of that entirely ourselves -- i.e., do
; we have getters, or just setters?

; TODO: make a setup, especially for resetting the heater countdown


(defn make-hvac []
  (let [hvac (atom nil)]
    (reset! hvac
            {:get-temp (fn []) ; not implemented
             :states {:heater :off
                      :cooler :off
                      :fan :off}
             :set-states! (partial swap! hvac assoc :states)})
    hvac))

(defn make-hvac-stub []
  (let [hvac (make-hvac)]
    (swap! hvac assoc :set-temp! (fn [temp]
                                   (swap! hvac assoc :get-temp (constantly temp))))
    hvac))

(defn make-hvac-stub-with-temp [temp]
  (let [hvac (make-hvac-stub)]
    ((:set-temp! @hvac) temp)
    hvac))


(defn assert-states [hvac expected]
  (is (= (@hvac :states)
         expected)))


(deftest test-tic-does-nothing-to-hvac-states-when-temp-starts-out-just-right
  (testing "tic does nothing to hvac states when :get-temp returns 70 degrees"
    (let [hvac (make-hvac-stub-with-temp 70)]
      (reset! heater-countdown 0)
      (tic hvac)
      (assert-states hvac {:heater :off, :cooler :off, :fan :off}))))

(deftest test-tic-turns-on-cooler-and-fan-when-temp-starts-out-too-high
  (testing "tic turns on cooler and fan when :get-temp returns 76 degrees"
    (let [hvac (make-hvac-stub-with-temp 76)]
      (reset! heater-countdown 0)
      (tic hvac)
      (assert-states hvac {:heater :off, :cooler :on, :fan :on}))))

(deftest test-tic-turns-on-heater-and-fan-when-temp-starts-out-too-low
  (testing "tic turns on heater and fan when :get-temp returns 64 degrees"
    (let [hvac (make-hvac-stub-with-temp 64)]
      (reset! heater-countdown 0)
      (tic hvac)
      (assert-states hvac {:heater :on, :cooler :off, :fan :on}))))

(deftest test-tic-keeps-fan-on-till-heater-cools-down
  (testing "fan stays on even under moderate conditions if heater has been off for less than 5 tics"
    (let [hvac (make-hvac-stub-with-temp 64)]
      (reset! heater-countdown 0)
      (tic hvac)
      ((:set-temp! @hvac) 70)
      (dorun 4 (repeatedly #(tic hvac)))
      (assert-states hvac {:heater :off, :cooler :off, :fan :on}))))

(deftest test-tic-turns-fan-off-after-heater-cools-down
  (testing "fan turns off under moderate conditions if heater has been off for at least 5 tics"
    (let [hvac (make-hvac-stub-with-temp 64)]
      (reset! heater-countdown 0)
      (tic hvac)
      ((:set-temp! @hvac) 70)
      (dorun 5 (repeatedly #(tic hvac)))
      (assert-states hvac {:heater :off, :cooler :off, :fan :off}))))



(run-tests 'environment-controller.core-test)
