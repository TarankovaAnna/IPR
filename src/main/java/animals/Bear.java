package animals;

import food.Food;
import food.Meat;

public class Bear extends Carnivorous implements Run, Swim, Voice {

    // получаем значение сытости
    public int getSatiety() {
        return super.getSatiety();
    }

    // реализовываем интерфейсы, соответствующие поведению медведя
    @Override
    public void run() {
        System.out.println("Медведь бегает");
    }

    @Override
    public void swim() {
        System.out.println("Медведь плавает");
    }

    @Override
    public String getVoice() {
        return "Медведь говорит \"Арррр\"";
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Meat) {
            System.out.println("Рабочий кормит медведя мясом");
            System.out.println("Медведь ест мясо");
            satiety++;
        } else {
            System.out.println("Рабочий кормит медведя травой");
            System.out.println("Медведь не ест траву!");
        }
    }
}
