
import soot.jimple.Stmt;


// Receiver object of LatticeElement possess the existing dataflow fact at a programpoint.

// Method implementation should not modify the receiver object. A fresh object should be returned.

// The Kildall implementation should not directly refer to IA implementation,
//    and should access the dataflow data only via LatticeElement interface.

interface LatticeElement
{

    public LatticeElement join_op(LatticeElement r);
    // represents: "this" JOIN "r"
    // this - the existing dataflow fact
    // r    - the incoming dataflow fact

    public LatticeElement widen_op(LatticeElement r);
    // represents: "this" WIDEN "r"
    // this - the existing dataflow fact
    // r    - the incoming dataflow fact

    public boolean equals(LatticeElement r);

    public LatticeElement tf_assignstmt(Stmt st);

    public LatticeElement tf_condstmt(boolean b, Stmt st);
}

