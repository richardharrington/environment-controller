(ns environment-controller.core)

(defn tic [get-temp-from-hvac]
  (let [temp (get-temp-from-hvac)]
    (cond
     (< temp 65) {:cool nil, :heat true, :fan true}
     (> temp 75) {:cool true, :heat nil, :fan true}
     :otherwise  {:cool nil, :heat nil, :fan nil})))
