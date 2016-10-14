package bancobeans;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class BancoGui extends ReceiverAdapter implements ActionListener,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9069762140056667691L;
	static JChannel channel;
	String user_name = System.getProperty("user.name", "n/a");

	Random gerador = new Random();
	
	/**
	 * Contrutor da classe BancoGui, constroi a interface Gráfica
	 * 
	 * */
	
	public BancoGui() {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Banco Bean");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton button = new JButton("Abrir conta");
		JButton button1 = new JButton("Consultar saldo");
		JButton button2 = new JButton("Extrato");
		JButton button3 = new JButton("Transferência");

		button.setFont(new java.awt.Font("Arial", 1, 24));
		button1.setFont(new java.awt.Font("Arial", 1, 24));
		button2.setFont(new java.awt.Font("Arial", 1, 24));
		button3.setFont(new java.awt.Font("Arial", 1, 24));

		button1.addActionListener((event) -> {
			try {
				this.button1(event);
			} catch (Exception ex) {
				Logger.getLogger(BancoGui.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		
		button2.addActionListener((event) -> {
			try {
				this.button2(event);
			} catch (Exception ex) {
				Logger.getLogger(BancoGui.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		
		button3.addActionListener((event) -> {
			try {
				this.button3(event);
			} catch (Exception ex) {
				Logger.getLogger(BancoGui.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
		
		button.addActionListener((event) -> {
			try {
				this.button(event);
			} catch (Exception ex) {
				Logger.getLogger(BancoGui.class.getName()).log(Level.SEVERE, null, ex);
			}
		});

		frame.addWindowStateListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            channel.close();
	        }

	    });
		
		frame.setLayout(new GridLayout(2, 2));
		frame.add(button2);
		frame.add(button1);
		frame.add(button);
		frame.add(button3);

		frame.setSize(800, 500);
		frame.setVisible(true);
	}

	private void start() throws Exception {
		channel = new JChannel("udp-confiavel.xml");
		channel.setReceiver(this);
		channel.connect("BancoBean");
		channel.setDiscardOwnMessages(true); /* Ignorar no receive a própria mensagem que enviou no send */  
	}


	/**
	 * @return void
	 * 
	 * Procedimento para consultar saldo, envia uma menssagem com Send para o canal; 
	 * para um membro aleatório do Cluster.
	 */
	private void consultarSaldo() throws Exception {
		
		String numeroConta = JOptionPane.showInputDialog(null,
				"============= Saldo ==============\n" + "Informe o número da conta. \n");
		if (numeroConta == null) {
			return;
		}

		Message msg = new Message(sorteiaMembro(), null, "S " + numeroConta); // Message.RSVP (faz send síncrono,
		channel.send(msg);															// vide manual item  3.8.8)
	}

	public Address sorteiaMembro() {
		int numero = gerador.nextInt(channel.getView().size());  /*sorteia entre os membros do cluster para solicitar o saldo */
        Address escolhido = channel.getView().getMembers().get(numero);   /*para não sobrecarragar um membro do grupo.*/
		return escolhido;
	}

	/**
	 * @return void
	 * 
	 *   Procedimento para Abrir Conta, solicita uma senha e cria uma conta com numero autoincremento,
	 *   cria uma conta com Valor de 
	 *
	 */
	@SuppressWarnings("deprecation")
	private void abrirConta() throws Exception {
	
		String senha = JOptionPane.showInputDialog(null,
				"============= Abrir Conta ==============\n" + "Informe sua senha:. \n");

		if (senha.equals(" ")){
                      return;
		}
		else if (senha == null) {
			return;
		}
		
		Message msg = new Message(null, null, "A "+senha);
		msg.setFlag(Message.RSVP);
		channel.send(msg);
	}
	/**
	 * @return void
	 * 
	 *  Recebe informações do usuario e envia uma mensaem para o BancoServidor que retorna o estrato do usuário.
	 * 
	 * */
	private void consultarExtrato() throws Exception {
        
		String numeroConta = JOptionPane.showInputDialog(null,
				"============= Extrato ==============\n" + "Informe o numero da conta para exibir extrato. \n");
		if (numeroConta == null) {
			return;
		}

		Message msg = new Message(sorteiaMembro(), null, "E " + numeroConta); 
		channel.send(msg);								

	}
	
	/**
	 * @return void
	 * 
	 *  Recebe informações do usuario e envia uma mensaem para o BancoServidor tratar  e se parametroes estiverem ok,
	 *  o servidor realiza tranferencia. 
	 * 
	 * */
	@SuppressWarnings("deprecation")
	private void trasferencia() throws Exception {
		String numeroContaDestino = JOptionPane.showInputDialog(null,
				"============= Trasnferência ==============\n" + "Transferir para: \n");
		if (numeroContaDestino == null) {
			return;
		}

		String valor = JOptionPane.showInputDialog(null,
				"============= Trasnferência ==============\n" + "Valor: \n");
		if (valor == null) {
			return;
		}
		
		String numeroContaOrigem = JOptionPane.showInputDialog(null,
				"============= Trasnferência ==============\n" + "Sua conta: \n");
		if (numeroContaOrigem == null) {
			return;
		}

		String senha = JOptionPane.showInputDialog(null,
				"============= Trasnferência ==============\n" + "Senha: \n");
		if (senha == null) {
			return;
		}
		
		Message msg = new Message(null, null, "T "+numeroContaDestino+" "+numeroContaOrigem +" "+ valor +" "+senha);
		msg.setFlag(Message.RSVP);
		channel.send(msg);

	}

	

	private void button1(ActionEvent event) throws Exception {   //ao clicar no botao
		consultarSaldo();
	}
	
	private void button2(ActionEvent event) throws Exception {   //ao clicar no botao
		consultarExtrato();
	}
	
	private void button3(ActionEvent event) throws Exception {   //ao clicar no botao
		trasferencia();
	}
	
	private void button(ActionEvent event) throws Exception {   //ao clicar no botao
		abrirConta();
	}

	 /**
	  * @return void
	  * @author Diego,Otavio
	  * @param String - impressao
	  * Mostra na tela tudo que for enviado pelo Receive.
	  * 
	  * */
	private void imprime(String imp) {
		JOptionPane.showMessageDialog(null, imp);
		try {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
	 /**
	  * 
	  * @author Diego,Otavio
	  * 
	  * Recebe mensagens enviadas do Banco Servidor, atraves do Canal BancoBean 
	  * trata a mensagem e envia a mensagem para
	  * 
	  * */
	  public void receive(Message msg) {
	        String extra = "";
	        System.out.println("BancoGUI.receive: " + msg.getObject());
	        String mensagem = (String) msg.getObject();
	        StringTokenizer st = new StringTokenizer(mensagem, " ");
	        String teste = st.nextToken();

	        if (teste.equals("SALDO")) { // se servidor enviou mensagem com primeiro
	            // token SALDO.
	            teste = st.nextToken();

	            if (teste.equals("1")) { // se exixte conta
	                imprime("Seu saldo e [R$" + st.nextToken() + "]");
	            } else { // se nao existe.
	                imprime(st.nextToken());
	            }
	        } else if (teste.equals("TRANFERENCIA")) {
	            imprime(st.nextToken());
	        } else if (teste.equals("EXTRATO")) {

	            teste = st.nextToken();

	            if (teste.equals("1")) { // se exixte conta

	                while (st.hasMoreTokens()) {
	                    extra += st.nextToken() + " ";
	                }

	                imprime(extra);
	            } else { // se nao existe.
	                imprime(st.nextToken());
	            }
	        }/////////////////////////////////////////////////////////////////////// 
	        else if (teste.equals("CONTA")) {
	            extra = "CONTA ";
	            while (st.hasMoreTokens()) {
	                extra += st.nextToken() + " ";
	            }

	            imprime(extra);
	        }///////////////////////////////////////////////////////////////////////
	    }

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}

	public static void main(String[] args) throws Exception {
		BancoGui banco = new BancoGui();
		banco.start();
	
	}

}
