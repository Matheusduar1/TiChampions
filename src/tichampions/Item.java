package tichampions;
import java.awt.Image;

public class Item {
    String nome, descricao;
    int tipo; // 0 = Consumível, 1 = Hardware(Arma), 2 = Manutenção(Armadura), 3 = Software, 4 = Firewall
    int poder; 
    Image icone;
    public Item(String n, String d, int t, int p, Image ic) { 
        nome = n; descricao = d; tipo = t; poder = p; icone = ic; 
    }
}