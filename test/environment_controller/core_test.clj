(ns environment-controller.core-test
  (:require [clojure.test :refer :all]
            [environment-controller.core :refer :all]))

; TODO: find out whether we're supposed to be querying
; the hvac device for its device states, or whether we
; keep track of that entirely ourselves -- i.e., do
; we have getters, or just setters?


(defn fixtures [f]
  (reset! heater-countdown 0)
  (reset! cooler-countdown 0)
  (reset! stored-states {})
  (f))



(use-fixtures :each fixtures)

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

(defn execute-tics-with-temps [hvac temp-sequence]
  (doseq [temp temp-sequence]
    (when temp
      ((:set-temp! @hvac) temp))
    (tic hvac)))

(defn assert-states [hvac expected]
  (is (= (@hvac :states)
         expected)))

(defn assert-temp-sequence-leads-to-states [temp-sequence expected-states]
  (let [hvac (make-hvac-stub)]
    (execute-tics-with-temps hvac temp-sequence)
    (assert-states hvac expected-states)))



(deftest test-tic-does-nothing-to-hvac-states-when-temp-starts-out-just-right
  (testing "tic does nothing to hvac states when :get-temp returns 70 degrees"
    (assert-temp-sequence-leads-to-states
     [70]
     {:heater :off, :cooler :off, :fan :off})))

(deftest test-tic-turns-on-cooler-and-fan-when-temp-starts-out-too-high
  (testing "tic turns on cooler and fan when :get-temp returns 76 degrees"
    (assert-temp-sequence-leads-to-states
     [76]
     {:heater :off, :cooler :on, :fan :on})))

(deftest test-tic-turns-on-heater-and-fan-when-temp-starts-out-too-low
  (testing "tic turns on heater and fan when :get-temp returns 64 degrees"
    (assert-temp-sequence-leads-to-states
     [64]
     {:heater :on, :cooler :off, :fan :on})))

(deftest test-tic-keeps-fan-on-till-heater-cools-down
  (testing "fan stays on even under moderate conditions if heater has been off for less than 5 tics"
    (assert-temp-sequence-leads-to-states
     [64 70 nil nil nil nil]
     {:heater :off, :cooler :off, :fan :on})))

(deftest test-tic-turns-fan-off-after-heater-cools-down
  (testing "fan turns off under moderate conditions if heater has been off for at least 5 tics"
    (assert-temp-sequence-leads-to-states
     [64 70 nil nil nil nil nil]
     {:heater :off, :cooler :off, :fan :off})))

(deftest test-tic-keeps-cooler-off-until-its-ready-to-start-up-again
  (testing "cooler stays off even under hot conditions if it's been off for less than 3 tics"
    (assert-temp-sequence-leads-to-states
     [76 70 76 nil]
     {:heater :off, :cooler :off, :fan :on})))

(deftest test-tic-turns-cooler-on-if-its-too-hot-and-cooler-has-been-off-long-enough
  (testing "cooler turns on under hot conditions if it's been off for at least 3 tics"
    (assert-temp-sequence-leads-to-states
     [76 70 76 nil nil]
     {:heater :off, :cooler :on, :fan :on})))


(run-tests 'environment-controller.core-test)
