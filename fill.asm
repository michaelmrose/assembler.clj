// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.


// PSEUDOCODE
// color_to_write = white
// end_of_screen = SCREEN + 8192

// 	(WRITESCREEN)
// 	current_screen = color_to_write
// 	(WRITELOOP)
// 	for (i=SCREEN,i<end_of_screen, i++)
// 	RAM[i]=color_to_write

// 	(INPUTLOOP)

// 	if (keyboard-pressed)
// 		color_to_write = black
// 	else
// 		color_to_write = white
// 	end

// 	if current_screen_color != color_to_write
// 		goto WRITESCREEN
// 	else
// 		goto INPUTLOOP
// 	end

	@color_to_write
	M = 1 // needs an inital value, herein white

	@24576
	D=A
	@end_of_screen
	M=D

	(WRITESCREEN)
	// set current_screen_color to new value of color_to_write
	// which will be true by the end of WRITELOOP

	@color_to_write
	D=M
	@current_screen_color
	M=D

	// store the starting address of SCREEN which we shall mutate throughout the loop
	@SCREEN
	D=A
	@target
	M=D

	(WRITELOOP)

	// write color_to_write to address in target
	@color_to_write
	D=M
	@target
	A=M
	M=D

	@target
	MD=M+1
	@end_of_screen
	D=M-D //end_of_screen - target will be greater than 1 if we haven't reached the end yet
	@WRITELOOP
	D;JGT

	(INPUTLOOP)

	// CHECK KBD and set color_to_write accordingly

	@KBD
	D=M
	@KEYBOARD_NOT_PRESSED
	D;JEQ
	@KEYBOARD_PRESSED
	D;JNE

		(KEYBOARD_PRESSED)
		@color_to_write
		MD=-1 // black
		@CHECKCOLORS
		0;JMP

		(KEYBOARD_NOT_PRESSED)
		@color_to_write
		MD=0 //white
		@CHECKCOLORS
		0;JMP

		// CHECK if color_to_write and current screen are the same and either jump to
		// WRITESCREEN or back up to INPUTLOOP

		// Importantly we aren't going to go into a write loop if the screen is already the
		// desired color and waste 8192 ops during which we can't respond to keyboard
		// input.

		(CHECKCOLORS)

		@current_screen_color
		D=D-M // color_to_write - current_screen_color will be zero if the same
		@INPUTLOOP
		D;JEQ
		@WRITESCREEN
		0;JMP

