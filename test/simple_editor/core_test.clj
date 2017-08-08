(ns simple-editor.core-test
  (:require [clojure.test :refer :all]
            [simple-editor.core :refer :all]))

(deftest test-simple-keys
  (testing "characters"
    (let [before-state {:lines ["123" "456"] :cursor {:x 0 :y 0}}
          actual (process-key \n before-state)
          expected {:lines ["n123" "456"] :cursor {:x 1 :y 0}}]
      (is (= actual expected))))

  (testing "up"
    (let [before-state {:lines ["a" "b"] :cursor {:x 0 :y 1}}
          actual (process-key :up before-state)
          expected {:lines ["a" "b"] :cursor {:x 0 :y 0}}]
      (is (= actual expected))))

  (testing "down"
    (let [before-state {:lines ["a" "b"] :cursor {:x 0 :y 0}}
          actual (process-key :down before-state)
          expected {:lines ["a" "b"] :cursor {:x 0 :y 1}}]
      (is (= actual expected)))))

(deftest test-enter
  (testing "creates new line if content of line is empty"
    (let [before-state {:lines [""] :cursor {:x 0 :y 0}}
          actual (process-key :enter before-state)
          expected {:lines ["" ""] :cursor {:x 0 :y 1}}]
      (is (= actual expected))))

  (testing "creates new line if at end of line"
    (let [before-state {:lines ["abc"] :cursor {:x 3 :y 0}}
          actual (process-key :enter before-state)
          expected {:lines ["abc" ""] :cursor {:x 0 :y 1}}]
      (is (= actual expected))))

  (testing "splits contents if in middle of line"
    (let [before-state {:lines ["abc"] :cursor {:x 1 :y 0}}
          actual (process-key :enter before-state)
          expected {:lines ["a" "bc"] :cursor {:x 0 :y 1}}]
      (is (= actual expected)))))

(deftest test-backspace
  (testing "does nothing if at start of document"
    (let [before-state {:lines ["abc"] :cursor {:x 0 :y 0}}
          actual (process-key :backspace before-state)
          expected {:lines ["abc"] :cursor {:x 0 :y 0}}]
      (is (= actual expected))))

  (testing "deletes character if in middle of line"
    (let [before-state {:lines ["abc"] :cursor {:x 1 :y 0}}
          actual (process-key :backspace before-state)
          expected {:lines ["bc"] :cursor {:x 0 :y 0}}]
      (is (= actual expected))))

  (testing "merges with previous line if at start of next"
    (let [before-state {:lines ["abc" "def"] :cursor {:x 0 :y 1}}
          actual (process-key :backspace before-state)
          expected {:lines ["abcdef"] :cursor {:x 3 :y 0}}]
      (is (= actual expected)))))
