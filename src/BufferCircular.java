import java.util.concurrent.Semaphore;

public class BufferCircular {
	Dados bufferDados;
	int putBuffer, getBuffer;
	// o sem�foro elementosLivres indica se h� posi��es livres para inserir ints
	// o sem�foro acessoElemento garante exclus�o m�tua no acesso a um elemento
	// o sem�foro elementosOcupados indica se h� posi��es com ints v�lidas
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
