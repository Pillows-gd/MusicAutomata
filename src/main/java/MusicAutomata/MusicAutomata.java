package MusicAutomata;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import static javax.swing.JOptionPane.showMessageDialog;
        
public class MusicAutomata extends JFrame {
    
    private enum CellStates {
        DEAD,
        ALIVE
    }
    
    private final int gridWidth = 7;
    private final int gridHeight = 7;
    private Cell[][] grid = new Cell[gridHeight][gridWidth];
    private ArrayList<Integer> birth = new ArrayList<>();
    private ArrayList<Integer> survive = new ArrayList<>();
    private Integer minSpeed = 5;
    private Integer maxSpeed = 200;
    private int volume = 80;
    private Integer speed = 50; // The actual speed will be 10000 / speed [ms per generation]
    private Integer changeProbability = 100;   // The probability [0-100] of a generation to actually change state
    
    // UI variables
    private int windowWidth = 640;
    private int windowHeight = 580;
    private JPanel midPanel = new JPanel();
    private JPanel rightOptionsPanel = new JPanel();
    private JButton helpButton = new JButton("?");
    private JLabel rootNoteLabel = new JLabel("Root note:");
    private JComboBox rootNoteComboBox = new JComboBox();
    private JLabel sequenceLabel = new JLabel("Note sequence:");
    private JComboBox sequenceComboBox = new JComboBox();
    private JLabel changeProbabilityLabel = new JLabel(
            "Change probability"
    );
    private JSlider changeProbabilitySlider = new JSlider(0, 100, changeProbability);
    private JPanel gridPanel = new JPanel(new GridLayout(gridWidth, gridHeight));
    private JPanel bottomOptionsPanel = new JPanel();
    private JPanel gridWithOptionsPanel = new JPanel();
    private JPanel gridOptionsPanel = new JPanel();
    private JPanel bottomLowOptionsPanel = new JPanel();
    private JButton playButton = new JButton("Start");
    private JButton randomButton = new JButton("Randomize");
    private JButton clearButton = new JButton("Clear");
    private JPanel bottomLowRightOptionsPanel = new JPanel();
    private JLabel volumeLabel = new JLabel("Volume");
    private JSlider volumeSlider = new JSlider(0, 100, volume);
    private JLabel speedLabel = new JLabel(
                "Speed [" +
                ((Integer)(10 / minSpeed)).toString() +
                "s - " + ((Integer)(10000 / maxSpeed)).toString() +
                "ms per generation]"
    );
    private JSlider speedSlider = new JSlider(minSpeed, maxSpeed, speed);
    private JPanel rulePanel = new JPanel();
    private JPanel ruleInputsPanel = new JPanel();
    private JButton setRuleButton = new JButton("Set Rule");
    private JPanel birthPanel = new JPanel();
    private JPanel survivePanel = new JPanel();
    private JLabel ruleLabel = new JLabel();
    private JLabel birthLabel = new JLabel("B");
    private JLabel surviveLabel = new JLabel("S");
    private JTextField birthTextField = new JTextField("3");
    private JTextField surviveTextField = new JTextField("23");
    private JLabel ruleErrorLabel = new JLabel("");
    
    private SwingWorker<Void,Void> worker;
    private boolean paused = false;
    
    // Audio variables
    private Synthesizer synth = JSyn.createSynthesizer();
    private LineOut lineOut = new LineOut();
    private int aliveCells = 0;
    private double[] envData = {
        0.01, 1 / (double)aliveCells,
        10 / (double)speed, 0
    };
    private SegmentedEnvelope env = new SegmentedEnvelope(envData);
    private Notation notation = new Notation();
    private double rootNote;
    private Integer[] semitoneSequence;
    private ArrayList<Integer> colMultiplier = new ArrayList<>();
    
