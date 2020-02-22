import java.util.Random;

public class MyRobot {
	
	public static final int Sensor1 = RobotLego.S_1;
	public static final int Sensor2 = RobotLego.S_2;
	public static final int Sensor3 = RobotLego.S_3;
	public static final int Sensor4 = RobotLego.S_4;

	private final boolean simulateRobot;
	private RobotLego robot;
	protected int touchSensor;
	
	Random numeroRandom = new Random();

	/*Classe auxiliar para a execução dos comandos do Robot. Quando a variavel 'simulateRobot'
	se encontra a false, significa que se está a usar o Robot verdadeiro e não o de simulacao.
	*/
	
	public MyRobot(boolean simulateRobot) {
		this.simulateRobot = simulateRobot;

		if (this.simulateRobot == false)
			robot = new RobotLego();
		else
			this.robot = null;
	}
	
	private int gerarNumeroRandom(int min, int max) {
		return numeroRandom.nextInt(max-min) + min;
	}

	public boolean OpenNXT(String nome) {
		if (this.simulateRobot == false)
			return this.robot.OpenNXT(nome);
		return true;
	}

	public void CloseNXT() {
		if (this.simulateRobot == false)
			this.robot.CloseNXT();
	}

	public void Parar(boolean b) {
		if (this.simulateRobot == false)
			this.robot.Parar(b);
	}

	public void Reta(int distancia) {
		if (this.simulateRobot == false)
			this.robot.Reta(distancia);
	}

	public void CurvarEsquerda(int raio, int angulo) {
		if (this.simulateRobot == false)
			this.robot.CurvarEsquerda(raio, angulo);
	}

	public void CurvarDireita(int raio, int angulo) {
		if (this.simulateRobot == false)
			this.robot.CurvarDireita(raio, angulo);
	}

	public void AjustarVME(int offSet) {
		if (this.simulateRobot == false)
			this.robot.AjustarVME(offSet);
	}

	public void AjustarVMD(int offSet) {
		if (this.simulateRobot == false)
			this.robot.AjustarVMD(offSet);
	}

	public void SetSpeed(int speed) {
		if (this.simulateRobot == false)
			this.robot.SetSpeed(speed);
	}

	public void SetSensorTouch() {
		if (this.simulateRobot == false)
			this.robot.SetSensorTouch(Sensor2);;
	}

	public boolean Sensor() {
		if (this.simulateRobot == false)
			return this.robot.Sensor(Sensor2) == 1;
		return false;
	}

	public int SensorUS(){
		if (this.simulateRobot == false)
			return this.robot.SensorUS(Sensor4);
		return gerarNumeroRandom(20, 200);
	}
	
	public void SetSensorLowspeed() {
		if (this.simulateRobot == false)
			this.robot.SetSensorLowspeed(Sensor4);
	}
}