package bancobeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Conta implements Serializable {
	
	//A CLASSE DEVE SER SERIALIZADA PARAPODER SER SALVA EM ARQUIVO OU SER PASSADA VIA MENSAGEM

		/**
	 * 
	 */
	private static final long serialVersionUID = 3624637775441395322L;  //ALGUMA COISA A VER COM HASH PARA VERIFICACAO DE INTEGRIDADE

	private int conta;
	private List<String> movimento = new ArrayList<String>();
	private double saldo;
	private String senha;

	
	public Conta(int conta, List<String> movimento, double saldo, String  senha) {
		
		this.conta = conta;
		this.movimento = movimento;
		this.saldo = saldo;
		this.senha = senha;
	}
	

	/**
	 * @return the movimento
	 */
	public List<String> getMovimento() {
		return movimento;
	}

	/**
	 * @param movimento the movimento to set
	 */
	public void setMovimento(List<String> movimento) {
		this.movimento = movimento;
	}

	/**
	 * @return the senha
	 */
	public String  getSenha() {
		return senha;
	}

	/**
	 * @param senha the senha to set
	 */
	public void setSenha(String  senha) {
		this.senha = senha;
	}

	/**
	 * @return the conta
	 */
	public int getConta() {
		return conta;
	}

	/**
	 * @param conta
	 *            the conta to set
	 */
	public void setConta(int conta) {
		this.conta = conta;
	}

	/**
	 * @return the saldo
	 */
	public double getSaldo() {
		return saldo;
	}

	/**
	 * @param saldo
	 *            the saldo to set
	 */
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

}
