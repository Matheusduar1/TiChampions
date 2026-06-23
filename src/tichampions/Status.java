package tichampions;

public class Status {
    int hp, hpMax, hardware, software, manutencao, firewall;
    public Status(int hp, int hard, int soft, int manut, int firew) {
        this.hpMax = hp; this.hp = hp; this.hardware = hard; 
        this.software = soft; this.manutencao = manut; this.firewall = firew;
    }
}