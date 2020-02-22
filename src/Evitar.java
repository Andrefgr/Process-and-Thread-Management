import java.util.concurrent.Semaphore;

public class Evitar extends Comportamento {
	//instancias
	Semaphore acessoRobot;
	MyRobot robot;
	//variaveis classe
	int estado;
	boolean toque;
	final int DORMIR = 0, SENSOR = 1, EVITAR = 2;

	public Evitar(MyRobot robot, Semaphore acessoRobot) {
		this.robot = robot;
		this.acessoRobot = acessoRobot;
		
		acederRobot();
		this.robot.SetSensorTouch();
		libertarRobot();
		
		toque = false;
		estado = SENSOR;
	}

	private void menuEvitar() {
		
		switch (estado) {

		case DORMIR:
			dormir(150);
			estado = SENSOR;
			break;
			
		case SENSOR:
			acederRobot();
			toque = robot.Sensor();
			libertarRobot();
			System.out.println("Sensor no evitar: " + toque);
			estado = toque ? EVITAR : DORMIR;
			break;
			
		case EVITAR:
			
			acederRobot();
			
			robot.SetSpeed(50);
			robot.Parar(true);
			robot.Reta(-15);
			robot.Parar(false);
			robot.CurvarEsquerda(0, 90);
			robot.Parar(false);
			dormir(2000); //tempo para curvar 90 graus à esquerda
			
			libertarRobot();
			
			estado = SENSOR;
			break;
		}
	}
	
	private void acederRobot() {
		try {
			acessoRobot.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void libertarRobot() {
		acessoRobot.release();
	}

	private void dormir(int tempo) {
		try {
			Thread.sleep(tempo);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// MAIN
	public void run() {
		while (!isToStop) {
			if (isToPause) {
				autoSuspend();
			}
			menuEvitar();
		}
	}
}