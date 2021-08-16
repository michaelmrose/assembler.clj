(ns assembler.clj
  (:require [instaparse.core :as insta]
			[superstring.core :as str]
			)
  (:gen-class))

(def label-map (atom {}))
(def counter (atom 0))
(def variable-allocation-counter (atom 0))

(defn parse-jump [s]
  (condp = s
	"" "000"
	"JGT" "001"
	"JEQ" "010"
	"JGE" "011"
	"JLT" "100"
	"JNE" "101"
	"JLE" "110"
	"JMP" "111"))

(defn parse-dest [s]
  (condp = s
	"" "000"
	"M" "001"
	"D" "010"
	"MD" "011"
	"A" "100"
	"AM" "101"
	"AD" "110"
	"AMD" 111))

;; TODO fill out the rest of comp
(defn parse-comp [s]
  (condp = s
	"0" "0101010"
	"1" "0111111"
	"-1" "0111010"
	"D" "0001100"
	"A" "0110000"
	"!D" "0"
	"!A" "0"
	"-D" "0"
	"-A" "0"
	"D+1" "0"
	"A+1" "0"
	"D-1" "0"
	"A-1" "0"
	"D+A" "0"
	"D-A" "0"
	"A-D" "0"
	"D&A" "0"
	"D|A" "0"
	"M" "1"
	"!M" "1"
	"-M" "1"
	"M+1" "1"
	"M-1" "1"
	"D+M" "1"
	"D-M" "1"
	"M-D" "1"
	"D&M" "1"
	"D|M" "1"))

;; TODO CHECK THAT SIZE IS IN RANGE
(defn parse-literal-ainstruction [s]
  (str/pad-left (Integer/toBinaryString (Integer. s)) 16 "0"))

;; TODO THIS NEEDS TO ALLOCATE variables as needed
(defn parse-symbolic-aistruction [s]
							(parse-literal-ainstruction (@label-map s)))

;; I NEED TO FIND A WAY TO INCLUDE ZERO PADDING FOR WHEN
;; DEST AND OR JUMP DON'T EXIST. PERHAPS CHECK IN THIS FUNCTION AND INSERT
;; ZEROS
(defn parse-cinstruction [& s]
  (str "111"(apply str s )))

(def whitespace
  (insta/parser
   "whitespace = #'\\s+'"))

(def asm-parser
  (insta/parser (slurp"/usr/home/michael/proj/clojure/assembler.clj/src/assembler/asm.bnf")
				:auto-whitespace whitespace ))

(defn parse-file [filename]
  (asm-parser (slurp filename)))

;; TODO in grammer file add new cinstruction types and transform them into the complete
;; cinstruction. Probably need to rename this.

(defn build-label-map-and-preprocess [parse-tree]
  (reset! label-map {})
  (reset! counter 0)
  (insta/transform {
		:INSTRUCTION (fn [x] (swap! counter inc) x)
		:LABEL (fn [x] (swap! label-map #(assoc % x (inc @counter))) nil )
					:CINSTRUCTION-SANS-JUMP (fn [& x]
											  `[:CINSTRUCTION-COMPLETE ~@x [:JUMP ""]])

					:CINSTRUCTION-SANS-DEST (fn [& x]
											  `[:CINSTRUCTION-COMPLETE[:DEST ""] ~@x ])
					}
		parse-tree))

(defn transform-nodes [parse-tree]
  (insta/transform {
					:AINSTRUCTION-LITERAL parse-literal-ainstruction
					;; :AINSTRUCTION-SYMBOLIC parse-symbolic-aistruction
					:CINSTRUCTION-COMPLETE parse-cinstruction
					:DEST parse-dest
					:COMP parse-comp
					:JUMP parse-jump
					}
				   parse-tree
				   ))

(->>(parse-file  "/usr/home/michael/proj/clojure/assembler.clj/fake.asm")
   (build-label-map-and-preprocess)
   (filter identity)
   (transform-nodes)
   )
