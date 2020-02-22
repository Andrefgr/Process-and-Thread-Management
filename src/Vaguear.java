import java.util.Random;
import java.util.concurrent.Semaphore;

public class Vaguear extends Comportamento {
	//instancias
	MyRobot robot;
	Semaphore acessRobot;
	Random comandoRandom = new Random();

	final int ESCOLHER = 0, FRENTE = 1, DIREITA = 2, ESQUERDA = 3, PARAR = 4;
	final int raioMin = 10, raioMax = 50, anguloMin = 10, anguloMax = 180, retaMin = 10, retaMax = 50;
	int estado, comando, ultimoComando;

	public Vaguear(MyRobot robot, Semaphore acessRobot) {
		this.robot = robot;
		this.acessRobot = acessRobot;
		comando = 0;
		ultimoComando = 0;
		estado = 0;
	}
	
	private int gerarNumeroRandom(int min, int max) {
		return comandoRandom.nextInt(max-min) + min;
	}

	private void menuVaguear() {
		
		switch (estado) {
		
		case ESCOLHER:
			/*Gera um numero aleatorio para um determinado comportamento.
			Quando esse comportamento terminar, volta ao estado = 0 e torna
			a gerar um numero aleatorio para outro comportamento.*/
			do {
				comando = gerarNumeroRandom(1, 4);
			} while (comando == ultimoComando);
			estado = comando;
			ultimoComando = comando;
			acederRobot();
			robot.SetSpeed(50);
			libertarRobot();
			break;

		case FRENTE:
			//andar para a frente
			System.out.println("Vaguear: frente");
			int distanciaFrente = gerarNumeroRandom(retaMin, retaMax);
			acederRobot();
			robot.Parar(true);
			robot.Reta(distanciaFrente);
			robot.Parar(false);
			libertarRobot();
			dormir(calcularDormir(distanciaFrente));
			estado = ESCOLHER;
			break;

		case DIREITA:
			//andar para a direita
			System.out.println("Vaguear: curvar direita");
			int raioDireita = gerarNumeroRandom(raioMin, raioMax);
			int anguloDireita = gerarNumeroRandom(anguloMin, anguloMax);
			acederRobot();
			robot.Parar(true);
			robot.CurvarDireita(raioDireita, anguloDireita);
			robot.Parar(false);
			libertarRobot();
			dormir(calcularDormir(raioDireita));
			estado = ESCOLHER;
			break;

		case ESQUERDA:
			//andar para a esquerda
			System.out.println("Vaguear: curvar esquerda");
			int raioEsquerda = gerarNumeroRandom(raioMin, raioMax);
			int anguloEsquerda = gerarNumeroRandom(anguloMin, anguloMax);
			acederRobot();
			robot.Parar(true);
			robot.CurvarEsquerda(raioEsquerda, anguloEsquerda);
			robot.Parar(false);
			libertarRobot();
			dormir(calcularDormir(raioEsquerda));
			estado = ESCOLHER;
			break;

		case PARAR:
			//parar a true
			System.out.println("Vaguear: parou a true");
			acederRobot();
			robot.Parar(true);
			libertarRobot();
			dormir(500); //pára meio segundo, o suficiente para se perceber na prática
			estado = ESCOLHER;
			break;

		}
	}
	
	private void acederRobot() {
		try {
			acessRobot.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void libertarRobot() {
		acessRobot.release();
	}

	private void dormir(int tempo) {
		try {
			Thread.sleep(tempo);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int calcularDormir(int distancia) {
		/*
		 * Regra dos 3 simples. Se o robot demora (sensivelmente) 5 segundos a
		 * percorrer um metro (100cm), então este metodo retorna o tempo de
		 * dormir para uma certa 'distanca'
		 */
		return (distancia * 5000) / 100;
	}
	

	public void run() {
		while (!isToStop) {
			if (isToPause) {
				autoSuspend();
			}
			menuVaguear();
		}
	}
}