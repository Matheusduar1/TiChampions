package tichampions;
import java.awt.Image;
/**
 *
 * @author matheus.duarte
 */
public class Entidades {
    

// 1. STATUS E ATRIBUTOS
class Status {
    int hp, hpMax;
    int danoFisico, hacking;
    int defFisica, antivirus;

    public Status(int hp, int fis, int hack, int defF, int antiV) {
        this.hpMax = hp; this.hp = hp;
        this.danoFisico = fis; this.hacking = hack;
        this.defFisica = defF; this.antivirus = antiV;
    }
}

// 2. CLASSE BASE DO HERÓI
abstract class HeroiGUI {
    String nome;
    Status status;
    Image sprite;
    boolean defendendo = false;

    public HeroiGUI(String nome, Status base) {
        this.nome = nome;
        this.status = base;
    }

    public abstract void aplicarPassivaTurno();
    
    public void receberDano(int danoFisicoBruto, int danoHackingBruto) {
        int danoF = Math.max(1, danoFisicoBruto - status.defFisica);
        int danoH = Math.max(1, danoHackingBruto - status.antivirus);
        int danoTotal = danoF + danoH;
        
        status.hp -= danoTotal;
        if(status.hp < 0) status.hp = 0;
        System.out.println(nome + " recebeu " + danoTotal + " de dano!");
    }
}

// 3. OS PROTAGONISTAS
class Matheus extends HeroiGUI {
    public Matheus() {
        // Mago: Baixa HP e Def Fís, Alto Hacking e Antivirus
        super("Matheus Duarte", new Status(80, 5, 25, 5, 15));
    }
    @Override
    public void aplicarPassivaTurno() {
        // Passiva Hiperfoco (Lógica a ser expandida no combate)
        System.out.println("Matheus ativou Hiperfoco!");
    }
}

class Lucas extends HeroiGUI {
    public Lucas() {
        // Tank: Alto HP e Def Fís, Baixo Hacking
        super("Lucas Narezzi", new Status(120, 15, 2, 20, 10));
    }
    @Override
    public void aplicarPassivaTurno() {
        System.out.println("Lucas ativou Eu sou IA!");
    }
}

class Elvis extends HeroiGUI {
    public Elvis() {
        // Guerreiro: Alto Fisico, Baixo Antivirus
        super("Elvis Almeida", new Status(100, 20, 5, 15, 2));
    }
    @Override
    public void aplicarPassivaTurno() {
        System.out.println("Elvis preparou Limão com Mel!");
    }
}

// 4. INIMIGO BASE
class InimigoGUI {
    String nome;
    Status status;
    Image sprite;

    public InimigoGUI(String nome, Status base) {
        this.nome = nome;
        this.status = base;
    }
}
}
