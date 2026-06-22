package tichampions;
import java.awt.Image;
import java.util.ArrayList;

public class Entidades {} 

// 1. STATUS
class Status {
    int hp, hpMax, danoFisico, hacking, defFisica, antivirus;
    public Status(int hp, int fis, int hack, int defF, int antiV) {
        this.hpMax = hp; this.hp = hp; this.danoFisico = fis; 
        this.hacking = hack; this.defFisica = defF; this.antivirus = antiV;
    }
}

// 2. SISTEMA DE ITENS E INVENTÁRIO
class Item {
    String nome, descricao;
    int tipo; // 0 = Consumível, 1 = Arma (Físico), 2 = Defesa, 3 = Arma (Hacking)
    int poder; // Valor de cura, ataque ou defesa
    
    public Item(String n, String d, int t, int p) {
        nome = n; descricao = d; tipo = t; poder = p;
    }
}

// 3. SISTEMA DE CLASSES (As 5 Classes solicitadas)
abstract class ClasseRPG {
    String nomeClasse;
    public ClasseRPG(String nome) { this.nomeClasse = nome; }
    public abstract String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos);
}

class HackerMan extends ClasseRPG {
    public HackerMan() { super("HackerMan (Mago)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        for(InimigoGUI ini : inimigos) ini.status.hp -= (heroi.status.hacking * 2);
        return heroi.nome + " usou DDOS! Dano em Área!";
    }
}

class Infra extends ClasseRPG {
    public Infra() { super("Infra (Guerreiro)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp -= 10; 
        inimigos.get(0).status.hp -= (heroi.status.danoFisico * 3);
        return heroi.nome + " usou Sobrecarga! Super dano, mas perdeu 10 HP!";
    }
}

class JavaChampion extends ClasseRPG {
    public JavaChampion() { super("Java Champion (Tank)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.defFisica += 15; heroi.status.antivirus += 15;
        return heroi.nome + " usou Encapsulamento! Defesas aumentadas!";
    }
}

class DonoLanHouse extends ClasseRPG {
    public DonoLanHouse() { super("Dono de LanHouse (Rogue)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        inimigos.get(0).status.hp -= (heroi.status.danoFisico * 4); // Simula o crítico / +1 Ficha
        return heroi.nome + " colocou +1 Ficha! Dano Crítico massivo!";
    }
}

class Professor extends ClasseRPG {
    public Professor() { super("Professor (Paladino)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp = Math.min(heroi.status.hpMax, heroi.status.hp + 40);
        return heroi.nome + " usou Ensinamentos! Curou a si mesmo!";
    }
}

// 4. HERÓIS ATUALIZADOS COM INVENTÁRIO
abstract class HeroiGUI {
    String nome;
    Status status;
    ClasseRPG classe;
    Image sprite;
    
    boolean fugiuNaUltima = false;
    boolean fugiuDestaBatalha = false; 
    boolean skillUsadaNoAndar = false;
    
    // Inventário
    ArrayList<Item> mochila = new ArrayList<>();
    Item armaEquipada = null;
    Item armaduraEquipada = null;

    public HeroiGUI(String nome, Status base) {
        this.nome = nome; this.status = base;
    }

    public void setClasse(ClasseRPG novaClasse) { this.classe = novaClasse; }
    public abstract String aplicarPassivaTurno();

    public String atacarBasico(InimigoGUI alvo) {
        int ataqueBonus = (armaEquipada != null) ? armaEquipada.poder : 0;
        int danoCausado;
        
        if (this.classe instanceof HackerMan) {
            danoCausado = Math.max(1, (status.hacking + ataqueBonus) - alvo.status.antivirus);
        } else {
            danoCausado = Math.max(1, (status.danoFisico + ataqueBonus) - alvo.status.defFisica);
        }
        
        if (fugiuNaUltima) danoCausado = danoCausado / 2; // Debuff de covardia
        alvo.status.hp -= danoCausado;
        return nome + " atacou! Causa " + danoCausado + " dano!";
    }
}

class Matheus extends HeroiGUI {
    public Matheus() { super("Matheus Duarte", new Status(80, 5, 25, 5, 15)); }
    @Override public String aplicarPassivaTurno() { return "Matheus ativou Hiperfoco!"; }
}
class Lucas extends HeroiGUI {
    public Lucas() { super("Lucas Narezzi", new Status(120, 15, 2, 20, 10)); }
    @Override public String aplicarPassivaTurno() { return "Lucas ativou Eu sou IA!"; }
}
class Elvis extends HeroiGUI {
    public Elvis() { super("Elvis Almeida", new Status(100, 20, 5, 15, 2)); }
    @Override public String aplicarPassivaTurno() { return "Elvis preparou Limão com Mel!"; }
}

// 5. INIMIGO
class InimigoGUI {
    String nome; Status status; int spriteId;
    public InimigoGUI(String nome, Status base, int spriteId) { 
        this.nome = nome; this.status = base; this.spriteId = spriteId; 
    }
    public String atacar(HeroiGUI alvo) {
        int defBonus = (alvo.armaduraEquipada != null) ? alvo.armaduraEquipada.poder : 0;
        int danoCausado = Math.max(1, status.danoFisico - (alvo.status.defFisica + defBonus));
        alvo.status.hp -= danoCausado;
        return nome + " atacou " + alvo.nome + " causando " + danoCausado + " de dano!";
    }
}