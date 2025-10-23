package test;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.Random;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * BumblebeeCapivara - Um robô híbrido que usa táticas de movimento de parede (Wall-Hugging)
 * e mira de precisão, com cores dinâmicas que mudam a cada ação.
 */
public class BumblebeeCapivara extends AdvancedRobot
{
    // Variáveis de estado
    private double moveDirection = 1; // 1 para frente, -1 para trás (para evasão)
    private double lastScanBearing = 0;
    private double wallHugDistance = 100; // Distância mínima para a parede (para começar a virar)
    
    // Ferramentas para cores
    private Random random = new Random();
    private final Color[] rainbowColors = {
        Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, 
        Color.BLUE, Color.CYAN, Color.MAGENTA, Color.PINK,
        Color.LIGHT_GRAY, Color.WHITE, Color.BLACK 
    };

    /**
     * run: Comportamento principal do robô
     */
    public void run() {
        // Cores Iniciais (Definidas uma vez, mas serão mudadas constantemente)
        setBodyColor(Color.YELLOW);
        setGunColor(Color.BLACK);
        setRadarColor(Color.YELLOW);
        setScanColor(Color.RED);
        setBulletColor(Color.ORANGE);

        // Configurações avançadas
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Movimento inicial (vai para a borda)
        turnLeft(getHeading() % 90);
        ahead(Math.max(getBattleFieldWidth(), getBattleFieldHeight()));
        turnRight(90);

        while (true) {
            // Lógica de Movimento Principal (Wall-Hugging contínuo)
            
            // Checa se o robô está muito perto de qualquer parede
            if (getX() < wallHugDistance || getX() > getBattleFieldWidth() - wallHugDistance ||
                getY() < wallHugDistance || getY() > getBattleFieldHeight() - wallHugDistance) {
                
                // Mudança de cor ao iniciar uma manobra evasiva perto da parede
                changeRandomColors();
                
                // Se estiver perto da parede, faz um movimento evasivo:
                moveDirection = -moveDirection;
                setTurnRight(90);
                setAhead(150 * moveDirection);
            } else {
                // Continua o movimento no centro ou longe da borda
                setAhead(100 * moveDirection);
                setTurnRight(5 * moveDirection);
            }

            // Lógica do Radar
            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(360);
            }

            execute();
        }
    }

    /**
     * Seleciona e aplica cores aleatórias a diferentes partes do robô.
     */
    private void changeRandomColors() {
        Color newBodyColor = rainbowColors[random.nextInt(rainbowColors.length)];
        Color newGunColor = rainbowColors[random.nextInt(rainbowColors.length)];
        Color newRadarColor = rainbowColors[random.nextInt(rainbowColors.length)];
        
        // Garante que o robô não seja totalmente monocromático (opcional)
        if (newBodyColor.equals(newGunColor) && newGunColor.equals(newRadarColor)) {
            newRadarColor = Color.getHSBColor(random.nextFloat(), 1.0f, 1.0f);
        }
        
        setBodyColor(newBodyColor);
        setGunColor(newGunColor);
        setRadarColor(newRadarColor);
        setScanColor(rainbowColors[random.nextInt(rainbowColors.length)]);
        setBulletColor(rainbowColors[random.nextInt(rainbowColors.length)]);
    }

    /**
     * onScannedRobot: O que fazer quando você vê outro robô (ação de ataque/mira)
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // AÇÃO: Mudar cor ao detectar e mirar em um inimigo
        changeRandomColors(); 
        
        // --- 1. Lógica de Mira ---
        lastScanBearing = e.getBearing();
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        setTurnGunRight(bearingFromGun);

        // --- 2. Lógica de Disparo Otimizada ---
        double distance = e.getDistance();
        double firePower;

        if (distance < 150) {
            firePower = 3.0;
        } else if (distance < 400) {
            firePower = 2.0;
        } else {
            firePower = 1.0;
        }

        if (getGunHeat() == 0 && Math.abs(bearingFromGun) <= 3) {
            setFire(firePower);
        }

        // --- 3. Lógica de Movimento Evasivo (Circular simples) ---
        double turnAngle = e.getBearing() + 90;
        setTurnRight(turnAngle * moveDirection);
        setAhead(150 * moveDirection);

        // --- 4. Trava do Radar ---
        double radarTurn = Utils.normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());
        setTurnRadarRight(radarTurn * 2);

        execute();
    }
    
    /**
     * onHitByBullet: O que fazer quando você for atingido por uma bala (ação de defesa/dano)
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // AÇÃO: Mudar cor ao ser atingido
        changeRandomColors();
        
        moveDirection = -moveDirection;
        setBack(50);
        setTurnRight(30);
    }

    /**
     * onHitWall: O que fazer quando você bate em uma parede (ação de desvio)
     */
    public void onHitWall(HitWallEvent e) {
        // AÇÃO: Mudar cor ao colidir e desviar
        changeRandomColors();
        
        moveDirection = -moveDirection;
        setAhead(150 * moveDirection);
        setTurnRight(90);
    }

    /**
     * onHitRobot: Reage se bater em outro robô (ação de colisão)
     */
    public void onHitRobot(HitRobotEvent e) {
        // AÇÃO: Mudar cor ao colidir com um robô
        changeRandomColors();
        
        if (e.isMyFault()) {
            back(10);
        }
        fire(3);
    }

    /**
     * onWin: Dança da vitória!
     */
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            changeRandomColors(); // Continua mudando de cor durante a dança
            fire(1);
            turnLeft(30);
        }
    }
}