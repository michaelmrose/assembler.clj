	@color_to_write // 16
	M = 1 // needs an inital value, herein white
	@24576
	D=A
	@end_of_screen //17
	M=D
	(WRITESCREEN)
	@color_to_write //16
	D=M
	@current_screen_color //18
	M=D
	@SCREEN //16384
	D=A
	@target //19
	M=D
	(WRITELOOP)
	@color_to_write //16
	D=M
	@target //19
	A=M
	M=D
	@target //19
	MD=M+1
	@end_of_screen //17
	D=M-D
	@WRITELOOP //?? 14
	D;JGT
	(INPUTLOOP)
	@KBD //24576
	D=M
	@KEYBOARD_NOT_PRESSED //20? actually 35
	D;JEQ
	@KEYBOARD_PRESSED //31 
	D;JNE
	(KEYBOARD_PRESSED) //
	@color_to_write // 16
	MD=-1 // black
	@CHECKCOLORS //39
	0;JMP
	(KEYBOARD_NOT_PRESSED)
	@color_to_write //16
	MD=0 //white
	@CHECKCOLORS //39
	0;JMP
	(CHECKCOLORS)
	@current_screen_color  //18
	D=D-M
	@INPUTLOOP //25
	D;JEQ
	@WRITESCREEN //6
	0;JMP
