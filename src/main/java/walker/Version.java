package walker;

public class Version {
	private static final String major = "1";
	private static final String minor = "0";
	private static final String release = "34";
	private static final String copyright = "2013©wjsjwr.org";
	private static final String code = "Waive";
	private static final String thanks = "@innocentius, @AsakuraFuuko";
	
	public static String strVersion() {
		return String.format("MAWalker(java) v%s.%s.%s %s, %s", major, minor, release, code, copyright); 
	}
	
	public static String strThanks(){
		return String.format("对下列网友表示感谢（排名不分先后）: %s", thanks);
	}
	
}
