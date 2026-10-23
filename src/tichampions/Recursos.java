package tichampions;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Recursos {
    public static Image bgMenu, bgLoja, spriteMarcao, spriteDiegao;
    public static Image[] bgsCombate = new Image[3]; 
    public static Image imgMatheus, imgLucas, imgElvis;
    public static Image[] imgInimigos = new Image[9]; 
    public static Image[] imgItens = new Image[6]; 

    public static void carregar() {
        try {
            bgMenu = carregarImagem("sprites/backgrounds/bg_menu.png"); 
            bgLoja = carregarImagem("sprites/backgrounds/bg_loja.png"); 
            bgsCombate[0] = carregarImagem("sprites/backgrounds/bg_combate1.png");
            bgsCombate[1] = carregarImagem("sprites/backgrounds/bg_combate2.png");
            bgsCombate[2] = carregarImagem("sprites/backgrounds/bg_combate3.png");
            
            spriteMarcao = carregarImagem("sprites/npc/marcao.png"); 
            spriteDiegao = carregarImagem("sprites/npc/diegao.png"); 
            imgMatheus = carregarImagem("sprites/personagens/matheus.png"); 
            imgLucas = carregarImagem("sprites/personagens/lucas.png"); 
            imgElvis = carregarImagem("sprites/personagens/elvis.png");
            
            // Inimigos normais (0 a 5)
            imgInimigos[0] = carregarImagem("sprites/inimigos/estagiario.png"); 
            imgInimigos[1] = carregarImagem("sprites/inimigos/sql_injection.png");
            imgInimigos[2] = carregarImagem("sprites/inimigos/hardware_curto.png"); 
            imgInimigos[3] = carregarImagem("sprites/inimigos/cavalo_troia.png");
            imgInimigos[4] = carregarImagem("sprites/inimigos/criptominerador.png");
            imgInimigos[5] = carregarImagem("sprites/inimigos/gabinete_assombrado.png");
            
            // Bosses (6 a 8)
            imgInimigos[6] = carregarImagem("sprites/inimigos/boss_arquiteto.png");
            imgInimigos[7] = carregarImagem("sprites/inimigos/boss_bsod.png");
            imgInimigos[8] = carregarImagem("sprites/inimigos/boss_wannacry.png");
            
            imgItens[0] = carregarImagem("sprites/itens/cafe.png"); 
            imgItens[1] = carregarImagem("sprites/itens/placa_video.png");
            imgItens[2] = carregarImagem("sprites/itens/nobreak.png");
            imgItens[3] = carregarImagem("sprites/itens/ferro_solda.png");
            imgItens[4] = carregarImagem("sprites/itens/camisa_evento.png");
            imgItens[5] = carregarImagem("sprites/itens/memoria_enferrujada.png");
        } catch (Exception e) {
            System.out.println("Erro ao carregar os recursos visuais. Verifique se a pasta sprites está dentro de src!");
        }
    }

    private static Image carregarImagem(String caminhoRelativo) throws Exception {
        File f = new File("src/" + caminhoRelativo);
        if (f.exists()) return ImageIO.read(f);
        java.net.URL url = Recursos.class.getResource("/" + caminhoRelativo);
        if (url != null) return ImageIO.read(url);
        return ImageIO.read(f);
    }
}