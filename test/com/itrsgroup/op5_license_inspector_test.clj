(ns com.itrsgroup.op5-license-inspector-test
  (:require [clojure.test :refer :all]
            [com.itrsgroup.op5-license-inspector :refer :all]))

(def example-license-file "resources/op5license.lic")
(def example-license-file-without-modules "resources/op5license3.lic")

(deftest read-example-license
  (is (license-file? example-license-file))
  (is (seq (decoded-license example-license-file))))

(deftest valid-module-map
  (let [m1 (modules-map (decoded-license example-license-file))
        m3 (modules-map (decoded-license example-license-file-without-modules))]
    (testing "all modules are included"
      (is (= {:modules {:logger true
                        :trapper true
                        :nagvis true
                        :bsm true
                        :reports true}}
             m1)))
    (testing "no modules are included"
      (is (= {:modules {:logger false
                        :trapper false
                        :nagvis false
                        :bsm false
                        :reports false}}
             m3)))))

(deftest valid-license-map
  (testing "the value of"
    (let [l (license-map example-license-file)
          t #(testing (str %1 " in the example license file")
               (is (= %2 (%1 l))))]
      (t :company-name "ITRS Group")
      (t :hosts 1)
      (t :services 1)
      (t :peers 1)
      (t :pollers 1)
      (t :valid-from "2022-01-01")
      (t :valid-until "2022-06-04")
      (t :customer-account "Johan Thoren")
      (t :customer-id "0036M00003GCTn7QAH")
      (t :recipient "jthoren@itrsgroup.com")
      (t :branding "enterprise_plus")
      (t :custom {"License details" "For internal development purposes only"})
      (t :modules {:logger true
                   :trapper true
                   :nagvis true
                   :bsm true
                   :reports true}))
    (let [l (license-map example-license-file-without-modules)
          t #(testing (str %1 " in the example license file without modules")
               (is (= %2 (%1 l))))]
      (t :company-name "ITRS Group")
      (t :hosts 1)
      (t :services 1)
      (t :peers 0)
      (t :pollers 0)
      (t :valid-from "2022-06-03")
      (t :valid-until "2022-06-04")
      (t :customer-account "Johan Thoren")
      (t :customer-id "0036M00003GCTn7QAH")
      (t :recipient "jthoren@itrsgroup.com")
      (t :branding "generic")
      (t :custom {})
      (t :modules {:logger false
                   :trapper false
                   :nagvis false
                   :bsm false
                   :reports false}))))
