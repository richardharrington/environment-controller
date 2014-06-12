(ns environment-controller.core-test
  (:require [clojure.test :refer :all]
            [environment-controller.core :refer :all]))

; TODO: find out whether we're supposed to be querying
; the hvac device for its device states, or whether we
; keep track of that entirely ourselves -- i.e., do
; we have getters, or just setters?


(defn make-hvac []
  (let [hvac (atom nil)]
    (reset! hvac
            {:get-temp (fn []) ; not implemented
             :heater :off
             :cooler :off
             :fan :off
             :set-heater (partial swap! hvac assoc :heater)
             :set-cooler (partial swap! hvac assoc :cooler)
             :set-fan (partial swap! hvac assoc :fan)})
    hvac))

(defn make-hvac-stub []
  (let [hvac (make-hvac)]
    (swap! hvac assoc :set-temp (fn [temp]
                                  (swap! hvac assoc :get-temp (constantly temp))))
    hvac))

(defn make-hvac-stub-with-temp [temp]
  (let [hvac (make-hvac-stub)]
    ((:set-temp @hvac) temp)
    hvac))

(defn hvac-states [hvac]
  (select-keys @hvac [:heater :cooler :fan]))


(deftest test-tic-does-nothing-to-hvac-states-when-temp-is-just-right
  (testing "tic does nothing to hvac states when hvac has 70 degree temp"
    (let [hvac (make-hvac-stub-with-temp 70)]
      (tic hvac)
      (is (= (hvac-states hvac)
             {:heater :off, :cooler :off, :fan :off})))))


;; (deftest test-tic-returns-correct-map-of-states-when-too-hot
;;   (testing "tic returns map of states (cool on, fan on, heat off) when hvac has 76 degree temp"
;;     (is (= (tic {:get-temp (constantly 76)})
;;            {:cool true, :heat nil, :fan true}))))

;; (deftest test-tic-returns-correct-map-of-states-when-too-cold
;;   (testing "tic returns map of states (cool off, fan on, heat on) when hvac has 64 degree temp"
;;     (is (= (tic {:get-temp (constantly 64)})
;;            {:cool nil, :heat true, :fan true}))))

(run-tests 'environment-controller.core-test)
