
import java.util.concurrent.Semaphore;

public class Consumidor extends Thread {

	BufferCircular bufferCircular;
	Dados consumirDados;
	Semaphore haDistancia, livreMyString, acessoMyString, ocupadaMyString;

	Consumidor(BufferCircular bc, Semaphore ht) {
		bufferCircular = bc;
		haDistancia = ht;
		livreMyString = new Semaphore(1);
		ocupadaMyString = new Semaphore(0);
		acessoMyString = new Semaphore(1);
	}

	public Dados getDados() throws InterruptedException {
		ocupadaMyString.acquire();
		acessoMyString.acquire();
		Dados getDados = consumirDados;
		acessoMyString.release();
		livreMyString.release();
		return getDados;
	}

	public void run() {
		while (true) {
			try {
				haDistancia.acquire();
				livreMyString.acquire();
				acessoMyString.acquire();
				consumirDados = bufferCircular.removerElemento();
				acessoMyString.release();
				ocupadaMyString.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}