/*
Copyright (c) 2009,  Ingo Kofler, ITEC, Klagenfurt University, Austria

Developed by Ingo Kofler (ingo.kofler@itec.uni-klu.ac.at)
Based on the Instruction Counter tool by Felipe Lessa (felipe.lessa@gmail.com)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */
package mars.tools;

import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.util.InstructionStatisticsHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;


/**
 * A MARS tool for obtaining instruction statistics by instruction category.
 * <p>
 * The code of this tools is initially based on the Instruction counter tool by Felipe Lassa.
 * <p>
 * Some references:
 * <ul>
 * <li><a href="https://www.cs.kzoo.edu/cs230/Resources/MIPS/MachineXL/mipsOpFunctCodes.html">Simple Reference</a>
 * <li><a href="https://mathcs.holycross.edu/~csci226/MIPS/SPIM.pdf">Complete Reference</a>
 *
 * @author Ingo Kofler <ingo.kofler@itec.uni-klu.ac.at>
 * @author Tony S. <tony-turmoil@outlook.com>
 */
// @SuppressWarnings("serial")
public class InstructionStatistics extends AbstractMarsToolAndApplication {
    /**
     * name of the tool
     */
    private static final String NAME = "Instruction Statistics";
    /**
     * version and author information of the tool
     */
    private static final String VERSION = "Version 1.1 (Ingo Kofler, Tony S.)";
    /**
     * heading of the tool
     */
    private static final String HEADING = "";
    private final InstructionStatisticsHelper m_helper = new InstructionStatisticsHelper();
    /**
     * The last address we saw. We ignore it because the only way for a
     * program to execute twice the same instruction is to enter an infinite
     * loop, which is not insteresting in the POV of counting instructions.
     */
    protected int lastAddress = -1;
    /**
     * text field for visualizing the total number of instructions processed
     */
    private JTextField m_tfTotalCounter;
    /**
     * text field for visualizing the final cycle of the simulation
     */
    private JTextField m_tfStatistics;

    // From Felipe Lessa's instruction counter.  Prevent double-counting of instructions 
    // which happens because 2 read events are generated.   
    /**
     * array of text field - one for each instruction category
     */
    private JTextField m_tfCounters[];
    /**
     * array of progress pars - one for each instruction category
     */
    private JProgressBar m_pbCounters[];

    /**
     * Simple constructor, likely used to run a stand-alone enhanced instruction counter.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public InstructionStatistics(String title, String heading) {
        super(title, heading);
    }

    /**
     * Simple construction, likely used by the MARS Tools menu mechanism.
     */
    public InstructionStatistics() {
        super(InstructionStatistics.NAME + ", " + InstructionStatistics.VERSION, InstructionStatistics.HEADING);
    }

    /**
     * Manual construction for CLI mode.
     *
     * @param headless boolean indicating if the tool is running in CLI mode
     */
    public InstructionStatistics(boolean headless) {
        super(InstructionStatistics.NAME + ", " + InstructionStatistics.VERSION, InstructionStatistics.HEADING);
    }


    /**
     * returns the name of the tool
     *
     * @return the tools's name
     */
    public String getName() {
        return NAME;
    }


