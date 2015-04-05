package extracells.api;

public class ECApi {

	public static ExtraCellsApi instance() {
		if (instance == null) {
			try {
				instance = (ExtraCellsApi) Class
						.forName("extracells.ExtraCellsApiInstance")
						.getField("instance").get(null);
			} catch (Exception e) {}
		}
		return instance;
	}

	private static ExtraCellsApi instance = null;

}
