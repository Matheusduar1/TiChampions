package tichampions;

public class InimigoGUI {
    String nome; Status status; int spriteId; int tipoAtaque; 
    public int timerPiscar = 0, timerAtacar = 0; 
    
    public InimigoGUI(int level, Status base, int spriteId, int tipoAtaque) { 
        this.status = base; this.spriteId = spriteId; this.tipoAtaque = tipoAtaque;
        if(spriteId == 0) this.nome = "Estagiário Lv." + level;
        else if(spriteId == 1) this.nome = "SQL Injection Lv." + level;
        else if(spriteId == 2) this.nome = "Hardware Curto Lv." + level;
        else if(spriteId == 3) this.nome = "BOSS Arquiteto";
        else this.nome = "Bug Lv." + level;
    }
    public void ativarPiscar() { this.timerPiscar = 30; } 
    public void ativarAtaqueAnim() { this.timerAtacar = 15; }
    
    public String atacar(HeroiGUI alvo) {
        int d100 = new java.util.Random().nextInt(100) + 1;
        if (d100 <= 15) return nome + " tentou atacar " + alvo.nome + " e ERROU!"; 
        boolean crit = (d100 >= 95); 

        int defBonusManut = (alvo.armaduraEquipada != null && alvo.armaduraEquipada.tipo == 2) ? alvo.armaduraEquipada.poder : 0;
        int defBonusFirew = (alvo.armaduraEquipada != null && alvo.armaduraEquipada.tipo == 4) ? alvo.armaduraEquipada.poder : 0;
        
        int danoCausado;
        if(tipoAtaque == 1) { 
            danoCausado = Math.max(1, status.software - (alvo.status.firewall + defBonusFirew));
            if(crit) danoCausado *= 2;
            
            alvo.receberDano(danoCausado); // Chama a nova função
            return nome + (crit ? " deu CRÍTICO em " : " atacou ") + alvo.nome + "! " + danoCausado + " de dano!";
        } else { 
            danoCausado = Math.max(1, status.hardware - (alvo.status.manutencao + defBonusManut));
            if(crit) danoCausado *= 2;
            
            alvo.receberDano(danoCausado); // Chama a nova função
            return nome + (crit ? " deu CRÍTICO em " : " atacou ") + alvo.nome + "! " + danoCausado + " de dano!";
        }
    }
}