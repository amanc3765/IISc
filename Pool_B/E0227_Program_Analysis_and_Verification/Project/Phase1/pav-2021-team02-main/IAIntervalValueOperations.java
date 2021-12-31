public class IAIntervalValueOperations {
    static int posInf = IAInterval.POS_INF.intValue();
    static int negInf = IAInterval.NEG_INF.intValue();

    public static Integer processOverflow(long val){
        if(val >= posInf) return IAInterval.POS_INF;
        if(val <= negInf) return IAInterval.NEG_INF;

        return Integer.valueOf((int)val);
    }

    public static Integer add(int x, int y){
        // int posInf = IAInterval.POS_INF.intValue();
        // int negInf = IAInterval.NEG_INF.intValue();

        // x posInf and y negInf case (or vice versa ) is not possible
        if(x == posInf || y == posInf) return IAInterval.POS_INF;
        if(x == negInf || y == negInf) return IAInterval.NEG_INF;

        long mayOverflowVal = x+y;
        return processOverflow(mayOverflowVal);
    }

    public static Integer subtract(int x, int y){
        if(x == posInf) return IAInterval.POS_INF;
        if(x == negInf) return IAInterval.NEG_INF;

        if(y == posInf) return IAInterval.NEG_INF;
        if(y == negInf) return IAInterval.POS_INF;

        // both values are not inf
        long mayOverflowVal = x-y;
        return processOverflow(mayOverflowVal);
    }

    public static Integer multiply(int x, int y){
        if(x==0 || y==0) return Integer.valueOf(0);

        if(x == posInf) return y<0 ? IAInterval.NEG_INF : IAInterval.POS_INF;
        if(x == negInf) return y<0 ? IAInterval.POS_INF : IAInterval.NEG_INF;

        if(y == posInf) return x<0 ? IAInterval.NEG_INF : IAInterval.POS_INF;
        if(y == negInf) return x<0 ? IAInterval.POS_INF : IAInterval.NEG_INF;

        // both values are not inf, we are skipping overflows as clarified on Teams
        long mayOverflowVal = x*y;
        return processOverflow(mayOverflowVal);
    }
}
