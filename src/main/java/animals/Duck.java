package animals;

import food.Food;
import food.Grass;
import food.Meat;

public class Duck extends Herbivore implements Run, Swim, Fly, Voice {
    private int satiety;
    public int getSatiety() {

        return satiety;
    }
    @Override
    public void fly() {
        System.out.println("Утка летает");
    }

    @Override
    public void run() {
        System.out.println("Утка бегает");
    }

    @Override
    public void swim() {
        System.out.println("Утка плавает");
    }

    @Override
    public String getVoice() {
        return "Утка говорит \"Кря\"";
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Grass) {
            System.out.println("Рабочий кормит утку травой");
            System.out.println("Утка ест траву");
        } else {
            System.out.println("Рабочий кормит утку мясом");
            System.out.println("Утка не ест мясо!");
        }
    }
}

