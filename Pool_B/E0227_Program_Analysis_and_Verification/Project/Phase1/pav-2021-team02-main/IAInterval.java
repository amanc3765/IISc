public class IAInterval {
    Integer low;
    Integer high;

    static final Integer NEG_INF= Integer.MIN_VALUE;
    static final Integer POS_INF= Integer.MAX_VALUE;

    public IAInterval(){
        this.low = NEG_INF;
        this.high = POS_INF;
    }

    public IAInterval(IAInterval ia){
        this.low = ia.low;
        this.high = ia.high;
    }

    public IAInterval(Integer low, Integer high){
        this.low = low;
        this.high = high;
    }

    public void setInterval(int low, int high){
        this.low = low;
        this.high = high;
    }

    public void setInterval(IAInterval ia2){
        this.low = ia2.low;
        this.high = ia2.high;
    }

    public IAInterval join(IAInterval int2){
        if(int2 == null) {
            IAInterval ret = new IAInterval(this);
            // Logger.logErr("join => arg is null so returning : " + ret);
            return ret;
        }

        IAInterval joinedInterval = new IAInterval();
        joinedInterval.low = Integer.min(this.low, int2.low);
        joinedInterval.high = Integer.max(this.high, int2.high);

        return joinedInterval;
    }

    public IAInterval widen(IAInterval int2){
        if(int2 == null) return new IAInterval(this);

        IAInterval widenedInterval = new IAInterval();
        widenedInterval.low = int2.low < this.low ? NEG_INF : this.low;
        widenedInterval.high = int2.high > this.high ? POS_INF : this.high;

        return widenedInterval;
    }

    public boolean equals(IAInterval int2){
        return int2 != null && this.low == int2.low && this.high == int2.high;
    }

    public Integer getLow() {
        return low;
    }

    public void setLow(Integer low) {
        this.low = low;
    }

    public Integer getHigh() {
        return high;
    }

    public void setHigh(Integer high) {
        this.high = high;
    }

    public boolean isCorrectInterval(){
        return this.low<=this.high;
    }

    public String toString(){
        String out = "[";

        out += (low.intValue() <= NEG_INF.intValue()) ? "-inf" : low.toString();
        out+=", ";
        out += (high.intValue() >= POS_INF.intValue()) ? "+inf" : high.toString();
        out+="]";

        // Logger.logSpecial("low : " + low.toString() + ", NEG_INF: "+NEG_INF);
        // Logger.logSpecial("high : " + high.toString() + ", POS_INF: "+POS_INF);

        return out;
    }
}
