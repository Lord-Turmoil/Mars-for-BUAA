package mars.util;

import mars.ProgramStatement;

import java.util.Arrays;

public class InstructionStatisticsHelper {
    /**
     * number of instruction categories used by this tool
     */
    public static final int MAX_CATEGORY = 5;
    /**
     * constant for div instructions category
     */
    public static final int CATEGORY_DIV = 0;
    /**
     * constant for mul instructions category
     */
    public static final int CATEGORY_MUL = 1;
    /**
     * constant for jump instructions category
     */
    public static final int CATEGORY_BRANCH = 2;
    /**
     * constant for memory instructions category
     */
    public static final int CATEGORY_MEM = 3;
    /**
     * constant for any other instruction category
     */
    public static final int CATEGORY_OTHER = 4;

    /**
     * array of counter variables - one for each instruction category
     */
    private final int[] m_counters = new int[InstructionStatisticsHelper.MAX_CATEGORY];
    /**
     * names of the instruction categories as array
     */
    private final String[] m_categoryLabels = { "Division", "Multiply", "Jump/Branch", "Memory", "Others" };
    private final double[] m_instWeights = new double[]{ 25.0, 4.0, 2.0, 3.0, 1.0 };

    /**
     * counter for the total number of instructions processed
     */
    private int m_totalCounter = 0;

    /**
     * final statistics cycle of the simulation
     */
    private double m_finalCycle = 0.0;

    public String getCategoryLabel(int category) {
        return m_categoryLabels[category];
    }

    public double getInstWeight(int category) {
        return m_instWeights[category];
    }

    public int getTotalCounter() {
        return m_totalCounter;
    }

    public int getCounter(int category) {
        return m_counters[category];
    }

    public double getFinalCycle() {
        return m_finalCycle;
    }

    public void increment(ProgramStatement stmt) {
        int category = getInstructionCategory(stmt);
        m_counters[category]++;
        m_totalCounter++;
    }

    public void updateFinalCycle() {
        double finalCycle = 0.0;
        for (int i = 0; i < InstructionStatisticsHelper.MAX_CATEGORY; i++) {
            finalCycle += m_counters[i] * m_instWeights[i];
        }
        m_finalCycle = finalCycle;
    }

    public void reset() {
        m_totalCounter = 0;
        Arrays.fill(m_counters, 0);
    }

    /**
     * decodes the instruction and determines the category of the instruction.
     * <p>
     * The instruction is decoded by extracting the operation and function code of the 32-bit instruction.
     * Only the most relevant instructions are decoded and categorized.
     *
     * @param stmt the instruction to decode
     * @return the category of the instruction
     * @see InstructionStatisticsHelper#CATEGORY_DIV
     * @see InstructionStatisticsHelper#CATEGORY_MUL
     * @see InstructionStatisticsHelper#CATEGORY_BRANCH
     * @see InstructionStatisticsHelper#CATEGORY_MEM
     * @see InstructionStatisticsHelper#CATEGORY_OTHER
     */
    private int getInstructionCategory(ProgramStatement stmt) {

        int opCode = stmt.getBinaryStatement() >>> (32 - 6);
        int funct = stmt.getBinaryStatement() & 0x1F;

        if (opCode == 0x00) {
            if (funct == 0x00) {
                return InstructionStatisticsHelper.CATEGORY_OTHER; // sll
            }
            if (0x02 <= funct && funct <= 0x07) {
                return InstructionStatisticsHelper.CATEGORY_OTHER; // srl, sra, sllv, srlv, srav
            }
            if (funct == 0x08 || funct == 0x09) {
                return InstructionStatisticsHelper.CATEGORY_BRANCH; // jr, jalr
            }
            if (funct == 0x0A || funct == 0x0B) {
                return InstructionStatisticsHelper.CATEGORY_OTHER; // movz, movn
            }
            if (funct == 0x0C || funct == 0x0D) {
                return InstructionStatisticsHelper.CATEGORY_OTHER; // syscall, break
            }
            if (funct == 0x1A || funct == 0x1B) {
                return InstructionStatisticsHelper.CATEGORY_DIV; // div, divu
            }
            if (funct == 0x18 || funct == 0x19) {
                return InstructionStatisticsHelper.CATEGORY_MUL; // mult, multu
            }
            return InstructionStatisticsHelper.CATEGORY_OTHER;
        }
        if (opCode == 0x01) {
            if (0x00 <= funct && funct <= 0x07) {
                return InstructionStatisticsHelper.CATEGORY_BRANCH; // bltz, bgez, bltzl, bgezl
            }
            if (0x10 <= funct && funct <= 0x13) {
                return InstructionStatisticsHelper.CATEGORY_BRANCH; // bltzal, bgezal, bltzall, bgczall
            }
            return InstructionStatisticsHelper.CATEGORY_OTHER;
        }
        if (opCode == 0x02 || opCode == 0x03) {
            return InstructionStatisticsHelper.CATEGORY_BRANCH; // j, jal
        }
        if (0x04 <= opCode && opCode <= 0x07) {
            return InstructionStatisticsHelper.CATEGORY_BRANCH; // beq, bne, blez, bgtz
        }
        if (0x08 <= opCode && opCode <= 0x0F) {
            return InstructionStatisticsHelper.CATEGORY_OTHER; // addi, addiu, slti, sltiu, andi, ori, xori, lui
        }
        if (opCode == 0x11) {
            if (funct == 0x03) {
                return InstructionStatisticsHelper.CATEGORY_DIV; // div.s
            }
            if (funct == 0x02) {
                return InstructionStatisticsHelper.CATEGORY_MUL; // mul.s
            }
            return InstructionStatisticsHelper.CATEGORY_OTHER;
        }
        if (0x14 <= opCode && opCode <= 0x17) {
            return InstructionStatisticsHelper.CATEGORY_BRANCH; // beql, bnel, blezl, bgtzl
        }
        if (opCode == 0x1C) {
            if ((funct == 0x00) || (funct == 0x01) || (funct == 0x02) || (funct == 0x04) || (funct == 0x05)) {
                return InstructionStatisticsHelper.CATEGORY_MUL; // madd, maddu, mul, msub, msubu
            } else {
                return InstructionStatisticsHelper.CATEGORY_OTHER;
            }
        }
        if (0x20 <= opCode && opCode <= 0x26) {
            return InstructionStatisticsHelper.CATEGORY_MEM; // lb, lh, lwl, lw, lbu, lhu, lwr
        }
        if (0x28 <= opCode && opCode <= 0x2E) {
            return InstructionStatisticsHelper.CATEGORY_MEM; // sb, sh, swl, sw, swr
        }
        if (opCode == 0x30 || opCode == 0x38) {
            return InstructionStatisticsHelper.CATEGORY_MEM; // lwc1, swc1
        }

        return InstructionStatisticsHelper.CATEGORY_OTHER;
    }
}
