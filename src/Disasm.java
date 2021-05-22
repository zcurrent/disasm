import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Disasm {


    public static void main(String args[]){
        ArrayList<Integer> instructions = new ArrayList<>();
        ArrayList<String> instructionsDecoded = new ArrayList<>();
        int instIndex = 0;

        String inputFileNoSpaces = "";
        File input = new File(args[0]);

        try {
            Scanner sc = new Scanner(input);

            while(sc.hasNext()){
                inputFileNoSpaces += sc.next();
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        for(int i = 0; i < inputFileNoSpaces.length(); i += 32){
            String temp = inputFileNoSpaces.substring(i, i + 32);
            if (temp.charAt(0) == '1') {
                double tempNum = Integer.parseInt(temp.substring(1), 2) - Math.pow(2, 31);
                instructions.add(instIndex, (int) tempNum);

            } else {
                instructions.add(instIndex, Integer.parseInt(temp, 2));
            }
            instIndex++;
        }

        boolean[] targetBranches = new boolean[instructions.size() + 1];

        for(int i = 0; i < instructions.size(); i++){
            instructionsDecoded.add(decode(instructions.get(i), targetBranches, i));
        }

        System.out.println("main: ");
        for(int i = 0; i < instructionsDecoded.size(); i++){
            if(targetBranches[i]){
                System.out.println("label" + i + ":");
            }
            System.out.println(instructionsDecoded.get(i));
        }

        System.out.println("label" + instructions.size() + ":");
    }

    public static String decode(int inst, boolean[] targetBranches, int instrIndex){
        int opcode6 = inst >>> 26;
        int opcode8 = inst >>> 24;
        int opcode10 = inst >>> 22;
        int opcode11 = inst >>> 21;
        int rtRd = inst & 0x1F;
        int rn = (inst >>> 5) & 0x1F;
        int shamt = (inst >>> 10) & 0x3F;
        int rm = (inst >>> 16) & 0x1F;
        int aluImm = (inst >>> 10) & 0xFFF;
        int dtAddr = (inst >>> 12) & 0x1FF;
        int brAddr = inst & 0x3FFFFFF;
        int condBrAddr = (inst >>> 5) & 0x7FFFF;
        HashMap<Integer, String> labels = new HashMap<>();

        if(brAddr >= 33554432){
            brAddr -= 67108864;

        }

        if(condBrAddr >= 262144){
            condBrAddr -= 524288;
        }

        int blabel = instrIndex + brAddr;

        //Check B instruction format
        switch (opcode6){
            case 5:
                targetBranches[instrIndex + brAddr] = true;
                return "B label" + blabel;
            case 37:
                targetBranches[instrIndex + brAddr] = true;
                return "BL label" + blabel;

            default:
                break;
        }

        //Check CB instruction format
        switch (opcode8){
            case 180:
                targetBranches[instrIndex + condBrAddr] = true;
                return "CBZ X" + rtRd + ", label" + (int) (instrIndex + condBrAddr);
            case 181:
                targetBranches[instrIndex + condBrAddr] = true;
                return "CBNZ X" + rtRd + ", label" + (int) (instrIndex + condBrAddr);
            case 84:
                targetBranches[instrIndex + condBrAddr] = true;
                switch(rtRd){
                    case 0:
                        return "B.EQ label" + (int) (instrIndex + condBrAddr);
                    case 1:
                        return "B.NE label" + (int) (instrIndex + condBrAddr);
                    case 2:
                        return "B.HS label" + (int) (instrIndex + condBrAddr);
                    case 3:
                        return "B.LO label" + (int) (instrIndex + condBrAddr);
                    case 4:
                        return "B.MI label" + (int) (instrIndex + condBrAddr);
                    case 5:
                        return "B.PL label" + (int) (instrIndex + condBrAddr);
                    case 6:
                        return "B.VS label" + (int) (instrIndex + condBrAddr);
                    case 7:
                        return "B.VC label" + (int) (instrIndex + condBrAddr);
                    case 8:
                        return "B.HI label" + (int) (instrIndex + condBrAddr);
                    case 9:
                        return "B.LS label" + (int) (instrIndex + condBrAddr);
                    case 10:
                        return "B.GE label" + (int) (instrIndex + condBrAddr);
                    case 11:
                        return "B.LT label" + (int) (instrIndex + condBrAddr);
                    case 12:
                        return "B.GT label" + (int) (instrIndex + condBrAddr);
                    case 13:
                        return "B.LE label" + (int) (instrIndex + condBrAddr);

                }
        }

        //Check I instruction format
        switch (opcode10){
            case 580:
                return "ADDI " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 584:
                return "ANDI" + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 708:
                return "ADDIS " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 712:
                return "ORRI " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 836:
                return "SUBI " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 840:
                return "EORI " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 964:
                return "SUBIS " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
            case 968:
                return "ANDIS " + "X" + rtRd + ", X" + rn + ", #" + aluImm;
        }

        //Check D instruction format
        switch (opcode11){
            case 448:
                return "STURB " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 450:
                return "LDURB " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 960:
                return "STURH " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 962:
                return "LDURH " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1472:
                return "STURSW " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1476:
                return "LDURSW " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1504:
                return "STURS " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1506:
                return "LDURS " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1984:
                return "STUR " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 1986:
                return "LDUR " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 2016:
                return "STURD " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
            case 2018:
                return "LDURD " + "X" + rtRd + ", [X" + rn + ", #" + dtAddr + "]";
        }

        //Check R instruction format and extras
        switch (opcode11){
            case 241:
                switch (shamt){
                    case 2:
                        return "FMULS " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 6:
                        return "FDIVS " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 8:
                        return "FCMPS " + "X" + rn + ", X" + rm;
                    case 10:
                        return "FADDS " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 14:
                        return "FSUBS " + "X" + rtRd + ", X" + rn + ", X" + rm;
                }
            case 243:
                switch (shamt){
                    case 2:
                        return "FMULD " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 6:
                        return "FDIVD " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 8:
                        return "FCMPD " + "X" + rn + ", X" + rm;
                    case 10:
                        return "FADDD " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 14:
                        return "FSUBD " + "X" + rtRd + ", X" + rn + ", X" + rm;
                }
            case 1112:
                return "ADD " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1238:
                switch (shamt) {
                    case 2:
                        return "SDIV " + "X" + rtRd + ", X" + rn + ", X" + rm;
                    case 3:
                        return "UDIV " + "X" + rtRd + ", X" + rn + ", X" + rm;
                }
            case 1240:
                return "MUL " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1242:
                return "SMULH " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1246:
                return "UMULH " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1360:
                return "ORR " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1368:
                return "ADDS " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1616:
                return "EOR " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1624:
                return "SUB " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1690:
                return "LSR " + "X" + rtRd + ", X" + rn + ", #" + shamt;
            case 1691:
                return "LSL " + "X" + rtRd + ", X" + rn + ", #" + shamt;
            case 1712:
                switch (rtRd){
                    case 0:
                        return "BR main";
                    default:
                        return "BR label" + rtRd;
                }
            case 1872:
                return "ANDS " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 1880:
                return "SUBS " + "X" + rtRd + ", X" + rn + ", X" + rm;
            case 2044:
                return "PRNL";
            case 2045:
                return "PRNT";
            case 2046:
                return "DUMP";
            case 2047:
                return "HALT";

        }
        return null;
    }
}