(ns environment-controller.core-test
  (:require [clojure.test :refer :all]
            [environment-controller.core :refer :all]))

(deftest test-tic-exists
  (testing "tic exists"
    (is (not (nil? tic)))))

(deftest test-tic-returns-correct-map-of-states-when-just-right
  (testing "tic returns map of off-states when hvac has 70 degree temp"
    (is (= (tic (constantly 70))
        {:cool nil, :heat nil, :fan nil}))))

(deftest test-tic-returns-correct-map-of-states-when-too-hot
  (testing "tic returns map of states (cool on, fan on, heat off) when hvac has 76 degree temp"
    (is (= (tic (constantly 76))
           {:cool true, :heat nil, :fan true}))))

(deftest test-tic-returns-correct-map-of-states-when-too-cold
  (testing "tic returns map of states (cool off, fan on, heat on) when hvac has 64 degree temp"
    (is (= (tic (constantly 64))
           {:cool nil, :heat true, :fan true}))))

(run-tests 'environment-controller.core-test)
