package test;
import robocode.*;
import java.awt.Color; // A importação de Color estava comentada, mas é necessária
import robocode.util.Utils; // Importar Utils é uma boa prática, embora não estritamente obrigatório para o seu uso aqui.

/**
 * Bumblebee - Um robô que se movimenta em círculos e atira com precisão.
 */
public class Bumblebee extends AdvancedRobot // Mudei para AdvancedRobot para usar setTurnGunRightRadians e setAhead/setTurnRight (melhor controle assíncrono)
{
    // Variáveis para armazenar a distância do robô escaneado e o evento ScannedRobotEvent
    private double distance = 0.0;
    private ScannedRobotEvent lastScannedEvent = null;
    
    /**
     * run: Bumblebee's default behavior
     */
    public void run() {
        // Cores do robô
        setBodyColor(Color.YELLOW);
        setGunColor(Color.BLACK);
        setRadarColor(Color.YELLOW);
        setScanColor(Color.RED);
        setBulletColor(Color.ORANGE);

        // Configurações do canhão e radar
        setAdjustRadarForGunTurn(true); // Mantém o radar se movendo independentemente do canhão
        setAdjustGunForRobotTurn(true); // Mantém o canhão se movendo independentemente do robô
        
        // Loop principal do robô
        while (true) {
            // Movimento do Radar: Gira o radar indefinidamente (procura por inimigos)
            setTurnRadarRight(360);
            
            // Movimento do Robô: Movimenta-se em um padrão circular simples
            setTurnRight(5); // Gira o corpo lentamente para manter o movimento circular
            setAhead(100);    // Avança
            
            // Tenta disparar se tiver um alvo e o canhão estiver pronto
            fireIfReady();
            
            // Executa todas as ações pendentes (movimentos, tiros, etc.)
            execute(); 
        }
    }
    
    /**
     * onScannedRobot: Chamado quando o radar detecta outro robô.
     * **ESTE MÉTODO NÃO PODE ESTAR DENTRO DO MÉTODO run() OU DO LOOP while(true).**
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        lastScannedEvent = e;
        distance = e.getDistance();
        
        // --- Lógica de Disparo e Mira (Targeting) ---
        
        // Calcular o ângulo absoluto para o inimigo
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        
        // Calcular o quanto o canhão precisa virar
        double gunTurn = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians());
        
        // Vira o canhão
        setTurnGunRightRadians(gunTurn);
        
        // Tenta disparar imediatamente após mirar
        fireIfReady();
        
        // --- Lógica de Movimento Evasivo no Escaneamento ---
        
        // Movimento lateral para dificultar ser atingido (Circular Movement)
        // Virar o corpo 90 graus em relação ao inimigo + um pequeno offset de 15 graus
        setTurnRight(e.getBearing() + 90 - 15);
        setAhead(150); // Anda mais para manter a distância
        
        // Após escanear, tenta travar o radar no alvo (mantendo o radar parado)
        // setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
    }
    
    /**
     * Método auxiliar para tentar disparar.
     */
    private void fireIfReady() {
        // Verifica se o canhão está frio e está apontando para o alvo (margem de erro de 10 graus)
        // **e.getDistance() não está disponível aqui, por isso usamos a variável 'distance' e checamos 'lastScannedEvent'**
        if (getGunHeat() == 0 && lastScannedEvent != null) {
            // Potência proporcional à proximidade (máximo de 3.0)
            double firePower = Math.min(400 / distance, 3.0); 
            // Dispara
            setFire(firePower);
        }
    }

    /**
     * onBulletHit: Chamado quando um tiro acerta outro robô.
     */
    public void onBulletHit(BulletHitEvent e) {
        // O robô avança um pouco ao acertar um inimigo
        setAhead(50);
    }
    
    /**
     * onHitByBullet: Chamado quando o robô é atingido por um tiro.
     * **Atenção: Havia dois métodos onHitByBullet no código original. Um foi removido.**
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Recua um pouco e gira para sair da linha de fogo
        back(50);
        turnRight(30);
    }
    
    /**
     * onHitWall: Chamado quando o robô bate na parede.
     */
    public void onHitWall(HitWallEvent e) {
        // Recua e vira 90 graus
        back(50);
        turnRight(90);
    }
    
    // Adicione outros métodos de evento se precisar (onRobotDeath, onHitRobot, etc.)
}