    // Constructor
    public MusicAutomata() {
        
        // Window settings
        super("Music Automata");
        setSize(windowWidth, windowHeight);
        setResizable(false);
        
        // Audio: fill Combo-Boxes
        for (String n : notation.notes.keySet()) {
            rootNoteComboBox.addItem(n);
        }
        for (String s : notation.sequences.keySet()) {
            sequenceComboBox.addItem(s);
        }
        
        // Audio: store the value of the frequency corresponding to the current root note 
        rootNote = notation.notes.get((String)rootNoteComboBox.getItemAt(rootNoteComboBox.getSelectedIndex()));
        
        // Audio: fill the array that will contain semitone shift depending on row
        semitoneSequence = notation.sequences.get((String)sequenceComboBox.getItemAt(sequenceComboBox.getSelectedIndex()));
        for (int col = 0; col < gridWidth; col++) {
            colMultiplier.add(semitoneSequence[col % semitoneSequence.length] + 12 * (col / semitoneSequence.length));
        }
        
        // UI management
        {
            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            add(midPanel);
            midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
            gridPanel.setPreferredSize(new Dimension(400, 400));
            midPanel.add(gridWithOptionsPanel);
            gridWithOptionsPanel.add(gridPanel);
            CreateGrid(gridHeight, gridWidth);
            rightOptionsPanel.setLayout(new BoxLayout(rightOptionsPanel, BoxLayout.Y_AXIS));
            midPanel.add(rightOptionsPanel);
            changeProbabilityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rootNoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sequenceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            rightOptionsPanel.add(helpButton);
            rightOptionsPanel.add(new JPanel());
            rightOptionsPanel.add(rootNoteLabel);
            rightOptionsPanel.add(rootNoteComboBox);
            rightOptionsPanel.add(new JPanel());
            rightOptionsPanel.add(sequenceLabel);
            rightOptionsPanel.add(sequenceComboBox);
            rightOptionsPanel.add(new JPanel());
            rightOptionsPanel.add(changeProbabilityLabel);
            rightOptionsPanel.add(changeProbabilitySlider);
            rightOptionsPanel.add(new JPanel());
            speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            speedSlider.addChangeListener(new SliderChangeListener());
            rightOptionsPanel.add(speedLabel);
            rightOptionsPanel.add(speedSlider);
            helpButton.addActionListener(new ButtonActionListener());
            rootNoteComboBox.addActionListener(new ComboBoxActionListener());
            sequenceComboBox.addActionListener(new ComboBoxActionListener());
            changeProbabilitySlider.addChangeListener(new SliderChangeListener());
            bottomOptionsPanel.setPreferredSize(new Dimension(windowWidth, 120));
            bottomOptionsPanel.setLayout(new BoxLayout(bottomOptionsPanel, BoxLayout.Y_AXIS));
            add(bottomOptionsPanel);
            gridWithOptionsPanel.setLayout(new BoxLayout(gridWithOptionsPanel, BoxLayout.Y_AXIS));
            bottomLowOptionsPanel.setLayout(new BoxLayout(bottomLowOptionsPanel, BoxLayout.X_AXIS));
            bottomOptionsPanel.add(new JPanel());
            //bottomOptionsPanel.add(gridOptionsPanel);
            bottomOptionsPanel.add(new JPanel());
            bottomOptionsPanel.add(bottomLowOptionsPanel);
            bottomOptionsPanel.add(ruleErrorLabel);
            bottomOptionsPanel.add(new JPanel());
            ruleErrorLabel.setForeground(Color.red);
            gridWithOptionsPanel.add(gridOptionsPanel);
            gridOptionsPanel.setLayout(new BoxLayout(gridOptionsPanel, BoxLayout.X_AXIS));
            gridOptionsPanel.add(playButton);
            playButton.addActionListener(new ButtonActionListener());
            gridOptionsPanel.add(randomButton);
            randomButton.addActionListener(new ButtonActionListener());
            gridOptionsPanel.add(clearButton);
            clearButton.addActionListener(new ButtonActionListener());
            rulePanel.setLayout(new BoxLayout(rulePanel, BoxLayout.Y_AXIS));
            bottomLowOptionsPanel.add(rulePanel);
            ruleLabel.setText("Active Rule: B" + birthTextField.getText() + "/S" + surviveTextField.getText());
            ruleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rulePanel.add(ruleLabel);
            ruleInputsPanel.setLayout(new BoxLayout(ruleInputsPanel, BoxLayout.X_AXIS));
            rulePanel.add(ruleInputsPanel);
            ruleInputsPanel.add(setRuleButton);
            setRuleButton.addActionListener(new ButtonActionListener());
            ruleInputsPanel.add(birthPanel);
            ruleInputsPanel.add(survivePanel);
            birthPanel.setLayout(new BoxLayout(birthPanel, BoxLayout.Y_AXIS));
            birthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            birthPanel.add(birthLabel);
            birthPanel.add(birthTextField);
            survivePanel.setLayout(new BoxLayout(survivePanel, BoxLayout.Y_AXIS));
            surviveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            survivePanel.add(surviveLabel);
            survivePanel.add(surviveTextField);
            bottomLowRightOptionsPanel.setLayout(new BoxLayout(bottomLowRightOptionsPanel, BoxLayout.Y_AXIS));
            bottomLowOptionsPanel.add(bottomLowRightOptionsPanel);
            bottomLowRightOptionsPanel.add(volumeLabel);
            bottomLowRightOptionsPanel.add(volumeSlider);
            volumeSlider.addChangeListener(new SliderChangeListener());
        }
        
        // Convert input rules into arrays
        String[] birthString = birthTextField.getText().split("");
        for (String s : birthString) {
            birth.add(Integer.valueOf(s));
        }
        String[] surviveString = surviveTextField.getText().split("");
        for (String s : surviveString) {
            survive.add(Integer.valueOf(s));
        }
        
        // Define worker behaviour
        worker = new SwingWorker<Void,Void>() {
            
            public boolean pause = false;
            
            @Override
            protected Void doInBackground() {
                while (!isCancelled()) {
                    try {
                        if (paused) {
                            Thread.sleep(200);
                        } else {
                            // Check if this generation actually changes state
                            if (Math.random() < (double)changeProbability / 100) {
                                // Reset alive cells counter
                                aliveCells = 0;
                                // Make a copy of the old states not to refer to the one we are changing
                                CellStates[][] oldStates = new CellStates[gridHeight][gridWidth];
                                for (int row = 0; row < gridHeight; row++) {
                                    for (int col = 0; col < gridWidth; col++) {
                                        oldStates[row][col] = grid[row][col].getState();
                                    }
                                }
                                // Apply the rule and update alive cells counter
                                for (int row = 0; row < gridHeight; row++) {
                                    for (int col = 0; col < gridWidth; col++) {
                                        ApplyRule(oldStates, row, col);
                                        if (grid[row][col].getState() == CellStates.ALIVE) {
                                            aliveCells++;
                                        }
                                    }
                                }
                                // Balance the volume
                                envData[1] = 1 / (double)aliveCells;
                                // Play notes (done here because we want to first calculate the volume in the upper for cicle)
                                for (int row = 0; row < gridHeight; row++) {
                                    for (int col = 0; col < gridWidth; col++) {
                                        if (grid[row][col].getState() == CellStates.ALIVE) {
                                            grid[row][col].playNote();
                                        }
                                    }
                                }
                                Thread.sleep(10000 / speed);
                            } else {
                                Thread.sleep(10000 / speed); 
                            }
                        }
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                }
                return null;
            }
        };
        
        // Audio
        synth.add(lineOut);
        synth.start();
        lineOut.start();
    }
    
    // Cellular Automata Behaviour
    private void ApplyRule(CellStates[][] oldStates, int row, int col) {
        
        // Count alive neighboors
        int aliveNeighbours = 0;
        
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                try {
                    if ((i != 0 || j != 0) && oldStates[row + i][col + j].equals(CellStates.ALIVE)) {
                        aliveNeighbours++;
                    }
                } catch (IndexOutOfBoundsException e) {
                    // Do nothing: if alive cells go out of the canvas, act as if they were dead.
                }
            }
        }
        
