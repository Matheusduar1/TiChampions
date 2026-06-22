package tichampions;
import java.awt.Image;
import java.util.ArrayList;

public class Entidades {} 

class Status {
    int hp, hpMax, hardware, software, manutencao, firewall;
    public Status(int hp, int hard, int soft, int manut, int firew) {
        this.hpMax = hp; this.hp = hp; this.hardware = hard; 
        this.software = soft; this.manutencao = manut; this.firewall = firew;
    }
}

class Item {
    String nome, descricao;
    int tipo; // 0 = Consumível, 1 = Arma(Hardware), 2 = Defesa(Manutenção), 3 = Software
    int poder; 
    Image icone; // Agora os itens guardam a própria imagem para o Mini-Inventário
    public Item(String n, String d, int t, int p, Image ic) { 
        nome = n; descricao = d; tipo = t; poder = p; icone = ic; 
    }
}

abstract class ClasseRPG {
    String nomeClasse, descAtributos, descSkill;
    public ClasseRPG(String nome, String descAtrib, String descSkill) { 
        this.nomeClasse = nome; this.descAtributos = descAtrib; this.descSkill = descSkill;
    }
    public abstract String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos);
}

class HackerMan extends ClasseRPG {
    public HackerMan() { super("HackerMan (Mago)", "+Software, -Manutenção", "DDOS: Dano Software em Área"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        for(InimigoGUI ini : inimigos) { ini.status.hp -= (heroi.status.software * 2); ini.ativarPiscar(); }
        return heroi.nome + " usou DDOS! Dano de Software em Área!";
    }
}

class Infra extends ClasseRPG {
    public Infra() { super("Infra (Guerreiro)", "+Hardware, HP Balanceado", "Sobrecarga: Super dano Hardware, perde 10 HP"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp -= 10; 
        inimigos.get(0).status.hp -= (heroi.status.hardware * 3);
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " usou Sobrecarga! Super dano, mas perdeu 10 HP!";
    }
}

class JavaChampion extends ClasseRPG {
    public JavaChampion() { super("Java Champion (Tank)", "+Manutenção, +Firewall", "Encapsulamento: Buffa Manutenção e Firewall"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.manutencao += 15; heroi.status.firewall += 15;
        return heroi.nome + " usou Encapsulamento! Defesas aumentadas!";
    }
}

class DonoLanHouse extends ClasseRPG {
    public DonoLanHouse() { super("Dono de LanHouse", "Focado em Dano Hardware Crítico", "+1 Ficha: Dano Hardware Massivo (1 Alvo)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        inimigos.get(0).status.hp -= (heroi.status.hardware * 4); 
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " colocou +1 Ficha! Dano Crítico massivo!";
    }
}

class Professor extends ClasseRPG {
    public Professor() { super("Professor (Paladino)", "Status Balanceados, Foco em Cura", "Ensinamentos: Cura 40 HP próprio"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp = Math.min(heroi.status.hpMax, heroi.status.hp + 40);
        return heroi.nome + " usou Ensinamentos! Curou a si mesmo!";
    }
}

abstract class HeroiGUI {
    String nome, passiva;
    Status status; ClasseRPG classe; Image sprite;
    boolean fugiuNaUltima = false, fugiuDestaBatalha = false, skillUsadaNoAndar = false, tentouFugirNoAndar = false;
    
    ArrayList<Item> mochila = new ArrayList<>();
    Item armaEquipada = null, armaduraEquipada = null, ativoEquipado = null;

    public HeroiGUI(String nome, String passiva, Status base) { this.nome = nome; this.passiva = passiva; this.status = base; }
    public void setClasse(ClasseRPG novaClasse) { this.classe = novaClasse; }
    public abstract String aplicarPassivaTurno();

    public String atacarBasico(InimigoGUI alvo) {
        int ataqueBonus = (armaEquipada != null) ? armaEquipada.poder : 0;
        int danoCausado;
        if (this.classe instanceof HackerMan) danoCausado = Math.max(1, (status.software + ataqueBonus) - alvo.status.firewall);
        else danoCausado = Math.max(1, (status.hardware + ataqueBonus) - alvo.status.manutencao);
        
        if (fugiuNaUltima) danoCausado = danoCausado / 2; 
        alvo.status.hp -= danoCausado;
        alvo.ativarPiscar(); // O alvo pisca quando apanha
        return nome + " atacou! Causa " + danoCausado + " dano!";
    }
}

class Matheus extends HeroiGUI {
    public Matheus() { super("Matheus Duarte", "Hiperfoco: Alterna buffs (Hardware/Software)", new Status(80, 5, 25, 5, 15)); }
    @Override public String aplicarPassivaTurno() { return "Matheus ativou Hiperfoco!"; }
}
class Lucas extends HeroiGUI {
    public Lucas() { super("Lucas Narezzi", "Eu sou IA: Buff de defesa no início", new Status(120, 15, 2, 20, 10)); }
    @Override public String aplicarPassivaTurno() { return "Lucas ativou Eu sou IA!"; }
}
class Elvis extends HeroiGUI {
    public Elvis() { super("Elvis Almeida", "Limão com Mel: Chance de sobreviver a 0 HP", new Status(100, 20, 5, 15, 2)); }
    @Override public String aplicarPassivaTurno() { return "Elvis preparou Limão com Mel!"; }
}

class InimigoGUI {
    String nome; Status status; int spriteId; int tipoAtaque; 
    public int timerPiscar = 0; // Para a animação de tomar dano
    
    public InimigoGUI(String nome, Status base, int spriteId, int tipoAtaque) { 
        this.nome = nome; this.status = base; this.spriteId = spriteId; this.tipoAtaque = tipoAtaque;
    }
    
    public void ativarPiscar() { this.timerPiscar = 30; } // 30 frames piscando
    
    public String atacar(HeroiGUI alvo) {
        int danoCausado, defBonus = (alvo.armaduraEquipada != null) ? alvo.armaduraEquipada.poder : 0;
        if(tipoAtaque == 1) { 
            danoCausado = Math.max(1, status.software - (alvo.status.firewall + defBonus));
            alvo.status.hp -= danoCausado;
            return nome + " atacou com Software! " + danoCausado + " dano mágico!";
        } else { 
            danoCausado = Math.max(1, status.hardware - (alvo.status.manutencao + defBonus));
            alvo.status.hp -= danoCausado;
            return nome + " atacou com Hardware! " + danoCausado + " dano físico!";
        }
    }
}