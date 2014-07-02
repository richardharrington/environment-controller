(ns environment-controller.hvac)

(defprotocol IHvac
  (get-temp [this])
  (set-device-states! [this states]))
