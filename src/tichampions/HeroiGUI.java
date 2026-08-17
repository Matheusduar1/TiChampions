package tichampions;
import java.awt.Image;
import java.util.ArrayList;

public abstract class HeroiGUI {
    String nome, passiva; Status status; ClasseRPG classe; Image sprite;
    
    boolean fugiuNaUltima = false, fugiuDestaBatalha = false, skillUsadaNoAndar = false, tentouFugirNoAndar = false;
    
    ArrayList<Item> mochila = new ArrayList<>();
    Item armaEquipada = null, armaduraEquipada = null;

    public HeroiGUI(String nome, String passiva, Status base) { this.nome = nome; this.passiva = passiva; this.status = base; }
    public void setClasse(ClasseRPG novaClasse) { this.classe = novaClasse; }
    public abstract String aplicarPassivaTurno();
    
    // NOVA FUNÇÃO: Gerencia a vida e permite que as passivas evitem a morte
    public void receberDano(int dano) {
        this.status.hp -= dano;
    }

    public String atacarBasico(InimigoGUI alvo, int tipoAtk) {
        int d100 = new java.util.Random().nextInt(100) + 1;
        if (d100 <= 10) return nome + " ERROU o ataque!"; 
        
        int critChance = (this.classe instanceof DonoLanHouse) ? 30 : 10;
        boolean crit = d100 > (100 - critChance); 

        int ataqueBonusHard = (armaEquipada != null && armaEquipada.tipo == 1) ? armaEquipada.poder : 0;
        int ataqueBonusSoft = (armaEquipada != null && armaEquipada.tipo == 3) ? armaEquipada.poder : 0;
        
        int danoCausado;
        String nomeAtaque;
        
        if (tipoAtk == 1) { 
            danoCausado = Math.max(1, (status.software + ataqueBonusSoft) - alvo.status.firewall);
            nomeAtaque = "Software";
        } else { 
            danoCausado = Math.max(1, (status.hardware + ataqueBonusHard) - alvo.status.manutencao);
            nomeAtaque = "Hardware";
        }
        
        if (fugiuNaUltima) danoCausado = danoCausado / 2; 
        if (crit) danoCausado *= 2;

        alvo.status.hp -= danoCausado; alvo.ativarPiscar(); 
        return nome + (crit ? " deu CRÍTICO ("+nomeAtaque+")! " : " atacou ("+nomeAtaque+")! ") + danoCausado + " dano!";
    }
}

class Matheus extends HeroiGUI {
    boolean focoHardware = false;
    public Matheus() { super("Matheus Duarte", "Hiperfoco: Alterna buffs", new Status(80, 5, 25, 5, 15)); }
    @Override public String aplicarPassivaTurno() { 
        focoHardware = !focoHardware;
        if(focoHardware) { status.hardware += 10; status.software -= 10; return "Matheus: Hiperfoco (+10 Hardware)"; }
        else { status.software += 10; status.hardware -= 10; return "Matheus: Hiperfoco (+10 Software)"; }
    }
}

class Lucas extends HeroiGUI {
    public Lucas() { super("Lucas Narezzi", "Eu sou IA: Buff defesa", new Status(120, 15, 2, 20, 10)); }
    @Override public String aplicarPassivaTurno() { status.manutencao += 5; return "Lucas: Eu Sou IA (+5 Manutenção)"; }
}

class Elvis extends HeroiGUI {
    boolean usouPassivaResurreicao = false; // Controle de 1 vez por Run
    
    public Elvis() { super("Elvis Almeida", "Limão com Mel: Sobrevive 0 HP (1x por Run)", new Status(100, 20, 5, 15, 2)); }
    @Override public String aplicarPassivaTurno() { return "Elvis: Limão com Mel Preparado!"; }
    
    // INTERCEPTA O DANO LETAL E SOBREVIVE COM 25% DE HP
    @Override
    public void receberDano(int dano) {
        super.receberDano(dano); // Toma o dano normalmente
        if (this.status.hp <= 0 && !usouPassivaResurreicao) {
            this.status.hp = this.status.hpMax / 4; // Sobrevive com 25% do HP máximo
            this.usouPassivaResurreicao = true;
        }
    }
}