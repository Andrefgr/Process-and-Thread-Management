import java.util.concurrent.Semaphore;

public class BufferCircular {
	Dados bufferDados;
	int putBuffer, getBuffer;
	// o semáforo elementosLivres indica se há posições livres para inserir ints
	// o semáforo acessoElemento garante exclusão mútua no acesso a um elemento
	// o semáforo elementosOcupados indica se há posições com ints válidas
	Semaphore elementosLivres, acessoElemento, elementosOcupados;

	public BufferCircular() {
		elementosLivres = new Semaphore(1);
		elementosOcupados = new Semaphore(0);
		acessoElemento = new Semaphore(1);
		bufferDados = new Dados();
	}

	public void inserirElemento(Dados dadosRecebidos) throws Exception {
		elementosLivres.acquire();
		acessoElemento.acquire();
		bufferDados = dadosRecebidos;
		acessoElemento.release();
		elementosOcupados.release();
	}

	public Dados removerElemento() throws Exception {
		Dados removerDados = new Dados();
		elementosOcupados.acquire();
		acessoElemento.acquire();
		removerDados = bufferDados;
		acessoElemento.release();
		elementosLivres.release();
		return removerDados;
	}
}
