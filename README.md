# simple-editor

This is a bare-bones text editor written in Clojure. It's just my excuse to learn Clojure, zero real-world value.

It supports the following keys:

- `[char]` Insert a character
- `up`/`down`/`left`/`right` Move cursor
- `enter` Insert a newline
- `backspace` delete previous character

Here's a quick gif:

![](/demo/simple-editor.gif)

## Usage

- Download this repo
- Run `lein run`

## Under the hood

- Uses [`clojure-lanterna`](https://github.com/MultiMUD/clojure-lanterna) for terminal manipulation
- Structure based on Gary Bernhardt's [Text Editor From Scratch](https://www.destroyallsoftware.com/screencasts/catalog/text-editor-from-scratch)
