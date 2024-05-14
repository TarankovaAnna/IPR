package animals;

import food.Food;
import food.Grass;

public class Walrus extends Herbivore implements Run, Swim, Voice {

    // получаем значение сытости
    public int getSatiety() {
        return super.getSatiety();
    }

    // // реализовываем интерфейсы, соответствующие поведению моржа
    @Override
    public void run() {
        System.out.println("Морж бегает");
    }

    @Override
    public void swim() {
        System.out.println("Морж плавает");
    }

    @Override
    public String getVoice() {
        return "Морж говорит \"Шшшшшшш\"";
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Grass) {
            System.out.println("Рабочий кормит моржа травой");
            System.out.println("Морж ест траву");
            satiety++;
        } else {
            System.out.println("Рабочий кормит моржа мясом");
            System.out.println("Морж не ест мясо!");
        }
    }
}
