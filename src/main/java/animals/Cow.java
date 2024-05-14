package animals;

import food.Food;
import food.Grass;


public class Cow extends Herbivore implements Run, Swim, Voice{
    private int satiety;
    @Override
    public void run() {
        System.out.println("Корова бегает");
    }

    @Override
    public void swim() {
        System.out.println("Корова плавает");
    }

    @Override
    public String getVoice() {
        return "Корова говорит \"Муууу\"";
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Grass) {
            System.out.println("Рабочий кормит корову травой");
            System.out.println("Корова ест траву");
        } else {
            System.out.println("Рабочий кормит корову мясом");
            System.out.println("Корова не ест мясо!");
        }
    }

    public int getSatiety() {

        return super.getSatiety();
    }


}
