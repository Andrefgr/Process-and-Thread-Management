import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;

public class InterfaceGrafica {

	private JFrame frame;
	private JTextField textFieldOffSetEsq, textFieldOffSetDir, textFieldNameRobot, textFieldDistancia, textFieldAngulo, textFieldRaio;
	private JButton botaoFrente, botaoEsquerda, botaoDireita, botaoParar, botaoRetaguarda, btnLimparMensagens;
	private JCheckBox checkDebug, checkEvitar, checkGestor;
	private String nomeRobot = "";
	private int distancia, angulo, raio, offSetEsq, offSetDir;
	public static JTextArea textArea;
	private JScrollPane scrollPane;
	private JRadioButton switchOpenClose;
	public static JProgressBar progressBar;
	private boolean gestorLigado = false, evitarLigado = false;
	public MyRobot robot;
	Semaphore semaphoreRobot;
	Evitar evitar;
	Gestor gestor;
	private JTextField textFieldConfig;
	private JTextField textFieldTraj;
	private String filenameConfig;
	private Path currentRelativePath = Paths.get("");
	private String path = currentRelativePath.toAbsolutePath().toString();
	// MAIN
	public static void main(String[] args) {
		InterfaceGrafica window = new InterfaceGrafica();
		window.frame.setVisible(true);
	}

	public InterfaceGrafica() {
		robot = new MyRobot(true);
		semaphoreRobot = new Semaphore(1);

		initialize();
	}

	public void updateBar(int percentagem) {
		progressBar.setValue(percentagem);
	}

	private void ligarGestor() {
		gestor = new Gestor(robot, semaphoreRobot);
		gestor.myPause();
		gestor.start();

		gestorLigado = true;
	}

	private void ligarEvitar() {
		evitar = new Evitar(robot, semaphoreRobot);
		evitar.myPause();
		evitar.start();

		evitarLigado = true;
	}

