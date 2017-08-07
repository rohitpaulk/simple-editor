(ns simple-editor.core)

(require '[lanterna.terminal :as t])

(defn render
  "Renders lines + cursor to the terminal"
  [term, {:keys [lines, cursor]}]
  (t/clear term)
  (defn put-line
    "Writes the string to a terminal and prints a newline"
    [line]
    (t/put-string term (str line "\n")))
  (dorun (map put-line lines))
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

(defn process-down
  [char {:keys [lines cursor]}]

  (let [max-y (count lines)
        new-y (+ (:y cursor) 1)
        new-cursor (assoc cursor :y (min max-y new-y))]
    {:lines lines :cursor new-cursor}))

(defn process-up
  [char {:keys [lines cursor]}]

  (let [min-y 0
        new-y (- (:y cursor) 1)
        new-cursor (assoc cursor :y (max min-y new-y))]
    {:lines lines :cursor new-cursor}))

(defn process-left
  [char {:keys [lines cursor]}]

  (let [min-x 0
        new-x (- (:x cursor) 1)
        new-cursor (assoc cursor :x (max min-x new-x))]
    {:lines lines :cursor new-cursor}))

(defn process-right
  [char {:keys [lines cursor]}]

  (let [current-line (nth lines (:y cursor))
        max-x (count current-line)
        new-x (+ (:x cursor) 1)
        new-cursor (assoc cursor :x (min max-x new-x))]
    {:lines lines :cursor new-cursor}))

(defn process-key
  "
  Processes one input character from the terminal.

  Returns the new state.
  "
  [key state]

  (defn is-char [x] (= java.lang.Character (type x)))
  (defn is-up [x] (= :up x))
  (defn is-down [x] (= :down x))
  (defn is-right [x] (= :right x))
  (defn is-left [x] (= :left x))

  (cond
    (is-char key) (process-char key state)
    (is-left key) (process-left key state)
    (is-right key) (process-right key state)
    (is-up key) (process-up key state)
    (is-down key) (process-down key state)
    :else state))

(defn -get-lines
  "Returns an array of lines, given a file path"
  [file-path]
  (def file-contents (slurp file-path))
  (clojure.string/split-lines file-contents))

(defn get-key-stream
  "Returns a lazy sequence that terminates when a user presses escape"
  [term]

  (defn nil-if-escape
    [key]
    (if (= key :escape) nil key))

  (defn da-func []
    (nil-if-escape (t/get-key-blocking  term)))

  (take-while identity (repeatedly da-func)))

(defn open-editor
  "Opens the editor with the given file"
  [file-path]
  (def lines (-get-lines file-path))
  (def cursor {:x 0 :y 0})
  (def state {:lines lines :cursor cursor})
  (def term (t/get-terminal :unix))
  (t/in-terminal term
    (render term state)
    (doseq [key (get-key-stream term)]
      (def state (process-key key state))
      (render term state))))

(defn -main
  "Entry point"
  []
  (open-editor "resources/foo.txt"))
