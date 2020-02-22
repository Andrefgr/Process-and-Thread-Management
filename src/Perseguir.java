import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Perseguir extends Comportamento {
	//Instancias
	MyRobot robot;
	Consumidor consumidor;
	Semaphore acessRobot, haDistance;
	BufferCircular caixaCorreio;
	ArrayList<Dados> dadosRecebidos = new ArrayList<Dados>();
	Dados dadosEstadoParado;
	//Variaveis da classe
	double distanciaAnterior, distanciaActual, tempoAnterior, tempoActual;
	boolean estouParado;
	int estado, tempoDormir, velocidadeRobot;
	final int RECEBER_CORREIO = 0, PERSEGUIR = 1, AVALIAR_OBJECTO = 2, CALCULAR_VELOCIDADE = 3, PARADO = 4, DORMIR = 5, REMOVER_DADOS = 6, 
			distanciaMaxObjecto = 15, velocidadeMin = 20, velocidadeMax = 80;
	
	
	public Perseguir(MyRobot robot, Semaphore acessRobot, Semaphore haDistance, BufferCircular mailBox, int tempoDormir) {
		this.robot = robot;
		this.acessRobot = acessRobot;
		this.caixaCorreio = mailBox;
		this.haDistance = haDistance;
		this.tempoDormir = tempoDormir;
		dadosEstadoParado = new Dados();
		estado = 0;
		estouParado = false;
		velocidadeRobot = 50; //velocidade standard do robotLego
		consumidor = new Consumidor(mailBox, haDistance);
		consumidor.start();
	}

	private void menuPerseguir() {
		switch (estado) {

		case RECEBER_CORREIO:
			try {
				//Receber os dados
				Dados dadosNovos = consumidor.getDados();
				dadosRecebidos.add(dadosNovos.clone()); //Adicionar ao arraylist de Dados
				
				//Quando o Perseguir obtiver 2 distancias, entao pode prosseguir para o estado
				//AVALIAR OBJECTO ou PERSEGUIR. Enquanto nao obtiver, passa para o estado DORMIR.
				if(dadosRecebidos.size() == 2) {
					distanciaAnterior = dadosRecebidos.get(0).getDistancia();
					tempoAnterior = dadosRecebidos.get(0).getTempo();
					distanciaActual = dadosRecebidos.get(1).getDistancia();
					tempoActual = dadosRecebidos.get(1).getTempo();
					
					System.out.println("Distancia anterior: " + distanciaAnterior + "; Tempo anterior: " + tempoAnterior);	
					System.out.println("Distancia actual: " + distanciaActual + "; Tempo actual: " + tempoActual);

					if(distanciaActual < distanciaMaxObjecto) //O robot pára quando está a 15cm do alvo/objecto
						estado = AVALIAR_OBJECTO;
					else
						estado = CALCULAR_VELOCIDADE;
				}

				else
					estado = DORMIR;

			} catch (InterruptedException e) {e.printStackTrace();}

			break;
			
		case CALCULAR_VELOCIDADE:
			//Calculo da velocidade, primeiro em centimetros e depois convertida para percentagem
			double deltaDistancia = (distanciaActual - distanciaAnterior);
			double deltaTempo = (tempoActual - tempoAnterior) / 1000.0;
			double velocidadeCm = (deltaDistancia / deltaTempo);
			float velocidadePercentagem = calcularPercentagem(velocidadeCm);

			
			//Se a distancia actual for igual à anterior e caso a velocidade seja 0, entao o 
			//objecto está parado. Logo, o PERSEGUIR passa para o estado AVALIAR_OBJECTO
			if(distanciaActual == distanciaAnterior && velocidadePercentagem == 0)
				estado = AVALIAR_OBJECTO;
			else {
				//Incremento da velocidade nova à velocidade antiga do robot
				velocidadeRobot += velocidadePercentagem;
				estado = PERSEGUIR;
			}
				
			break;

		case PERSEGUIR:
			
			acederRobot();

			if(velocidadeRobot < velocidadeMin) //Para velocidades abaixo de 20%, o robot anda no min a 20%
				robot.SetSpeed(20);
			if(velocidadeRobot > velocidadeMin && velocidadeRobot <= velocidadeMax)
				robot.SetSpeed(velocidadeRobot);
			if(velocidadeRobot > velocidadeMax) //Para velocidades acima de 80%, o robot anda no max a 80%
				robot.SetSpeed(80);

			robot.Reta(1);
			libertarRobot();
			
			estado = REMOVER_DADOS;
			break;

		case AVALIAR_OBJECTO:
			System.out.println("Estou a avaliar o objecto!");
			acederRobot();
			robot.Parar(true);
			libertarRobot();

			/*tempoAnalise - tempo actual + 5 segundos. É o tempo necessario para analisar se o
			objecto está efectivamente parado. Durante estes 5 segundos sao analisadas varias
			distancias consecutivamente. Caso uma dessas distancias seja diferente da
			'distanciaActual' (definida no estado 'LER_EMAIL', ou seja, distancia que definiu 
			inicialmente que o objecto estava parado), entao significa que o objecto se moveu.
			Logo, o programa sai do estado 'AVALIAR_OBJECTO', e continua o PERSEGUIR.
			Se passado 5 segundos a distancia se manteve sempre igual, entao passa para o estado
			PARADO.
			 */

			long tempoAnalise = System.currentTimeMillis() + 5000;
			boolean objectoMexeu = false;

			while(System.currentTimeMillis() < tempoAnalise) {
				try {
					dadosEstadoParado = consumidor.getDados();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				double distanciaNova = dadosEstadoParado.getDistancia();
				if(distanciaAnterior != distanciaNova) {
					objectoMexeu = true;
					estado = REMOVER_DADOS;
					break;
				}
				dormir();
			}
			if(!objectoMexeu) estado = PARADO;
			break;

		case PARADO:
			System.out.println("Entrei no estado parado!");
			/*A variavel 'estouParado' é colocada a true, o que possibilita
			 *o GESTOR colocar o PERSEGUIR em pausa durante 5 segundos.*/
			estouParado = true;
			estado = REMOVER_DADOS;
			break;
			
		case REMOVER_DADOS:
			dadosRecebidos.remove(0);
			estado = DORMIR;
			break;

		case DORMIR:
			dormir();
			estado = RECEBER_CORREIO;
			break;
		}
	}

	//Recebe uma velocidade em centimetros e converte em percentagem, de modo a utilizar-se
	//futuramente no setSpeed do robot.
	private float calcularPercentagem(double velocidade) {
		return (float) (((0.5 * velocidade) / 20.0) * 100);
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

	private void dormir() {
		try {
			Thread.sleep(this.tempoDormir);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (!isToStop) {
			if (isToPause) {
				autoSuspend();
			}
			menuPerseguir();
		}
	}
}