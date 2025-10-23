package test;
import robocode.*;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.WinEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**8
 * Capivara - a robot by (your name here)
 */
public class Capivara extends Robot
{
    boolean peek;
    double moveAmount;
    /**
     * run: Capivara's default behavior
     */
    public void run() {
        // Set colors
        setBodyColor(Color.black);
        setGunColor(Color.black);
        setRadarColor(Color.orange);
        setBulletColor(Color.cyan);
        setScanColor(Color.cyan);

        // Inicializa moveAmount para o máximo possível para este campo de batalha.
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        // Inicializar peek como falso
        peek = false;

		// vire à esquerda para ficar de frente para uma parede.
		// getHeading() % 90 significa o resto de
		// getHeading() dividido por 90.
        turnLeft(getHeading() % 90);
        ahead(moveAmount);
        // Gire a arma para virar 90 graus para a direita.
        peek = true;
        turnGunRight(90);
        turnRight(90);

        while (true) {
            // Olhe antes de virar quando ahead() for concluído.
            peek = true;
            // Suba na parede
            ahead(moveAmount);
			turnGunRight(45);
            // Não olhe agora
            peek = false;
            // Vire para a próxima parede
            turnRight(90);
			
		
        }
    }
    /**
     * onScannedRobot: O que fazer quando você vê outro robô
     */
public void onScannedRobot(ScannedRobotEvent e) {
        // Calcula a localização exata do robô
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        // Se estiver perto o suficiente, atire!
        if (Math.abs(bearingFromGun) <= 3) {
             
		// Verificamos o calor da arma aqui, porque chamar fire()
		// usa um turno, o que pode nos fazer perder o controle
		// do outro robô.
            if (getGunHeat() == 0) {
                fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
            }
        } // caso contrário, basta configurar a arma para girar..
        // Nota: Isso não terá efeito até que chamemos scan()
        else {
            turnGunRight(bearingFromGun);
        }
 		// Gera outro evento de varredura se virmos um robô.
		// Só precisamos chamar isso se a arma (e, portanto, o radar)
		// não estiver girando. Caso contrário, a varredura é chamada automaticamente.
        if (bearingFromGun == 0) {
            scan();
        }
    }

    /**
     * onHitByBullet: O que fazer quando você for atingido por uma bala
     */
   /*
 public void onHitByBullet(HitByBulletEvent e) {
        // Substitua a próxima linha por qualquer comportamento que você desejar
		 // Inicializa moveAmount para o máximo possível para este campo de batalha.
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        // Inicializar peek como falso
        peek = false;

		// vire à esquerda para ficar de frente para uma parede.
		// getHeading() % 90 significa o resto de
		// getHeading() dividido por 90.
        turnLeft(getHeading() % 90);
        ahead(moveAmount);
    }
    */
    /**
     * onHitWall: O que fazer quando você bate em uma parede
     */
    public void onHitWall(HitWallEvent e) {
       //Substitua a próxima linha por qualquer comportamento que você desejar
       	moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
 		back(45 + (getBattleFieldWidth() * getBattleFieldHeight() / 20000));
		turnLeft(getHeading() % 90);
		turnGunRight(360);
    }
	
	public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
			fire(1);
            turnLeft(30);
        }
    }
}


