(ns assembler.clj
  (:require [instaparse.core :as insta]
			[superstring.core :as str]
			)
  (:gen-class))



(def label-map (atom {}))
(def variable-map (atom {}))
(def counter (atom -1))
(def variable-allocation-counter (atom 15))

(defn third [c]
  (nth c 2))

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

(defn parse-comp [s]
  (condp = s
	"0" "0101010"
	"1" "0111111"
	"-1" "0111010"
	"D" "0001100"
	"A" "0110000"
	"!D" "0001100"
	"!A" "0110001"
	"-D" "0001111"
	"-A" "0110011"
	"D+1" "0011111"
	"A+1" "0110111"
	"D-1" "0001110"
	"A-1" "0110010"
	"D+A" "0000010"
	"D-A" "0010011"
	"A-D" "0 000111"
	"D&A" "0000000"
	"D|A" "0010101"
	"M" "1110000"
	"!M" "1110001"
	"-M" "1110011"
	"M+1" "1110111"
	"M-1" "1110010"
	"D+M" "1000010"
	"D-M" "1010011"
	"M-D" "1000111"
	"D&M" "1000000"
	"D|M" "1010101"))

;; TODO CHECK THAT SIZE IS IN RANGE
(defn parse-literal-ainstruction [s]
  (str/pad-left (Integer/toBinaryString (Integer. s)) 16 "0"))

(defn parse-symbolic-aistruction [s]
  (if-let [instruction-number (@label-map s) ]
	(parse-literal-ainstruction instruction-number)
	(parse-literal-ainstruction (swap! variable-allocation-counter inc))))

(swap! variable-allocation-counter inc)

(defn parse-cinstruction [& s]
  (str "111"(apply str [(second s) (first s) (third s)] )))

(def whitespace
  (insta/parser
   "whitespace = #'\\s+'"))

(def asm-parser
  (insta/parser (slurp"/usr/home/michael/proj/clojure/assembler.clj/src/assembler/asm.bnf")
				:auto-whitespace whitespace ))

(defn parse-file [filename]
  (asm-parser (slurp filename)))

(defn build-label-map-and-preprocess [parse-tree]
  (reset! label-map {})
  (reset! variable-map {"R0" 0 "R1" 1 "R2" 2 "R3" 3 "R4" 4 "R5" 5 "R6" 6 "R7" 7 "R8" 8
					"R9" 9 "R10" 10 "R11" 11 "R12" 12 "R13" 13 "R14" 14 "R15" 15
					"SCREEN" 16384
					"KBD" 24576
					})
  (reset! counter -1)
  (reset! variable-allocation-counter 15)
  (insta/transform {
		:INSTRUCTION (fn [x] (swap! counter inc) x)
		:LABEL (fn [x] (swap! label-map #(assoc % x (inc @counter))) nil )
					:CINSTRUCTION-SANS-JUMP (fn [& x]
											  `[:CINSTRUCTION-COMPLETE ~@x [:JUMP ""]])

					:CINSTRUCTION-SANS-DEST (fn [& x]
											  `[:CINSTRUCTION-COMPLETE [:DEST ""] ~@x ])
					}
		parse-tree))

(defn transform-nodes [parse-tree]
  (insta/transform {
					:AINSTRUCTION-LITERAL parse-literal-ainstruction
					:AINSTRUCTION-SYMBOLIC parse-symbolic-aistruction
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
   (str/join "\n")
   (spit "out.hack")
   )

;; (->>(parse-file  "/usr/home/michael/proj/nand2tetris/projects/04/Mult.asm")
;;    (build-label-map-and-preprocess)
;;    (filter identity)
;;    (transform-nodes)
;;    ;; (str/join "\n")
;;    ;; (spit "out.hack")
;;    )
