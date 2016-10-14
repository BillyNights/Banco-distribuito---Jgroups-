package bancobeans;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;

import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class BancoServidor extends ReceiverAdapter implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3478646137632673294L;

    JChannel channel;
    JChannel channelState;
    String user_name = System.getProperty("user.name", "n/a");

    List<Conta> banco = new ArrayList<Conta>();

    /**
     * Contrutor do Banco Servidor
     *
     *
     */
    public BancoServidor() {
    
    }

    /**
     * @return void
     *
     * Cria canal de comunucação com as Views e as persistencia.
     *
     *
     */
    private void start() throws Exception {
        channel = new JChannel("udp-confiavel.xml");
        channel.setReceiver(this);
        channel.connect("BancoBean");
        channel.setDiscardOwnMessages(true);
        /* Ignorar no receive a própria mensagem que enviou no send */

        channelState = new JChannel();
        channelState.setReceiver(
                new ReceiverAdapter() {
            @Override
            public void receive(Message msg) {

            }

            @Override
            public void getState(OutputStream output) throws Exception {
                synchronized (banco) {
                    Util.objectToStream(banco, new DataOutputStream(output));
                }
                output.close();
            }

            @Override
            public void setState(InputStream input) throws Exception {
                List<Conta> list;
                list = (List<Conta>) Util.objectFromStream(new DataInputStream(input));
                synchronized (banco) {
                    banco.clear();
                    banco.addAll(list);
                }
                input.close();
                System.out.print("ESTADO RECEBIDO: ");
                for (Conta conta : list) {
                    System.out.print(conta.getConta() + ", ");
                }
                System.out.println(".");
            }
        }
        );
        channelState.connect("Persistencia");
        channelState.getState(null, 10000);
        eventLoop();
        channel.close();
        channelState.close();
    }

    /**
     * @return String
     *
     * Pega a hora do sistema e coloca no formato dd/MM/yyyy HH:mm:ss, para ser
     * utilizado para salvar a movimentação.
	 *
     */
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void getTotal(){
      double total = 0.0;
       for (Conta conta : banco) {
                total +=  conta.getSaldo();
       }

       System.out.println("Somatório do Banco : R$ " + total);
    }

    /**
     *
     * @author Diego
     *  
     * Função quE encontra a conta e penvia uma mensagens ao solicitante com o saldo da
     * conta
     * @param numeroConta
     * @param solicitante
     * @throws java.lang.Exception
     *
     *
     */
    public void getSaldo(int numeroConta, Address solicitante) throws Exception {
        Message msg = null;

        for (Conta conta : banco) {

            if (conta.getConta() == numeroConta) {
                msg = new Message(solicitante, null, "SALDO 1 " + conta.getSaldo());
                break;
            }

        }
        if (msg != null) {
            channel.send(msg);
        } else {
            msg = new Message(solicitante, null, "SALDO 0 Conta-inexistente");
            channel.send(msg);
        }
    }

    /**
     *
     * @author Diego
     * 
     * Função que encontra a conta e percorre a lista de movimentação e envia uma mensagens
     * ao solicitante com o extrato
     * @param numeroConta
     * @param solicitante
     * @throws java.lang.Exception
     *
     *
     */
    public void getExtrato(int numeroConta, Address solicitante) throws Exception {

        Message msg = null;
        String extrato = " ";

        for (Conta conta : banco) {
            if (conta.getConta() == numeroConta) {

                for (String movimento : conta.getMovimento()) {

                    extrato += movimento;
                }

                msg = new Message(solicitante, null, "EXTRATO 1 " + extrato);
                break;
            }

        }

        if (msg != null) {
            channel.send(msg);
        } else {
            msg = new Message(solicitante, null, "EXTRATO 0 Cliente-sem-movimentacao");
            channel.send(msg);
        }

    }

    /**
     *
     * função que transfere de uma conta pata outra e salva a transação na
     * movimentação da conta
     *
     *
     * @param contaDestino
     * @param contaOrigem
     * @param valor
     * @param senha
     * @param solicitante
     * @throws java.lang.Exception
     */
    public void setTrasnferir(int contaDestino, int contaOrigem, double valor, String senha, Address solicitante) throws Exception { // falta
        // implementar

        Message msg = null;
        boolean transferido = false;

        for (Conta contaOri : banco) {
            if (contaOri.getConta() == contaOrigem && !transferido) {
                if (contaOri.getSenha().equals(senha) && !transferido) {
                    if (contaOri.getSaldo() >= valor && !transferido) {
                        for (Conta contaDes : banco) {
                            if (contaDes.getConta() == contaDestino) {
                                contaOri.setSaldo(contaOri.getSaldo() - valor);
                                contaDes.setSaldo(contaDes.getSaldo() + valor);

                                contaOri.getMovimento().add(getDateTime() + " Transferência no valor de R$" + valor + " para conta " + contaDes.getConta() + " \n");
                                contaDes.getMovimento().add(getDateTime() + " Depósito no valor de R$" + valor + " da conta " + contaOri.getConta() + " \n");

                                msg = new Message(solicitante, null, "TRANFERENCIA Tranferencia_reslizada_com_Sucesso");
								getTotal();
                                transferido = true;
                                System.out.println("Tranferencia realizada");

                                channel.send(msg);
                                atualizaPersistencia(banco);
                                return;
                            } else {
                                msg = new Message(solicitante, null, "TRANFERENCIA Conta_Destino_inexistente");
                            }
                        }
                    } else {
                        msg = new Message(solicitante, null, "TRANFERENCIA Saldo_insuficiente");
                        break;
                    }
                } else {
                    msg = new Message(solicitante, null, "TRANFERENCIA  Senha_Incorreta");
                    break;
                }
            } else {
                msg = new Message(solicitante, null, "TRANFERENCIA Conta_Origem_inexistente");
            }

        }
        if (!transferido) {

            System.out.println("transferencia não realizada");
            channel.send(msg);
        }

    }

    /**
     * @author Diego
     *
     * função que abre uma conta, com um código auto-incremento. já com um valor
     * de 100$
     * @param senha
     * @param solicitante
     *
     *
     */

    public void setAbrirConta(String senha, Address solicitante) {
        int numConta = banco.size() + 1;

        List<String> mov = new ArrayList<>();
        mov.add(getDateTime() + " Conta criada, saldo de R$100\n");
        banco.add(new Conta(numConta, mov, 100.00, senha));
        Message msg = new Message(solicitante, null, "CONTA CRIADA: " + numConta + " COM SALDO DE R$100.00");
        getTotal();
        try {
            channel.send(msg);
            atualizaPersistencia(banco);
        } catch (Exception ex) {
            Logger.getLogger(BancoServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("deprecation")
    public void atualizaPersistencia(List<Conta> banco) throws Exception {
        Message msg = new Message();
        msg.setObject(banco);
        msg.setFlag(Message.RSVP);
        channelState.send(msg);
    }

    /**
     * 
     * @author Diego 
     * Permite que o servidor fique em execução, porem ele sai do
     * processador a cada intervalo de tempo Nao permanecendo no processadoR todo tempo
     */
    private void eventLoop() {
        while (true) { // depois trarar, isso faz com que o banco nunca pare de
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }			// funcionar
        }

    }

    /**
     * 
     * Recebe a mensagens enviadas pelas Views e trata para saber qual função fou solicitada. 
	 *
     * @param msg
     */
    @Override
    public void receive(Message msg) {

        String mensagem = (String) msg.getObject();

        StringTokenizer st = new StringTokenizer(mensagem, " ");
        /*separa a mensagem recebida em por espaço */

        String teste = st.nextToken(); // pega primeiro toke e compara

        if(teste.equals("T")) {

            try {
                setTrasnferir(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Double.parseDouble(st.nextToken()), st.nextToken(), msg.getSrc());
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (teste.equals("S")) {
            /*se token S , ou seja cliente quer consultar saldo */
            try {
                getSaldo(Integer.parseInt(st.nextToken()), msg.getSrc());
                /* chama o getSaldo e passa
																			 *numero da conta e interface T que pediu */
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (teste.equals("E")) {
            /*se token E , ou seja cliente quer consultar Extrato */
            try {
                getExtrato(Integer.parseInt(st.nextToken()), msg.getSrc());
                /* chama o getExtrato e passa
																			 * numero da conta e interface E que pediu */
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (teste.equals("A")) {
            try {
                setAbrirConta(st.nextToken(), msg.getSrc());
                /* chama o getExtrato e passa numero da conta e interface E que pediu */
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        BancoServidor bancoServidor = new BancoServidor();
        /* cria um objeto da classe */
        bancoServidor.start();

    }
}