        // Check if birth
        if (oldStates[row][col].equals(CellStates.DEAD)) {
            if (birth.contains(aliveNeighbours)) {
                grid[row][col].setState(CellStates.ALIVE);
            }
        }
        // Check if survive
        else {
            if (!survive.contains(aliveNeighbours)) {
                grid[row][col].setState(CellStates.DEAD);
            }
        }
    }
    
    // Place and populate grid
    private void CreateGrid(int height, int width) {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col]= new Cell(row, col);
            }
        }  
    }
    
    // Check if the input to the rule is valid and return the values describing the new rule
    private ArrayList<Integer> ParseInputRule(String inputString) {
        
        if (inputString.equals("")) {
            return new ArrayList<>();
        }
        
        ArrayList<Integer> newRule = new ArrayList();
        
        // CHECK INTEGER
        try {
            for (String s : inputString.split("")) {
                newRule.add(Integer.valueOf(s));
            }
        } catch (NumberFormatException e) {
            throw e;
        }
        // CHECK < 9
        for (Integer v : newRule) {
            if (v == 9) {
                throw new IllegalArgumentException();
            }
        }
        // CHECK DOUBLES
        for (int i = 0; i < newRule.size(); i++) {
            if (i != newRule.lastIndexOf(newRule.get(i))) {
                newRule.remove(newRule.get(i));
                i--;
            }
        }
        // REORDER
        newRule.sort(Comparator.naturalOrder());
        
        // RETURN
        return newRule;
    }
    
    // Start the application
    public static void main(String[] args) {
        MusicAutomata frame = new MusicAutomata();
        frame.setVisible(true);
    }
    
    
    // Cell class
    class Cell extends JPanel {
        
        int row;
        int col;
        
        double amp;
        private CellStates state;
        private SineOscillator osc = new SineOscillator();
        private VariableRateMonoReader envPlayer = new VariableRateMonoReader();
        //private JLabel freq = new JLabel();
        
        public Cell(int row, int col) {
            
            this.row = row;
            this.col = col;
            
            MouseAdapterPanel mouseAdapterPanel = new MouseAdapterPanel();
            addMouseListener(mouseAdapterPanel);
            setState(CellStates.DEAD);
            gridPanel.add(this);
            //this.add(freq);
            
            // Audio
            envPlayer.output.connect(osc.amplitude);
            calculateFrequency();
            calculateAmplitude();
            synth.add(osc);
            synth.add(envPlayer);
            osc.output.connect(0, lineOut.input, 0);
            osc.output.connect(0, lineOut.input, 1);
            //freq.setText(String.valueOf((int)osc.frequency.get()));
        }
        
        public CellStates getState() {
            return state;
        }
        
        public void setState(CellStates newState) {
            state = newState;
            switch(state) {
                case CellStates.DEAD:
                    setBackground(Color.black);
                    break;
                case CellStates.ALIVE:
                    setBackground(Color.white);
                    break;
                default:
                    break;
            }
        }

        private void playNote() {
            envPlayer.dataQueue.clear();
            double[] cellEnvData = new double[]{ envData[0], amp * envData[1], envData[2], envData[3] };
            env = new SegmentedEnvelope(cellEnvData);
            envPlayer.dataQueue.queue(env);
        }

        private void calculateFrequency() {
            osc.frequency.set(
                (gridHeight - row) *                                            // Harmonic multiplier
                rootNote *                                                      // Root note
                Math.pow(Math.pow(2, (double)1/12),(colMultiplier.get(col)))    // Semitone multiplier
            );
        }
        
        private void calculateAmplitude() {
            amp = Math.exp(-(double)(gridHeight-1 - row) * 3 / (double)gridHeight) * (double)volume * 0.01;
        }
    }
    
    // Manage cell state change by clicking
    class MouseAdapterPanel extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            // On click: switch the state of a cell to the next state
            
            CellStates newState = CellStates.DEAD;            
            Cell c = (Cell)e.getSource();
            
            // Get the next state
            for (int i = 0; i < CellStates.values().length; i++) {
                if (c.getState().equals(CellStates.values()[i])) {
                    newState = CellStates.values()[(i + 1) % CellStates.values().length];
                }
            }
            
            // Set the next state
            c.setState(newState);
        }
    }
    
    // Manage button actions
    class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == playButton) {
                if (worker.getState().equals(SwingWorker.StateValue.PENDING)) {
                    worker.execute();
                    playButton.setText("Stop");
                    synth.start();
                    lineOut.start();
                } else if (worker.getState().equals(SwingWorker.StateValue.STARTED) && !paused) {
                    paused = true;
                    playButton.setText("Start");
                    synth.stop();
                    lineOut.stop();
                } else if (worker.getState().equals(SwingWorker.StateValue.STARTED) && paused) {
                    paused = false;
                    playButton.setText("Stop");
                    synth.start();
                    lineOut.start();
                }
            } else if (e.getSource() == randomButton) {
                for(Cell[] row : grid) {
                    for (Cell c : row) {
                        c.setState(CellStates.values()[(int)(Math.random()*CellStates.values().length)]);
                    }
                }
            } else if (e.getSource() == clearButton) {
                for(Cell[] row : grid) {
                    for (Cell c : row) {
                        c.setState(CellStates.DEAD);
                    }
                }
            } else if (e.getSource() == setRuleButton) {
                try {
                    ArrayList<Integer> newBirth = ParseInputRule(birthTextField.getText());
                    ArrayList<Integer> newSurvive = ParseInputRule(surviveTextField.getText());
                    birth = newBirth;
                    survive = newSurvive;
                    StringBuilder birthString = new StringBuilder();
                    StringBuilder surviveString = new StringBuilder();
                    birth.forEach(x -> birthString.append(x.toString()));
                    survive.forEach(x -> surviveString.append(x.toString()));
                    ruleLabel.setText("Active Rule: B" + birthString + "/S" + surviveString);
                    birthTextField.setText(birthString.toString());
                    surviveTextField.setText(surviveString.toString());
                    ruleErrorLabel.setText("");
                } catch (Exception ex) {
                    ruleErrorLabel.setText("Invalid Rule! Please, insert digits from 0 to 8");
                }
            } else if (e.getSource() == helpButton) {
                showMessageDialog(((Component)e.getSource()).getParent().getParent(),
                        """
                        HELP WINDOW:
                        
                        Music Automata is a software that lets you play with a highly controllable 2D cellular automata that will generate
                        harmonically related sinusoidal sounds.
                        
                        Control guide:
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
                        
                        """);
            }
        }
    }
    
    class ComboBoxActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == rootNoteComboBox) {
                rootNote = notation.notes.get((String)rootNoteComboBox.getItemAt(rootNoteComboBox.getSelectedIndex()));
                UpdateCellsFrequencies();
            } else if (e.getSource() == sequenceComboBox) {
                semitoneSequence = notation.sequences.get((String)sequenceComboBox.getItemAt(sequenceComboBox.getSelectedIndex()));
                colMultiplier.clear();
                for (int col = 0; col < gridWidth; col++) {
                    colMultiplier.add(semitoneSequence[col % semitoneSequence.length] + 12 * (col / semitoneSequence.length));
                }
                UpdateCellsFrequencies();
            }
        }
        
        private void UpdateCellsFrequencies() {
            for (Cell[] row : grid) {
                for (Cell c : row) {
                    c.calculateFrequency();
                }
            }
        }
    }
    
    class SliderChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == speedSlider) {
                speed = ((JSlider)e.getSource()).getValue();
                envData[2] = 10 / (double)speed;   
            } else if (e.getSource() == changeProbabilitySlider) {
                changeProbability = ((JSlider)e.getSource()).getValue();
            } else if (e.getSource() == volumeSlider) {
                volume = ((JSlider)e.getSource()).getValue();
                UpdateCellsAmplitude();
            }
        }
        
        private void UpdateCellsAmplitude() {
            for (Cell[] row : grid) {
                for (Cell c : row) {
                    c.calculateAmplitude();
                }
            }
        }
    }
}