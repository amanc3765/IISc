import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fj.P;
import soot.Immediate;
import soot.Value;
import soot.ValueBox;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.Expr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.UnopExpr;
import soot.jimple.internal.ConditionExprBox;
import soot.jimple.internal.IdentityRefBox;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JDivExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JMulExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JSubExpr;
import soot.jimple.internal.JimpleLocal;

public class IALatticeElement implements LatticeElement {

    public boolean isBot;
    HashMap<String, IAInterval> state;

    public IALatticeElement() {
        isBot = true;
        state = new HashMap<>();
    }

    public IALatticeElement(List<String> vars) {
        isBot = false;
        setDefaultInitValue(vars);
    }

    // copy constructor
    public IALatticeElement(IALatticeElement ialElem) {
        this.isBot = ialElem.isBot;
        this.state = new HashMap<>();
        for (String var : ialElem.state.keySet()) {
            this.state.put(var, new IAInterval(ialElem.state.get(var)));
        }
    }

    public void setDefaultInitValue(List<String> vars) {
        this.isBot = false;
        this.state = new HashMap<String, IAInterval>();
        for (String var : vars) {
            // new IAInterval() automatically assigns NEG_INF, POS_INF
            state.put(var, new IAInterval());
        }
    }

    public void setBot() {
        this.isBot = true;
        this.state = new HashMap<>();
    }

    @Override
    public LatticeElement join_op(LatticeElement inElem) {
        IALatticeElement joinedElement = new IALatticeElement();
        IALatticeElement inIAElem = (IALatticeElement) inElem;

        if (inIAElem.isBot)
            return this;
        if (this.isBot)
            return inIAElem;

        joinedElement.isBot = false;
        for (String var : this.state.keySet()) {
            IAInterval thisIAInterval = this.state.get(var);
            IAInterval inIAInterval = inIAElem.state.get(var);

            joinedElement.state.put(var, thisIAInterval.join(inIAInterval));
        }

        return joinedElement;
    }

    @Override
    public LatticeElement widen_op(LatticeElement inElem) {
        IALatticeElement widenedElement = new IALatticeElement();
        IALatticeElement inIAElem = (IALatticeElement) inElem;

        if (inIAElem.isBot)
            return this;
        if (this.isBot)
            return inIAElem;

        widenedElement.isBot = false;
        for (String var : this.state.keySet()) {
            IAInterval thisIAInterval = this.state.get(var);
            IAInterval inIAInterval = inIAElem.state.get(var);

            widenedElement.state.put(var, thisIAInterval.widen(inIAInterval));
        }

        return widenedElement;
    }

