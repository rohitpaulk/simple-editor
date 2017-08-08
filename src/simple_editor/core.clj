(ns simple-editor.core)

(require '[lanterna.terminal :as t])
(require '[taoensso.encore :as encore])

(defn render
  "Renders lines + cursor to the terminal"
  [term, {:keys [lines, cursor]}]
  (t/clear term)
  (t/put-string term (clojure.string/join "\n" lines))
  (t/move-cursor term (:x cursor) (:y cursor)))

(defn str-insert
  "Insert char in string at index i."
  [string char i]
  (str (subs string 0 i) char (subs string i)))

(defn process-char
  "
  Inserts a character at the cursor position, moves the cursor forward
  "
  [char {:keys [lines cursor]}]
  (let [line-to-modify (nth lines (:y cursor))
        modified-line (str-insert line-to-modify char (:x cursor))
        new-lines (assoc lines (:y cursor) modified-line)
        new-cursor (assoc cursor :x (+ (:x cursor) 1))]
    {:lines new-lines :cursor new-cursor}))

(defn clamp-cursor
  [cursor lines]
  (let [y (:y cursor)
        x (:x cursor)
        clamped-y (encore/clamp 0 (dec (count lines)) y)
        current-line (get lines clamped-y "")
        clamped-x (encore/clamp 0 (count current-line) x)]
    {:x clamped-x :y clamped-y}))

(defn process-down
  [_ {:keys [lines cursor]}]

  (let [new-cursor (assoc cursor :y (inc (:y cursor)))
        new-cursor (clamp-cursor new-cursor lines)]
    {:lines lines :cursor new-cursor}))

(defn process-up
  [_ {:keys [lines cursor]}]

  (let [new-cursor (assoc cursor :y (dec (:y cursor)))
        new-cursor (clamp-cursor new-cursor lines)]
    {:lines lines :cursor new-cursor}))

(defn process-left
  [_ {:keys [lines cursor]}]

  (let [new-cursor (assoc cursor :x (dec (:x cursor)))
        new-cursor (clamp-cursor new-cursor lines)]
    {:lines lines :cursor new-cursor}))

(defn process-right
  [_ {:keys [lines cursor]}]

  (let [new-cursor (assoc cursor :x (inc (:x cursor)))
        new-cursor (clamp-cursor new-cursor lines)]
    {:lines lines :cursor new-cursor}))

(defn remove-char
  [string pos]
  (if (= pos 0)
    string
    (str (subs string 0 (dec pos)) (subs string pos))))

(defn process-backspace
  [_ {:keys [lines cursor]}]

  (let [y (:y cursor)
        x (:x cursor)
        is-line-start (= 0 x)
        is-first-line (= 0 y)
        should-collapse (and is-line-start (not is-first-line))]

    (if should-collapse
      (let [current-line (nth lines y)
            previous-line (nth lines (dec y))
            merged-line (clojure.string/join "" [previous-line current-line])
            new-lines (assoc lines y [merged-line])
            new-lines (assoc new-lines (dec y) [])
            new-lines (into [] (flatten new-lines))
            new-cursor {:x (count previous-line) :y (dec (:y cursor))}]
        {:lines new-lines :cursor new-cursor})

      ; Else, we just remove a single character
      (let [current-line (nth lines y)
            new-line (remove-char current-line x)
            new-lines (assoc lines y new-line)
            new-cursor (assoc cursor :x (- (:x cursor) 1))
            new-cursor (clamp-cursor new-cursor lines)]
       {:lines new-lines :cursor new-cursor}))))

(defn process-enter
  [_ {:keys [lines cursor]}]

  (let [current-line (nth lines (:y cursor))
        replaced-line-contents (subs current-line 0 (:x cursor))
        next-lines-contents (subs current-line (:x cursor))
        new-lines (assoc lines (:y cursor) replaced-line-contents)
        [before, after] (split-at (inc (:y cursor)) new-lines)
        new-lines (into [] (concat before [next-lines-contents] after))
        new-cursor {:x 0 :y (inc (:y cursor))}]
    {:lines new-lines :cursor new-cursor}))

(defn process-key
  "
  Processes one input character from the terminal.

  Returns the new state.
  "
  [key state]

  (cond
    (= java.lang.Character (type key)) (process-char key state)
    (= :left key) (process-left key state)
    (= :right key) (process-right key state)
    (= :up key) (process-up key state)
    (= :down key) (process-down key state)
    (= :backspace key) (process-backspace key state)
    (= :enter key) (process-enter key state)
    :else state))

(defn -get-lines
  "Returns an array of lines, given a file path"
  [file-path]
  (clojure.string/split-lines (slurp file-path)))

(defn editor-loop
  [term state]
  (let [key (t/get-key-blocking term)]
    (cond
      (= :escape key) nil ; Terminate loop
      :else (let [next-state (process-key key state)]
              (render term next-state)
              (editor-loop term next-state)))))

(defn open-editor
  "Opens the editor with the given file"
  [file-path]
  (let [lines (-get-lines file-path)
        cursor {:x 0 :y 0}
        term (t/get-terminal :unix)
        state {:lines lines :cursor cursor}]
    (t/in-terminal term
      (render term state)
      (editor-loop term state))))

(defn -main
  "Entry point"
  []
  (open-editor "resources/foo.txt"))
