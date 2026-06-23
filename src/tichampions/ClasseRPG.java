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
    public HackerMan() { super("HackerMan", "+Software, -Manutenção", "DDOS: Dano Software em Área"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        for(InimigoGUI ini : inimigos) { ini.status.hp -= (heroi.status.software * 2); ini.ativarPiscar(); }
        return heroi.nome + " usou DDOS! Dano em Área!";
    }
}

class Infra extends ClasseRPG {
    public Infra() { super("Infra", "+Hardware, HP Balanceado", "Sobrecarga: Super dano Hardware, perde 15 HP"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp -= 15; 
        inimigos.get(0).status.hp -= (heroi.status.hardware * 3) + 20;
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " usou Sobrecarga! Perdeu 15 HP!";
    }
}

class JavaChampion extends ClasseRPG {
    public JavaChampion() { super("Java Champion", "+Manutenção, +Firewall", "Encapsulamento: Buffa Defesas (+25)"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.manutencao += 25; heroi.status.firewall += 25;
        return heroi.nome + " usou Encapsulamento! Defesas UP!";
    }
}

class DonoLanHouse extends ClasseRPG {
    public DonoLanHouse() { super("Dono de LanHouse", "Focado em Hardware, +Crit Chance", "+1 Ficha: Dano Crítico e +1 Turno"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        inimigos.get(0).status.hp -= (heroi.status.hardware * 4); 
        inimigos.get(0).ativarPiscar();
        return heroi.nome + " botou +1 Ficha! CRÍTICO e joga de novo!";
    }
}

class Professor extends ClasseRPG {
    public Professor() { super("Professor", "Status Balanceados, Foco em Cura", "Ensinamentos: Cura 60 HP próprio"); }
    @Override public String usarSkill(HeroiGUI heroi, ArrayList<InimigoGUI> inimigos) {
        heroi.status.hp = Math.min(heroi.status.hpMax, heroi.status.hp + 60);
        return heroi.nome + " usou Ensinamentos! Curou a si mesmo!";
    }
}