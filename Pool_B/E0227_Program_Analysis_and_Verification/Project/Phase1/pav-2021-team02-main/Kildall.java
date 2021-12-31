import java.util.*;

import org.slf4j.helpers.Util;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class Kildall {

    public static void printAllStates(HashMap<Unit, LatticeElement> allStates, HashSet<Unit> markedUnits) {
        Logger.log("\nprintAllStates=> \n");

        for (Unit u : allStates.keySet()) {
            Logger.log("Program point : " + Logger.ANSI_CYAN + u.toString() + Logger.ANSI_RESET);
            String isMarked = markedUnits.contains(u) ? Logger.ANSI_RED+"MARKED"+Logger.ANSI_RESET : "UNMARKED";
            Logger.log(" [" + isMarked+"]");
            Logger.log(Logger.ANSI_PURPLE + allStates.get(u).toString() + "\n" + Logger.ANSI_RESET);
        }

        Logger.log("\n");
    }

    public static void dfs(Unit u, ExceptionalUnitGraph graph, HashMap<Unit, Integer> visited, HashSet<ArrayList<Unit>> backEdges){
        visited.put(u, 1);
        Logger.log("\n\nVisited unit : "+u.toString());

        for(Unit adj : graph.getSuccsOf(u)){
            int visitedVal=0;
            if(visited.get(adj) == null) {
                // Logger.logErr()
                System.exit(1);
            }
            else{
                visitedVal = visited.get(adj).intValue();
            }

            if(visitedVal == 0){
                dfs(adj, graph, visited, backEdges);
            }
            else if(visitedVal == 1){
                ArrayList<Unit> backEdge = new ArrayList<>();
                backEdge.add(u);
                backEdge.add(adj);
                backEdges.add(backEdge);

                Logger.log("\nAdded back edge : [\"" + u.toString() + "\" => \""+adj.toString()+"\"]");
            }
        }
        visited.put(u, 2);
    }

    public static HashSet<ArrayList<Unit>> findBackEdges(Body body, ExceptionalUnitGraph graph){
        HashSet<ArrayList<Unit>> backEdges =  new HashSet<>();
        Unit startUnit = body.getUnits().getFirst();
        HashMap<Unit, Integer> visited = new HashMap<>();

        for(Unit u : body.getUnits()){
            visited.put(u, 0);
        }

        dfs(startUnit, graph, visited, backEdges);

        return backEdges;
    }

    public static void runKildall(SootMethod tMethod, HashMap<Unit, LatticeElement> allStates, ArrayList<String> listVars) {
        Body body = tMethod.retrieveActiveBody();
        ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
        
        HashSet<ArrayList<Unit>> backEdges = findBackEdges(body, graph);

        String fullOutFilePath = Analysis.gTargetDirectoryName + "/" + Analysis.gTargetClassName +"." + Analysis.gTargetMethodName+".fulloutput.txt";
        Utilities.truncateFile(fullOutFilePath);

        HashSet<Unit> markedUnits = new HashSet<>();
        for (Unit u : body.getUnits()) {
            markedUnits.add(u);
        }

        // System.out.print("\nrunKildall()=> States at all units : \n\n");
        // for (Unit u : allStates.keySet()) {
        //     System.out.print(markedUnits.contains(u) ? "MARKED" : "NMARKED");
        //     System.out.print(" => " + Logger.ANSI_CYAN + u.toString() + Logger.ANSI_RESET + " => \n" + Logger.ANSI_PURPLE
        //             + allStates.get(u).toString() + "\n" + Logger.ANSI_RESET);
        // }

        int iterationIdx = 0;
        // main kildall loop
        while (!markedUnits.isEmpty()) {
            iterationIdx++;

            Logger.log(Logger.ANSI_GREEN + "\n________________________________________________________________"
                    + Logger.ANSI_RESET);
            Logger.log(Logger.ANSI_GREEN + "runKildall()=> Kildall Iteration Number : " + iterationIdx
                    + "\n" + Logger.ANSI_RESET);

            printAllStates(allStates, markedUnits);

            Unit curUnit = markedUnits.iterator().next();
            markedUnits.remove(curUnit);

            LatticeElement curState = allStates.get(curUnit);
            Logger.log("runKildall()=> Unmarked unit: " + Logger.ANSI_CYAN + curUnit.toString() + Logger.ANSI_RESET);
            Logger.log("CurState at this unmarked unit: " + Logger.ANSI_PURPLE + curState.toString() + Logger.ANSI_RESET + "\n");

            // calculate the transferred state on the basis of type of current node
            // and operator used
            if (curUnit instanceof JIfStmt) {
                Logger.log("runKildall()=> " + Logger.ANSI_CYAN + curUnit.toString() + Logger.ANSI_RESET
                        + " is instance of JIfStmt");

                LatticeElement transStateTrue = curState.tf_condstmt(true, (Stmt) curUnit);
                LatticeElement transStateFalse = curState.tf_condstmt(false, (Stmt) curUnit);

                Logger.log("runKildall()=> transStateTrue: " + Logger.ANSI_PURPLE + transStateTrue.toString()
                        + Logger.ANSI_RESET);
                Logger.log("runKildall()=> transStateFalse: " + Logger.ANSI_PURPLE + transStateFalse.toString()
                        + Logger.ANSI_RESET);

                Unit nextUnitTrue = curUnit.getUnitBoxes().get(0).getUnit();
                Unit nextUnitFalse = null;

                for (Unit adjU : graph.getSuccsOf(curUnit)) {
                    if (adjU != nextUnitTrue) {
                        nextUnitFalse = adjU;
                        break;
                    }
                }

                Logger.log("runKildall()=> nextUnitTrue : " + Logger.ANSI_PURPLE + nextUnitTrue
                        + Logger.ANSI_RESET + ", nextUnitFalse: " + Logger.ANSI_PURPLE + nextUnitFalse
                        + Logger.ANSI_RESET);

                LatticeElement nextStateTrue = allStates.get(nextUnitTrue);
                LatticeElement joinedNextStateTrue = nextStateTrue.join_op(transStateTrue);
                if (!joinedNextStateTrue.equals(nextStateTrue)) {
                    markedUnits.add(nextUnitTrue);
                }
                allStates.put(nextUnitTrue, joinedNextStateTrue);

                if(nextUnitFalse != null){
                    LatticeElement nextStateFalse = allStates.get(nextUnitFalse);
                    LatticeElement joinedNextStateFalse = nextStateFalse.join_op(transStateFalse);
                    if (!joinedNextStateFalse.equals(nextStateFalse)) {
                        markedUnits.add(nextUnitFalse);
                    }
                    allStates.put(nextUnitFalse, joinedNextStateFalse);
                }
            }

            else if (curUnit instanceof JAssignStmt || curUnit instanceof JIdentityStmt) {
                Logger.log("runKildall()=> " + Logger.ANSI_CYAN + curUnit.toString() + Logger.ANSI_RESET
                        + " is instance of JAssignStmt");
                LatticeElement transState = curState.tf_assignstmt((Stmt) curUnit);

                Logger.log("runKildall()=> transState: " + Logger.ANSI_PURPLE +transState.toString() + Logger.ANSI_RESET);

                Unit nextUnit = graph.getSuccsOf(curUnit).get(0);
                LatticeElement nextState = allStates.get(nextUnit);
                LatticeElement joinedNextState = nextState.join_op(transState);
                // Logger.log("runKildall()=> nextUnit: " + Logger.ANSI_PURPLE +nextUnit.toString() + Logger.ANSI_RESET);
                // Logger.log("runKildall()=> nextState: " + Logger.ANSI_PURPLE +nextState.toString() + Logger.ANSI_RESET);
                // Logger.log("runKildall()=> joinedNextState: " + Logger.ANSI_PURPLE +joinedNextState.toString() + Logger.ANSI_RESET);


                if (!joinedNextState.equals(nextState)) {
                    markedUnits.add(nextUnit);
                }
                allStates.put(nextUnit, joinedNextState);
            } else if (curUnit instanceof JGotoStmt) {
                Logger.log("runKildall()=> " + Logger.ANSI_CYAN + curUnit.toString() + Logger.ANSI_RESET
                        + " is instance of JGotoStmt");
                Unit nextUnit = graph.getSuccsOf(curUnit).get(0);
                LatticeElement nextState = allStates.get(nextUnit);
                LatticeElement joinedNextState;
                
                ArrayList<Unit> backEdge = new ArrayList<>();
                backEdge.add(curUnit);
                backEdge.add(nextUnit);
                if(backEdges.contains(backEdge)){
                    joinedNextState = nextState.widen_op(curState);
                    Logger.logSpecial("Applying widening operator, joinedNextState: " + joinedNextState.toString());
                }
                else{
                    joinedNextState = nextState.join_op(curState);
                    Logger.logSpecial("Applying join operator, joinedNextState: " + joinedNextState.toString());
                }

                if (!joinedNextState.equals(nextState)) {
                    markedUnits.add(nextUnit);
                }
                allStates.put(nextUnit, joinedNextState);
            } else if (curUnit instanceof JReturnStmt || curUnit instanceof JReturnVoidStmt) {
                Logger.log("runKildall()=> " + Logger.ANSI_CYAN + curUnit.toString() + Logger.ANSI_RESET
                        + " is instance of " + curUnit.getClass().getName());
            } else {
                Logger.logErr("runKildall()=> UNHANDLED: " + curUnit.toString() + " is instance of "
                        + curUnit.getClass().getName());
            }

            Utilities.appendToFile(fullOutFilePath, body, allStates, listVars, iterationIdx);
        }

    }

}
