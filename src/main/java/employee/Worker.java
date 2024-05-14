package employee;

import animals.Animal;
import animals.Voice;
import food.Food;

public class Worker {
    // покормить животное
    public void feed(Animal animal, Food food) {
        animal.eat(food);
    }

    // заставить животное говорить
    public void getVoice(Voice animal) {
        System.out.println(animal.getVoice());
    }

}
