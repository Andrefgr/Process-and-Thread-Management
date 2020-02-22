public class Dados {
	
	private double distancia;
	private double tempo;
	
	public void setDistancia(double distancia) {
		this.distancia = distancia;
	}
	
	public void setTempo(double tempo) {
		this.tempo = tempo;
	}
	
	public double getDistancia() {
		return this.distancia;
	}
	
	public double getTempo() {
		return this.tempo;
	}
	
	public Dados clone() {
		Dados dados = new Dados();
		dados.setDistancia(getDistancia());
		dados.setTempo(getTempo());
		return dados;
	}
}