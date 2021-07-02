public class UniqueStudent {
	private String username;
	private double major;
	private double whatSubj;
	private double classification;
	
	public UniqueStudent(String username, double major, double whatSubj, double classification) {
		this.username = username;
		this.major = major;
		this.whatSubj = whatSubj;
		this.classification = classification;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public double getMajor() {
		return this.major;
	}
	
	public double getSubj() {
		return this.whatSubj;
	}
	
	public double getClassification() {
		return this.classification;
	}
}