    @Override
    public boolean equals(LatticeElement inElem) {
        IALatticeElement inIAElem = (IALatticeElement) inElem;

        // check if all intervals for all variables are equal
        for (String var : this.state.keySet()) {
            if (!(this.state.get(var).equals(inIAElem.state.get(var)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public LatticeElement tf_assignstmt(Stmt st) {
        IALatticeElement transState = new IALatticeElement(this);
        ValueBox lhsVBox = st.getDefBoxes().get(0);

        if (this.isBot) {
            Logger.logSpecial("tf_assignstmt=> Since incoming state is bot, returning bot");
            return transState;
        }

        if (st instanceof JIdentityStmt) {
            ValueBox rhsVBox = st.getUseBoxes().get(0);
            String lhsVarName = lhsVBox.getValue().toString();
            if (rhsVBox instanceof IdentityRefBox) {
                IdentityRefBox rhsIRBox = (IdentityRefBox) rhsVBox;
                ParameterRef rhsParRef = (ParameterRef) rhsIRBox.getValue();

                int paramIdx = rhsParRef.getIndex();
                String rhsParamName = "@parameter" + paramIdx;

                transState.state.put(lhsVarName, this.state.get(rhsParamName));
            }
        } else { // if stmt is a normal JAssignStmt
            String lhsVarName = lhsVBox.getValue().toString();
            List<ValueBox> rhsVBoxList = st.getUseBoxes();
            ValueBox linkedRValueBox;

            BinopExpr binopExpr = null;
            UnopExpr unopExpr = null;

            for (ValueBox vb : rhsVBoxList) {
                Value v = vb.getValue();
                Logger.logSpecial(v.toString() + ", type: " + v.getClass().getName());
                if (v instanceof BinopExpr) {
                    binopExpr = (BinopExpr) v;

                    // Logger.logSpecial("binopExpr= "+binopExpr);
                    break;
                } else if (v instanceof UnopExpr) {
                    unopExpr = (UnopExpr) v;
                    break;
                }
            }

            IAInterval transInterval = new IAInterval();

            if (binopExpr != null) {
                Value op1 = null, op2 = null;
                op1 = binopExpr.getOp1();
                op2 = binopExpr.getOp2();

                if (op1 == null || op2 == null) {
                    Logger.logErr("tf_assignstmt : null value in operands, op1= " + op1 + ", op2= " + op2);
                    System.exit(1);
                }

                IAInterval op1Interval, op2Interval;
                op1Interval = getIntervalFromImmediate(op1);
                op2Interval = getIntervalFromImmediate(op2);

                Integer tempLow = null;
                Integer tempHigh = null;
                if (binopExpr instanceof JAddExpr) {
                    int r1, r2, r3, r4;
                    r1 = IAIntervalValueOperations.add(op1Interval.low, op2Interval.low);
                    r2 = IAIntervalValueOperations.add(op1Interval.low, op2Interval.high);
                    r3 = IAIntervalValueOperations.add(op1Interval.high, op2Interval.low);
                    r4 = IAIntervalValueOperations.add(op1Interval.high, op2Interval.high);

                    tempLow = Integer.min(Integer.min(r1, r2), Integer.min(r3, r4));
                    tempHigh = Integer.max(Integer.max(r1, r2), Integer.max(r3, r4));
                } else if (binopExpr instanceof JSubExpr) {
                    int r1, r2, r3, r4;
                    r1 = IAIntervalValueOperations.subtract(op1Interval.low, op2Interval.low);
                    r2 = IAIntervalValueOperations.subtract(op1Interval.low, op2Interval.high);
                    r3 = IAIntervalValueOperations.subtract(op1Interval.high, op2Interval.low);
                    r4 = IAIntervalValueOperations.subtract(op1Interval.high, op2Interval.high);

                    tempLow = Integer.min(Integer.min(r1, r2), Integer.min(r3, r4));
                    tempHigh = Integer.max(Integer.max(r1, r2), Integer.max(r3, r4));
                } else if (binopExpr instanceof JMulExpr) {
                    int r1, r2, r3, r4;
                    r1 = IAIntervalValueOperations.multiply(op1Interval.low, op2Interval.low);
                    r2 = IAIntervalValueOperations.multiply(op1Interval.low, op2Interval.high);
                    r3 = IAIntervalValueOperations.multiply(op1Interval.high, op2Interval.low);
                    r4 = IAIntervalValueOperations.multiply(op1Interval.high, op2Interval.high);

                    tempLow = Integer.min(Integer.min(r1, r2), Integer.min(r3, r4));
                    tempHigh = Integer.max(Integer.max(r1, r2), Integer.max(r3, r4));
                } else {
                    Logger.logErr("tf_assignstmt : Unknown expression type : " + binopExpr.getClass().getName());
                    System.exit(1);
                }

                transInterval.low = tempLow.intValue();
                transInterval.high = tempHigh.intValue();
            } else if (unopExpr != null) {
                Value op = null;
                op = unopExpr.getOp();
                IAInterval opInterval = getIntervalFromImmediate(op);

                transInterval = new IAInterval(IAIntervalValueOperations.multiply(opInterval.high, -1),
                        IAIntervalValueOperations.multiply(opInterval.low, -1));
                Logger.logSpecial("unopExpr= " + unopExpr + ", incomingInterval: "+opInterval + ", transInterval: " + transInterval);

            }
            // if we couldn't find a binary expression or unary expression, it is the case
            // of a unary assignment
            // to a JimpleLocal or IntConstant e.g. $i1 = 0
            else {
                for (ValueBox vb : rhsVBoxList) {
                    Value v = vb.getValue();

                    if (v instanceof IntConstant) {
                        IntConstant vIntConst = (IntConstant) v;
                        transInterval.low = vIntConst.value;
                        transInterval.high = vIntConst.value;
                        // Logger.logSpecial("transInterval: "+transInterval.toString());
                    } else if (v instanceof JimpleLocal) {
                        JimpleLocal vJimpleLocal = (JimpleLocal) v;
                        String varName = vJimpleLocal.getName();
                        IAInterval interval = this.state.get(varName);
                        transInterval = new IAInterval(interval);
                    }
                }
            }

            transState.state.put(lhsVarName, transInterval);

        }

        return transState;
    }

    @Override
    public LatticeElement tf_condstmt(boolean b, Stmt st) {
        Logger.logSpecial("tf_condstmt => Branch Type : " + b);
        IALatticeElement transState = new IALatticeElement(this);
        if (this.isBot) {
            Logger.logSpecial("tf_condstmt => Since incoming state is bot, returning bot");
            return transState;
        }

        // Let's assume conditional is of format X op Y
        ConditionExprBox condExprBox = (ConditionExprBox) ((IfStmt) st).getConditionBox();
        ConditionExpr expr = (ConditionExpr) condExprBox.getValue();

        System.out.println("tf_condstmt => expr : " + expr.toString() + ", type: " + expr.getClass().getName());

        Value op1, op2;
        IAInterval op1Interval, op2Interval;
        op1 = expr.getOp1();
        op2 = expr.getOp2();

        op1Interval = getIntervalFromImmediate(op1);
        op2Interval = getIntervalFromImmediate(op2);

        Logger.logSpecial("tf_condstmt => op1: " + op1Interval.toString() + ", op2: " + op2Interval.toString());

        // 0th index is for left operand, 1st index for right operand
        ArrayList<IAInterval> transIntervals = null;

        if (expr instanceof JEqExpr) {
            transIntervals = condEqualTo(b, op1Interval, op2Interval);
        } else if (expr instanceof JGeExpr) {
            transIntervals = condGreaterThanEqTo(b, op1Interval, op2Interval);
        } else if (expr instanceof JGtExpr) {
            transIntervals = condGreaterThan(b, op1Interval, op2Interval);
        } else if (expr instanceof JLeExpr) {
            transIntervals = condLessThanEqTo(b, op1Interval, op2Interval);
        } else if (expr instanceof JLtExpr) {
            transIntervals = condLessThan(b, op1Interval, op2Interval);
        } else if (expr instanceof JNeExpr) {
            transIntervals = condNotEqualTo(b, op1Interval, op2Interval);
        } else {
            Logger.logErr("tf_condstmt => Unknown type of conditional expression : " + expr.getClass().getName());
            System.exit(1);
        }

        if (transIntervals == null || transIntervals.size() < 2) {
            Logger.logErr("tf_condstmt => Unexpected condition : transIntervals is null or size < 2");
            System.exit(1);
        }
        op1Interval = transIntervals.get(0);
        op2Interval = transIntervals.get(1);

        // decide if state should be bot, depending on whether every variable can take
        // sane values or not
        if (op1Interval == null || op2Interval == null) {
            transState.setBot();
        } else {
            if (op1 instanceof JimpleLocal) {
                String op1Name = ((JimpleLocal) op1).getName();
                transState.state.put(op1Name, op1Interval);
            }
            if (op2 instanceof JimpleLocal) {
                String op2Name = ((JimpleLocal) op2).getName();
                transState.state.put(op2Name, op2Interval);
            }
        }

        return transState;
    }

    @Override
    public String toString() {
        String out;
        if (this.isBot) {
            out = "bot";
        } else {
            out = "{\n";
            for (String var : state.keySet()) {
                out += var + ":" + state.get(var).toString() + ",\n";
            }
            out += "}";
        }

        return out;
    }

    public IAInterval getIntervalFromImmediate(Value op) {
        if (op == null) {
            Logger.logErr("getIntervalFromImmediate : Didn't expect argument to be null");
        }
        if (op instanceof IntConstant) {
            IntConstant opIntConst = (IntConstant) op;
            return new IAInterval(opIntConst.value, opIntConst.value);
        }

        if (op instanceof JimpleLocal) {
            JimpleLocal opJimpleLocal = (JimpleLocal) op;
            String opVarName = opJimpleLocal.getName();
            // Logger.logSpecial("opVarName= "+opVarName + ", this.state.get(opVarName)=" +
            // this.state.get(opVarName));
            return new IAInterval(this.state.get(opVarName));
        }

        Logger.logErr("getIntervalFromImmediate : Didn't expect argument to be of type : " + op.getClass().getName());
        return null;
    }

    private ArrayList<IAInterval> condGreaterThanEqTo(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        ArrayList<IAInterval> transIntervalsGreaterThanEqTo = null;
        transIntervalsGreaterThanEqTo = condLessThan(!branchType, iaLeft, iaRight);
        if (transIntervalsGreaterThanEqTo == null || transIntervalsGreaterThanEqTo.size() < 2) {
            Logger.logErr(
                    "condGreaterThanEqTo => Unexpected condition : transIntervalsGreaterThanEqTo is null or size < 2");
            System.exit(1);
        }

        return transIntervalsGreaterThanEqTo;
    }

    private ArrayList<IAInterval> condLessThanEqTo(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        ArrayList<IAInterval> transIntervalsLessThanEqTo = null;
        transIntervalsLessThanEqTo = condGreaterThan(!branchType, iaLeft, iaRight);
        if (transIntervalsLessThanEqTo == null || transIntervalsLessThanEqTo.size() < 2) {
            Logger.logErr("condLessThanEqTo => Unexpected condition : transIntervalsLessThanEqTo is null or size < 2");
            System.exit(1);
        }

        return transIntervalsLessThanEqTo;
    }

    private ArrayList<IAInterval> condGreaterThan(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        int a = iaLeft.low;
        int b = iaLeft.high;
        int c = iaRight.low;
        int d = iaRight.high;

        Logger.logSpecial(
                "condGreaterThan=> branchType: " + branchType + ", iaLeft: " + iaLeft + ", iaRight: " + iaRight);

        // Keep right side intact and break left interval into 3 sub-intervals
        IAInterval subInterval1Left = new IAInterval(a, Integer.min(b, c)); // [a, min(b, c)]
        IAInterval subInterval2Left = new IAInterval(Integer.max(a, IAIntervalValueOperations.add(c, 1)),
                Integer.min(b, d)); // [max(a, c+1), min(b,d)]
        IAInterval subInterval3Left = new IAInterval(Integer.max(a, IAIntervalValueOperations.add(d, 1)), b); // [max(a,
                                                                                                              // d+1),
                                                                                                              // b]

        IAInterval transSubInterval1Left = null;
        IAInterval transSubInterval2Left = null;
        IAInterval transSubInterval3Left = null;

        IAInterval transSubInterval1Right = null;
        IAInterval transSubInterval2Right = null;
        IAInterval transSubInterval3Right = null;

        int sc = iaRight.low;
        int sd = iaRight.high;

        // Case P1
        if (subInterval1Left.isCorrectInterval()) {
            int sa = subInterval1Left.low;
            int sb = subInterval1Left.high;

            if (branchType == true) { // make bot
                transSubInterval1Left = null;
                transSubInterval1Right = null;
            } else {
                transSubInterval1Left = new IAInterval(sa, sb);
                transSubInterval1Right = new IAInterval(sc, sd);
            }
        }

        // Case P2
        if (subInterval2Left.isCorrectInterval()) {
            int sa = subInterval2Left.low;
            int sb = subInterval2Left.high;

            if (branchType == true) {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(sc, IAIntervalValueOperations.subtract(sb, 1));
            } else {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(sa, sd);
            }
        }

        // Case P3
        if (subInterval3Left.isCorrectInterval()) {
            int sa = subInterval3Left.low;
            int sb = subInterval3Left.high;

            if (branchType == true) {
                transSubInterval3Left = new IAInterval(sa, sb);
                transSubInterval3Right = new IAInterval(sc, sd);
            } else {
                transSubInterval3Left = null;
                transSubInterval3Right = null;
            }
        }

        IAInterval joinedTransLeft = null;
        IAInterval joinedTransRight = null;

        if (transSubInterval1Left != null)
            joinedTransLeft = transSubInterval1Left.join(joinedTransLeft);
        if (transSubInterval2Left != null)
            joinedTransLeft = transSubInterval2Left.join(joinedTransLeft);
        if (transSubInterval3Left != null)
            joinedTransLeft = transSubInterval3Left.join(joinedTransLeft);

        if (transSubInterval1Right != null)
            joinedTransRight = transSubInterval1Right.join(joinedTransRight);
        if (transSubInterval2Right != null)
            joinedTransRight = transSubInterval2Right.join(joinedTransRight);
        if (transSubInterval3Right != null)
            joinedTransRight = transSubInterval3Right.join(joinedTransRight);

        Logger.logSpecial("condGreaterThan=> transSubInterval1Left : " + transSubInterval1Left);
        Logger.logSpecial("condGreaterThan=> transSubInterval2Left : " + transSubInterval2Left);
        Logger.logSpecial("condGreaterThan=> transSubInterval3Left : " + transSubInterval3Left);

        Logger.logSpecial("condGreaterThan=> After joining, joinedTransLeft: " + joinedTransLeft
                + ", joinedTransRight: " + joinedTransRight);

        ArrayList<IAInterval> retTransIntervals = new ArrayList<>();
        retTransIntervals.add(joinedTransLeft);
        retTransIntervals.add(joinedTransRight);

        return retTransIntervals;
    }

    private ArrayList<IAInterval> condLessThan(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        int a = iaLeft.low;
        int b = iaLeft.high;
        int c = iaRight.low;
        int d = iaRight.high;

        Logger.logSpecial("condLessThan=> branchType: " + branchType + ", iaLeft: " + iaLeft + ", iaRight: " + iaRight);

        // Keep right side intact and break left interval into 3 sub-intervals
        IAInterval subInterval1Left = new IAInterval(a, Integer.min(b, IAIntervalValueOperations.subtract(c, 1)));
        IAInterval subInterval2Left = new IAInterval(Integer.max(a, c),
                Integer.min(b, IAIntervalValueOperations.subtract(d, 1)));
        IAInterval subInterval3Left = new IAInterval(Integer.max(a, d), b);

        IAInterval transSubInterval1Left = null;
        IAInterval transSubInterval2Left = null;
        IAInterval transSubInterval3Left = null;

        IAInterval transSubInterval1Right = null;
        IAInterval transSubInterval2Right = null;
        IAInterval transSubInterval3Right = null;

        int sc = iaRight.low;
        int sd = iaRight.high;

        // Case P1
        if (subInterval1Left.isCorrectInterval()) {
            int sa = subInterval1Left.low;
            int sb = subInterval1Left.high;

            if (branchType == true) {
                transSubInterval1Left = new IAInterval(sa, sb);
                transSubInterval1Right = new IAInterval(sc, sd);
            } else {
                transSubInterval1Left = null;
                transSubInterval1Right = null;
            }
        }

        // Case P2
        if (subInterval2Left.isCorrectInterval()) {
            int sa = subInterval2Left.low;
            int sb = subInterval2Left.high;

            if (branchType == true) {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(IAIntervalValueOperations.add(sa, 1), sd);
            } else {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(sc, sb);
            }
        }

        // Case P3
        if (subInterval3Left.isCorrectInterval()) {
            int sa = subInterval3Left.low;
            int sb = subInterval3Left.high;

            if (branchType == true) {
                transSubInterval3Left = null;
                transSubInterval3Right = null;
            } else {
                transSubInterval3Left = new IAInterval(sa, sb);
                transSubInterval3Right = new IAInterval(sc, sd);
            }
        }

        IAInterval joinedTransLeft = null;
        IAInterval joinedTransRight = null;

        if (transSubInterval1Left != null)
            joinedTransLeft = transSubInterval1Left.join(joinedTransLeft);
        if (transSubInterval2Left != null)
            joinedTransLeft = transSubInterval2Left.join(joinedTransLeft);
        if (transSubInterval3Left != null)
            joinedTransLeft = transSubInterval3Left.join(joinedTransLeft);

        if (transSubInterval1Right != null)
            joinedTransRight = transSubInterval1Right.join(joinedTransRight);
        if (transSubInterval2Right != null)
            joinedTransRight = transSubInterval2Right.join(joinedTransRight);
        if (transSubInterval3Right != null)
            joinedTransRight = transSubInterval3Right.join(joinedTransRight);

        Logger.logSpecial("condLessThan=> transSubInterval1Left : " + transSubInterval1Left);
        Logger.logSpecial("condLessThan=> transSubInterval2Left : " + transSubInterval2Left);
        Logger.logSpecial("condLessThan=> transSubInterval3Left : " + transSubInterval3Left);

        Logger.logSpecial("condLessThan=> After joining, joinedTransLeft: " + joinedTransLeft + ", joinedTransRight: "
                + joinedTransRight);

        ArrayList<IAInterval> retTransIntervals = new ArrayList<>();
        retTransIntervals.add(joinedTransLeft);
        retTransIntervals.add(joinedTransRight);

        return retTransIntervals;
    }

    private ArrayList<IAInterval> condNotEqualTo(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        return condEqualTo(!branchType, iaLeft, iaRight);
    }

    private ArrayList<IAInterval> condEqualTo(boolean branchType, IAInterval iaLeft, IAInterval iaRight) {
        int a = iaLeft.low;
        int b = iaLeft.high;
        int c = iaRight.low;
        int d = iaRight.high;

        Logger.logSpecial("condEqualTo=> branchType: " + branchType + ", iaLeft: " + iaLeft + ", iaRight: " + iaRight);

        // Keep right side intact and break left interval into 4 sub-intervals
        IAInterval subInterval1Left = new IAInterval(a, Integer.min(b, IAIntervalValueOperations.subtract(c, 1)));
        IAInterval subInterval2Left = new IAInterval(Integer.max(a, c),
                Integer.min(b, IAIntervalValueOperations.subtract(d, 1)));
        IAInterval subInterval3Left = new IAInterval(Integer.max(a, d), Integer.min(b, d));
        IAInterval subInterval4Left = new IAInterval(Integer.max(a, IAIntervalValueOperations.add(d, 1)), b);

        IAInterval transSubInterval1Left = null;
        IAInterval transSubInterval2Left = null;
        IAInterval transSubInterval3Left = null;
        IAInterval transSubInterval4Left = null;

        IAInterval transSubInterval1Right = null;
        IAInterval transSubInterval2Right = null;
        IAInterval transSubInterval3Right = null;
        IAInterval transSubInterval4Right = null;

        int sc = iaRight.low;
        int sd = iaRight.high;

        // Case P1
        if (subInterval1Left.isCorrectInterval()) {
            int sa = subInterval1Left.low;
            int sb = subInterval1Left.high;

            if (branchType == true) {
                transSubInterval1Left = null;
                transSubInterval1Right = null;
            } else {
                transSubInterval1Left = new IAInterval(sa, sb);
                transSubInterval1Right = new IAInterval(sc, sd);
            }
        }

        // Case P2
        if (subInterval2Left.isCorrectInterval()) {
            int sa = subInterval2Left.low;
            int sb = subInterval2Left.high;

            if (branchType == true) {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(sa, sb);
            } else {
                transSubInterval2Left = new IAInterval(sa, sb);
                transSubInterval2Right = new IAInterval(sc, sd);
            }
        }

        // Case P3
        if (subInterval3Left.isCorrectInterval()) {
            int sa = subInterval3Left.low;
            int sb = subInterval3Left.high;

            if (branchType == true) {
                transSubInterval3Left = new IAInterval(sd, sd);
                transSubInterval3Right = new IAInterval(sd, sd);
            } else {
                transSubInterval3Left = new IAInterval(sd, sd);
                transSubInterval3Right = new IAInterval(sc, IAIntervalValueOperations.subtract(sd, 1));
            }
        }

        // Case P4
        if (subInterval4Left.isCorrectInterval()) {
            int sa = subInterval4Left.low;
            int sb = subInterval4Left.high;

            if (branchType == true) {
                transSubInterval4Left = null;
                transSubInterval4Right = null;
            } else {
                transSubInterval4Left = new IAInterval(sa, sb);
                transSubInterval4Right = new IAInterval(sc, sd);
            }
        }

        Logger.logSpecial("condEqualTo=> transSubInterval1Left : " + transSubInterval1Left);
        Logger.logSpecial("condEqualTo=> transSubInterval2Left : " + transSubInterval2Left);
        Logger.logSpecial("condEqualTo=> transSubInterval3Left : " + transSubInterval3Left);
        Logger.logSpecial("condEqualTo=> transSubInterval4Left : " + transSubInterval4Left);

        IAInterval joinedTransLeft = null;
        IAInterval joinedTransRight = null;

        if (transSubInterval1Left != null)
            joinedTransLeft = transSubInterval1Left.join(joinedTransLeft);
        if (transSubInterval2Left != null)
            joinedTransLeft = transSubInterval2Left.join(joinedTransLeft);
        if (transSubInterval3Left != null)
            joinedTransLeft = transSubInterval3Left.join(joinedTransLeft);
        if (transSubInterval4Left != null)
            joinedTransLeft = transSubInterval4Left.join(joinedTransLeft);

        if (transSubInterval1Right != null)
            joinedTransRight = transSubInterval1Right.join(joinedTransRight);
        if (transSubInterval2Right != null)
            joinedTransRight = transSubInterval2Right.join(joinedTransRight);
        if (transSubInterval3Right != null)
            joinedTransRight = transSubInterval3Right.join(joinedTransRight);
        if (transSubInterval4Right != null)
            joinedTransRight = transSubInterval4Right.join(joinedTransRight);

        Logger.logSpecial("condEqualTo=> After joining, joinedTransLeft: " + joinedTransLeft + ", joinedTransRight: "
                + joinedTransRight);

        ArrayList<IAInterval> transIntervals = new ArrayList<>();
        transIntervals.add(joinedTransLeft);
        transIntervals.add(joinedTransRight);

        return transIntervals;
    }
}
