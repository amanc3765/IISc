package pods.cabs.values;

public class CabStates {
	public static class MinorStates {
		public static String NONE="N";
		public static String AVAILABLE="A";
		public static String COMMITTED="C";
		public static String GIVING_RIDE="R";
	}
	
	public static class MajorStates {
		public static String SIGNED_IN="I";
		public static String SIGNED_OUT="O";
	}
	
}
