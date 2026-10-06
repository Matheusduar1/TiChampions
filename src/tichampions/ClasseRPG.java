package tichampions;
import java.util.ArrayList;

public abstract class ClasseRPG {
    String nomeClasse, descAtributos, descSkill;
    public ClasseRPG(String nome, String descAtrib, String descSkill) { 
        this.nomeClasse = nome; this.descAtributos = descAtrib; this.descSkill = descSkill;
    }
    public abstract String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos);
}

class HackerMan extends ClasseRPG {
    public HackerMan() { super("HackerMan", "+10 Software, -5 Manutenção", "DDOS: Dano Software em Área"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        GerenciadorAudio.tocarEfeito(GerenciadorAudio.special);
        for(InimigoGUI ini : inimigos) { ini.status.hp -= (heroi.status.software * 2); ini.ativarPiscar(); }
        return heroi.nome + " usou DDOS! Dano em Área!";
    }
}

class Infra extends ClasseRPG {
    public Infra() { super("Infra", "+10 Hardware, +20 HP", "Sobrecarga: Super dano Hardware, perde 15 HP"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        GerenciadorAudio.tocarEfeito(GerenciadorAudio.special);
        heroi.receberDano(15); 
        inimigos.get(0).status.hp -= (heroi.status.hardware * 3) + 20;
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " usou Sobrecarga! Perdeu 15 HP!";
    }
}

class JavaChampion extends ClasseRPG {
    public JavaChampion() { super("Java Champion", "+10 Defesas, -5 Hardware", "Encapsulamento: Buffa Defesas (+25)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        GerenciadorAudio.tocarEfeito(GerenciadorAudio.special);
        heroi.status.manutencao += 25; heroi.status.firewall += 25;
        return heroi.nome + " usou Encapsulamento! Defesas UP!";
    }
}

class DonoLanHouse extends ClasseRPG {
    public DonoLanHouse() { super("Dono de LanHouse", "+8 Hardware, -5 Software", "+1 Ficha: Dano Crítico e +1 Turno"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        GerenciadorAudio.tocarEfeito(GerenciadorAudio.special);
        inimigos.get(0).status.hp -= (heroi.status.hardware * 4); // Reduzido o multiplicador de 5 para 4 (estava roubado contra boss)
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " colocou +1 Ficha! Dano Crítico!";
    }
}

class Professor extends ClasseRPG {
    public Professor() { super("Professor", "+30 HP, +5 Firewall", "Ensinamentos: Cura 60 HP próprio"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        GerenciadorAudio.tocarEfeito(GerenciadorAudio.special);
        heroi.status.hp = Math.min(heroi.status.hpMax, heroi.status.hp + 60);
        return heroi.nome + " usou Ensinamentos! Curou a si mesmo!";
    }
}