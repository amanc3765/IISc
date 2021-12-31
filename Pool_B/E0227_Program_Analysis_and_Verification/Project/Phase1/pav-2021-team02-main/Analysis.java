// This program will plot a CFG for a method using soot [ExceptionalUnitGraph feature].
// Arguements : <ProcessOrTargetDirectory> <MainClass> <TargetClass> <TargetMethod>
//Ref: 1) https://gist.github.com/bdqnghi/9d8d990b29caeb4e5157d7df35e083ce
//     2) https://github.com/soot-oss/soot/wiki/Tutorials

////////////////////////////////////////////////////////////////////////////////
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import heros.flowfunc.Kill;

////////////////////////////////////////////////////////////////////////////////

import soot.options.Options;

import soot.Unit;
import soot.Scene;
import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.parser.node.TNeg;
import soot.UnitPrinter;
import soot.Value;
import soot.NormalUnitPrinter;

import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;

////////////////////////////////////////////////////////////////////////////////

public class Analysis extends PAVBase {
    private static HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    public static String gTargetDirectoryName, gTargetClassName, gTargetMethodName;

    public static ArrayList<String> getVariableNamesFromBody(Body body) {
        ArrayList<String> listVars = new ArrayList<>();

        for (Local l : body.getLocals()) {
            listVars.add(l.getName());
        }

        for (Value p : body.getParameterRefs()) {
            ParameterRef pRef = (ParameterRef) p;
            listVars.add("@parameter" + pRef.getIndex());
        }

        System.out.print("\nList of variables : ");
        for (String var : listVars) {
            System.out.print(var + ", ");
        }
        System.out.println();

        return listVars;
    }

    public static void doAnalysis(SootClass tClass, SootMethod tMethod) {
        HashMap<Unit, LatticeElement> allStates = new HashMap<>();
        Body body = tMethod.retrieveActiveBody();

        // find all variables
        ArrayList<String> listVars = getVariableNamesFromBody(body);
        Collections.sort(listVars);

        for (Unit u : body.getUnits()) {
            allStates.put(u, new IALatticeElement());
        }

        Unit startUnit = body.getUnits().getFirst();
        IALatticeElement startState = (IALatticeElement) allStates.get(startUnit);
        startState.setDefaultInitValue(listVars);

        Kildall.runKildall(tMethod, allStates, listVars);

        String outFilePath = gTargetDirectoryName + "/" + gTargetClassName + "." + gTargetMethodName + ".output.txt";
        Utilities.truncateFile(outFilePath);
        Utilities.appendToFile(outFilePath, body, allStates, listVars, null);

    }

    public static void main(String[] args) {

        // String targetDirectory="./target";
        // String mClass="AddNumFun";
        // String tClass="AddNumFun";
        // String tMethod="expr"

        String targetDirectory = args[0];
        String mClass = args[1];
        String tClass = args[2];
        String tMethod = args[3];
        boolean methodFound = false;

        gTargetDirectoryName = targetDirectory;
        gTargetClassName = tClass;
        gTargetMethodName = tMethod;

        List<String> procDir = new ArrayList<String>();
        procDir.add(targetDirectory);

        // Set Soot options
        soot.G.reset();
        Options.v().set_process_dir(procDir);
        // Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().set_keep_line_number(true);
        Options.v().setPhaseOption("cg.spark", "verbose:false");

        Scene.v().loadNecessaryClasses();

        SootClass entryClass = Scene.v().getSootClassUnsafe(mClass);
        SootMethod entryMethod = entryClass.getMethodByNameUnsafe("main");
        SootClass targetClass = Scene.v().getSootClassUnsafe(tClass);
        SootMethod targetMethod = entryClass.getMethodByNameUnsafe(tMethod);

        Options.v().set_main_class(mClass);
        Scene.v().setEntryPoints(Collections.singletonList(entryMethod));

        // System.out.println (entryClass.getName());

        System.out.println("\n________________________________________________________________");
        System.out.println();
        System.out.println("tclass: " + targetClass);
        System.out.println("tmethod: " + targetMethod);
        System.out.println("tmethodname: " + tMethod);
        System.out.println();

        Iterator mi = targetClass.getMethods().iterator();
        while (mi.hasNext()) {
            SootMethod sm = (SootMethod) mi.next();
            // System.out.println("method: " + sm);
            if (sm.getName().equals(tMethod)) {
                methodFound = true;
                break;
            }
        }

        if (methodFound) {
            printInfo(targetMethod);
            /*************************************************************
             * XXX This would be a good place to call the function which performs the
             * Kildalls Analysis
             *************************************************************/
            drawMethodDependenceGraph(targetMethod);

            doAnalysis(targetClass, targetMethod);
        } else {
            System.out.println("Method not found: " + tMethod);
        }
    }

}