    /**
     * creates the display area for the tool as required by the API
     *
     * @return a panel that holds the GUI of the tool
     */
    protected JComponent buildMainDisplayArea() {

        // Create GUI elements for the tool
        JPanel panel = new JPanel(new GridBagLayout());

        m_tfTotalCounter = new JTextField("0", 10);
        m_tfTotalCounter.setEditable(false);

        m_tfCounters = new JTextField[InstructionStatisticsHelper.MAX_CATEGORY];
        m_pbCounters = new JProgressBar[InstructionStatisticsHelper.MAX_CATEGORY];

        // for each category a text field and a progress bar is created
        for (int i = 0; i < InstructionStatisticsHelper.MAX_CATEGORY; i++) {
            m_tfCounters[i] = new JTextField("0", 10);
            m_tfCounters[i].setEditable(false);
            m_pbCounters[i] = new JProgressBar(JProgressBar.HORIZONTAL);
            m_pbCounters[i].setStringPainted(true);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridheight = c.gridwidth = 1;

        // create the label and text field for the total instruction counter
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 17, 0);
        panel.add(new JLabel("Total: "), c);
        c.gridx = 3;
        panel.add(m_tfTotalCounter, c);

        c.insets = new Insets(3, 3, 3, 3);

        // create label, text field and progress bar for each category
        for (int i = 0; i < InstructionStatisticsHelper.MAX_CATEGORY; i++) {
            c.gridy++;
            c.gridx = 2;
            panel.add(new JLabel(m_helper.getCategoryLabel(i) + " (" + String.format("%.1f", m_helper.getInstWeight(i)) + "):    "), c);
            c.gridx = 3;
            panel.add(m_tfCounters[i], c);
            c.gridx = 4;
            panel.add(m_pbCounters[i], c);
        }

        c.gridy++;
        c.gridx = 2;
        panel.add(new JLabel("Final Cycle: "), c);
        c.gridx = 3;
        c.gridwidth = 2;
        m_tfStatistics = new JTextField("0", 10);
        m_tfStatistics.setEditable(false);
        panel.add(m_tfStatistics, c);

        return panel;
    }

    /**
     * performs initialization tasks of the counters before the GUI is created.
     */
    protected void initializePreGUI() {
        lastAddress = -1; // from Felipe Lessa's instruction counter tool
        m_helper.reset();
    }

    /**
     * resets the counter values of the tool and updates the display.
     */
    protected void reset() {
        lastAddress = -1; // from Felipe Lessa's instruction counter tool
        m_helper.reset();
        updateDisplay();
    }

    /**
     * method that is called each time the MIPS simulator accesses the text segment.
     * Before an instruction is executed by the simulator, the instruction is fetched from the program memory.
     * This memory access is observed and the corresponding instruction is decoded and categorized by the tool.
     * According to the category the counter values are increased and the display gets updated.
     *
     * @param resource the observed resource
     * @param notice   signals the type of access (memory, register etc.)
     */
    protected void processMIPSUpdate(Observable resource, AccessNotice notice) {

        if (!notice.accessIsFromMIPS()) {
            return;
        }

        // check for a read access in the text segment
        if (notice.getAccessType() == AccessNotice.READ && notice instanceof MemoryAccessNotice) {

            // now it is safe to make a cast of the notice
            MemoryAccessNotice memAccNotice = (MemoryAccessNotice) notice;

            // The next three statements are from Felipe Lessa's instruction counter.  Prevents double-counting.
            int a = memAccNotice.getAddress();
            if (a == lastAddress) {
                return;
            }
            lastAddress = a;

            try {

                // access the statement in the text segment without notifying other tools etc.
                ProgramStatement stmt = Memory.getInstance().getStatementNoNotify(memAccNotice.getAddress());

                // necessary to handle possible null pointers at the end of the program
                // (e.g., if the simulator tries to execute the next instruction after the last instruction in the text segment)
                if (stmt != null) {
                    m_helper.increment(stmt);
                    updateDisplay();
                }
            } catch (AddressErrorException e) {
                // silently ignore these exceptions
            }
        }
    }

    /**
     * registers the tool as observer for the text segment of the MIPS program
     */
    protected void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    /**
     * updates the text fields and progress bars according to the current counter values.
     */
    protected void updateDisplay() {
        m_helper.updateFinalCycle();

        m_tfTotalCounter.setText(String.valueOf(m_helper.getTotalCounter()));
        for (int i = 0; i < InstructionStatisticsHelper.MAX_CATEGORY; i++) {
            m_tfCounters[i].setText(String.valueOf(m_helper.getCounter(i)));
            // prevent division by zero by setting the maximum value to at least 1
            m_pbCounters[i].setMaximum(Math.max(m_helper.getTotalCounter(), 1));
            m_pbCounters[i].setValue(m_helper.getCounter(i));
        }
        m_tfStatistics.setText(String.format("%1$,.1f", m_helper.getFinalCycle()));
    }
}
