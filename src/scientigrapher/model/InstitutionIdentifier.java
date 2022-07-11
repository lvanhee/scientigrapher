package scientigrapher.model;

public class InstitutionIdentifier {
	private final Long scopusId;

	public InstitutionIdentifier(long scopusAffiliationId) {
		this.scopusId = scopusAffiliationId;
	}

	public static InstitutionIdentifier newInstance(long scopusAffiliationId) {
		return new InstitutionIdentifier(scopusAffiliationId);
	}

	public String getId() {
		return ""+scopusId;
	}

}
