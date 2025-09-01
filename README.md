# MusicAutomata
MusicAutomata is Java application that runs a 2D Cellular Automata simulation with customizable rules, and uses the resulting grid to generate real-time harmonically related sinusoudal sounds.

## Demo
The following video shows the application running.
https://github.com/user-attachments/assets/f7197f81-57e4-4f0b-b632-a236225c44f3


## Control guide:
-Grid: the grid displays cellular automata itself. The logic depends on the rule (see below) and it behave as every cell
outside the grid was always dead. You can change the state of every cell by clicking on it (both while the automata is
evolving and when it's not).
                        
-Start/Stop, Random, Clear Buttons: Start/Stop lets you start and stop the evolution of the cellular automata. Random will
randomize the state of every cell and clear will shut them all down. Both of the last 2 buttons can be pressed anytime.
                        
-Rule Section: the rule that the cellular automata will follow can be modified according to the "Rule String Notation": the
rule is decribed by 2 numbers:
  B: how many neighboors (the closest 8 cells) need to be alive to give birth to the cell;
  S: how many neighboors need to be alive to let the cell survive.
In any other case the cell dies (or simply does not birth). Both B and S can be changed anytime, with valid input (integer
numbers from 0 to 8), by clicking on Set Rule Button.
                        
-Change probability Slider: it controls the probability that each generation will actually occur. If lower then maximum value
there is a chance that the automata will skip an evolutionary step, creating a sort of random rythm in the audio pattern.
                        
-Speed Slider: controls the speed at witch the automata evolves.
                        
-Volume slider: it controls the overall volume.
                        
-Root Note and Sequence Combo Boxes: the logic of the audio part is simple: every cell in the lowest row of the grid will play
(if alive) a sinusoidal pluck-like sound starting from a root note (the bottom left cell) and following a certain sequence until
the bottom right cell (both the sequence and the root note can be chosen by the respective Combo Boxes on the right, anytime).
The cells above do the same thing, but their frequency is determined by the harmonic series (where the fundamental is
the lowest cell of their column, and their harmonic number is determined by their height in the grid).

