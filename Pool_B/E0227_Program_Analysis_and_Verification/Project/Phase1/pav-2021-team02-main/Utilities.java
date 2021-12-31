import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import soot.Body;
import soot.Unit;

public class Utilities {
    public static void appendToFile(String filePath, Body body, HashMap<Unit, LatticeElement> allStates,
        ArrayList<String> listVars, Integer optionalIterationIdx) {
        int lineNo = 0;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));

            // Logger.logSpecial("body.getUnits().size(): " + body.getUnits().size());

            for (Unit u : body.getUnits()) {
                if (allStates.containsKey(u)) {
                    String prefix = "";
                    if(optionalIterationIdx != null) prefix += String.format("%02d", optionalIterationIdx) + ": ";
                    prefix += Analysis.gTargetClassName + "." + Analysis.gTargetMethodName + ": in"
                            + String.format("%02d", lineNo);
                    IALatticeElement latticeElem = (IALatticeElement) allStates.get(u);
                    if(!latticeElem.isBot){
                        for (String var : listVars) {
                            // System.out.println("Printing for var: "+var);
                            String outputLine = prefix + ": " + var + ": " + latticeElem.state.get(var).toString() + "\n";
                            writer.append(outputLine);
                            // System.out.print(outputLine);
                        }
                    }
                    else{
                        // System.out.println("Unit at line " + lineNo + " is \\bot");
                    }
                    
                    // System.out.println();
                    // writer.append("\n");
                    lineNo++;
                }

            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void truncateFile(String filePath){
        PrintWriter pw;
        try {
            pw = new PrintWriter(filePath);
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
