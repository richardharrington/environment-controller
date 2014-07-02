(ns environment-controller.hvac)

(defprotocol IHvac
  (get-temp [this])
  (get-device-states [this])
  (set-device-states! [this states]))
