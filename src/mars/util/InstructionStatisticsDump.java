package mars.util;

import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * This is the CLI version of {@link mars.tools.InstructionStatistics}.
 * It has no GUI dependencies and can be used in a headless environment.
 */
public class InstructionStatisticsDump implements Observer {
    private final InstructionStatisticsHelper m_helper = new InstructionStatisticsHelper();
    private int lastAddress = -1;

    public InstructionStatisticsDump() {
        addAsObserver();
    }

    @Override
    public void update(Observable resource, Object accessNotice) {
        if (((AccessNotice) accessNotice).accessIsFromMIPS()) {
            processMIPSUpdate(resource, (AccessNotice) accessNotice);
        }
    }

    /**
     * Outputs the final statistics of the instruction categories to a file.
     */
    public void dump() {
        m_helper.updateFinalCycle();
        try {
            FileWriter fw = new FileWriter("InstructionStatistics.txt", false);
            for (int i = 0; i < InstructionStatisticsHelper.MAX_CATEGORY; i++) {
                fw.write(String.format("%s (%.1f): %d\n",
                        m_helper.getCategoryLabel(i),
                        m_helper.getInstWeight(i),
                        m_helper.getCounter(i)));
            }
            fw.write(String.format("%s: %.1f\n", "Final Cycle", m_helper.getFinalCycle()));
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                }
            } catch (AddressErrorException e) {
                // silently ignore these exceptions
            }
        }
    }

    private void addAsObserver() {
        addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }

    private void addAsObserver(int lowEnd, int highEnd) {
        try {
            Globals.memory.addObserver(this, lowEnd, highEnd);
        } catch (AddressErrorException e) {
            throw new RuntimeException("Unexpected AddressErrorException", e);
        }
    }
}
