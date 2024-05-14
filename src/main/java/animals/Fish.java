package animals;

import food.Food;
import food.Grass;
import food.Meat;

public class Fish extends Carnivorous implements Swim {

    // получаем значение сытости
    public int getSatiety() {
        return super.getSatiety();
    }

    // реализовываем интерфейсы, соответствующие поведению рыбы
    @Override
    public void swim() {
        System.out.println("Рыба плавает");
    }

    @Override
    public void eat(Food food) {
        if (food instanceof Meat) {
            System.out.println("Рабочий кормит рыбу мясом");
            System.out.println("Рыба ест мясо");
            satiety++;
        } else {
            System.out.println("Рабочий кормит рыбу травой");
            System.out.println("Рыба не ест траву!");
        }
    }
}
