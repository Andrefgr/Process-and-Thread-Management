
import java.util.concurrent.Semaphore;

public class Produtor extends Thread {
	BufferCircular bufferCircular;
	Dados produzirDados;
	Semaphore haDistancia, livreMyString, acessoMyString, ocupadaMyString;

	Produtor(BufferCircular bc, Semaphore ht) {
		bufferCircular = bc;
		haDistancia = ht;
		livreMyString = new Semaphore(1);
		ocupadaMyString = new Semaphore(0);
		acessoMyString = new Semaphore(1);
	}

	public void setDados(Dados dadosInserir) throws InterruptedException {
		livreMyString.acquire();
		acessoMyString.acquire();
		produzirDados = dadosInserir;
		acessoMyString.release();
		ocupadaMyString.release();
	}

	public void run() {
		while (true) {
			try {
				ocupadaMyString.acquire();
				acessoMyString.acquire();
				bufferCircular.inserirElemento(produzirDados);
				acessoMyString.release();
				livreMyString.release();
				haDistancia.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}