package model;

public class Camera {
	private String frequency;
	private String resolution;

	public Camera(String resolution, String frequency) {
		this.frequency = frequency;
		this.resolution = resolution;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public boolean validParameters() {
		return (((frequency != null) && (!frequency.equals("")) && (frequency.matches("[0-9]+"))) &&

				((resolution != null) && (!resolution.equals(""))));

	}

	public String formatParametersForServer() {
		return "resolution=" + resolution + "&fps=" + frequency;
	}

	public String invalidFrequencyOrResolution() {
		return "Error -> Frequency or/and resolution is invalid";
	}

}
