import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class Gestor extends Comportamento{
	// Instancias da classe
	MyRobot robot;
	BufferCircular caixaCorreio;
	Semaphore acessRobot, haDistancia;
	Vaguear vaguear;
	Perseguir perseguir;
	Produtor produtor;
	Dados dados;
	// Variaveis da classe
	ArrayList<Integer> distanciasObtidas;
	final int LER_DISTANCIA = 0, VAGUEAR = 1, PERSEGUIR = 2, RECEBER_CORREIO = 3, 
			PERSEGUIR_PARADO = 4, DORMIR = 5, tempoDormir = 400, distanciaMaxPerseguir = 80, tempoImpedePerseguir = 5000;
	int estado, contador = -1;
	double tempoObjectoParado = 0.0;
	boolean aVerificarTempo = false;

	/**
	 * @param acessRobot
	 */

	public Gestor(MyRobot robot, Semaphore acessRobot) {
		this.robot = robot;
		this.acessRobot = acessRobot;
		distanciasObtidas = new ArrayList<Integer>();
		dados = new Dados();
		caixaCorreio = new BufferCircular();
		haDistancia = new Semaphore(0);

		produtor = new Produtor(caixaCorreio, haDistancia);
		produtor.start();

		vaguear = new Vaguear(robot, acessRobot);
		vaguear.myPause();
		vaguear.start();

		perseguir = new Perseguir(robot, acessRobot, haDistancia, caixaCorreio, tempoDormir);
		perseguir.myPause();
		perseguir.start();
		
		acederRobot();
		this.robot.SetSensorLowspeed();
		libertarRobot();
		
		estado = LER_DISTANCIA;
	}

	private void menuGestor() {

		switch (estado) {
			
		case LER_DISTANCIA:
			
			acederRobot();
			int distanciaNova = robot.SensorUS();
			libertarRobot();

			distanciasObtidas.remove(0);
			distanciasObtidas.add(distanciaNova);
			
			try {
				/*Para o caso do contador ser numero par, a distancia obtida for inferior
				a 80 e a variavel 'verificarTempo' esteja a false, entao envia uma nova
				distancia ao Perseguir via buffer.
				 */ 
				
				if((++contador % 2 == 0) && (distanciaNova <= distanciaMaxPerseguir)) {
					Collections.sort(distanciasObtidas);
					int distanciaEscolhida = distanciasObtidas.get(1);
					long tempoActual = System.currentTimeMillis();
					if(distanciaEscolhida <= distanciaMaxPerseguir) {
						System.out.println("GESTOR -> Distancia enviada: " + distanciaEscolhida);
						dados.setDistancia(distanciaEscolhida);
						dados.setTempo(tempoActual);
						produtor.setDados(dados);
						estado = PERSEGUIR;
					}
					break;
				}
				/*Caso a distancia obtida seja maior que 80 ou caso o gestor esteja a impedir
				durante 5 segundos a execução do Perseguir, então a variavel 'verificarTempo'
				permite ao gestor controlar o programa de modo a que o vaguear continue o seu
				trabalho*/
				else if(distanciaNova > distanciaMaxPerseguir) {
					estado = VAGUEAR;
					break;
				}
				/*Caso nenhum dos casos se verifique, entao o gestor dorme durante t/2.
				O gestor dorme metade do tempo do Perseguir, de modo a obter 2 distancias 
				em cada t.
				 */
				else 
					estado = DORMIR;

			} catch (InterruptedException e2) {e2.printStackTrace();}

			break;

		case VAGUEAR:
			perseguir.myPause();
			vaguear.myResume();
			estado = DORMIR;
			break;

		case PERSEGUIR:
			vaguear.myPause();
			perseguir.myResume();
			estado = DORMIR;
			break;

		case RECEBER_CORREIO:
			/**Vê se o perseguir deixou uma mensagem para o gestor suspender a sua
			 * actividade e activar o vaguear*/
			if(perseguir.estouParado) {
				tempoObjectoParado = System.currentTimeMillis();
				perseguir.estouParado = false;
				aVerificarTempo = true;
				estado = PERSEGUIR_PARADO;
			}
			else
				estado = LER_DISTANCIA;
			break;


		case PERSEGUIR_PARADO:
			/* Quando o Perseguir informa o gestor de que está parado ha mais de 5 segundos,
			 * entao a variavel 'verificarTempo' no gestor fica a true. A condicao que se segue
			 * verifica se essa variavel se mantem a true e se o tempo actual subtraido pelo
			 * 'tempoObjectoParado' é superior a 5 segundos. Quando for superior, entao o 
			 * Perseguir já pode voltar a ser reproduzido e a variavel 'verificarTempo' fica a
			 * falso.
			 */
			if((System.currentTimeMillis() - tempoObjectoParado > tempoImpedePerseguir)) {
				aVerificarTempo = false;
				estado = LER_DISTANCIA;
			}
			else
				estado = VAGUEAR;
			break;


		case DORMIR:
			dormir(tempoDormir/2);
			if(aVerificarTempo)
				estado = PERSEGUIR_PARADO;
			else
				estado = RECEBER_CORREIO;
			break;
		}
	}

	private void dormir(int tempo) {
		try {
			Thread.sleep(tempo);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mede 3 distâncias do sensor de ultrassons de modo a preencher-se o arraylist "distancias".
	 * Será usado futuramente para calcular um filtro de mediana, evitando assim grande parte dos 
	 * eventuais erros provinientes da leitura do Sensor de ultrassons
	 */
	private void lerPrimeirasDistancias() {

		for (int i = 0; i < 3; i++) {
			distanciasObtidas.add(robot.SensorUS());
			if(i == 2) break;
			dormir(tempoDormir/3);
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


	public void run() {
		lerPrimeirasDistancias();
		while (!isToStop) {
			if (isToPause) {
				vaguear.myPause();
				perseguir.myPause();
				autoSuspend();
			}
			menuGestor();
		}
	}
}