	private void acederRobot() {
		try {
			semaphoreRobot.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void libertarRobot() {
		semaphoreRobot.release();
	}

	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 764);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		apresentacao();

		// ------------------------------------------------------------------
		// NOME ROBOT
		JLabel lblNome = new JLabel("Nome");
		lblNome.setBounds(10, 67, 46, 14);
		frame.getContentPane().add(lblNome);

		textFieldNameRobot = new JTextField();
		textFieldNameRobot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nomeRobot = textFieldNameRobot.getText();
				if (textArea.isEnabled())
					textArea.append("Nome do Robot definido é: " + nomeRobot + "\n");
			}
		});
		textFieldNameRobot.setBounds(10, 92, 252, 20);
		frame.getContentPane().add(textFieldNameRobot);
		textFieldNameRobot.setColumns(10);

		// ------------------------------------------------------------------
		// SWITCH LIGACAO ROBOT
		switchOpenClose = new JRadioButton("Open/Close");
		switchOpenClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (switchOpenClose.isSelected()) {
					acederRobot();
					if (robot.OpenNXT(nomeRobot)) {
						if (textArea.isEnabled())
							textArea.append("Ligado a " + nomeRobot + " \n");
						progressBar.setValue(100);
						estadoBotoes(true);
						libertarRobot();
					} else {
						if (textArea.isEnabled())
							textArea.append("Não conectou a " + nomeRobot + " \n");
						progressBar.setValue(0);
						estadoBotoes(false);
						libertarRobot();
					}
				} else {
					acederRobot();
					robot.CloseNXT();
					libertarRobot();
					if (checkEvitar.isSelected())
						evitar.myPause();
					if (checkGestor.isSelected())
						gestor.myPause();
					if (textArea.isEnabled())
						textArea.append("Desconectado de " + nomeRobot + " \n");
					estadoBotoes(false);
					progressBar.setValue(0);
				}
			}
		});
		switchOpenClose.setBounds(338, 77, 92, 23);
		frame.getContentPane().add(switchOpenClose);

		// ------------------------------------------------------------------
		// OFFSET ESQUERDO
		JLabel lblOffsetEsquerdo = new JLabel("Offset Esquerdo");
		lblOffsetEsquerdo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblOffsetEsquerdo.setBounds(10, 11, 101, 14);
		frame.getContentPane().add(lblOffsetEsquerdo);

		textFieldOffSetEsq = new JTextField();
		textFieldOffSetEsq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				offSetEsq = Integer.parseInt(textFieldOffSetEsq.getText());
				acederRobot();
				robot.AjustarVME(offSetEsq);
				libertarRobot();
				log("Offset Esquerdo", offSetEsq);
			}
		});
		textFieldOffSetEsq.setBounds(10, 36, 86, 20);
		frame.getContentPane().add(textFieldOffSetEsq);
		textFieldOffSetEsq.setColumns(10);

		// ------------------------------------------------------------------
		// OFFSET DIREITO
		JLabel lblOffsetDireito = new JLabel("Offset Direito");
		lblOffsetDireito.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblOffsetDireito.setBounds(338, 11, 86, 14);
		frame.getContentPane().add(lblOffsetDireito);

		textFieldOffSetDir = new JTextField();
		textFieldOffSetDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				offSetDir = Integer.parseInt(textFieldOffSetDir.getText());
				acederRobot();
				robot.AjustarVMD(offSetDir);
				libertarRobot();
				log("Offset Direito", offSetDir);
			}
		});
		textFieldOffSetDir.setBounds(338, 36, 86, 20);
		frame.getContentPane().add(textFieldOffSetDir);
		textFieldOffSetDir.setColumns(10);

		// ------------------------------------------------------------------
		// BOTOES CONTROLO ROBOT
		botaoFrente = new JButton("Frente");
		botaoFrente.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acederRobot();
				robot.SetSpeed(50);
				robot.Parar(true);
				robot.Reta(distancia);
				robot.Parar(false);
				libertarRobot();
				if (textArea.isEnabled())
					textArea.append("Reta(" + distancia + ") \n");
			}
		});
		botaoFrente.setForeground(Color.BLACK);
		botaoFrente.setBounds(160, 188, 102, 44);
		frame.getContentPane().add(botaoFrente);
		// ------------------------------------------------------------------
		botaoParar = new JButton("Parar");
		botaoParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acederRobot();
				robot.Parar(true);
				libertarRobot();
				if (textArea.isEnabled())
					textArea.append("Parou a true. \n");
			}
		});
		botaoParar.setForeground(Color.RED);
		botaoParar.setBounds(160, 241, 102, 44);
		frame.getContentPane().add(botaoParar);

		// ------------------------------------------------------------------
		botaoRetaguarda = new JButton("Retaguarda");
		botaoRetaguarda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acederRobot();
				robot.SetSpeed(50);
				robot.Parar(true);
				robot.Reta(-distancia);
				robot.Parar(false);
				libertarRobot();
				if (textArea.isEnabled())
					textArea.append("Reta(" + -distancia + ") \n");
			}
		});
		botaoRetaguarda.setForeground(new Color(139, 0, 0));
		botaoRetaguarda.setBounds(160, 296, 101, 44);
		frame.getContentPane().add(botaoRetaguarda);
		// ------------------------------------------------------------------
		botaoDireita = new JButton("Direita");
		botaoDireita.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acederRobot();
				robot.SetSpeed(50);
				robot.Parar(true);
				robot.CurvarDireita(raio, angulo);
				robot.Parar(false);
				libertarRobot();
				if (textArea.isEnabled())
					textArea.append("CurvarDireita(" + raio + ", " + angulo + ") \n");
			}
		});
		botaoDireita.setForeground(new Color(0, 102, 51));
		botaoDireita.setBounds(272, 241, 101, 44);
		frame.getContentPane().add(botaoDireita);
		// ------------------------------------------------------------------
		botaoEsquerda = new JButton("Esquerda");
		botaoEsquerda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acederRobot();
				robot.SetSpeed(50);
				robot.Parar(true);
				robot.CurvarEsquerda(raio, angulo);
				robot.Parar(false);
				libertarRobot();
				if (textArea.isEnabled())
					textArea.append("CurvarEsquerda(" + raio + ", " + angulo + ") \n");
			}
		});
		botaoEsquerda.setForeground(Color.BLUE);
		botaoEsquerda.setBounds(49, 241, 101, 44);
		frame.getContentPane().add(botaoEsquerda);
		// ------------------------------------------------------------------
		// BOTAO EVITAR

		checkEvitar = new JCheckBox("Evitar");
		checkEvitar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkEvitar.isSelected()) {
					// Activa o comportamento Evitar. Verifica se já está activo
					if (!evitarLigado)
						ligarEvitar();
					evitar.myResume();
					if (textArea.isEnabled())
						textArea.append("Iniciou o Evitar. \n");
				} else {
					// Caso o gestor esteja activo, então o evitar não pode
					// deixar de funcionar
					evitar.myPause();
					if (textArea.isEnabled())
						textArea.append("Pausou o Evitar. \n");
				}
			}
		});
		checkEvitar.setBounds(329, 147, 97, 23);
		frame.getContentPane().add(checkEvitar);

		// ------------------------------------------------------------------
		// BOTAO GESTOR

		checkGestor = new JCheckBox("Gestor");
		checkGestor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkGestor.isSelected()) {
					if (!gestorLigado)
						ligarGestor();
					gestor.myResume();
					if (textArea.isEnabled())
						textArea.append("Iniciou o Gestor. \n");
				} else {
					// Caso a checkbox do evitar esteja desactivada, então faz
					// pausa do comportamento
					gestor.myPause();
					if (textArea.isEnabled())
						textArea.append("Pausou o Gestor. \n");
				}
			}
		});
		checkGestor.setBounds(329, 173, 97, 23);
		frame.getContentPane().add(checkGestor);

		// ------------------------------------------------------------------
		// DISTANCIA
		JLabel lblDistncia = new JLabel("Distancia");
		lblDistncia.setBounds(10, 123, 74, 14);
		frame.getContentPane().add(lblDistncia);

		textFieldDistancia = new JTextField();
		textFieldDistancia.setEnabled(false);
		textFieldDistancia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				distancia = Integer.parseInt(textFieldDistancia.getText());
				log("distancia", distancia);
			}
		});
		textFieldDistancia.setBounds(10, 148, 86, 20);
		frame.getContentPane().add(textFieldDistancia);
		textFieldDistancia.setColumns(10);

		// ------------------------------------------------------------------
		// ANGULO

		JLabel lblngulo = new JLabel("Angulo");
		lblngulo.setBounds(106, 123, 74, 14);
		frame.getContentPane().add(lblngulo);

		textFieldAngulo = new JTextField();
		textFieldAngulo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				angulo = Integer.parseInt(textFieldAngulo.getText());
				log("angulo", angulo);
			}
		});
		textFieldAngulo.setBounds(106, 148, 86, 20);
		frame.getContentPane().add(textFieldAngulo);
		textFieldAngulo.setColumns(10);
		// ------------------------------------------------------------------
		// RAIO

		JLabel lblRaio = new JLabel("Raio");
		lblRaio.setBounds(205, 123, 57, 14);
		frame.getContentPane().add(lblRaio);

		textFieldRaio = new JTextField();
		textFieldRaio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				raio = Integer.parseInt(textFieldRaio.getText());
				log("raio", raio);
			}
		});
		textFieldRaio.setBounds(202, 148, 86, 20);
		frame.getContentPane().add(textFieldRaio);
		textFieldRaio.setColumns(10);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 586, 414, 128);
		frame.getContentPane().add(scrollPane);

		// ------------------------------------------------------------------
		// AREA TEXTO

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setBackground(Color.WHITE);

		JScrollBar scrollBar = new JScrollBar();
		scrollBar.setBounds(407, 586, 17, 128);
		frame.getContentPane().add(scrollBar);

		// ------------------------------------------------------------------
		// CHECKBOX
		checkDebug = new JCheckBox("Debug");
		checkDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (checkDebug.isSelected())
					textArea.setEnabled(true);
				else
					textArea.setEnabled(false);
			}
		});
		checkDebug.setBounds(10, 544, 97, 23);
		frame.getContentPane().add(checkDebug);

		// ------------------------------------------------------------------
		// BOTAO LIMPA MENSAGENS
		btnLimparMensagens = new JButton("Limpar mensagens");
		btnLimparMensagens.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		btnLimparMensagens.setBounds(272, 552, 152, 23);
		frame.getContentPane().add(btnLimparMensagens);

		progressBar = new JProgressBar();
		progressBar.setBounds(329, 107, 101, 14);
		frame.getContentPane().add(progressBar);

		JButton btnGravar = new JButton("Gravar configura\u00E7\u00E3o");
		btnGravar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				guardarConfig();
			}
		});
		btnGravar.setBounds(10, 351, 152, 23);
		frame.getContentPane().add(btnGravar);

		JButton btnCarregarConf = new JButton("Carregar configura\u00E7\u00E3o");
		btnCarregarConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnCarregarConf.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnCarregarConf.setBounds(266, 351, 143, 23);
		frame.getContentPane().add(btnCarregarConf);

		JButton btnGravarTraj = new JButton("Gravar traject\u00F3ria");
		btnGravarTraj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnGravarTraj.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnGravarTraj.setBounds(10, 419, 129, 23);
		frame.getContentPane().add(btnGravarTraj);

		JButton btnReproduzirTraj = new JButton("Reproduzir traject\u00F3ria");
		btnReproduzirTraj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnReproduzirTraj.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnReproduzirTraj.setBounds(256, 419, 168, 23);
		frame.getContentPane().add(btnReproduzirTraj);

		JButton btnParar = new JButton("Parar grava\u00E7\u00E3o ou reprodu\u00E7\u00E3o");
		btnParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnParar.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnParar.setBounds(131, 507, 186, 23);
		frame.getContentPane().add(btnParar);
		
		textFieldConfig = new JTextField();
		textFieldConfig.setBounds(136, 385, 284, 20);
		
		frame.getContentPane().add(textFieldConfig);
		textFieldConfig.setColumns(10);
		
		textFieldTraj = new JTextField();
		textFieldTraj.setBounds(140, 456, 284, 20);
		frame.getContentPane().add(textFieldTraj);
		textFieldTraj.setColumns(10);
		
		JLabel lblConfPath = new JLabel("Configura\u00E7\u00E3o Path:");
		lblConfPath.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblConfPath.setBounds(10, 385, 129, 14);
		frame.getContentPane().add(lblConfPath);
		
		JLabel lblTrajectriaPath = new JLabel("Traject\u00F3ria Path:");
		lblTrajectriaPath.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTrajectriaPath.setBounds(10, 462, 120, 14);
		frame.getContentPane().add(lblTrajectriaPath);

		estadoBotoes(false);
	}

	private void log(String input, int valor) {
		if (textArea.isEnabled())
			textArea.append("O input " + input + " foi alterado para: " + valor + "\n");
	}

	private void estadoBotoes(boolean estado) {
		textFieldNameRobot.setEnabled(!estado);
		botaoFrente.setEnabled(estado);
		botaoEsquerda.setEnabled(estado);
		botaoDireita.setEnabled(estado);
		botaoRetaguarda.setEnabled(estado);
		botaoParar.setEnabled(estado);
		textFieldAngulo.setEnabled(estado);
		textFieldOffSetDir.setEnabled(estado);
		textFieldOffSetEsq.setEnabled(estado);
		textFieldRaio.setEnabled(estado);
		textFieldDistancia.setEnabled(estado);
		checkDebug.setSelected(true);
		textArea.setEnabled(true);
		textArea.setEditable(false);
		checkEvitar.setEnabled(estado);
		checkGestor.setEnabled(estado);
	}

	private void apresentacao() {
		JLabel lblFso = new JLabel("FSO 2017");
		lblFso.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblFso.setForeground(new Color(0, 0, 0));
		lblFso.setHorizontalAlignment(SwingConstants.CENTER);
		lblFso.setBounds(131, 11, 143, 14);
		frame.getContentPane().add(lblFso);
		JLabel label = new JLabel("39085 - 41854 - 41943");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(131, 26, 143, 14);
		frame.getContentPane().add(label);
	}
	public void guardarConfig() {
		System.out.println(path);
		try {
			Properties props = new Properties();
			props.setProperty("Nome", textFieldNameRobot.getText());
			props.setProperty("Distancia", Integer.toString(distancia));
			props.setProperty("Angulo", Integer.toString(angulo));
			props.setProperty("Raio", Integer.toString(raio));
			props.setProperty("Offset Esquerda", Integer.toString(offSetEsq));
			props.setProperty("Offset Direita", Integer.toString(offSetEsq));
			filenameConfig =path + "\\config_" + textFieldNameRobot.getText() + ".properties";
			File f = new File(filenameConfig);
			OutputStream out = new FileOutputStream(f);
//			props.store(out, "This is an optional header comment string");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void carregarConfig() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filenameConfig));
			nomeRobot = props.getProperty("Nome");
			distancia = Integer.parseInt(props.getProperty("Distancia"));
			angulo = Integer.parseInt(props.getProperty("Angulo"));
			raio = Integer.parseInt(props.getProperty("Raio"));
			offSetEsq = Integer.parseInt(props.getProperty("Offset Esquerda"));
			offSetDir = Integer.parseInt(props.getProperty("Offset Direita"));
			textFieldOffSetEsq.setText("" + offSetEsq);
			textFieldOffSetDir.setText("" + offSetDir);
			textFieldNameRobot.setText(""+nomeRobot);
			textFieldAngulo.setText("" + angulo);
			textFieldRaio.setText("" + raio);
			textFieldDistancia.setText("" + distancia);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}