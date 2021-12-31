// You can customize these methods if needed, by overriding the methods in Analysis.java

import java.util.*;
import soot.jimple.Stmt;
import soot.Unit;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.UnitPrinter;
import soot.NormalUnitPrinter;

import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PAVBase {

    protected static String getPrgPointName(int st1) {
        String name1 = "in" + String.format("%02d", st1);
        return name1;
    }

    public static class ResultTuple {
        public final String m;
        public final String p;
        public final String v;
        public final String l;
        public final String u;

        public ResultTuple(String method, String prgpoint, String varname,
                String lowerval, String upperval) {
            this.m = method;
            this.p = prgpoint;
            this.v = varname;
            this.l = lowerval;
            this.u = upperval;
        }
    }

    protected static String fmtOutputLine(ResultTuple tup, String prefix) {
        String line = tup.m + ": " + tup.p + ": " + tup.v + ": " + "[" + tup.l + ", " + tup.u + "]";
        return (prefix + line);
    }
    protected static String fmtOutputLine(ResultTuple tup) {
        return fmtOutputLine(tup, "");
    }

    protected static String[] fmtOutputData(Set<ResultTuple> data, String prefix) {

        String[] outputlines = new String[ data.size() ];

        int i = 0;
        for (ResultTuple tup : data) {
            outputlines[i] = fmtOutputLine(tup, prefix);
            i++;
        }

        Arrays.sort(outputlines);
        return outputlines;
    }
    protected static String[] fmtOutputData(Set<ResultTuple> data) {
        return fmtOutputData(data, "");
    }


    ////////////////////////////////////////////////////////////////////////////

    protected static void drawMethodDependenceGraph(SootMethod entryMethod){
        if (!entryMethod.isPhantom() && entryMethod.isConcrete())
        {
            Body body = entryMethod.retrieveActiveBody();
            ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
            //ExceptionalBlockGraph  graph = new ExceptionalBlockGraph (body);

            CFGToDotGraph cfgForMethod = new CFGToDotGraph();
            cfgForMethod.drawCFG(graph);
            DotGraph cfgDot =  cfgForMethod.drawCFG(graph);
            cfgDot.plot("cfg.dot");
        }
    }

    protected static void printUnit(int lineno, Body b, Unit u){
        UnitPrinter up = new NormalUnitPrinter(b);
        u.toString(up);
        String linenostr = String.format("%02d", lineno) + ": ";
        System.out.println(linenostr + up.toString());
    }


    protected static void printInfo(SootMethod entryMethod) {
        if (!entryMethod.isPhantom() && entryMethod.isConcrete())
        {
            Body body = entryMethod.retrieveActiveBody();

            int lineno = 0;
            for (Unit u : body.getUnits()) {
                if (!(u instanceof Stmt)) {
                    continue;
                }
                Stmt s = (Stmt) u;
                printUnit(lineno, body, u);
                lineno++;
            }

        }
    }
}
