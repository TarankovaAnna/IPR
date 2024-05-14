package employee;

import animals.Animal;
import animals.Kotik;
import animals.Voice;
import food.Food;

public class Worker {
    public void feed(Animal animal, Food food) {

        animal.eat(food);

    }

    public void getVoice(Voice animal){

        System.out.println(animal.getVoice());
    }


}
